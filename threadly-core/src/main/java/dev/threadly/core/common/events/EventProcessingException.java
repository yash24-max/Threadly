package dev.threadly.core.common.events;

/**
 * Exception thrown when event processing fails.
 * Signals a non-retryable application error (as opposed to infrastructure errors).
 */
public class EventProcessingException extends Exception {

  private final String eventId;

  public EventProcessingException(String message) {
    super(message);
    this.eventId = null;
  }

  public EventProcessingException(String message, Throwable cause) {
    super(message, cause);
    this.eventId = null;
  }

  public EventProcessingException(String eventId, String message, Throwable cause) {
    super(message, cause);
    this.eventId = eventId;
  }

  public String getEventId() {
    return eventId;
  }
}
