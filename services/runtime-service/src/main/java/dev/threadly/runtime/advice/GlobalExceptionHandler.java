package dev.threadly.runtime.advice;

import dev.threadly.runtime.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler provides centralized exception handling across the application
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handle SessionNotFoundException
   */
  @ExceptionHandler(SessionNotFoundException.class)
  public ResponseEntity<?> handleSessionNotFound(SessionNotFoundException ex, WebRequest request) {
    log.warn("Session not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
        createErrorResponse("SESSION_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND)
    );
  }

  /**
   * Handle FlowExecutionException
   */
  @ExceptionHandler(FlowExecutionException.class)
  public ResponseEntity<?> handleFlowExecutionException(FlowExecutionException ex, WebRequest request) {
    log.error("Flow execution error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        createErrorResponse("FLOW_EXECUTION_ERROR", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)
    );
  }

  /**
   * Handle InvalidFlowException
   */
  @ExceptionHandler(InvalidFlowException.class)
  public ResponseEntity<?> handleInvalidFlow(InvalidFlowException ex, WebRequest request) {
    log.warn("Invalid flow: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        createErrorResponse("INVALID_FLOW", ex.getMessage(), HttpStatus.BAD_REQUEST)
    );
  }

  /**
   * Handle VariableResolutionException
   */
  @ExceptionHandler(VariableResolutionException.class)
  public ResponseEntity<?> handleVariableResolution(VariableResolutionException ex, WebRequest request) {
    log.warn("Variable resolution error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        createErrorResponse("VARIABLE_RESOLUTION_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST)
    );
  }

  /**
   * Handle validation exceptions
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "VALIDATION_ERROR",
            "message", "Validation failed",
            "errors", errors
        )
    );
  }

  /**
   * Handle generic exceptions
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGenericException(Exception ex, WebRequest request) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        createErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR)
    );
  }

  /**
   * Create error response structure
   */
  private Map<String, Object> createErrorResponse(String errorCode, String message, HttpStatus status) {
    return Map.of(
        "timestamp", LocalDateTime.now(),
        "status", status.value(),
        "error", errorCode,
        "message", message
    );
  }
}
