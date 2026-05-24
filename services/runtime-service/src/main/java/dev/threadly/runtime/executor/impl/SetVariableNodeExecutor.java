package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import dev.threadly.runtime.service.SessionVariableManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SetVariableNodeExecutor handles setting or modifying session variables.
 * Supports variable resolution and type coercion.
 */
@Component
@Slf4j
public class SetVariableNodeExecutor extends NodeExecutor {

  @Autowired
  private SessionVariableManager variableManager;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public String getName() {
    return "Set Variable Executor";
  }

  @Override
  public String getType() {
    return "SET_VARIABLE";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("variable") && node.has("value");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Set Variable node must have 'variable' and 'value' fields";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing SET_VARIABLE node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      String variableName = node.get("variable").asText();
      JsonNode valueNode = node.get("value");

      // Resolve variable references in value
      Object resolvedValue = resolveValue(valueNode, context.getSessionVariables());

      // Set variable
      context.setVariable(variableName, resolvedValue);
      log.info("Set variable '{}' to value: {}", variableName, resolvedValue);

      // Build result
      ExecutionResult result = ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Variable set successfully")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build()
          .addVariable(variableName, resolvedValue);

      // Determine next node
      if (node.has("next")) {
        result.setNextNodeId(node.get("next").asText());
      }

      return result;

    } catch (Exception e) {
      log.error("Error executing SET_VARIABLE node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Failed to set variable")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }

  /**
   * Resolve value with variable substitution
   */
  private Object resolveValue(JsonNode valueNode, java.util.Map<String, Object> variables) {
    if (valueNode.isTextual()) {
      String text = valueNode.asText();
      // Check if it's a variable reference
      if (text.startsWith("{{") && text.endsWith("}}")) {
        String varName = text.substring(2, text.length() - 2).trim();
        return variables.getOrDefault(varName, null);
      }
      // Otherwise return as string with variable resolution
      return variableManager.resolveVariables(text, variables);
    } else if (valueNode.isNumber()) {
      if (valueNode.isDouble() || valueNode.isFloat()) {
        return valueNode.asDouble();
      }
      return valueNode.asLong();
    } else if (valueNode.isBoolean()) {
      return valueNode.asBoolean();
    } else if (valueNode.isArray()) {
      return valueNode;
    } else if (valueNode.isObject()) {
      return valueNode;
    }
    return valueNode.asText();
  }
}
