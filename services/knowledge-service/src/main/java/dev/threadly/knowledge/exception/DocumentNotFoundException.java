package dev.threadly.knowledge.exception;

/**
 * Exception thrown when a requested knowledge base document is not found.
 */
public class DocumentNotFoundException extends RuntimeException {

  /**
   * Construct with document ID.
   *
   * @param documentId the document ID that was not found
   */
  public DocumentNotFoundException(String documentId) {
    super("Document not found: " + documentId);
  }

  /**
   * Construct with custom message.
   *
   * @param message the error message
   */
  public DocumentNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construct with document ID and bot ID for multi-tenant context.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   */
  public DocumentNotFoundException(String documentId, String botId) {
    super("Document not found: " + documentId + " for bot: " + botId);
  }
}
