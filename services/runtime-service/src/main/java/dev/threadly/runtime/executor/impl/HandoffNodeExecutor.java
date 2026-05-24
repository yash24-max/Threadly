package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * HandoffNodeExecutor transfers conversation to human agent
 */
@Component
@Slf4j
public class HandoffNodeExecutor extends NodeExecutor {

  @Override
  public String getName() {
    return "Handoff Executor";
  }

  @Override
  public String getType() {
    return "HANDOFF";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    // Handoff node is optional
    return true;
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing HANDOFF node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      String queue = node.has("queue") ? node.get("queue").asText() : "default";
      String message = node.has("message") ? node.get("message").asText() : "Connecting to agent...";

      log.info("Handing off conversation to queue: {}", queue);

      // TODO: Implement handoff logic
      // This would involve:
      // 1. Creating task in queue management system
      // 2. Assigning to available agent
      // 3. Pausing current flow
      // 4. Forwarding conversation context

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.PAUSED)
          .statusMessage("Conversation handed off to agent")
          .shouldPause(true)
          .pauseReason("Waiting for agent")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build()
          .addMessage("TEXT", message)
          .addVariable("_handoff_queue", queue);

    } catch (Exception e) {
      log.error("Error executing HANDOFF node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Handoff failed")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
