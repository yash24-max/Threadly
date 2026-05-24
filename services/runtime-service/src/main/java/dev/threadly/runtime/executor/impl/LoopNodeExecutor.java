package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LoopNodeExecutor handles repeating actions based on conditions or counts
 */
@Component
@Slf4j
public class LoopNodeExecutor extends NodeExecutor {

  @Override
  public String getName() {
    return "Loop Executor";
  }

  @Override
  public String getType() {
    return "LOOP";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("body_node_id") || node.has("count");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Loop node must have 'body_node_id' and/or 'count' field";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing LOOP node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      int iterations = 0;

      // Get loop count
      if (node.has("count")) {
        iterations = node.get("count").asInt();
      }

      log.info("Starting loop with {} iterations", iterations);

      // TODO: Implement loop execution logic
      // This would involve:
      // 1. Storing loop state (iteration count, body node)
      // 2. Executing body node multiple times
      // 3. Breaking on condition or max iterations

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Loop executed")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .nextNodeId(node.has("next") ? node.get("next").asText() : null)
          .build();

    } catch (Exception e) {
      log.error("Error executing LOOP node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Loop execution failed")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
