package dev.threadly.core.common.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox service for guaranteed event delivery.
 *
 * Pattern:
 * 1. publishEvent() writes event to outbox table in same transaction as business logic
 * 2. publishOutboxEvents() polls outbox every 5 seconds
 * 3. For each unpublished event: send to Kafka, mark as published
 * 4. Failed events: retry 3 times, then move to DLQ
 *
 * This ensures at-least-once delivery: even if service crashes after writing outbox,
 * event will be picked up by poller and published to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final OutboxEventRepository outboxRepository;

  private static final int MAX_RETRIES = 3;
  private static final String DLQ_TOPIC = "outbox.dlq";

  /**
   * Save event to outbox table.
   * Called in same transaction as business logic.
   */
  @Transactional
  public void saveEvent(String eventId, String topic, String payload) {
    try {
      OutboxEvent event = OutboxEvent.builder()
          .id(UUID.randomUUID())
          .eventId(eventId)
          .topic(topic)
          .payload(payload)
          .published(false)
          .retryCount(0)
          .createdAt(Instant.now())
          .build();

      outboxRepository.save(event);
      log.debug("Event saved to outbox: eventId={}, topic={}", eventId, topic);
    } catch (Exception e) {
      log.error("Failed to save event to outbox: {}", eventId, e);
      throw new EventPublishingException("Failed to save event to outbox", e);
    }
  }

  /**
   * Poll outbox table and publish unpublished events.
   * Scheduled to run every 5 seconds.
   *
   * For production, consider using a separate poller service to avoid blocking
   * the main application.
   */
  @Scheduled(fixedDelay = 5000, initialDelay = 10000)
  @Transactional
  public void publishOutboxEvents() {
    try {
      // Get all unpublished events
      List<OutboxEvent> events = outboxRepository.findUnpublishedEvents();

      if (events.isEmpty()) {
        return;
      }

      log.debug("Processing {} outbox events", events.size());

      for (OutboxEvent event : events) {
        publishSingleEvent(event);
      }
    } catch (Exception e) {
      log.error("Error processing outbox events", e);
    }
  }

  /**
   * Publish a single event from outbox.
   */
  private void publishSingleEvent(OutboxEvent event) {
    try {
      kafkaTemplate.send(event.getTopic(), event.getEventId(), event.getPayload());
      markAsPublished(event);
      log.debug("Outbox event published: eventId={}, topic={}", event.getEventId(), event.getTopic());
    } catch (Exception e) {
      handlePublishingFailure(event, e);
    }
  }

  /**
   * Mark event as published in database.
   */
  @Transactional
  private void markAsPublished(OutboxEvent event) {
    event.setPublished(true);
    event.setPublishedAt(Instant.now());
    outboxRepository.save(event);
  }

  /**
   * Handle publishing failures with retry logic.
   */
  @Transactional
  private void handlePublishingFailure(OutboxEvent event, Exception e) {
    log.error("Failed to publish outbox event: eventId={}, topic={}, attempt={}/{}",
        event.getEventId(), event.getTopic(), event.getRetryCount() + 1, MAX_RETRIES, e);

    if (event.getRetryCount() < MAX_RETRIES) {
      // Increment retry count and try again
      event.setRetryCount(event.getRetryCount() + 1);
      outboxRepository.save(event);
    } else {
      // Max retries exceeded: send to DLQ
      sendToDLQ(event, "Max retries exceeded: " + e.getMessage());
      outboxRepository.delete(event);
    }
  }

  /**
   * Send event to dead-letter queue when retries are exhausted.
   */
  private void sendToDLQ(OutboxEvent event, String reason) {
    try {
      String dlqPayload = String.format(
          "{\"originalTopic\":\"%s\",\"eventId\":\"%s\",\"reason\":\"%s\",\"payload\":%s}",
          event.getTopic(),
          event.getEventId(),
          reason.replace("\"", "\\\""),
          event.getPayload()
      );
      kafkaTemplate.send(DLQ_TOPIC, event.getEventId(), dlqPayload);
      log.error("Event sent to DLQ: eventId={}, topic={}, reason={}", event.getEventId(), event.getTopic(), reason);
    } catch (Exception e) {
      log.error("Failed to send event to DLQ: eventId={}", event.getEventId(), e);
    }
  }

  /**
   * Get count of unpublished events (for monitoring).
   */
  public long getUnpublishedEventCount() {
    return outboxRepository.countUnpublished();
  }

  /**
   * Get count of failed events in DLQ (for monitoring).
   */
  public long getFailedEventCount() {
    // In production, query separate DLQ event table
    return 0;
  }
}
