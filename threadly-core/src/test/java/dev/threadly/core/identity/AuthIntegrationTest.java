package dev.threadly.core.identity;

import static org.assertj.core.api.Assertions.assertThat;

import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.fixtures.TestUserFactory;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

class AuthIntegrationTest extends AbstractIntegrationTest {

  // ── Signup ────────────────────────────────────────────────────────────────

  @Test
  void signup_creates_user_returns_201() {
    String email = TestUserFactory.randomEmail();
    var payload = TestUserFactory.signupPayload("Test Org A", email);

    var response = rest.postForEntity(baseUrl("/v1/auth/signup"), payload, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).containsKey("accessToken");
    assertThat(response.getBody()).containsKey("refreshToken");

    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) response.getBody().get("user");
    assertThat(user).isNotNull();
    assertThat(user.get("email")).isEqualTo(email);
    assertThat(user.get("orgName")).isEqualTo("Test Org A");
  }

  @Test
  void signup_duplicate_email_returns_409() {
    String email = TestUserFactory.randomEmail();
    var payload = TestUserFactory.signupPayload("Org Duplicate", email);

    // First signup succeeds
    rest.postForEntity(baseUrl("/v1/auth/signup"), payload, Map.class);

    // Second signup with same email must be rejected
    var response = rest.postForEntity(baseUrl("/v1/auth/signup"), payload, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  // ── Login ─────────────────────────────────────────────────────────────────

  @Test
  void login_valid_credentials_returns_tokens() {
    String email = TestUserFactory.randomEmail();
    String password = TestUserFactory.defaultPassword();
    rest.postForEntity(
        baseUrl("/v1/auth/signup"),
        TestUserFactory.signupPayload("Login Org", email),
        Map.class);

    var response = rest.postForEntity(
        baseUrl("/v1/auth/login"),
        Map.of("email", email, "password", password),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsKey("accessToken");
    assertThat(response.getBody()).containsKey("refreshToken");
    assertThat(response.getBody().get("expiresIn")).isNotNull();
  }

  @Test
  void login_wrong_password_returns_401() {
    String email = TestUserFactory.randomEmail();
    rest.postForEntity(
        baseUrl("/v1/auth/signup"),
        TestUserFactory.signupPayload("WrongPass Org", email),
        Map.class);

    var response = rest.postForEntity(
        baseUrl("/v1/auth/login"),
        Map.of("email", email, "password", "totally-wrong-password"),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  // ── Refresh token ─────────────────────────────────────────────────────────

  @Test
  void refresh_token_rotates_and_returns_new_tokens() {
    String email = TestUserFactory.randomEmail();
    var signupResp = rest.postForEntity(
        baseUrl("/v1/auth/signup"),
        TestUserFactory.signupPayload("Refresh Org", email),
        Map.class);
    String originalRefresh = (String) signupResp.getBody().get("refreshToken");

    var refreshResp = rest.postForEntity(
        baseUrl("/v1/auth/refresh"),
        Map.of("refreshToken", originalRefresh),
        Map.class);

    assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String newAccess = (String) refreshResp.getBody().get("accessToken");
    String newRefresh = (String) refreshResp.getBody().get("refreshToken");

    assertThat(newAccess).isNotBlank();
    assertThat(newRefresh).isNotBlank();
    // Rotated refresh token must differ from the original
    assertThat(newRefresh).isNotEqualTo(originalRefresh);
  }

  // ── /me ───────────────────────────────────────────────────────────────────

  @Test
  void me_with_valid_token_returns_user_info() {
    String email = TestUserFactory.randomEmail();
    String token = signup_and_login(email, TestUserFactory.defaultPassword(), "Me Org");

    var response = rest.exchange(
        baseUrl("/v1/auth/me"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().get("email")).isEqualTo(email);
    assertThat(response.getBody().get("orgId")).isNotNull();
    assertThat(response.getBody().get("role")).isEqualTo("ADMIN");
  }

  // ── Auth guard ────────────────────────────────────────────────────────────

  @Test
  void protected_endpoint_without_token_returns_401() {
    // Attempt to access a protected resource with no Authorization header
    var response = rest.getForEntity(baseUrl("/v1/bots"), Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
