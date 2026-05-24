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
 * ClassifyIntentNodeExecutor detects user intent from input text
 */
@Component
@Slf4j
public class ClassifyIntentNodeExecutor extends NodeExecutor {

  @Autowired
  private SessionVariableManager variableManager;

  @Override
  public String getName() {
    return "Classify Intent Executor";
  }

  @Override
  public String getType() {
    return "CLASSIFY_INTENT";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("intents") || node.has("intent_variable");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Classify Intent node must have 'intents' or 'intent_variable' field";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing CLASSIFY_INTENT node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();

      // Get input text from variable
      String inputVariable = node.has("input_variable") ? node.get("input_variable").asText() : "user_input";
      Object inputValue = context.getVariable(inputVariable, Object.class);
      String inputText = inputValue != null ? inputValue.toString() : "";

      log.info("Classifying intent from input: {}", inputText.substring(0, Math.min(100, inputText.length())));

      // TODO: Call intent classification service
      // This would involve:
      // 1. Sending text to NLU service
      // 2. Parsing classification results
      // 3. Determining branch based on intent

      String detectedIntent = "general"; // Placeholder

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Intent classified")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .nextNodeId(node.has("next") ? node.get("next").asText() : null)
          .build()
          .addVariable("_detected_intent", detectedIntent);

    } catch (Exception e) {
      log.error("Error executing CLASSIFY_INTENT node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Intent classification failed")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
