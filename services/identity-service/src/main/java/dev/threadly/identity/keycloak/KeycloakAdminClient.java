package dev.threadly.identity.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.identity.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin HTTP client for Keycloak's Admin REST API.
 * Uses the threadly-admin service account (client_credentials grant) to
 * obtain an admin token, then creates users and manages sessions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    @Value("${keycloak.base-url:http://keycloak:8080}")
    private String baseUrl;

    @Value("${keycloak.realm:threadly}")
    private String realm;

    @Value("${keycloak.admin.client-id:threadly-admin}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret:threadly-admin-secret}")
    private String adminClientSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ── Admin token ──────────────────────────────────────────────────────────

    private String getAdminToken() {
        String tokenUrl = baseUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type",    "client_credentials");
        body.add("client_id",     adminClientId);
        body.add("client_secret", adminClientSecret);

        ResponseEntity<JsonNode> resp = restTemplate.postForEntity(
                tokenUrl, new HttpEntity<>(body, headers), JsonNode.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new KeycloakAdminException("Failed to obtain admin token from Keycloak");
        }
        return resp.getBody().get("access_token").asText();
    }

    // ── User management ──────────────────────────────────────────────────────

    /**
     * Create a Keycloak user with initial password.
     * @return the Keycloak user UUID (from Location response header)
     */
    public String createUser(String name, String email, String password) {
        String adminToken = getAdminToken();
        String usersUrl   = baseUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> userRep = Map.of(
                "username",      email,
                "email",         email,
                "firstName",     name.contains(" ") ? name.substring(0, name.indexOf(' ')) : name,
                "lastName",      name.contains(" ") ? name.substring(name.indexOf(' ') + 1) : "",
                "enabled",       true,
                "emailVerified", true,
                "credentials",   List.of(Map.of(
                        "type",      "password",
                        "value",     password,
                        "temporary", false
                ))
        );

        ResponseEntity<Void> resp;
        try {
            resp = restTemplate.postForEntity(
                    usersUrl, new HttpEntity<>(userRep, headers), Void.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new DuplicateEmailException(email);
            }
            log.error("Keycloak user creation HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new KeycloakAdminException("Keycloak user creation failed: " + e.getStatusCode());
        }

        if (resp.getStatusCode() != HttpStatus.CREATED) {
            throw new KeycloakAdminException("Keycloak user creation failed: " + resp.getStatusCode());
        }

        String location = resp.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (location == null) {
            throw new KeycloakAdminException("Keycloak did not return Location header after user creation");
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }

    /**
     * Set custom attributes on a Keycloak user (e.g. orgId, role).
     */
    public void setUserAttributes(String keycloakUserId, Map<String, String> attributes) {
        String adminToken = getAdminToken();
        String userUrl    = baseUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, List<String>> attrMap = new HashMap<>();
        attributes.forEach((k, v) -> attrMap.put(k, List.of(v)));

        Map<String, Object> updateRep = Map.of("attributes", attrMap);

        restTemplate.exchange(
                userUrl,
                HttpMethod.PUT,
                new HttpEntity<>(updateRep, headers),
                Void.class);

        log.info("Set Keycloak user attributes: userId={} attrs={}", keycloakUserId, attributes.keySet());
    }

    /**
     * Get active sessions for a user.
     */
    public JsonNode getUserSessions(String keycloakUserId) {
        String adminToken = getAdminToken();
        String sessionsUrl = baseUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/sessions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<JsonNode> resp = restTemplate.exchange(
                sessionsUrl, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new KeycloakAdminException("Failed to retrieve sessions from Keycloak");
        }
        return resp.getBody();
    }

    /**
     * Revoke (logout) a specific session.
     */
    public void revokeSession(String sessionId) {
        String adminToken = getAdminToken();
        String sessionUrl = baseUrl + "/admin/realms/" + realm + "/sessions/" + sessionId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        try {
            restTemplate.exchange(sessionUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            log.info("Revoked Keycloak session: {}", sessionId);
        } catch (HttpClientErrorException e) {
            log.warn("Failed to revoke session {}: {}", sessionId, e.getStatusCode());
            throw new KeycloakAdminException("Failed to revoke session: " + e.getStatusCode());
        }
    }

    // ── Exception ────────────────────────────────────────────────────────────

    public static class KeycloakAdminException extends RuntimeException {
        public KeycloakAdminException(String message) { super(message); }
    }
}
