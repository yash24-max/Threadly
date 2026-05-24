package dev.threadly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RFC 7807 Problem+JSON response model.
 * Use for all error responses to maintain consistent error contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorModel {
  /** Error type URI (e.g., "https://threadly.dev/errors/invalid-flow") */
  private String type;

  /** HTTP status code (e.g., 400, 401, 404, 500) */
  private int status;

  /** Short error message (e.g., "Invalid flow definition") */
  private String title;

  /** Detailed error description */
  private String detail;

  /** Application-specific error code (e.g., "INVALID_FLOW", "ORG_NOT_FOUND") */
  private String code;

  /** Request ID for tracing */
  private String traceId;

  /** Timestamp of error occurrence */
  private Instant timestamp;

  /** Additional context as key-value pairs */
  private Map<String, Object> context;

  /**
   * Create a 400 Bad Request error.
   */
  public static ErrorModel badRequest(String code, String title, String detail) {
    return ErrorModel.builder()
        .status(400)
        .type("https://threadly.dev/errors/" + code.toLowerCase())
        .code(code)
        .title(title)
        .detail(detail)
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Create a 401 Unauthorized error.
   */
  public static ErrorModel unauthorized(String code, String detail) {
    return ErrorModel.builder()
        .status(401)
        .type("https://threadly.dev/errors/" + code.toLowerCase())
        .code(code)
        .title("Unauthorized")
        .detail(detail)
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Create a 403 Forbidden error.
   */
  public static ErrorModel forbidden(String code, String detail) {
    return ErrorModel.builder()
        .status(403)
        .type("https://threadly.dev/errors/" + code.toLowerCase())
        .code(code)
        .title("Forbidden")
        .detail(detail)
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Create a 404 Not Found error.
   */
  public static ErrorModel notFound(String code, String resource, String identifier) {
    return ErrorModel.builder()
        .status(404)
        .type("https://threadly.dev/errors/" + code.toLowerCase())
        .code(code)
        .title("Not Found")
        .detail(String.format("%s with ID %s not found", resource, identifier))
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Create a 409 Conflict error.
   */
  public static ErrorModel conflict(String code, String detail) {
    return ErrorModel.builder()
        .status(409)
        .type("https://threadly.dev/errors/" + code.toLowerCase())
        .code(code)
        .title("Conflict")
        .detail(detail)
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Create a 429 Too Many Requests error.
   */
  public static ErrorModel tooManyRequests(String detail) {
    return ErrorModel.builder()
        .status(429)
        .type("https://threadly.dev/errors/rate-limit-exceeded")
        .code("RATE_LIMIT_EXCEEDED")
        .title("Too Many Requests")
        .detail(detail)
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Create a 500 Internal Server Error.
   */
  public static ErrorModel internalServerError(String code, String detail) {
    return ErrorModel.builder()
        .status(500)
        .type("https://threadly.dev/errors/" + code.toLowerCase())
        .code(code)
        .title("Internal Server Error")
        .detail(detail)
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Create a 503 Service Unavailable error.
   */
  public static ErrorModel serviceUnavailable(String detail) {
    return ErrorModel.builder()
        .status(503)
        .type("https://threadly.dev/errors/service-unavailable")
        .code("SERVICE_UNAVAILABLE")
        .title("Service Unavailable")
        .detail(detail)
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Add trace ID for distributed tracing.
   */
  public ErrorModel withTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  /**
   * Add context data.
   */
  public ErrorModel withContext(String key, Object value) {
    if (this.context == null) {
      this.context = new java.util.HashMap<>();
    }
    this.context.put(key, value);
    return this;
  }
}
