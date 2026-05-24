package dev.threadly.runtime.exception;

/**
 * FlowExecutionException is thrown when flow execution encounters an error
 */
public class FlowExecutionException extends RuntimeException {

  public FlowExecutionException(String message) {
    super(message);
  }

  public FlowExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
