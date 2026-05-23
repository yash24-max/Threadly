package dev.threadly.core.fixtures;

import java.util.Map;

/**
 * Factory helpers for seeding conversations in integration tests via the
 * Centrifugo proxy endpoint (which the runtime uses to create conversations).
 *
 * <p>Since conversations are created by the widget/runtime, not by a direct
 * management API, tests use the proxy endpoint to simulate an incoming message
 * from a visitor which triggers conversation creation.
 */
public final class TestConversationFactory {

  private TestConversationFactory() {}

  /**
   * Returns the JSON body for POST /v1/proxy/connect (Centrifugo proxy connect event).
   * Sending this causes the runtime to create a new conversation session.
   *
   * @param botId    the bot's UUID string
   * @param visitorId the visitor identifier
   */
  public static Map<String, Object> connectPayload(String botId, String visitorId) {
    return Map.of(
        "client", visitorId,
        "transport", "websocket",
        "protocol", "json",
        "data", Map.of(
            "botId", botId,
            "visitorId", visitorId,
            "channel", "website"));
  }

  /**
   * Returns the JSON body for POST /v1/proxy/publish (Centrifugo proxy publish event).
   * Sending this simulates a visitor message which the runtime processes.
   *
   * @param botId     the bot's UUID string
   * @param visitorId the visitor identifier
   * @param message   the text message from the visitor
   */
  public static Map<String, Object> publishPayload(String botId, String visitorId, String message) {
    return Map.of(
        "client", visitorId,
        "channel", "visitor:" + visitorId,
        "data", Map.of(
            "type", "user_message",
            "content", message,
            "botId", botId,
            "visitorId", visitorId));
  }
}
