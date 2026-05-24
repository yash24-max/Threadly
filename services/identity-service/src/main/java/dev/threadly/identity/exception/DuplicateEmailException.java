package dev.threadly.identity.exception;

/**
 * Exception thrown when attempting to create a user with an email that already exists.
 * Emails must be unique across all users in the system.
 */
public class DuplicateEmailException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String email;

  /**
   * Constructs exception with email that's already in use.
   *
   * @param email the duplicate email address
   */
  public DuplicateEmailException(String email) {
    super("Email already in use: " + email);
    this.email = email;
  }

  /**
   * Constructs exception with custom message.
   *
   * @param message the error message
   * @param email the duplicate email address
   */
  public DuplicateEmailException(String message, String email) {
    super(message);
    this.email = email;
  }

  /**
   * Get the email that caused the conflict.
   *
   * @return the duplicate email address
   */
  public String getEmail() {
    return email;
  }
}
