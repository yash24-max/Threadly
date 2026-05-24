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
 * QuestionNodeExecutor handles asking questions and capturing user responses.
 * Pauses flow execution waiting for user input, stores response in variables.
 */
@Component
@Slf4j
public class QuestionNodeExecutor extends NodeExecutor {

  @Autowired
  private SessionVariableManager variableManager;

  @Override
  public String getName() {
    return "Question Executor";
  }

  @Override
  public String getType() {
    return "QUESTION";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("text") && node.has("variable");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Question node must have 'text' and 'variable' fields";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing QUESTION node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      String questionText = node.get("text").asText();
      String variableName = node.get("variable").asText();

      // Resolve variables in question
      String resolvedQuestion = variableManager.resolveVariables(
          questionText,
          context.getSessionVariables()
      );

      log.info("Asking question to user, storing in variable: {}", variableName);

      // Build result
      ExecutionResult result = ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.PAUSED)
          .statusMessage("Waiting for user response")
          .shouldPause(true)
          .pauseReason("Waiting for user input")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();

      // Add question output
      result.addMessage("QUESTION", resolvedQuestion);

      // Store metadata for response handling
      if (node.has("type")) {
        result.addVariable("_question_type", node.get("type").asText());
      }
      result.addVariable("_question_variable", variableName);
      result.addVariable("_question_node_id", node.get("id").asText());

      // Store options if provided
      if (node.has("options")) {
        result.addVariable("_question_options", node.get("options"));
      }

      return result;

    } catch (Exception e) {
      log.error("Error executing QUESTION node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Failed to ask question")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
