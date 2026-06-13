package dev.threadly.analytics.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for analytics service.
 * Provides consistent error responses across all endpoints.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle DashboardNotFoundException.
     */
    @ExceptionHandler(DashboardNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDashboardNotFound(DashboardNotFoundException e) {
        log.warn("Dashboard not found: {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "NOT_FOUND");
        response.put("message", e.getMessage());
        response.put("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle InvalidMetricQueryException.
     */
    @ExceptionHandler(InvalidMetricQueryException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidMetricQuery(InvalidMetricQueryException e) {
        log.warn("Invalid metric query: {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "INVALID_QUERY");
        response.put("message", e.getMessage());
        response.put("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "INVALID_REQUEST");
        response.put("message", e.getMessage());
        response.put("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        log.error("Unexpected error", e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "UNEXPECTED_ERROR");
        response.put("message", "An unexpected error occurred");
        response.put("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
