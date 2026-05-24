package dev.threadly.core.common.saga;

/**
 * Exception thrown when saga orchestration fails.
 */
public class SagaException extends RuntimeException {

  private final String sagaId;

  public SagaException(String message) {
    super(message);
    this.sagaId = null;
  }

  public SagaException(String message, Throwable cause) {
    super(message, cause);
    this.sagaId = null;
  }

  public SagaException(String sagaId, String message, Throwable cause) {
    super(message, cause);
    this.sagaId = sagaId;
  }

  public String getSagaId() {
    return sagaId;
  }
}
