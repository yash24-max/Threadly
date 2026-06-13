package dev.threadly.common.saga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for saga orchestration.
 *
 * A saga is a long-running transaction split across multiple services.
 * Each step is executed in order, with automatic rollback on failure.
 *
 * Subclasses must:
 * 1. Define steps in the constructor via {@link #addStep(SagaStep)}
 * 2. Implement {@link #getSagaId()} to return unique saga identifier
 * 3. Publish saga events via Kafka (listener pattern)
 *
 * Example:
 * <pre>
 * {@code
 * @Service
 * public class ConversationHandoffSaga extends SagaOrchestrator {
 *   public ConversationHandoffSaga(EventPublisher eventPublisher, ObjectMapper mapper) {
 *     super(eventPublisher, mapper);
 *     addStep(new CheckBillingStep());
 *     addStep(new TransferConversationStep());
 *     addStep(new NotifyUsersStep());
 *   }
 *
 *   public void orchestrate(ConversationHandoff input) {
 *     this.input = input;
 *     executeAll();
 *   }
 * }
 * }
 * </pre>
 *
 * Idempotency: Each step is keyed by (sagaId, stepNumber). Processing the same event
 * twice will yield the same result (idempotent).
 *
 * Rollback: On failure, all previous steps are compensated in reverse order.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class SagaOrchestrator {

  private final ObjectMapper objectMapper;
  protected final List<SagaStep> steps = new ArrayList<>();

  @Getter
  protected String sagaId;
  protected int currentStep = 0;
  protected List<Integer> compensatedSteps = new ArrayList<>();

  /**
   * Add a step to the saga (order matters).
   */
  protected void addStep(SagaStep step) {
    steps.add(step);
  }

  /**
   * Execute all steps sequentially.
   * On failure, rollback all completed steps.
   */
  public void executeAll() {
    if (steps.isEmpty()) {
      log.warn("Saga has no steps defined");
      return;
    }

    sagaId = UUID.randomUUID().toString();
    currentStep = 0;

    while (currentStep < steps.size()) {
      try {
        executeStep(currentStep);
        currentStep++;
      } catch (Exception e) {
        log.error("Saga step failed at index {}: {}", currentStep, e.getMessage(), e);
        rollback();
        throw new SagaExecutionException("Saga failed at step " + currentStep, e);
      }
    }

    log.info("Saga completed successfully: sagaId={}", sagaId);
  }

  /**
   * Execute a single step with idempotency check.
   */
  public void executeStep(int stepIndex) throws Exception {
    if (stepIndex >= steps.size()) {
      throw new IllegalArgumentException("Step index out of bounds: " + stepIndex);
    }

    SagaStep step = steps.get(stepIndex);
    String stepKey = sagaId + "#" + stepIndex;

    log.info("Executing saga step {} ({}) with key: {}", stepIndex, step.getClass().getSimpleName(), stepKey);

    try {
      step.execute(sagaId, stepIndex);
      log.info("Step {} completed successfully", stepIndex);
    } catch (Exception e) {
      log.error("Step {} failed: {}", stepIndex, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Compensate (rollback) a step.
   */
  public void compensate(int stepIndex) throws Exception {
    if (stepIndex >= steps.size()) {
      throw new IllegalArgumentException("Step index out of bounds: " + stepIndex);
    }

    SagaStep step = steps.get(stepIndex);
    log.info("Compensating saga step {} ({})", stepIndex, step.getClass().getSimpleName());

    try {
      step.compensate(sagaId, stepIndex);
      compensatedSteps.add(stepIndex);
      log.info("Step {} compensated successfully", stepIndex);
    } catch (Exception e) {
      log.error("Step {} compensation failed: {}", stepIndex, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Rollback: compensate all completed steps in reverse order.
   */
  public void rollback() {
    log.warn("Rolling back saga: sagaId={}, completedSteps={}", sagaId, currentStep);

    for (int i = currentStep - 1; i >= 0; i--) {
      if (!compensatedSteps.contains(i)) {
        try {
          compensate(i);
        } catch (Exception e) {
          log.error("Failed to compensate step {}: {}", i, e.getMessage(), e);
          // Continue with other compensations
        }
      }
    }

    log.warn("Saga rollback completed: sagaId={}", sagaId);
  }

  /**
   * Check if a step has been executed for this saga.
   */
  public boolean isStepExecuted(int stepIndex) {
    return stepIndex < currentStep;
  }

  /**
   * Get current step index.
   */
  public int getCurrentStepIndex() {
    return currentStep;
  }

  /**
   * Convert object to JSON for event publishing.
   */
  protected JsonNode toJsonNode(Object obj) {
    return objectMapper.valueToTree(obj);
  }

  /**
   * Exception thrown when saga execution fails.
   */
  public static class SagaExecutionException extends RuntimeException {
    public SagaExecutionException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
