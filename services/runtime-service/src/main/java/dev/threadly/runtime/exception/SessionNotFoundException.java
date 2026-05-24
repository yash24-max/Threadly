package dev.threadly.runtime.exception;

/**
 * SessionNotFoundException is thrown when a session cannot be found
 */
public class SessionNotFoundException extends RuntimeException {

  public SessionNotFoundException(String message) {
    super(message);
  }

  public SessionNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
