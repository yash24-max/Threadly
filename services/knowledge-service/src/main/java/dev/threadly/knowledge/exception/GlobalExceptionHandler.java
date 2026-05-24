package dev.threadly.knowledge.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the knowledge service.
 * Provides consistent error response format across all endpoints.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handle DocumentNotFoundException.
   *
   * @param ex the exception
   * @param request the request
   * @return error response
   */
  @ExceptionHandler(DocumentNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleDocumentNotFound(
      DocumentNotFoundException ex,
      WebRequest request) {

    log.error("Document not found: {}", ex.getMessage());

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("error", "Not Found");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false).replace("uri=", ""));

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  /**
   * Handle DocumentIngestionException.
   *
   * @param ex the exception
   * @param request the request
   * @return error response
   */
  @ExceptionHandler(DocumentIngestionException.class)
  public ResponseEntity<Map<String, Object>> handleDocumentIngestion(
      DocumentIngestionException ex,
      WebRequest request) {

    log.error("Document ingestion failed: {}", ex.getMessage());

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Ingestion Error");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false).replace("uri=", ""));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  /**
   * Handle VectorSearchException.
   *
   * @param ex the exception
   * @param request the request
   * @return error response
   */
  @ExceptionHandler(VectorSearchException.class)
  public ResponseEntity<Map<String, Object>> handleVectorSearch(
      VectorSearchException ex,
      WebRequest request) {

    log.error("Vector search failed: {}", ex.getMessage());

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Search Error");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false).replace("uri=", ""));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  /**
   * Handle generic exceptions.
   *
   * @param ex the exception
   * @param request the request
   * @return error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(
      Exception ex,
      WebRequest request) {

    log.error("Unhandled exception", ex);

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Internal Server Error");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false).replace("uri=", ""));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
