package dev.threadly.identity.controller;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.identity.keycloak.KeycloakAdminClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user sessions via Keycloak Admin API.
 */
@Slf4j
@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "User session management")
public class SessionController {

    private final KeycloakAdminClient keycloakAdminClient;

    /**
     * GET /v1/me/sessions — returns all active sessions for the current user.
     *
     * @param auth the authenticated user (Keycloak subject = user UUID)
     * @return array of session objects from Keycloak
     */
    @GetMapping("/sessions")
    @Operation(summary = "List active sessions for the current user")
    public ResponseEntity<JsonNode> listSessions(Authentication auth) {
        String keycloakUserId = auth.getName();
        log.debug("Listing sessions for user: {}", keycloakUserId);
        JsonNode sessions = keycloakAdminClient.getUserSessions(keycloakUserId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * DELETE /v1/me/sessions/{sessionId} — revokes a specific session.
     *
     * @param sessionId the Keycloak session ID to revoke
     * @param auth      the authenticated user
     * @return 204 No Content
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Revoke a specific session")
    public ResponseEntity<Void> revokeSession(
            @PathVariable String sessionId,
            Authentication auth) {
        log.info("Revoking session: {} for user: {}", sessionId, auth.getName());
        keycloakAdminClient.revokeSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
