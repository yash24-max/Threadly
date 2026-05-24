package dev.threadly.common.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade for event publishing via Kafka + Outbox pattern.
 *
 * Usage:
 * <pre>
 * {@code
 * @Autowired EventPublisher eventPublisher;
 *
 * @Transactional
 * public void completeConversation(Conversation conv) {
 *   conversation.setStatus("completed");
 *   conversationRepository.save(conversation);
 *
 *   eventPublisher.publishEvent("conversation-events",
 *     OutboxEvent.builder()
 *       .eventType("conversation.completed")
 *       .aggregateId(conversation.getId())
 *       .payload(objectMapper.valueToTree(Map.of(
 *         "conversationId", conversation.getId(),
 *         "duration", conversation.getDuration()
 *       )))
 *       .build()
 *   );
 * }
 * }
 * </pre>
 *
 * Atomicity: Event is saved to outbox table in same transaction as business logic.
 * Eventual consistency: Outbox poller publishes to Kafka every 5 seconds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

  private final OutboxService outboxService;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  /**
   * Publish an event via outbox pattern.
   *
   * Must be called within @Transactional context to ensure atomicity with business logic.
   *
   * @param topic Kafka topic name (e.g., "conversation-events", "billing-events")
   * @param event OutboxEvent with eventType, aggregateId, payload
   */
  @Transactional
  public void publishEvent(String topic, OutboxEvent event) {
    if (topic == null || topic.trim().isEmpty()) {
      throw new IllegalArgumentException("Topic cannot be null or empty");
    }

    log.debug("Publishing event: topic={}, type={}, aggregateId={}",
        topic, event.getEventType(), event.getAggregateId());

    try {
      // Save to outbox (in-transaction)
      outboxService.publish(event);
      log.debug("Event saved to outbox: type={}, aggregateId={}", event.getEventType(), event.getAggregateId());
    } catch (Exception e) {
      log.error("Failed to publish event to outbox: topic={}, type={}", topic, event.getEventType(), e);
      throw new EventPublishingException("Failed to publish event: " + event.getEventType(), e);
    }
  }

  /**
   * Publish an event synchronously to Kafka (bypass outbox).
   *
   * Use only when outbox pattern is not applicable (e.g., external integrations).
   * Not recommended for critical events - prefer publishEvent() instead.
   *
   * @param topic Kafka topic name
   * @param key Partition key (typically aggregateId)
   * @param payload Event payload as JSON
   */
  public void publishEventNow(String topic, String key, JsonNode payload) {
    try {
      kafkaTemplate.send(topic, key, payload);
      log.debug("Event published synchronously: topic={}, key={}", topic, key);
    } catch (Exception e) {
      log.error("Failed to publish event to Kafka: topic={}, key={}", topic, key, e);
      throw new EventPublishingException("Failed to publish event to Kafka", e);
    }
  }

  /**
   * Get count of unpublished events (for monitoring).
   */
  public long getUnpublishedEventCount() {
    return outboxService.getUnpublishedCount();
  }

  /**
   * Exception thrown when event publishing fails.
   */
  public static class EventPublishingException extends RuntimeException {
    public EventPublishingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
