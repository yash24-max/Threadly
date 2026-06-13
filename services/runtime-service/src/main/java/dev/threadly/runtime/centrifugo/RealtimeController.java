package dev.threadly.runtime.centrifugo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Centrifugo connection tokens.
 *
 * Endpoints:
 *   GET  /api/v1/realtime/token   — dashboard user connection token (authenticated)
 *   POST /api/v1/widget/token     — anonymous widget visitor token (public)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RealtimeController {

    private static final long DASHBOARD_TOKEN_TTL = 3600L;  // 1 hour
    private static final long VISITOR_TOKEN_TTL   = 86400L; // 24 hours

    private final CentrifugoClient centrifugoClient;

    /**
     * Issue a Centrifugo connection token for an authenticated dashboard user.
     * Reads userId from the Spring Security context (set by JwtAuthFilter).
     */
    @GetMapping("/api/v1/realtime/token")
    public ResponseEntity<Map<String, String>> getDashboardToken(Authentication auth) {
        String userId = (auth != null && auth.getPrincipal() != null)
                ? auth.getPrincipal().toString()
                : UUID.randomUUID().toString();

        // orgId is embedded in JWT claims — retrieve from authorities or default
        String orgId = auth != null
                ? auth.getAuthorities().stream()
                      .filter(a -> a.getAuthority().startsWith("ORG_"))
                      .findFirst()
                      .map(a -> a.getAuthority().replace("ORG_", ""))
                      .orElse("")
                : "";

        String token = centrifugoClient.generateConnectionToken(userId, orgId, DASHBOARD_TOKEN_TTL);
        log.debug("Issued realtime token for user: {}", userId);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Issue a Centrifugo connection token for an anonymous widget visitor.
     * Called by the embedded widget before opening a WebSocket connection.
     *
     * @param botId     the bot the widget is attached to
     * @param visitorId stable anonymous identifier for the visitor (set by widget SDK)
     */
    @PostMapping("/api/v1/widget/token")
    public ResponseEntity<Map<String, String>> getVisitorToken(
            @RequestParam String botId,
            @RequestParam(required = false) String visitorId) {

        String vid = (visitorId != null && !visitorId.isBlank())
                ? visitorId
                : "anon-" + UUID.randomUUID();

        String token = centrifugoClient.generateVisitorToken(vid, botId, VISITOR_TOKEN_TTL);
        log.debug("Issued widget token for bot: {} visitor: {}", botId, vid);
        return ResponseEntity.ok(Map.of("token", token, "visitorId", vid));
    }
}
