package dev.threadly.core.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * Normalized response from an integration action execution.
 *
 * <p>
 * All external API responses (Slack, Gmail, Stripe, etc.) are normalized into
 * this structure. Success or failure is indicated by the {@code success} flag.
 * Additional context is provided via {@code statusCode} and {@code errorMessage}.
 * </p>
 *
 * <p>
 * Immutable. Built via {@link #builder()} or static factory methods
 * ({@link #success(Map)}, {@link #failure(String)}, etc).
 * </p>
 */
@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationResult {

  /**
   * True if the action succeeded, false otherwise.
   *
   * <p>
   * Use this flag in conditional logic. When false, check {@code errorMessage}
   * and {@code statusCode} for details.
   * </p>
   */
  boolean success;

  /**
   * Output data from the action, if successful.
   *
   * <p>
   * The structure depends on the action and connector. Examples:
   * </p>
   *
   * <ul>
   *   <li>send_message: {id: "msg123", timestamp: 1234567890}
   *   <li>create_customer: {id: "cust456", email: "user@example.com"}
   *   <li>list_channels: [{id: "C123", name: "general"}, ...]
   * </ul>
   *
   * <p>
   * Null if the action failed or returned no data.
   * </p>
   */
  @Builder.Default Map<String, Object> outputData = new HashMap<>();

  /**
   * Error message if the action failed.
   *
   * <p>
   * Should be concise and actionable. Examples:
   * </p>
   *
   * <ul>
   *   <li>"Invalid API key"
   *   <li>"Rate limit exceeded. Retry after 60 seconds"
   *   <li>"Required parameter 'channel_id' is missing"
   *   <li>"Network timeout after 30 seconds"
   * </ul>
   *
   * <p>
   * Null if the action succeeded.
   * </p>
   */
  String errorMessage;

  /**
   * HTTP status code from the external API.
   *
   * <p>
   * Examples: 200 (success), 400 (bad request), 401 (unauthorized), 429 (rate
   * limit), 500 (server error).
   * </p>
   *
   * <p>
   * Null if no HTTP response was received (e.g., network error, connector
   * validation error).
   * </p>
   */
  Integer statusCode;

  /**
   * Creates a successful result with output data.
   *
   * @param data the output map, must not be null
   * @return a new IntegrationResult with success = true
   */
  public static IntegrationResult success(Map<String, Object> data) {
    return IntegrationResult.builder()
        .success(true)
        .outputData(data != null ? data : Collections.emptyMap())
        .build();
  }

  /**
   * Creates a successful result with empty output.
   *
   * @return a new IntegrationResult with success = true and empty outputData
   */
  public static IntegrationResult success() {
    return success(Collections.emptyMap());
  }

  /**
   * Creates a failure result with an error message.
   *
   * @param message the error message, must not be null
   * @return a new IntegrationResult with success = false
   */
  public static IntegrationResult failure(String message) {
    return IntegrationResult.builder().success(false).errorMessage(message).build();
  }

  /**
   * Creates a failure result with message and status code.
   *
   * @param message the error message, must not be null
   * @param statusCode the HTTP status code
   * @return a new IntegrationResult with success = false
   */
  public static IntegrationResult failure(String message, int statusCode) {
    return IntegrationResult.builder()
        .success(false)
        .errorMessage(message)
        .statusCode(statusCode)
        .build();
  }

  /**
   * Checks if this result represents a retriable error.
   *
   * <p>
   * Retriable errors include timeouts (408, 504), rate limits (429), and
   * temporary failures (5xx).
   * </p>
   *
   * @return true if the failure is retriable
   */
  public boolean isRetriable() {
    if (success) {
      return false;
    }
    if (statusCode == null) {
      return true; // Network errors are retriable
    }
    return statusCode == 408 // Request Timeout
        || statusCode == 429 // Too Many Requests
        || (statusCode >= 500 && statusCode < 600); // Server errors
  }

  /**
   * Gets a value from outputData as a String, or returns null.
   *
   * @param key the key to look up
   * @return the value as string, or null if key missing or value is null
   */
  public String getStringOutput(String key) {
    Object val = outputData.get(key);
    return val != null ? val.toString() : null;
  }

  /**
   * Gets a value from outputData as a Long, or returns null.
   *
   * @param key the key to look up
   * @return the value as long, or null if key missing or value is null
   */
  public Long getLongOutput(String key) {
    Object val = outputData.get(key);
    if (val == null) {
      return null;
    }
    if (val instanceof Long) {
      return (Long) val;
    }
    if (val instanceof Number) {
      return ((Number) val).longValue();
    }
    return null;
  }

  /**
   * Gets a value from outputData as a Boolean, or returns null.
   *
   * @param key the key to look up
   * @return the value as boolean, or null if key missing or value is null
   */
  public Boolean getBooleanOutput(String key) {
    Object val = outputData.get(key);
    if (val instanceof Boolean) {
      return (Boolean) val;
    }
    return null;
  }

  @Override
  public String toString() {
    if (success) {
      return String.format(
          "IntegrationResult{success=true, outputSize=%d}", outputData.size());
    } else {
      return String.format(
          "IntegrationResult{success=false, status=%s, error='%s'}",
          statusCode, errorMessage);
    }
  }
}
