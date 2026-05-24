package dev.threadly.runtime.exception;

/**
 * VariableResolutionException is thrown when variable resolution fails
 */
public class VariableResolutionException extends RuntimeException {

  public VariableResolutionException(String message) {
    super(message);
  }

  public VariableResolutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
