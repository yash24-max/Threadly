package dev.threadly.core.common.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Abstract base class for saga orchestrators.
 *
 * Sagas coordinate distributed transactions across multiple services via async events.
 * Each saga step is idempotent (keyed by sagaId + stepNumber) to handle retries.
 *
 * Saga pattern:
 * 1. SagaOrchestrator.start() → emit "saga.started" event
 * 2. Subscribe to service response → process & emit next step
 * 3. On failure: emit "saga.failed" event, trigger compensating transactions
 * 4. Idempotency: store (sagaId, stepNumber) in database to prevent duplicate execution
 *
 * Example: ConversationHandoffSaga
 * - Start: conversation.handoff.initiated
 * - Step 1: emit billing.check (check credits)
 * - Listen: billing.check.completed
 * - Step 2: emit response (handoff confirmed)
 * - Compensate: emit conversation.handoff.failed
 */
@Slf4j
@RequiredArgsConstructor
public abstract class SagaOrchestrator {

  protected final KafkaTemplate<String, String> kafkaTemplate;
  protected final ObjectMapper objectMapper;

  protected static final String SAGA_STARTED = "saga.started";
  protected static final String SAGA_FAILED = "saga.failed";

  /**
   * Start a new saga.
   * Each saga should have a unique ID (usually UUID).
   */
  public void startSaga(String sagaId, String event, Map<String, Object> payload) {
    try {
      Map<String, Object> sagaEvent = buildSagaEvent(sagaId, event, 0, payload);
      String topic = getSagaTopic();
      String message = objectMapper.writeValueAsString(sagaEvent);
      kafkaTemplate.send(topic, sagaId, message);
      log.info("Saga started: {} for event: {}", sagaId, event);
    } catch (Exception e) {
      log.error("Failed to start saga: {}", sagaId, e);
      throw new SagaException("Failed to start saga: " + sagaId, e);
    }
  }

  /**
   * Emit the next saga step.
   * Called by event listeners when current step completes.
   */
  protected void emitSagaStep(
      String sagaId,
      int stepNumber,
      String nextEvent,
      Map<String, Object> payload
  ) {
    try {
      Map<String, Object> sagaEvent = buildSagaEvent(sagaId, nextEvent, stepNumber, payload);
      String topic = getSagaTopic();
      String message = objectMapper.writeValueAsString(sagaEvent);
      kafkaTemplate.send(topic, sagaId, message);
      log.info("Saga step emitted: sagaId={}, step={}, event={}", sagaId, stepNumber, nextEvent);
    } catch (Exception e) {
      log.error("Failed to emit saga step: {}", sagaId, e);
      failSaga(sagaId, stepNumber, "Failed to emit step", e);
    }
  }

  /**
   * Fail the saga and emit compensating transactions.
   * Override in subclasses to define compensation logic.
   */
  protected void failSaga(String sagaId, int stepNumber, String reason, Exception cause) {
    try {
      log.error("Saga failed: sagaId={}, step={}, reason={}", sagaId, stepNumber, reason, cause);

      Map<String, Object> failureEvent = new HashMap<>();
      failureEvent.put("sagaId", sagaId);
      failureEvent.put("stepNumber", stepNumber);
      failureEvent.put("reason", reason);
      failureEvent.put("timestamp", System.currentTimeMillis());

      Map<String, Object> sagaEvent = buildSagaEvent(sagaId, SAGA_FAILED, stepNumber, failureEvent);
      String topic = getSagaTopic();
      String message = objectMapper.writeValueAsString(sagaEvent);
      kafkaTemplate.send(topic, sagaId, message);

      // Subclasses override compensate() to execute rollback logic
      compensate(sagaId, stepNumber);
    } catch (Exception e) {
      log.error("Failed to handle saga failure: {}", sagaId, e);
    }
  }

  /**
   * Execute compensating transactions on saga failure.
   * Override in subclasses to define rollback logic.
   */
  protected void compensate(String sagaId, int stepNumber) {
    log.warn("No compensation logic defined for saga: {}", sagaId);
  }

  /**
   * Build a saga event envelope.
   * Includes: sagaId, stepNumber (for idempotency), timestamp, original payload.
   */
  private Map<String, Object> buildSagaEvent(
      String sagaId,
      String event,
      int stepNumber,
      Map<String, Object> payload
  ) {
    Map<String, Object> envelope = new HashMap<>();
    envelope.put("sagaId", sagaId);
    envelope.put("event", event);
    envelope.put("stepNumber", stepNumber);
    envelope.put("timestamp", System.currentTimeMillis());
    envelope.put("traceId", extractOrCreateTraceId());
    envelope.put("payload", payload);
    return envelope;
  }

  /**
   * Extract trace ID from context or create new one.
   * Used for distributed tracing across saga steps.
   */
  protected String extractOrCreateTraceId() {
    // In production, extract from MDC or thread-local context
    // For now, generate UUID
    return UUID.randomUUID().toString();
  }

  /**
   * Get the Kafka topic for this saga.
   * Override in subclasses for specific topic names.
   */
  protected abstract String getSagaTopic();

  /**
   * Get saga name for logging.
   */
  protected abstract String getSagaName();
}
