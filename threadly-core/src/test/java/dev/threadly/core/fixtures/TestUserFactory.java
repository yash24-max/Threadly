package dev.threadly.core.fixtures;

import java.util.Map;
import java.util.UUID;

/**
 * Factory helpers for creating test user payloads.
 * All methods are pure (no I/O) — they produce Maps or Strings only.
 */
public final class TestUserFactory {

  private TestUserFactory() {}

  /** Returns a unique email address guaranteed not to collide between test runs. */
  public static String randomEmail() {
    return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@threadly-test.dev";
  }

  /**
   * Returns a signup request body suitable for POST /v1/auth/signup.
   *
   * @param orgName the organisation name to create
   * @return an unmodifiable Map with all required fields populated
   */
  public static Map<String, Object> signupPayload(String orgName) {
    return Map.of(
        "email", randomEmail(),
        "password", "Threadly@Test1!",
        "orgName", orgName,
        "name", "Integration Tester");
  }

  /**
   * Returns a signup payload with an explicit email address (useful when
   * the calling test needs to reference the email afterwards for login).
   */
  public static Map<String, Object> signupPayload(String orgName, String email) {
    return Map.of(
        "email", email,
        "password", "Threadly@Test1!",
        "orgName", orgName,
        "name", "Integration Tester");
  }

  public static String defaultPassword() {
    return "Threadly@Test1!";
  }
}
