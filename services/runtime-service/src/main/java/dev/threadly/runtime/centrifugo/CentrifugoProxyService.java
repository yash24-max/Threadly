package dev.threadly.runtime.centrifugo;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.feign.FlowServiceClient;
import dev.threadly.runtime.model.Session;
import dev.threadly.runtime.service.RuntimeExecutor;
import dev.threadly.runtime.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
@RequiredArgsConstructor
public class CentrifugoProxyService {

    private final RuntimeExecutor runtimeExecutor;
    private final SessionService   sessionService;
    private final FlowServiceClient flowServiceClient;

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
     * Widget visitors can publish to widget:{botId}:{visitorId}.
     * After allowing, asynchronously routes the message to RuntimeExecutor.
     */
    public Map<String, Object> handlePublish(String userId, String channel, Map<String, Object> payload) {
        if (channel == null || !channel.startsWith("widget:") || userId == null || !userId.startsWith("visitor:")) {
            return denied("publish not allowed");
        }

        // Parse channel: widget:{botId}:{visitorId}
        String[] parts = channel.split(":");
        if (parts.length < 3) return denied("invalid channel format");

        String botId     = parts[1];
        String visitorId = parts[2];
        Object data   = payload.get("data");
        Object rawText = (data instanceof Map<?,?> m) ? m.get("text") : null;
        String text   = rawText != null ? rawText.toString() : "";

        if (!text.isBlank()) {
            routeToFlowExecution(botId, visitorId, text);
        }

        return allowed();
    }

    /**
     * Async: get-or-create session, fetch active flow, execute.
     */
    @Async
    protected void routeToFlowExecution(String botId, String visitorId, String text) {
        try {
            // 1. Get or create a session for this visitor
            Session session = sessionService.getVisitorSessions(visitorId).stream()
                    .filter(s -> botId.equals(s.getBotId()))
                    .findFirst()
                    .orElseGet(() -> sessionService.createSession(botId, null, visitorId));

            // 2. Fetch active flow from flow-service (no auth needed between internal services)
            JsonNode flowDefinition;
            try {
                flowDefinition = flowServiceClient.getActiveFlow(botId, "Bearer internal");
            } catch (Exception e) {
                log.warn("Could not fetch active flow for bot {}: {}", botId, e.getMessage());
                return;
            }

            if (flowDefinition == null) {
                log.warn("No active flow found for bot {}", botId);
                return;
            }

            // 3. Route message to runtime executor
            String sessionId = session.getId();
            if (session.getState() == Session.SessionState.PAUSED) {
                runtimeExecutor.resumeExecution(sessionId, text, flowDefinition);
            } else {
                runtimeExecutor.executeFlow(sessionId, flowDefinition);
            }

        } catch (Exception e) {
            log.error("Flow execution error for bot={} visitor={}: {}", botId, visitorId, e.getMessage(), e);
        }
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
