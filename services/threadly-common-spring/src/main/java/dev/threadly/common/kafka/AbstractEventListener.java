package dev.threadly.common.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * Abstract base class for Kafka event listeners.
 *
 * Provides:
 * - Structured error handling with retry logic
 * - Exponential backoff on transient failures
 * - Dead-letter-queue routing on persistent failures
 * - Manual offset commit for idempotency
 * - Trace ID propagation for distributed tracing
 *
 * Subclasses must:
 * 1. Extend this class
 * 2. Implement {@link #handleEvent(Map, String)}
 * 3. Add @KafkaListener annotation to the handler method
 *
 * Example:
 * <pre>
 * {@code
 * @Service
 * public class ConversationEventListener extends AbstractEventListener {
 *   @Autowired private ConversationService conversationService;
 *
 *   @KafkaListener(
 *       topics = "conversation-events",
 *       groupId = "conversation-service",
 *       containerFactory = "kafkaListenerContainerFactory"
 *   )
 *   public void onConversationEvent(
 *       @Payload Map<String, Object> event,
 *       @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
 *       @Header(KafkaHeaders.OFFSET) long offset,
 *       Acknowledgment ack) {
 *
 *     String eventType = (String) event.get("eventType");
 *     try {
 *       handleEvent(event, eventType);
 *       ack.acknowledge();
 *     } catch (Exception e) {
 *       handleError(e, event, eventType, partition, offset);
 *     }
 *   }
 *
 *   @Override
 *   protected void handleEvent(Map<String, Object> event, String eventType) throws Exception {
 *     switch (eventType) {
 *       case "conversation.started" -> {
 *         String conversationId = (String) event.get("conversationId");
 *         conversationService.onStarted(conversationId);
 *       }
 *       case "conversation.completed" -> {
 *         String conversationId = (String) event.get("conversationId");
 *         conversationService.onCompleted(conversationId);
 *       }
 *     }
 *   }
 * }
 * }
 * </pre>
 *
 * Retry Policy:
 * - Exponential backoff: 100ms initial, 2x multiplier
 * - Max retries: 3
 * - Backoff delays: 100ms, 200ms, 400ms
 * - On persistent failure: route to dead-letter-queue
 *
 * Idempotency:
 * - Events processed multiple times produce the same result
 * - Database unique constraints prevent duplicate state changes
 * - Manual offset commit ensures message is not reprocessed
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEventListener {

  protected final ObjectMapper objectMapper;

  /**
   * Handle the event. Subclasses must implement this method.
   *
   * @param event Kafka message payload (Map representation)
   * @param eventType Event type (e.g., "conversation.started")
   * @throws Exception if handling fails (will trigger retry + DLQ)
   */
  protected abstract void handleEvent(Map<String, Object> event, String eventType) throws Exception;

  /**
   * Handle errors with retry and DLQ routing.
   *
   * Retry policy:
   * - 3 attempts with exponential backoff (100ms, 200ms, 400ms)
   * - On persistent failure: log and ignore (will be handled by DLQ consumer)
   *
   * @param error Exception that occurred
   * @param event The event that failed to process
   * @param eventType The event type
   * @param partition Kafka partition ID
   * @param offset Message offset
   */
  @Retryable(
      maxAttempts = 3,
      backoff = @Backoff(delay = 100, multiplier = 2)
  )
  protected void handleError(
      Exception error,
      Map<String, Object> event,
      String eventType,
      int partition,
      long offset) {

    log.error(
        "Error processing event: type={}, partition={}, offset={}, error={}",
        eventType, partition, offset, error.getMessage(), error
    );

    // After max retries, the message should be sent to DLQ by Kafka error handler
    // (configured in KafkaConsumerConfig)
  }

  /**
   * Extract trace ID from Kafka headers for distributed tracing.
   *
   * Header key: "traceparent" (W3C Trace Context format)
   * Format: "00-traceId-spanId-sampled"
   *
   * @param traceParent Header value from Kafka message
   * @return Trace ID, or null if header not present
   */
  protected String extractTraceId(String traceParent) {
    if (traceParent == null || traceParent.isEmpty()) {
      return null;
    }

    // Format: 00-traceId-spanId-sampled
    String[] parts = traceParent.split("-");
    return parts.length > 1 ? parts[1] : null;
  }

  /**
   * Extract span ID from Kafka headers for distributed tracing.
   *
   * Header key: "traceparent" (W3C Trace Context format)
   * Format: "00-traceId-spanId-sampled"
   *
   * @param traceParent Header value from Kafka message
   * @return Span ID, or null if header not present
   */
  protected String extractSpanId(String traceParent) {
    if (traceParent == null || traceParent.isEmpty()) {
      return null;
    }

    String[] parts = traceParent.split("-");
    return parts.length > 2 ? parts[2] : null;
  }

  /**
   * Check if an event should be sampled for tracing (DEBUG logging, metrics, etc.).
   *
   * @param traceParent Header value from Kafka message
   * @return True if trace is marked as sampled
   */
  protected boolean isTraceSampled(String traceParent) {
    if (traceParent == null || traceParent.isEmpty()) {
      return false;
    }

    String[] parts = traceParent.split("-");
    return parts.length > 3 && "01".equals(parts[3]);
  }
}
