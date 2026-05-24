package dev.threadly.common.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for publishing domain events via Outbox pattern.
 *
 * Usage in services:
 * @Service
 * public class SessionService {
 *   @Autowired OutboxService outboxService;
 *   @Autowired OutboxRepository outboxRepository;
 *
 *   @Transactional
 *   public void completeSession(Session session) {
 *     session.setStatus("completed");
 *     sessionRepository.save(session);
 *
 *     // Save to outbox in same transaction
 *     outboxService.publish(
 *       OutboxEvent.builder()
 *         .eventType("session.completed")
 *         .aggregateId(session.getId())
 *         .payload(objectMapper.valueToTree(Map.of(
 *           "sessionId", session.getId(),
 *           "botId", session.getBotId(),
 *           "orgId", session.getOrgId()
 *         )))
 *         .build()
 *     );
 *   }
 * }
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

  private final OutboxRepository outboxRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${threadly.outbox.batch-size:100}")
  private int batchSize;

  @Value("${threadly.outbox.polling-interval:5000}")
  private long pollingInterval;

  /**
   * Save event to outbox table (in-transaction).
   * Must be called within @Transactional context.
   */
  @Transactional
  public void publish(OutboxEvent event) {
    if (event.getId() == null) {
      event.setId(UUID.randomUUID());
    }
    outboxRepository.save(event);
    log.debug("Outbox event saved: type={}, aggregateId={}", event.getEventType(), event.getAggregateId());
  }

  /**
   * Scheduled job to poll outbox and publish unpublished events to Kafka.
   * Runs every 5 seconds by default.
   */
  @Scheduled(fixedDelayString = "${threadly.outbox.polling-interval:5000}")
  @Transactional
  public void publishOutboxEvents() {
    long unpublishedCount = outboxRepository.countByPublishedAtIsNull();
    if (unpublishedCount == 0) {
      return; // No events to publish
    }

    log.debug("Publishing outbox events: count={}", unpublishedCount);

    List<OutboxEvent> events = outboxRepository.findUnpublishedLimited(batchSize);
    for (OutboxEvent event : events) {
      try {
        // Derive Kafka topic from event type (e.g., "session.completed" → "session-events")
        String topic = deriveTopicFromEventType(event.getEventType());

        // Send to Kafka with aggregateId as key (for partitioning)
        kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload());

        // Mark as published
        event.markAsPublished();
        outboxRepository.save(event);

        log.debug("Published outbox event: type={}, topic={}, aggregateId={}",
            event.getEventType(), topic, event.getAggregateId());

      } catch (Exception e) {
        log.error("Failed to publish outbox event: type={}, aggregateId={}",
            event.getEventType(), event.getAggregateId(), e);
        // Don't mark as published, will retry next cycle
      }
    }
  }

  /**
   * Derive Kafka topic name from event type.
   * Examples:
   * - "session.completed" → "session-events"
   * - "conversation.started" → "conversation-events"
   * - "flow.published" → "flow-events"
   */
  private String deriveTopicFromEventType(String eventType) {
    String prefix = eventType.split("\\.")[0]; // Get first part before dot
    return prefix + "-events";
  }

  /**
   * Manual publishing (for testing or special cases).
   */
  public void publishNow(OutboxEvent event) {
    try {
      String topic = deriveTopicFromEventType(event.getEventType());
      kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload());
      event.markAsPublished();
      log.info("Manual outbox publish: type={}, topic={}", event.getEventType(), topic);
    } catch (Exception e) {
      log.error("Failed to manually publish outbox event: {}", event.getEventType(), e);
      throw new RuntimeException("Failed to publish event: " + event.getEventType(), e);
    }
  }

  /**
   * Get count of unpublished events (for monitoring/debugging).
   */
  public long getUnpublishedCount() {
    return outboxRepository.countByPublishedAtIsNull();
  }
}
