package dev.threadly.workspace.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for all REST endpoints.
 * Converts all exceptions to RFC 7807 Problem+JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handle IllegalArgumentException (validation errors)
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleValidationError(
      IllegalArgumentException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.validation(ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle ResourceNotFoundException
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      ResourceNotFoundException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.notFound(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handle UnauthorizedException
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(
      UnauthorizedException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.unauthorized(ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Handle ForbiddenException
   */
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(
      ForbiddenException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.forbidden(ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  /**
   * Handle all other exceptions as internal server errors
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, WebRequest request) {
    ErrorResponse error =
        ErrorResponse.internal("An unexpected error occurred: " + ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
