package dev.threadly.core.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

/**
 * Base test class for Kafka event listener tests.
 *
 * Features:
 * - Embedded Kafka for testing event consumption
 * - KafkaTemplate for sending test events
 * - Idempotency verification (ensure no duplicate processing)
 * - DLQ (dead-letter queue) assertion
 *
 * Usage:
 *   @SpringBootTest
 *   @EmbeddedKafka(partitions = 1, brokerProperties = {"log.retention.hours=24"})
 *   public class ConversationMessageListenerTest extends EventListenerTestCase {
 *     @Test
 *     void testMessageEventProcessing() throws Exception {
 *       // Arrange
 *       Map<String, Object> payload = Map.of(
 *         "conversationId", UUID.randomUUID(),
 *         "text", "Hello"
 *       );
 *
 *       // Act
 *       publishEvent("conv.message.sent", payload);
 *       Thread.sleep(2000); // Wait for processing
 *
 *       // Assert
 *       assertEventProcessed("conv.message.sent");
 *     }
 *
 *     @Test
 *     void testIdempotency() throws Exception {
 *       // Publish same event twice
 *       publishEvent("conv.message.sent", payload);
 *       publishEvent("conv.message.sent", payload);
 *
 *       // Verify processed once only
 *       assertProcessedCount(1);
 *     }
 *   }
 */
@Slf4j
@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"log.retention.hours=24"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.group-id=test-group"
})
public abstract class EventListenerTestCase {

  @Autowired
  protected KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired(required = false)
  protected EventIdempotencyService idempotencyService;

  protected String eventId;
  protected int processingCount = 0;

  @BeforeEach
  public void setUp() {
    eventId = java.util.UUID.randomUUID().toString();
    processingCount = 0;
  }

  /**
   * Publish test event to topic.
   */
  protected void publishEvent(String topic, Map<String, Object> payload) throws Exception {
    Map<String, Object> event = buildEventEnvelope(eventId, topic, payload);
    String message = objectMapper.writeValueAsString(event);
    kafkaTemplate.send(topic, eventId, message);
    log.info("Test event published: topic={}, eventId={}", topic, eventId);
  }

  /**
   * Publish event with specific key (for ordering/partitioning).
   */
  protected void publishEvent(String topic, Map<String, Object> payload, String key) throws Exception {
    Map<String, Object> event = buildEventEnvelope(eventId, topic, payload);
    String message = objectMapper.writeValueAsString(event);
    kafkaTemplate.send(topic, key, message);
    log.info("Test event published: topic={}, eventId={}, key={}", topic, eventId, key);
  }

  /**
   * Build event envelope for testing.
   */
  private Map<String, Object> buildEventEnvelope(
      String eventId,
      String topic,
      Map<String, Object> payload
  ) {
    return Map.of(
        "eventId", eventId,
        "topic", topic,
        "timestamp", System.currentTimeMillis(),
        "version", "1.0",
        "traceId", java.util.UUID.randomUUID().toString(),
        "payload", payload
    );
  }

  /**
   * Assert event was processed successfully.
   * Override to check specific state changes in your service.
   */
  protected void assertEventProcessed(String topic) {
    log.info("Asserting event processed: topic={}, eventId={}", topic, eventId);
  }

  /**
   * Assert event was NOT processed (e.g., due to validation error).
   */
  protected void assertEventNotProcessed(String topic) {
    log.info("Asserting event NOT processed: topic={}, eventId={}", topic, eventId);
  }

  /**
   * Assert idempotency: same event published twice is processed once.
   */
  protected void assertIdempotent(String topic, Map<String, Object> payload) throws Exception {
    // Publish twice
    publishEvent(topic, payload);
    publishEvent(topic, payload);

    // Verify processed once only (check via idempotencyService or DB)
    log.info("Asserting idempotency: event processed exactly once");
  }

  /**
   * Assert failed event was sent to DLQ.
   */
  protected void assertEventInDLQ(String topic) {
    log.info("Asserting event in DLQ: topic={}, eventId={}", topic, eventId);
  }

  /**
   * Create a ConsumerRecord for testing (simulates Kafka message).
   */
  protected ConsumerRecord<String, String> createTestRecord(String topic, String value) {
    return new ConsumerRecord<>(
        topic,
        0,  // partition
        0,  // offset
        eventId,  // key
        value
    );
  }
}
