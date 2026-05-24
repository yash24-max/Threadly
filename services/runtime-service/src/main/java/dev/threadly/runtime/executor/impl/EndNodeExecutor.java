package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import dev.threadly.runtime.service.SessionVariableManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * EndNodeExecutor terminates flow execution.
 * Optional: sends exit message and stores session end data.
 */
@Component
@Slf4j
public class EndNodeExecutor extends NodeExecutor {

  @Autowired
  private SessionVariableManager variableManager;

  @Override
  public String getName() {
    return "End Executor";
  }

  @Override
  public String getType() {
    return "END";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing END node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();

      // Send optional exit message
      if (node.has("message")) {
        String message = node.get("message").asText();
        String resolvedMessage = variableManager.resolveVariables(
            message,
            context.getSessionVariables()
        );
        log.info("End message: {}", resolvedMessage);
      }

      log.info("Flow execution completed");

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Flow completed")
          .nextNodeId(null) // End node has no next
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build()
          .addMessage("TEXT", "Flow completed");

    } catch (Exception e) {
      log.error("Error executing END node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Failed to end flow")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
