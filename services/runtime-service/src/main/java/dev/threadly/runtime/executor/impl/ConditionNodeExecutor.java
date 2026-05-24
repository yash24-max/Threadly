package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import dev.threadly.runtime.service.ConditionEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ConditionNodeExecutor evaluates conditions and branches flow accordingly.
 * Supports multiple conditions with true/false branching.
 */
@Component
@Slf4j
public class ConditionNodeExecutor extends NodeExecutor {

  @Autowired
  private ConditionEvaluator conditionEvaluator;

  @Override
  public String getName() {
    return "Condition Executor";
  }

  @Override
  public String getType() {
    return "CONDITION";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("condition") && (node.has("true_next") || node.has("false_next"));
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Condition node must have 'condition', and 'true_next' or 'false_next'";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing CONDITION node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      JsonNode conditionDef = node.get("condition");

      // Evaluate condition
      boolean result = conditionEvaluator.evaluate(
          conditionDef,
          context.getSessionVariables()
      );

      log.info("Condition evaluated to: {}", result);

      // Determine next node based on condition
      String nextNodeId;
      if (result && node.has("true_next")) {
        nextNodeId = node.get("true_next").asText();
        log.debug("Taking TRUE branch to node: {}", nextNodeId);
      } else if (!result && node.has("false_next")) {
        nextNodeId = node.get("false_next").asText();
        log.debug("Taking FALSE branch to node: {}", nextNodeId);
      } else {
        // Default next node
        nextNodeId = node.has("next") ? node.get("next").asText() : null;
        log.debug("No branch match, using default next node: {}", nextNodeId);
      }

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Condition evaluated")
          .nextNodeId(nextNodeId)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build()
          .addVariable("_condition_result", result);

    } catch (Exception e) {
      log.error("Error executing CONDITION node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Failed to evaluate condition")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
