package dev.threadly.identity.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * RFC 7807 Problem+JSON error response format.
 * Standard error response structure for REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  /**
   * Timestamp when the error occurred (ISO 8601 format).
   */
  private LocalDateTime timestamp;

  /**
   * HTTP status code.
   */
  private int status;

  /**
   * Error type/title (e.g., "Bad Request", "Unauthorized").
   */
  private String error;

  /**
   * Detailed error message.
   */
  private String message;

  /**
   * API path that caused the error.
   */
  private String path;

  /**
   * Additional error details (e.g., field validation errors).
   */
  private Map<String, String> details;
}
