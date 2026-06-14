package dev.threadly.identity.controller;

import dev.threadly.identity.entity.Organization;
import dev.threadly.identity.keycloak.KeycloakAdminClient;
import dev.threadly.identity.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles new user registration via Keycloak.
 *
 * Flow:
 *  1. Create org record in local DB
 *  2. Create Keycloak user via Admin API
 *  3. Set orgId + role attributes on Keycloak user (mapped to JWT claims)
 *
 * After this endpoint returns 200, the frontend calls signIn("credentials")
 * which goes through NextAuth to Keycloak ROPC token endpoint.
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "User registration via Keycloak")
public class OnboardingController {

    private final OrganizationService organizationService;
    private final KeycloakAdminClient keycloakAdminClient;

    @PostMapping("/register")
    @Transactional
    @Operation(summary = "Register a new user + org via Keycloak")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        log.info("Registering new user: email={} org={}", req.getEmail(), req.getOrgName());

        // 1. Create org with a temporary placeholder ownerId;
        //    will be updated once the Keycloak user is provisioned.
        Organization org = organizationService.createOrganization(req.getOrgName(), "pending");

        // 2. Create Keycloak user
        String keycloakUserId = keycloakAdminClient.createUser(
                req.getName(), req.getEmail(), req.getPassword());

        // 3. Set orgId + role so they appear in Keycloak JWTs via Protocol Mappers
        keycloakAdminClient.setUserAttributes(keycloakUserId, Map.of(
                "orgId",   org.getId(),
                "role",    "admin",
                "orgName", org.getName()
        ));

        // 4. Update org owner to the Keycloak user id
        organizationService.updateOrganization(org.getId(), null, null, null, null);

        log.info("Registration complete: keycloakUserId={} orgId={}", keycloakUserId, org.getId());
        return ResponseEntity.ok().build();
    }

    // ── Request DTO ──────────────────────────────────────────────────────────

    @Data
    public static class RegisterRequest {
        @NotBlank @Size(max = 200)
        private String name;

        @NotBlank @Size(max = 200)
        private String orgName;

        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 8, max = 128)
        private String password;
    }
}
