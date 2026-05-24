package dev.threadly.flow.config;

import dev.threadly.flow.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for the flow service.
 * Maps domain exceptions to HTTP responses with appropriate status codes.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles flow not found exceptions.
   */
  @ExceptionHandler(FlowNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleFlowNotFoundException(
      FlowNotFoundException ex, WebRequest request) {
    log.warn("Flow not found: {}", ex.getFlowId());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("error", "Not Found");
    body.put("message", ex.getMessage());
    body.put("flow_id", ex.getFlowId());

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles flow validation exceptions.
   */
  @ExceptionHandler(FlowValidationException.class)
  public ResponseEntity<Map<String, Object>> handleFlowValidationException(
      FlowValidationException ex, WebRequest request) {
    log.warn("Flow validation failed: {} - {}", ex.getFlowId(), String.join("; ", ex.getErrors()));

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Validation Failed");
    body.put("message", ex.getMessage());
    body.put("flow_id", ex.getFlowId());
    body.put("errors", ex.getErrors());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles flow publish exceptions.
   */
  @ExceptionHandler(FlowPublishException.class)
  public ResponseEntity<Map<String, Object>> handleFlowPublishException(
      FlowPublishException ex, WebRequest request) {
    log.warn("Flow publish failed: {} - {}", ex.getFlowId(), ex.getReason());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("error", "Publish Failed");
    body.put("message", ex.getMessage());
    body.put("flow_id", ex.getFlowId());
    body.put("reason", ex.getReason());

    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  /**
   * Handles cyclic flow exceptions.
   */
  @ExceptionHandler(CyclicFlowException.class)
  public ResponseEntity<Map<String, Object>> handleCyclicFlowException(
      CyclicFlowException ex, WebRequest request) {
    log.warn("Cyclic flow detected: {}", ex.getFlowId());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("flow_id", ex.getFlowId());
    body.put("cycle", ex.getCycle());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles invalid node type exceptions.
   */
  @ExceptionHandler(InvalidNodeTypeException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidNodeTypeException(
      InvalidNodeTypeException ex, WebRequest request) {
    log.warn("Invalid node type: {} in flow {}", ex.getNodeType(), ex.getFlowId());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("node_type", ex.getNodeType());
    body.put("flow_id", ex.getFlowId());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles invalid flow definition exceptions.
   */
  @ExceptionHandler(InvalidFlowDefinitionException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidFlowDefinitionException(
      InvalidFlowDefinitionException ex, WebRequest request) {
    log.warn("Invalid flow definition: {}", ex.getReason());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("flow_id", ex.getFlowId());
    body.put("reason", ex.getReason());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles general illegal argument exceptions.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    log.warn("Illegal argument: {}", ex.getMessage());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles general illegal state exceptions.
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalStateException(
      IllegalStateException ex, WebRequest request) {
    log.warn("Illegal state: {}", ex.getMessage());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("error", "Conflict");
    body.put("message", ex.getMessage());

    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  /**
   * Handles all other exceptions.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGlobalException(
      Exception ex, WebRequest request) {
    log.error("Unexpected error occurred", ex);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Internal Server Error");
    body.put("message", "An unexpected error occurred");

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
