package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import dev.threadly.runtime.service.RuntimeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * SubflowNodeExecutor handles calling another flow from within a flow.
 * Manages flow nesting and data passing between flows.
 */
@Component
@Slf4j
public class SubflowNodeExecutor extends NodeExecutor {

  @Autowired @Lazy
  private RuntimeExecutor runtimeExecutor;

  @Override
  public String getName() {
    return "Subflow Executor";
  }

  @Override
  public String getType() {
    return "SUBFLOW";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("flow_id");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Subflow node must have 'flow_id' field";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing SUBFLOW node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      String subflowId = node.get("flow_id").asText();

      log.info("Calling subflow: {}", subflowId);

      // TODO: Implement subflow execution logic
      // This would involve:
      // 1. Loading the subflow definition
      // 2. Creating a new execution context for the subflow
      // 3. Executing the subflow
      // 4. Merging variables back to parent context

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Subflow executed")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .nextNodeId(node.has("next") ? node.get("next").asText() : null)
          .build();

    } catch (Exception e) {
      log.error("Error executing SUBFLOW node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Subflow execution failed")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
