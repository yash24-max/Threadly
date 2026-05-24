package dev.threadly.runtime.exception;

/**
 * InvalidFlowException is thrown when flow definition is invalid
 */
public class InvalidFlowException extends RuntimeException {

  public InvalidFlowException(String message) {
    super(message);
  }

  public InvalidFlowException(String message, Throwable cause) {
    super(message, cause);
  }
}
