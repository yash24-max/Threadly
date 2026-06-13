package dev.threadly.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.core.identity.AuthController.SignupRequest;
import dev.threadly.core.identity.AuthController.TokenResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared base class for all Spring Boot integration tests.
 *
 * <p>Starts PostgreSQL 16 and Redis 7 via Testcontainers (containers are shared across the JVM
 * process through static fields) and wires dynamic property sources so that Spring picks up the
 * container ports.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    // ── Shared containers (started once per JVM via static) ─────────────────

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("threadly_test")
                    .withUsername("threadly")
                    .withPassword("threadly");

    @SuppressWarnings("resource")
    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
        // Point JWT service at the test PEM files we include in src/test/resources
        registry.add("threadly.jwt.private-key-path",
                () -> "src/test/resources/test-private.pem");
        registry.add("threadly.jwt.public-key-path",
                () -> "src/test/resources/test-public.pem");
    }

    // ── Spring-injected beans ────────────────────────────────────────────────

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    // ── Counter for unique e-mail generation ────────────────────────────────

    private static final java.util.concurrent.atomic.AtomicLong EMAIL_SEQ =
            new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Sign up a new user + org, then immediately log in to get a fresh token pair.
     *
     * @return the {@code Authorization: Bearer <token>} header value
     */
    protected String loginAndGetToken(String email, String password) {
        // Login (user was created by createTestOrg or a preceding signup)
        var loginBody = new java.util.HashMap<String, String>();
        loginBody.put("email", email);
        loginBody.put("password", password);

        ResponseEntity<TokenResponse> resp = restTemplate.postForEntity(
                "/v1/auth/login", loginBody, TokenResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        return "Bearer " + resp.getBody().getAccessToken();
    }

    /**
     * Create a new org + admin user via the signup endpoint.
     *
     * @return the orgId returned by signup
     */
    protected UUID createTestOrg(String email, String password, String orgName) {
        SignupRequest req = new SignupRequest();
        req.setOrgName(orgName);
        req.setName("Test User");
        req.setEmail(email);
        req.setPassword(password);

        ResponseEntity<TokenResponse> resp = restTemplate.postForEntity(
                "/v1/auth/signup", req, TokenResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getUser()).isNotNull();
        return UUID.fromString(resp.getBody().getUser().getOrgId());
    }

    /**
     * Builds {@link HttpHeaders} with the given bearer token.
     */
    protected HttpHeaders authHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    /**
     * Performs an authenticated GET request.
     */
    protected <T> ResponseEntity<T> authGet(String url, String token, Class<T> responseType) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(token));
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
    }

    /**
     * Performs an authenticated POST request with a body.
     */
    protected <T> ResponseEntity<T> authPost(
            String url, String token, Object body, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(body, authHeaders(token));
        return restTemplate.postForEntity(url, entity, responseType);
    }

    /**
     * Performs an authenticated PATCH request.
     */
    protected <T> ResponseEntity<T> authPatch(
            String url, String token, Object body, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(body, authHeaders(token));
        return restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
    }

    /**
     * Performs an authenticated DELETE request.
     */
    protected ResponseEntity<Void> authDelete(String url, String token) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(token));
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    /**
     * Generates a unique e-mail address safe to use in a single test.
     */
    protected static String uniqueEmail() {
        return "test." + EMAIL_SEQ.incrementAndGet() + "@threadly-test.dev";
    }
}
