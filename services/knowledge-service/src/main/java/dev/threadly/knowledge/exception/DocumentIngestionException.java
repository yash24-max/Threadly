package dev.threadly.knowledge.exception;

/**
 * Exception thrown when document ingestion/indexing fails.
 * This includes parsing, chunking, or embedding errors.
 */
public class DocumentIngestionException extends RuntimeException {

  /**
   * Construct with error message.
   *
   * @param message the error message
   */
  public DocumentIngestionException(String message) {
    super(message);
  }

  /**
   * Construct with error message and cause.
   *
   * @param message the error message
   * @param cause the root cause
   */
  public DocumentIngestionException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construct for a specific document and step.
   *
   * @param documentId the document ID
   * @param step the processing step (e.g., "parsing", "chunking", "embedding")
   * @param cause the root cause
   */
  public DocumentIngestionException(String documentId, String step, Throwable cause) {
    super("Failed to ingest document " + documentId + " at step: " + step, cause);
  }
}
