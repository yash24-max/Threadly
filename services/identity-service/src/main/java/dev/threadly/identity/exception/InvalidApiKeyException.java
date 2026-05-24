package dev.threadly.identity.exception;

/**
 * Exception thrown when an API key is invalid, revoked, or expired.
 * Used during API key validation and authentication.
 */
public class InvalidApiKeyException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs exception with default message.
   */
  public InvalidApiKeyException() {
    super("Invalid or expired API key");
  }

  /**
   * Constructs exception with custom message.
   *
   * @param message the error message
   */
  public InvalidApiKeyException(String message) {
    super(message);
  }

  /**
   * Constructs exception with custom message and cause.
   *
   * @param message the error message
   * @param cause the underlying exception
   */
  public InvalidApiKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}
