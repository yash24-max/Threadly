package dev.threadly.workspace.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * RFC 7807 Problem+JSON error response format.
 *
 * Used by all endpoints to provide consistent error information:
 * - type: URI that identifies the error class
 * - title: Short human-readable summary
 * - detail: Human-readable explanation specific to this error
 * - status: HTTP status code
 * - instance: URI identifying the specific error occurrence
 * - timestamp: When the error occurred
 */
public record ErrorResponse(
    @JsonProperty("type") String type,
    @JsonProperty("title") String title,
    @JsonProperty("detail") String detail,
    @JsonProperty("status") int status,
    @JsonProperty("instance") String instance,
    @JsonProperty("timestamp") String timestamp) {

  public static ErrorResponse of(String type, String title, String detail, int status) {
    return new ErrorResponse(
        type, title, detail, status, null, Instant.now().toString());
  }

  public static ErrorResponse validation(String detail) {
    return of(
        "https://api.threadly.dev/errors/validation",
        "Validation Error",
        detail,
        400);
  }

  public static ErrorResponse notFound(String detail) {
    return of(
        "https://api.threadly.dev/errors/not-found",
        "Not Found",
        detail,
        404);
  }

  public static ErrorResponse internal(String detail) {
    return of(
        "https://api.threadly.dev/errors/internal-server-error",
        "Internal Server Error",
        detail,
        500);
  }

  public static ErrorResponse unauthorized(String detail) {
    return of(
        "https://api.threadly.dev/errors/unauthorized",
        "Unauthorized",
        detail,
        401);
  }

  public static ErrorResponse forbidden(String detail) {
    return of(
        "https://api.threadly.dev/errors/forbidden",
        "Forbidden",
        detail,
        403);
  }
}
