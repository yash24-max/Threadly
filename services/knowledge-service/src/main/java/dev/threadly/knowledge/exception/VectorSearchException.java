package dev.threadly.knowledge.exception;

/**
 * Exception thrown when vector database operations fail.
 * This includes connection errors, search failures, or indexing errors in Qdrant.
 */
public class VectorSearchException extends RuntimeException {

  /**
   * Construct with error message.
   *
   * @param message the error message
   */
  public VectorSearchException(String message) {
    super(message);
  }

  /**
   * Construct with error message and cause.
   *
   * @param message the error message
   * @param cause the root cause
   */
  public VectorSearchException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construct for a specific operation.
   *
   * @param operation the operation (e.g., "search", "index", "delete")
   * @param collectionName the Qdrant collection name
   * @param cause the root cause
   */
  public VectorSearchException(String operation, String collectionName, Throwable cause) {
    super("Vector search operation '" + operation + "' failed for collection: " + collectionName, cause);
  }
}
