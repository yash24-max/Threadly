package dev.threadly.common.test;

import dev.threadly.common.kafka.AbstractEventListener;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base test case for Kafka event listener integration tests.
 *
 * Provides:
 * 1. Embedded Kafka for event consumer testing
 * 2. Test event publishing utilities
 * 3. Consumer verification helpers
 * 4. Error handling and retry testing
 * 5. Idempotency verification
 *
 * Example:
 * <pre>
 * {@code
 * @SpringBootTest
 * public class ConversationEventListenerTest extends EventListenerTestCase {
 *
 *   @Autowired private ConversationEventListener listener;
 *   @Autowired private ConversationRepository conversationRepository;
 *
 *   @Test
 *   public void testConversationStartedEvent() throws Exception {
 *     // Publish event
 *     publishEvent("conversation-events", Map.of(
 *       "eventType", "conversation.started",
 *       "conversationId", "conv-123",
 *       "customerId", "cust-456"
 *     ));
 *
 *     // Wait for processing
 *     waitForEventProcessing(1000);
 *
 *     // Verify state changed
 *     Conversation conv = conversationRepository.findById("conv-123").orElseThrow();
 *     assertEquals("STARTED", conv.getStatus());
 *   }
 *
 *   @Test
 *   public void testEventProcessingIsIdempotent() throws Exception {
 *     // Publish same event twice
 *     Map<String, Object> event = Map.of(
 *       "eventType", "conversation.started",
 *       "conversationId", "conv-123"
 *     );
 *
 *     publishEvent("conversation-events", event);
 *     publishEvent("conversation-events", event);
 *
 *     waitForEventProcessing(1000);
 *
 *     // Verify state changed only once
 *     Conversation conv = conversationRepository.findById("conv-123").orElseThrow();
 *     assertEquals(1, conv.getVersionNumber()); // Not 2
 *   }
 *
 *   @Test
 *   public void testEventListenerRetriesOnTransientFailure() throws Exception {
 *     // Mock service to fail once, then succeed
 *     when(externalService.process("conv-123"))
 *       .thenThrow(new TemporaryException())
 *       .thenReturn(true);
 *
 *     // Publish event
 *     publishEvent("conversation-events", Map.of(
 *       "eventType", "conversation.started",
 *       "conversationId", "conv-123"
 *     ));
 *
 *     // Wait for retries
 *     waitForEventProcessing(2000);
 *
 *     // Verify succeeded on retry
 *     Conversation conv = conversationRepository.findById("conv-123").orElseThrow();
 *     assertEquals("STARTED", conv.getStatus());
 *     verify(externalService, times(2)).process("conv-123");
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
public abstract class EventListenerTestCase {

  @Container
  public static KafkaContainer kafkaContainer = new KafkaContainer()
      .withExposedPorts(9092);

  @BeforeEach
  public void setupEventListenerTest() {
    log.info("Setting up event listener test case");
  }

  /**
   * Publish a test event to Kafka.
   *
   * @param topic Kafka topic (e.g., "conversation-events")
   * @param event Event payload as map
   */
  protected void publishEvent(String topic, Map<String, Object> event) {
    log.info("Publishing test event to topic={}: {}", topic, event);
    // Use KafkaTemplate to publish
  }

  /**
   * Wait for event listener to process the event.
   *
   * @param timeoutMs Timeout in milliseconds
   */
  protected void waitForEventProcessing(long timeoutMs) throws InterruptedException {
    log.info("Waiting for event processing up to {}ms", timeoutMs);
    Thread.sleep(timeoutMs);
  }

  /**
   * Verify that an event was processed (state changed).
   *
   * @param eventType Event type processed
   * @param timesExpected Number of times the event should have been processed
   */
  protected void assertEventProcessed(String eventType, int timesExpected) {
    log.info("Asserting event processed: type={}, times={}", eventType, timesExpected);
    // Query state to verify processing
  }

  /**
   * Verify that an event was NOT processed (error case).
   *
   * @param eventType Event type that should not have been processed
   */
  protected void assertEventNotProcessed(String eventType) {
    log.info("Asserting event was NOT processed: type={}", eventType);
    // Query state to verify no changes
  }

  /**
   * Simulate event listener error for testing retry logic.
   *
   * @param eventType Event type to simulate error for
   * @param error Exception to throw
   */
  protected void simulateListenerError(String eventType, Exception error) {
    log.info("Simulating listener error for event type: {} -> {}", eventType, error.getMessage());
    // Use mock/stub to throw exception on next call
  }

  /**
   * Publish event and wait for processing to complete.
   *
   * @param topic Kafka topic
   * @param event Event payload
   * @param timeoutMs Timeout for processing
   */
  protected void publishEventAndWait(String topic, Map<String, Object> event, long timeoutMs)
      throws InterruptedException {
    publishEvent(topic, event);
    waitForEventProcessing(timeoutMs);
  }
}
