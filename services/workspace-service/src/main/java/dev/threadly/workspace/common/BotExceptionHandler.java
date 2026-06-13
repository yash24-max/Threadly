package dev.threadly.workspace.common;

import dev.threadly.workspace.bot.exception.BotAccessDeniedException;
import dev.threadly.workspace.bot.exception.BotNotFoundException;
import dev.threadly.workspace.bot.exception.DuplicateBotNameException;
import dev.threadly.workspace.bot.exception.InvalidBotConfigException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Exception handlers for bot-specific exceptions.
 * Extends global exception handling with bot domain exceptions.
 */
@RestControllerAdvice
public class BotExceptionHandler {

  /**
   * Handle BotNotFoundException
   */
  @ExceptionHandler(BotNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBotNotFound(
      BotNotFoundException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.notFound(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handle BotAccessDeniedException
   */
  @ExceptionHandler(BotAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleBotAccessDenied(
      BotAccessDeniedException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.forbidden(ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  /**
   * Handle DuplicateBotNameException
   */
  @ExceptionHandler(DuplicateBotNameException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateBotName(
      DuplicateBotNameException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.of(
        "https://api.threadly.dev/errors/duplicate-bot-name",
        "Duplicate Bot Name",
        ex.getMessage(),
        409);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handle InvalidBotConfigException
   */
  @ExceptionHandler(InvalidBotConfigException.class)
  public ResponseEntity<ErrorResponse> handleInvalidBotConfig(
      InvalidBotConfigException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.validation(ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle IllegalStateException
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(
      IllegalStateException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.of(
        "https://api.threadly.dev/errors/invalid-state",
        "Invalid State",
        ex.getMessage(),
        409);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }
}
