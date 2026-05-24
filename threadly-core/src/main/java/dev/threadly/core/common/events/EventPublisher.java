package dev.threadly.core.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event Publisher facade.
 *
 * Publishes events to Kafka with guaranteed delivery via outbox pattern.
 * In the MVP version, events are published directly (async).
 * In production, events are written to outbox table first, then picked up by a poller.
 *
 * Usage:
 *   eventPublisher.publishEvent("conv.msg.sent", Map.of("conversationId", uuid, "text", "hello"));
 *
 * Event structure:
 *   {
 *     "eventId": "uuid",
 *     "topic": "conv.msg.sent",
 *     "timestamp": 1234567890,
 *     "traceId": "trace-uuid",
 *     "payload": { ... }
 *   }
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final OutboxService outboxService;

  /**
   * Publish an event to Kafka.
   * In MVP: publishes directly via KafkaTemplate.
   * In v2: writes to outbox table, poller picks it up.
   */
  @Transactional
  public void publishEvent(String topic, Map<String, Object> payload) {
    publishEvent(topic, payload, null);
  }

  /**
   * Publish an event with a specific partition key (for ordering).
   * Key is used to ensure events for same entity go to same partition.
   */
  @Transactional
  public void publishEvent(String topic, Map<String, Object> payload, String partitionKey) {
    try {
      String eventId = UUID.randomUUID().toString();
      Map<String, Object> event = buildEventEnvelope(eventId, topic, payload);
      String message = objectMapper.writeValueAsString(event);

      // Write to outbox (for v2 at-least-once semantics)
      outboxService.saveEvent(eventId, topic, message);

      // Publish immediately in same transaction
      String key = partitionKey != null ? partitionKey : eventId;
      kafkaTemplate.send(topic, key, message);

      log.debug("Event published: topic={}, eventId={}, key={}", topic, eventId, key);
    } catch (Exception e) {
      log.error("Failed to publish event to topic: {}", topic, e);
      throw new EventPublishingException("Failed to publish event to topic: " + topic, e);
    }
  }

  /**
   * Build event envelope with metadata.
   */
  private Map<String, Object> buildEventEnvelope(
      String eventId,
      String topic,
      Map<String, Object> payload
  ) {
    Map<String, Object> envelope = new HashMap<>();
    envelope.put("eventId", eventId);
    envelope.put("topic", topic);
    envelope.put("timestamp", Instant.now().toEpochMilli());
    envelope.put("version", "1.0");
    envelope.put("traceId", extractTraceId());
    envelope.put("payload", payload);
    return envelope;
  }

  /**
   * Extract trace ID from context.
   * In production, use OpenTelemetry MDC.
   */
  private String extractTraceId() {
    // TODO: Extract from Micrometer/OpenTelemetry context
    return UUID.randomUUID().toString();
  }

  /**
   * Batch publish multiple events.
   * Useful for publishing multiple related events in one transaction.
   */
  @Transactional
  public void publishEvents(String topic, java.util.List<Map<String, Object>> payloads) {
    for (Map<String, Object> payload : payloads) {
      publishEvent(topic, payload);
    }
  }
}
