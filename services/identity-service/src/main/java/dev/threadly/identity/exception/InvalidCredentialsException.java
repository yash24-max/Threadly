package dev.threadly.identity.exception;

/**
 * Exception thrown when user credentials (email/password) are invalid.
 * Used during login attempts with incorrect credentials.
 */
public class InvalidCredentialsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs exception with default message.
   */
  public InvalidCredentialsException() {
    super("Invalid email or password");
  }

  /**
   * Constructs exception with custom message.
   *
   * @param message the error message
   */
  public InvalidCredentialsException(String message) {
    super(message);
  }

  /**
   * Constructs exception with custom message and cause.
   *
   * @param message the error message
   * @param cause the underlying exception
   */
  public InvalidCredentialsException(String message, Throwable cause) {
    super(message, cause);
  }
}
