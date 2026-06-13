package dev.threadly.common.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.common.kafka.EventPublisher;
import dev.threadly.common.kafka.OutboxEvent;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Example saga orchestration: Conversation Handoff
 *
 * Flow:
 * 1. conversation.handoff event received
 * 2. Check billing: can user converse? (from billing service)
 * 3. Transfer conversation: handoff to agent (from conversation service)
 * 4. Notify users: inform customer and agent (from notification service)
 * 5. On failure: emit saga.failed, compensate previous steps
 *
 * Kafka topics:
 * - Subscribe: conversation-events (conversation.handoff)
 * - Publish: saga-events (saga.started, saga.failed), billing-events, conversation-events
 *
 * Idempotency:
 * - Each step keyed by (sagaId, stepNumber)
 * - Kafka consumer group ensures exactly-once per partition
 * - Database stored procedures ensure step idempotency
 */
@Slf4j
@Service
public class ConversationHandoffSaga extends SagaOrchestrator {

  private final EventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  // Input data
  private String conversationId;
  private String agentId;
  private String customerId;
  private String workspaceId;

  public ConversationHandoffSaga(EventPublisher eventPublisher, ObjectMapper objectMapper) {
    super(objectMapper);
    this.eventPublisher = eventPublisher;
    this.objectMapper = objectMapper;

    // Define saga steps in order
    addStep(new CheckBillingStep());
    addStep(new TransferConversationStep());
    addStep(new NotifyUsersStep());
  }

  /**
   * Kafka listener: Listen for conversation.handoff events.
   */
  @KafkaListener(
      topics = "conversation-events",
      groupId = "handoff-saga-orchestrator",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onConversationHandoff(Map<String, Object> event) {
    try {
      if (!"conversation.handoff".equals(event.get("eventType"))) {
        return;
      }

      Map<String, Object> payload = (Map<String, Object>) event.get("payload");
      conversationId = (String) payload.get("conversationId");
      agentId = (String) payload.get("agentId");
      customerId = (String) payload.get("customerId");
      workspaceId = (String) payload.get("workspaceId");

      sagaId = conversationId + "-handoff-" + UUID.randomUUID();

      log.info("Starting conversation handoff saga: sagaId={}, conversationId={}, agentId={}",
          sagaId, conversationId, agentId);

      // Publish saga.started event
      eventPublisher.publishEvent("saga-events",
          OutboxEvent.builder()
              .eventType("saga.started")
              .aggregateId(UUID.fromString(workspaceId))
              .payload(toJsonNode(Map.of(
                  "sagaId", sagaId,
                  "sagaType", "conversation.handoff",
                  "conversationId", conversationId,
                  "agentId", agentId
              )))
              .build()
      );

      executeAll();

      log.info("Conversation handoff saga completed: sagaId={}", sagaId);

    } catch (SagaOrchestrator.SagaExecutionException e) {
      log.error("Saga execution failed: {}", e.getMessage());
      publishSagaFailed(e);
    } catch (Exception e) {
      log.error("Unexpected error in saga: {}", e.getMessage(), e);
      publishSagaFailed(e);
    }
  }

  /**
   * Publish saga.failed event with error details.
   */
  private void publishSagaFailed(Exception error) {
    try {
      eventPublisher.publishEvent("saga-events",
          OutboxEvent.builder()
              .eventType("saga.failed")
              .aggregateId(UUID.fromString(workspaceId))
              .payload(toJsonNode(Map.of(
                  "sagaId", sagaId,
                  "sagaType", "conversation.handoff",
                  "conversationId", conversationId,
                  "completedSteps", currentStep,
                  "errorMessage", error.getMessage()
              )))
              .build()
      );
    } catch (Exception e) {
      log.error("Failed to publish saga.failed event: {}", e.getMessage(), e);
    }
  }

  /**
   * Step 1: Check billing permission.
   */
  @RequiredArgsConstructor
  private class CheckBillingStep implements SagaStep {

    @Override
    public void execute(String sagaId, int stepIndex) throws Exception {
      log.info("[Saga {}] Step {}: Checking billing for customer {}", sagaId, stepIndex, customerId);

      // In real implementation, call billing service via Feign client
      // BillingServiceClient.canConverse(customerId) -> check if customer can converse
      // If not allowed, throw BillingException

      // For now, simulate success
      Thread.sleep(100);
      log.info("[Saga {}] Step {}: Billing check passed", sagaId, stepIndex);
    }

    @Override
    public void compensate(String sagaId, int stepIndex) throws Exception {
      // No state to rollback - just a check
      log.info("[Saga {}] Step {}: No compensation needed for billing check", sagaId, stepIndex);
    }
  }

  /**
   * Step 2: Transfer conversation to agent.
   */
  @RequiredArgsConstructor
  private class TransferConversationStep implements SagaStep {

    @Override
    public void execute(String sagaId, int stepIndex) throws Exception {
      log.info("[Saga {}] Step {}: Transferring conversation {} to agent {}",
          sagaId, stepIndex, conversationId, agentId);

      // In real implementation, call conversation service via Feign client
      // ConversationServiceClient.assignAgent(conversationId, agentId)

      // For now, simulate success
      Thread.sleep(100);
      log.info("[Saga {}] Step {}: Conversation transferred successfully", sagaId, stepIndex);
    }

    @Override
    public void compensate(String sagaId, int stepIndex) throws Exception {
      log.info("[Saga {}] Step {}: Unassigning agent {} from conversation {}",
          sagaId, stepIndex, agentId, conversationId);

      // In real implementation, call conversation service to unassign
      // ConversationServiceClient.unassignAgent(conversationId, agentId)

      Thread.sleep(100);
      log.info("[Saga {}] Step {}: Agent unassigned successfully", sagaId, stepIndex);
    }
  }

  /**
   * Step 3: Notify users (customer + agent).
   */
  @RequiredArgsConstructor
  private class NotifyUsersStep implements SagaStep {

    @Override
    public void execute(String sagaId, int stepIndex) throws Exception {
      log.info("[Saga {}] Step {}: Notifying users of handoff", sagaId, stepIndex);

      // In real implementation, call notification service
      // NotificationServiceClient.notifyHandoff(customerId, agentId, conversationId)

      Thread.sleep(100);
      log.info("[Saga {}] Step {}: Users notified successfully", sagaId, stepIndex);
    }

    @Override
    public void compensate(String sagaId, int stepIndex) throws Exception {
      log.info("[Saga {}] Step {}: No compensation needed for notifications", sagaId, stepIndex);
      // Notifications are informational, no state to rollback
    }
  }
}
