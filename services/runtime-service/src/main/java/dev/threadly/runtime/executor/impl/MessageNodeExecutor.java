package dev.threadly.runtime.executor.impl;

import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import dev.threadly.runtime.service.SessionVariableManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MessageNodeExecutor handles sending text messages to the user.
 * Resolves variable references and supports message formatting.
 */
@Component
@Slf4j
public class MessageNodeExecutor extends NodeExecutor {

  @Autowired
  private SessionVariableManager variableManager;

  @Override
  public String getName() {
    return "Message Executor";
  }

  @Override
  public String getType() {
    return "MESSAGE";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    return context.getCurrentNode().has("text") || context.getCurrentNode().has("message");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Message node must have 'text' or 'message' field";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing MESSAGE node: {}", context.getCurrentNode().get("id"));

      // Extract message text
      String messageText = context.getCurrentNode().has("text")
          ? context.getCurrentNode().get("text").asText()
          : context.getCurrentNode().get("message").asText();

      // Resolve variables in message
      String resolvedMessage = variableManager.resolveVariables(
          messageText,
          context.getSessionVariables()
      );

      log.info("Sending message to user: {}", resolvedMessage.substring(0, Math.min(100, resolvedMessage.length())));

      // Build result
      ExecutionResult result = ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Message sent successfully")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .shouldPause(false)
          .build();

      // Add message output
      result.addMessage("TEXT", resolvedMessage);

      // Determine next node
      if (context.getCurrentNode().has("next")) {
        result.setNextNodeId(context.getCurrentNode().get("next").asText());
      }

      return result;

    } catch (Exception e) {
      log.error("Error executing MESSAGE node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Failed to send message")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
