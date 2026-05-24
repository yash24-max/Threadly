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
 * AiReplyNodeExecutor handles AI-generated responses using LLM services
 */
@Component
@Slf4j
public class AiReplyNodeExecutor extends NodeExecutor {

  @Autowired
  private SessionVariableManager variableManager;

  @Override
  public String getName() {
    return "AI Reply Executor";
  }

  @Override
  public String getType() {
    return "AI_REPLY";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("prompt") || node.has("system_prompt");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "AI Reply node must have 'prompt' or 'system_prompt' field";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing AI_REPLY node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      String prompt = node.has("prompt") ? node.get("prompt").asText() : "";
      String systemPrompt = node.has("system_prompt") ? node.get("system_prompt").asText() : "";

      // Resolve variables in prompts
      String resolvedPrompt = variableManager.resolveVariables(prompt, context.getSessionVariables());
      String resolvedSystemPrompt = variableManager.resolveVariables(systemPrompt, context.getSessionVariables());

      log.info("Generating AI response for prompt: {}", prompt.substring(0, Math.min(100, prompt.length())));

      // TODO: Call AI service to generate response
      // This would involve:
      // 1. Building prompt with context
      // 2. Calling LLM API (Claude, OpenAI, etc.)
      // 3. Parsing response
      // 4. Storing tokens used

      String aiResponse = "AI generated response";

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("AI response generated")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .nextNodeId(node.has("next") ? node.get("next").asText() : null)
          .build()
          .addMessage("TEXT", aiResponse)
          .addVariable("_ai_response", aiResponse);

    } catch (Exception e) {
      log.error("Error executing AI_REPLY node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("AI response generation failed")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
