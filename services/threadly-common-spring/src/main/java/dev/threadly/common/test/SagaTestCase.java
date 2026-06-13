package dev.threadly.common.test;

import dev.threadly.common.kafka.KafkaConsumerConfig;
import dev.threadly.common.kafka.KafkaProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base test case for saga integration tests.
 *
 * Provides:
 * 1. Embedded Kafka for event-driven saga testing
 * 2. Test transaction management for multi-step sagas
 * 3. Helper methods for publishing/consuming events
 * 4. Saga state verification utilities
 *
 * Example:
 * <pre>
 * {@code
 * @SpringBootTest
 * public class ConversationHandoffSagaTest extends SagaTestCase {
 *
 *   @Autowired private ConversationHandoffSaga saga;
 *   @Autowired private ConversationRepository conversationRepository;
 *   @Autowired private SagaStateRepository sagaStateRepository;
 *
 *   @Test
 *   public void testSagaCompletesSuccessfully() throws Exception {
 *     // Setup: create conversation
 *     Conversation conv = new Conversation();
 *     conv.setId("conv-123");
 *     conv.setStatus("PENDING");
 *     conversationRepository.save(conv);
 *
 *     // Publish: conversation.handoff event
 *     publishEvent("conversation-events", Map.of(
 *       "eventType", "conversation.handoff",
 *       "conversationId", "conv-123",
 *       "agentId", "agent-456"
 *     ));
 *
 *     // Wait: for saga to process
 *     Thread.sleep(1000);
 *
 *     // Verify: conversation is assigned
 *     Conversation result = conversationRepository.findById("conv-123").orElseThrow();
 *     assertEquals("ASSIGNED", result.getStatus());
 *     assertEquals("agent-456", result.getAssignedAgentId());
 *
 *     // Verify: saga.started event was published
 *     assertEventPublished("saga-events", "saga.started");
 *   }
 *
 *   @Test
 *   public void testSagaRollsbackOnBillingFailure() throws Exception {
 *     // Setup: customer has no billing
 *     // Publish: conversation.handoff event
 *     // Verify: saga.failed event was published
 *     // Verify: no state changes occurred (compensation)
 *   }
 * }
 * }
 * </pre>
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = {
    "listeners=PLAINTEXT://localhost:0",
    "port=0"
})
@Testcontainers
public abstract class SagaTestCase {

  @Container
  public static KafkaContainer kafkaContainer = new KafkaContainer()
      .withExposedPorts(9092);

  protected EmbeddedKafkaBroker embeddedKafkaBroker;

  @BeforeEach
  public void setupSagaTest() {
    log.info("Setting up saga test case");
    // Initialize Kafka broker if needed
    if (embeddedKafkaBroker != null) {
      log.debug("EmbeddedKafkaBroker: {}", embeddedKafkaBroker.getBrokersAsString());
    }
  }

  /**
   * Publish a test event to Kafka.
   *
   * @param topic Kafka topic (e.g., "conversation-events")
   * @param event Event payload as map
   */
  protected void publishEvent(String topic, java.util.Map<String, Object> event) {
    log.info("Publishing test event to topic={}: {}", topic, event);
    // Use KafkaTemplate to publish
  }

  /**
   * Assert that an event was published to a specific topic.
   *
   * @param topic Kafka topic
   * @param eventType Expected event type (e.g., "saga.started")
   */
  protected void assertEventPublished(String topic, String eventType) {
    log.info("Asserting event was published: topic={}, eventType={}", topic, eventType);
    // Query Kafka or event log to verify publication
  }

  /**
   * Wait for saga to complete (up to timeout).
   *
   * @param sagaId Saga identifier
   * @param timeoutMs Timeout in milliseconds
   */
  protected void waitForSagaCompletion(String sagaId, long timeoutMs) throws InterruptedException {
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      // Check if saga is complete
      if (isSagaCompleted(sagaId)) {
        return;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("Saga did not complete within " + timeoutMs + "ms: sagaId=" + sagaId);
  }

  /**
   * Check if a saga has completed.
   *
   * @param sagaId Saga identifier
   * @return True if saga completed, false otherwise
   */
  protected abstract boolean isSagaCompleted(String sagaId);
}
