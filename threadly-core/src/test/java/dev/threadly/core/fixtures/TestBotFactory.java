package dev.threadly.core.fixtures;

import java.util.Map;

/**
 * Factory helpers for creating test bot payloads.
 */
public final class TestBotFactory {

  /** Default accent colour used in widget theme. */
  public static final String DEFAULT_COLOR = "#4F46E5";

  private TestBotFactory() {}

  /**
   * Returns a create-bot request body suitable for POST /v1/bots.
   *
   * @param name the bot display name
   */
  public static Map<String, Object> createBotPayload(String name) {
    return Map.of(
        "name", name,
        "description", "Integration test bot — " + name,
        "language", "en");
  }

  /**
   * Returns a create-bot payload with a custom language.
   */
  public static Map<String, Object> createBotPayload(String name, String language) {
    return Map.of(
        "name", name,
        "description", "Integration test bot — " + name,
        "language", language);
  }

  /**
   * Returns an update-bot request body suitable for PATCH /v1/bots/{id}.
   *
   * @param newName the replacement name
   */
  public static Map<String, Object> updateBotPayload(String newName) {
    return Map.of("name", newName);
  }
}
