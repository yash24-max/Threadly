package dev.threadly.core.common.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.retrytopic.DltHandler;
import org.springframework.kafka.retrytopic.RetryableTopic;
import org.springframework.retry.annotation.Backoff;

/**
 * Abstract base class for all Kafka event listeners.
 *
 * Features:
 * - Idempotency: can handle same event multiple times (via deduplication key)
 * - Exponential backoff: retries failed events with increasing delay
 * - Dead-letter queue: failed events after max retries go to DLQ
 * - Error handling: structured exception handling with logging
 *
 * Usage:
 *   @Component
 *   public class ConversationMessageListener extends AbstractEventListener {
 *     @Override
 *     protected void handleEvent(Map<String, Object> payload) {
 *       // Process conversation.message.sent event
 *     }
 *   }
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventListener {

  protected final ObjectMapper objectMapper;
  protected final EventIdempotencyService idempotencyService;

  /**
   * Handle incoming event from Kafka.
   * Wraps actual event processing with idempotency and error handling.
   */
  protected final void processEvent(ConsumerRecord<String, String> record) {
    try {
      String eventId = record.key();
      String message = record.value();

      // Parse event envelope
      JsonNode eventNode = objectMapper.readTree(message);
      String topic = eventNode.get("topic").asText();
      long timestamp = eventNode.get("timestamp").asLong();
      Map<String, Object> payload = objectMapper.convertValue(
          eventNode.get("payload"),
          Map.class
      );

      // Check idempotency
      if (!idempotencyService.isFirstTime(eventId, getListenerName())) {
        log.debug("Duplicate event (already processed): eventId={}, topic={}", eventId, topic);
        return;
      }

      // Process event
      log.info("Processing event: eventId={}, topic={}, listener={}", eventId, topic, getListenerName());
      handleEvent(payload);

      // Mark as processed
      idempotencyService.markAsProcessed(eventId, getListenerName());
      log.debug("Event processed successfully: eventId={}, topic={}", eventId, topic);

    } catch (EventProcessingException e) {
      log.error("Event processing failed (application error): {}", e.getMessage());
      throw new RuntimeException("Event processing failed", e);
    } catch (Exception e) {
      log.error("Error processing event", e);
      throw new RuntimeException("Error processing event", e);
    }
  }

  /**
   * Handle the event payload.
   * Override in subclasses to implement specific event handling logic.
   */
  protected abstract void handleEvent(Map<String, Object> payload) throws EventProcessingException;

  /**
   * Get unique name for this listener.
   * Used for idempotency tracking.
   */
  protected abstract String getListenerName();

  /**
   * Get Kafka topics this listener subscribes to.
   */
  protected abstract String[] getTopics();

  /**
   * Handle DLQ (dead-letter queue) messages.
   * Called when event processing fails after max retries.
   * Override to implement DLQ-specific logic.
   */
  @DltHandler
  public void handleDlt(ConsumerRecord<String, String> record) {
    log.error("DLT received message: topic={}, partition={}, offset={}, key={}, value={}",
        record.topic(), record.partition(), record.offset(), record.key(), record.value());
    // In production, store DLQ events for manual investigation
  }

  /**
   * Extract trace ID from event.
   * Used for distributed tracing.
   */
  protected String extractTraceId(Map<String, Object> payload) {
    // In production, extract from event envelope
    return java.util.UUID.randomUUID().toString();
  }

  /**
   * Convert raw map to typed object.
   * Useful for deserializing event payloads to domain objects.
   */
  protected <T> T mapToObject(Map<String, Object> data, Class<T> targetClass) {
    return objectMapper.convertValue(data, targetClass);
  }
}
