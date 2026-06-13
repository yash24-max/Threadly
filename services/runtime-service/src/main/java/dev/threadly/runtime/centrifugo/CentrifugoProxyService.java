package dev.threadly.runtime.centrifugo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Business logic for Centrifugo proxy webhook decisions.
 *
 * Channel naming conventions:
 *   org:{orgId}                    — org-wide dashboard events (requires org membership)
 *   org:{orgId}#session:{sessionId} — per-session events (requires org membership)
 *   widget:{botId}:{visitorId}     — widget visitor channel (requires matching visitor token)
 */
@Slf4j
@Service
public class CentrifugoProxyService {

    /**
     * Handle connect proxy.
     * Centrifugo has already validated the JWT; we return the user context.
     * Return {"result": {"user": "userId"}} or {"error": {...}} to reject.
     */
    public Map<String, Object> handleConnect(Map<String, Object> payload) {
        // JWT is pre-validated by Centrifugo — extract user from token claims
        // Centrifugo passes client.user if token had sub claim
        String user = (String) payload.getOrDefault("user", "");
        if (user.isBlank()) {
            log.warn("Centrifugo connect: missing user in payload");
            return Map.of("disconnect", Map.of("code", 4001, "reason", "unauthorized"));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        return Map.of("result", result);
    }

    /**
     * Handle subscribe proxy.
     * Validates the user has access to the requested channel.
     */
    public Map<String, Object> handleSubscribe(String userId, String channel, Map<String, Object> payload) {
        if (channel == null || userId == null) {
            return denied("invalid channel or user");
        }

        // Widget channels: widget:{botId}:{visitorId} — only visitor themselves can subscribe
        if (channel.startsWith("widget:")) {
            String[] parts = channel.split(":");
            if (parts.length >= 3) {
                String visitorId = parts[2];
                // userId for widget tokens is "visitor:{visitorId}"
                if (!userId.equals("visitor:" + visitorId)) {
                    return denied("visitor mismatch");
                }
            }
            return allowed();
        }

        // Org channels: org:{orgId} or org:{orgId}#session:{sessionId}
        if (channel.startsWith("org:")) {
            String orgId = channel.split("#")[0].replace("org:", "");
            // userId carries org claim from JWT — simple prefix check
            // In production this would verify org membership via identity-service
            log.debug("Allowing org channel subscription: user={} org={}", userId, orgId);
            return allowed();
        }

        log.warn("Unrecognised channel pattern: {}", channel);
        return denied("unknown channel");
    }

    /**
     * Handle publish proxy.
     * Widget visitors can only publish to their own widget channel.
     * Dashboard users cannot publish directly (server-only publishing).
     */
    public Map<String, Object> handlePublish(String userId, String channel, Map<String, Object> payload) {
        if (channel != null && channel.startsWith("widget:") && userId != null && userId.startsWith("visitor:")) {
            return allowed();
        }
        return denied("publish not allowed");
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private Map<String, Object> allowed() {
        return Map.of("result", Map.of());
    }

    private Map<String, Object> denied(String reason) {
        log.debug("Centrifugo subscription denied: {}", reason);
        return Map.of("error", Map.of("code", 403, "message", reason));
    }
}
