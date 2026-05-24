package dev.threadly.core.common.resilience;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fallback error DTO returned when circuit breaker opens or timeout occurs.
 * Instead of throwing exceptions, return structured error response to caller.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private String error;
  private String message;
  private String code;
  private long timestamp;
  private String service;
  private String path;

  public static ErrorResponse circuitBreakerOpen(String service) {
    return ErrorResponse.builder()
        .error("SERVICE_UNAVAILABLE")
        .message("Circuit breaker is open. Service is temporarily unavailable.")
        .code("CB_OPEN")
        .service(service)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  public static ErrorResponse timeout(String service) {
    return ErrorResponse.builder()
        .error("REQUEST_TIMEOUT")
        .message("Request to " + service + " timed out.")
        .code("TIMEOUT")
        .service(service)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  public static ErrorResponse serviceUnavailable(String service, String reason) {
    return ErrorResponse.builder()
        .error("SERVICE_UNAVAILABLE")
        .message("Service is unavailable: " + reason)
        .code("UNAVAILABLE")
        .service(service)
        .timestamp(System.currentTimeMillis())
        .build();
  }
}
