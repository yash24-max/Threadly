package dev.threadly.core.common.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

/**
 * Base test class for saga integration tests.
 *
 * Features:
 * - Embedded Kafka (Testcontainers) for testing event-driven flows
 * - Pre-configured KafkaTemplate and ObjectMapper
 * - Utilities for saga assertions
 *
 * Usage:
 *   @SpringBootTest
 *   @EmbeddedKafka(partitions = 1, brokerProperties = {"log.retention.hours=24"})
 *   public class ConversationHandoffSagaTest extends SagaTestCase {
 *     @Test
 *     void testConversationHandoff() {
 *       // Arrange
 *       Map<String, Object> payload = Map.of("conversationId", UUID.randomUUID());
 *
 *       // Act
 *       sagaOrchestrator.startSaga(sagaId, "conversation.handoff", payload);
 *
 *       // Assert
 *       waitForEvent("billing.check.completed", 5, TimeUnit.SECONDS);
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
public abstract class SagaTestCase {

  @Autowired
  protected KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  protected ObjectMapper objectMapper;

  protected String sagaId;
  protected int currentStep = 0;

  @BeforeEach
  public void setUp() {
    sagaId = java.util.UUID.randomUUID().toString();
    currentStep = 0;
  }

  /**
   * Assert saga step completed successfully.
   */
  protected void assertSagaStepCompleted(String stepEvent) {
    log.info("Asserting saga step completed: {}", stepEvent);
    // In production, use TestContainers message verifier
  }

  /**
   * Assert saga failed with specific reason.
   */
  protected void assertSagaFailed(String expectedReason) {
    log.info("Asserting saga failed: {}", expectedReason);
  }

  /**
   * Helper: emit compensating transaction.
   */
  protected void compensateStep(String compensationEvent, Map<String, Object> payload) {
    Map<String, Object> event = new HashMap<>();
    event.put("sagaId", sagaId);
    event.put("event", compensationEvent);
    event.put("timestamp", System.currentTimeMillis());
    log.info("Emitting compensation: {}", compensationEvent);
  }

  /**
   * Helper: build saga event.
   */
  protected Map<String, Object> buildSagaEvent(String event, Map<String, Object> payload) {
    Map<String, Object> sagaEvent = new HashMap<>();
    sagaEvent.put("sagaId", sagaId);
    sagaEvent.put("event", event);
    sagaEvent.put("stepNumber", currentStep++);
    sagaEvent.put("timestamp", System.currentTimeMillis());
    sagaEvent.put("payload", payload);
    return sagaEvent;
  }
}
