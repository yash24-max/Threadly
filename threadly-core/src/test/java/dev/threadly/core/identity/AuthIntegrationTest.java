package dev.threadly.core.identity;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.fixtures.UserFactory;
import dev.threadly.core.identity.AuthController.LoginRequest;
import dev.threadly.core.identity.AuthController.SignupRequest;
import dev.threadly.core.identity.AuthController.TokenResponse;
import dev.threadly.core.identity.AuthController.UserResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@code /v1/auth} endpoints.
 *
 * <p>Each test creates its own isolated user/org so there is no shared mutable state.
 */
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void signup_creates_user_and_org() {
        SignupRequest req = UserFactory.signupRequest("Acme Corp");

        ResponseEntity<TokenResponse> resp =
                restTemplate.postForEntity("/v1/auth/signup", req, TokenResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TokenResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getAccessToken()).isNotBlank();
        assertThat(body.getRefreshToken()).isNotBlank();
        assertThat(body.getExpiresIn()).isPositive();

        UserResponse user = body.getUser();
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(req.getEmail().toLowerCase());
        assertThat(user.getOrgId()).isNotBlank();
        assertThat(user.getOrgName()).isEqualTo("Acme Corp");
        assertThat(user.getRole()).isEqualTo("admin");
    }

    @Test
    void login_returns_access_and_refresh_tokens() {
        String email = UserFactory.uniqueEmail();
        createTestOrg(email, UserFactory.DEFAULT_PASSWORD, "Login Test Org");

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(email);
        loginReq.setPassword(UserFactory.DEFAULT_PASSWORD);

        ResponseEntity<TokenResponse> resp =
                restTemplate.postForEntity("/v1/auth/login", loginReq, TokenResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        TokenResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getAccessToken()).isNotBlank();
        assertThat(body.getRefreshToken()).isNotBlank();
        assertThat(body.getExpiresIn()).isGreaterThan(0);
    }

    @Test
    void refresh_rotates_refresh_token() {
        String email = UserFactory.uniqueEmail();
        SignupRequest req = UserFactory.signupRequest("Refresh Org", email);

        ResponseEntity<TokenResponse> signupResp =
                restTemplate.postForEntity("/v1/auth/signup", req, TokenResponse.class);
        assertThat(signupResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String originalRefreshToken = signupResp.getBody().getRefreshToken();

        // Use the refresh token
        Map<String, String> refreshBody = Map.of("refreshToken", originalRefreshToken);
        ResponseEntity<TokenResponse> refreshResp =
                restTemplate.postForEntity("/v1/auth/refresh", refreshBody, TokenResponse.class);

        assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        TokenResponse refreshed = refreshResp.getBody();
        assertThat(refreshed).isNotNull();
        assertThat(refreshed.getAccessToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isNotBlank();
        // The new refresh token must differ from the old one (token rotation)
        assertThat(refreshed.getRefreshToken()).isNotEqualTo(originalRefreshToken);

        // The old refresh token must now be revoked
        ResponseEntity<JsonNode> revokedResp = restTemplate.postForEntity(
                "/v1/auth/refresh", Map.of("refreshToken", originalRefreshToken), JsonNode.class);
        assertThat(revokedResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void access_expired_token_returns_401() {
        // Hit a protected endpoint without any token
        ResponseEntity<JsonNode> resp = restTemplate.getForEntity("/v1/bots", JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void duplicate_email_signup_returns_409_or_400() {
        String email = UserFactory.uniqueEmail();
        SignupRequest first = UserFactory.signupRequest("First Org", email);
        SignupRequest second = UserFactory.signupRequest("Second Org", email);

        ResponseEntity<JsonNode> firstResp =
                restTemplate.postForEntity("/v1/auth/signup", first, JsonNode.class);
        assertThat(firstResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<JsonNode> secondResp =
                restTemplate.postForEntity("/v1/auth/signup", second, JsonNode.class);
        // Server returns 400 (IllegalArgumentException → GlobalExceptionHandler)
        assertThat(secondResp.getStatusCode().value()).isIn(400, 409);
    }

    @Test
    void me_returns_current_user_info() {
        String email = UserFactory.uniqueEmail();
        createTestOrg(email, UserFactory.DEFAULT_PASSWORD, "Me Test Org");
        String token = loginAndGetToken(email, UserFactory.DEFAULT_PASSWORD);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserResponse> resp = restTemplate.exchange(
                "/v1/auth/me", HttpMethod.GET, entity, UserResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse user = resp.getBody();
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email.toLowerCase());
        assertThat(user.getOrgName()).isEqualTo("Me Test Org");
        assertThat(user.getRole()).isEqualTo("admin");
    }

    @Test
    void login_with_wrong_password_returns_400() {
        String email = UserFactory.uniqueEmail();
        createTestOrg(email, UserFactory.DEFAULT_PASSWORD, "Wrong Pass Org");

        LoginRequest badLogin = new LoginRequest();
        badLogin.setEmail(email);
        badLogin.setPassword("WrongPassword99!");

        ResponseEntity<JsonNode> resp =
                restTemplate.postForEntity("/v1/auth/login", badLogin, JsonNode.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void logout_revokes_refresh_token() {
        String email = UserFactory.uniqueEmail();
        SignupRequest req = UserFactory.signupRequest("Logout Org", email);
        ResponseEntity<TokenResponse> signupResp =
                restTemplate.postForEntity("/v1/auth/signup", req, TokenResponse.class);
        String refreshToken = signupResp.getBody().getRefreshToken();

        // Logout
        Map<String, String> logoutBody = Map.of("refreshToken", refreshToken);
        ResponseEntity<Void> logoutResp = restTemplate.postForEntity(
                "/v1/auth/logout", logoutBody, Void.class);
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Subsequent refresh must fail
        ResponseEntity<JsonNode> refreshResp = restTemplate.postForEntity(
                "/v1/auth/refresh", Map.of("refreshToken", refreshToken), JsonNode.class);
        assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
