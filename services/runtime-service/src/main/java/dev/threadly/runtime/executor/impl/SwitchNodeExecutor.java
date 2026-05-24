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
 * SwitchNodeExecutor handles multi-branch logic based on variable values.
 * Similar to switch statement in programming languages.
 */
@Component
@Slf4j
public class SwitchNodeExecutor extends NodeExecutor {

  @Autowired
  private SessionVariableManager variableManager;

  @Override
  public String getName() {
    return "Switch Executor";
  }

  @Override
  public String getType() {
    return "SWITCH";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("variable") && node.has("cases");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Switch node must have 'variable' and 'cases' fields";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing SWITCH node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      String variableName = node.get("variable").asText();
      Object variableValue = context.getVariable(variableName, Object.class);

      log.info("Switch on variable '{}' with value: {}", variableName, variableValue);

      // Find matching case
      JsonNode casesNode = node.get("cases");
      String nextNodeId = null;

      if (casesNode != null && casesNode.isArray()) {
        for (JsonNode caseNode : casesNode) {
          String caseValue = caseNode.get("value").asText();
          String caseValueStr = String.valueOf(variableValue);

          if (caseValueStr.equals(caseValue)) {
            nextNodeId = caseNode.get("next").asText();
            log.debug("Found matching case: {}", caseValue);
            break;
          }
        }
      }

      // Use default if no case matched
      if (nextNodeId == null && node.has("default")) {
        nextNodeId = node.get("default").asText();
        log.debug("No matching case, using default");
      }

      // Use next if no default
      if (nextNodeId == null && node.has("next")) {
        nextNodeId = node.get("next").asText();
      }

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Switch evaluated")
          .nextNodeId(nextNodeId)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build()
          .addVariable("_switch_matched_value", String.valueOf(variableValue));

    } catch (Exception e) {
      log.error("Error executing SWITCH node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Failed to evaluate switch")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
