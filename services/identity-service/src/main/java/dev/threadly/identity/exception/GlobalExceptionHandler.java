package dev.threadly.identity.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API.
 * Implements RFC 7807 Problem+JSON format for error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handle InvalidCredentialsException (401).
   */
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(
      InvalidCredentialsException ex,
      WebRequest request) {

    log.warn("Invalid credentials attempt: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .status(HttpStatus.UNAUTHORIZED.value())
        .error("Unauthorized")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handle DuplicateEmailException (409).
   */
  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateEmail(
      DuplicateEmailException ex,
      WebRequest request) {

    log.warn("Duplicate email attempt: {}", ex.getEmail());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .status(HttpStatus.CONFLICT.value())
        .error("Conflict")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  /**
   * Handle InvalidApiKeyException (401).
   */
  @ExceptionHandler(InvalidApiKeyException.class)
  public ResponseEntity<ErrorResponse> handleInvalidApiKey(
      InvalidApiKeyException ex,
      WebRequest request) {

    log.warn("Invalid API key attempt: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .status(HttpStatus.UNAUTHORIZED.value())
        .error("Unauthorized")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handle ResourceNotFoundException (404).
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex,
      WebRequest request) {

    log.warn("Resource not found: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .status(HttpStatus.NOT_FOUND.value())
        .error("Not Found")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  /**
   * Handle validation errors (400).
   * Collects field-level validation errors from @Valid annotations.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex,
      WebRequest request) {

    log.warn("Validation error: {}", ex.getMessage());

    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String message = error.getDefaultMessage();
      fieldErrors.put(fieldName, message);
    });

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Bad Request")
        .message("Validation failed")
        .path(request.getDescription(false).replace("uri=", ""))
        .details(fieldErrors)
        .build();

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle generic runtime exceptions (500).
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      RuntimeException ex,
      WebRequest request) {

    log.error("Unexpected runtime exception: ", ex);

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Internal Server Error")
        .message("An unexpected error occurred")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handle all other exceptions (500).
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(
      Exception ex,
      WebRequest request) {

    log.error("Unexpected exception: ", ex);

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Internal Server Error")
        .message("An unexpected error occurred")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
