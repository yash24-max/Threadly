package dev.threadly.core.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Thin HTTP client for Keycloak's Admin REST API.
 *
 * Uses the threadly-admin service account (client_credentials grant) to
 * obtain an admin token, then creates users and sets their attributes.
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

    /** Obtain a short-lived admin access token via client_credentials grant. */
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
     * Create a Keycloak user and set their initial password.
     *
     * @return the Keycloak user UUID (extracted from the Location response header)
     */
    public String createUser(String name, String email, String password) {
        String adminToken = getAdminToken();
        String usersUrl   = baseUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> userRep = Map.of(
                "username",  email,
                "email",     email,
                "firstName", name.contains(" ") ? name.substring(0, name.indexOf(' ')) : name,
                "lastName",  name.contains(" ") ? name.substring(name.indexOf(' ') + 1) : "",
                "enabled",   true,
                "emailVerified", true,
                "credentials", List.of(Map.of(
                        "type",      "password",
                        "value",     password,
                        "temporary", false
                ))
        );

        ResponseEntity<Void> resp = restTemplate.postForEntity(
                usersUrl, new HttpEntity<>(userRep, headers), Void.class);

        if (resp.getStatusCode() != HttpStatus.CREATED) {
            throw new KeycloakAdminException("Keycloak user creation failed: " + resp.getStatusCode());
        }

        // Keycloak returns the new user ID in the Location header:
        // /admin/realms/threadly/users/{uuid}
        String location = resp.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (location == null) {
            throw new KeycloakAdminException("Keycloak did not return Location header after user creation");
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }

    /**
     * Set custom attributes on a Keycloak user (e.g. orgId, role).
     *
     * @param keycloakUserId the Keycloak user UUID
     * @param attributes     map of attribute name → single value
     */
    public void setUserAttributes(String keycloakUserId, Map<String, String> attributes) {
        String adminToken = getAdminToken();
        String userUrl    = baseUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        // Keycloak attribute values must be List<String>
        Map<String, List<String>> attrMap = new java.util.HashMap<>();
        attributes.forEach((k, v) -> attrMap.put(k, List.of(v)));

        Map<String, Object> updateRep = Map.of("attributes", attrMap);

        restTemplate.exchange(
                userUrl,
                HttpMethod.PUT,
                new HttpEntity<>(updateRep, headers),
                Void.class);

        log.info("Set Keycloak user attributes: userId={} attrs={}", keycloakUserId, attributes.keySet());
    }

    // ── Exception ────────────────────────────────────────────────────────────

    public static class KeycloakAdminException extends RuntimeException {
        public KeycloakAdminException(String message) { super(message); }
    }
}
