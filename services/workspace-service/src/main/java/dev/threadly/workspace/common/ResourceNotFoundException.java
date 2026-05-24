package dev.threadly.workspace.common;

/**
 * Thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public static ResourceNotFoundException botNotFound(String botId) {
    return new ResourceNotFoundException("Bot not found: " + botId);
  }

  public static ResourceNotFoundException templateNotFound(String templateId) {
    return new ResourceNotFoundException("Template not found: " + templateId);
  }
}
