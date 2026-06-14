package dev.threadly.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.admin.feign.IdentityServiceClient;
import dev.threadly.admin.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Super-admin REST API.
 *
 * All endpoints require ROLE_super_admin (enforced by SecurityConfig).
 * The calling admin's JWT is forwarded to identity-service for each data request.
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Platform administration endpoints")
public class AdminController {

    private final IdentityServiceClient identityClient;
    private final AdminStatsService     statsService;

    // ── Orgs ─────────────────────────────────────────────────────────────────

    @GetMapping("/orgs")
    @Operation(summary = "List all organizations")
    public ResponseEntity<JsonNode> listOrgs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        log.info("Admin list orgs — admin={}", auth.getName());
        JsonNode result = identityClient.listOrganizations(bearer(auth), page, size);
        return ResponseEntity.ok(result);
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "List all users across all orgs")
    public ResponseEntity<JsonNode> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        log.info("Admin list users — admin={}", auth.getName());
        JsonNode result = identityClient.listUsers(bearer(auth), page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Enable or disable a user account")
    public ResponseEntity<Void> setUserStatus(
            @PathVariable String id,
            @RequestBody StatusRequest req,
            Authentication auth) {
        log.info("Admin set user status — userId={} enabled={} admin={}", id, req.enabled(), auth.getName());
        identityClient.setUserStatus(bearer(auth), id,
            new IdentityServiceClient.StatusRequest(req.enabled()));
        return ResponseEntity.noContent().build();
    }

    // ── Platform stats ────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Get platform-wide statistics")
    public ResponseEntity<Map<String, Object>> getStats(Authentication auth) {
        log.debug("Admin stats — admin={}", auth.getName());
        return ResponseEntity.ok(statsService.getPlatformStats());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String bearer(Authentication auth) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        return "Bearer " + jwt.getTokenValue();
    }

    record StatusRequest(boolean enabled) {}
}
