package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.feign.AiServiceClient;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import dev.threadly.runtime.service.SessionVariableManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AiReplyNodeExecutor handles AI-generated responses using the threadly-ai service.
 *
 * Node config fields:
 *   prompt        (required) user-facing prompt text (supports {{variable}} interpolation)
 *   system_prompt (optional) override system context
 *   provider      (optional) anthropic | openai | gemini
 *   temperature   (optional, default 0.7)
 *   max_tokens    (optional, default 2000)
 *   use_kb        (optional, default true) include knowledge-base context
 */
@Component
@Slf4j
public class AiReplyNodeExecutor extends NodeExecutor {

    @Autowired
    private SessionVariableManager variableManager;

    @Autowired
    private AiServiceClient aiServiceClient;

    @Value("${threadly.ai.default-provider:anthropic}")
    private String defaultProvider;

    @Override
    public String getName() { return "AI Reply Executor"; }

    @Override
    public String getType() { return "AI_REPLY"; }

    @Override
    public boolean validate(ExecutionContext context) {
        if (!super.validate(context)) return false;
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
            JsonNode node = context.getCurrentNode();
            String prompt       = node.has("prompt")        ? node.get("prompt").asText()        : "";
            String systemPrompt = node.has("system_prompt") ? node.get("system_prompt").asText() : "";
            String provider     = node.has("provider")      ? node.get("provider").asText()      : defaultProvider;
            double temperature  = node.has("temperature")   ? node.get("temperature").asDouble() : 0.7;
            int    maxTokens    = node.has("max_tokens")     ? node.get("max_tokens").asInt()     : 2000;
            boolean useKb       = !node.has("use_kb") || node.get("use_kb").asBoolean(true);

            // Resolve {{variable}} placeholders
            String resolvedPrompt = variableManager.resolveVariables(prompt, context.getSessionVariables());
            String resolvedSystem = variableManager.resolveVariables(systemPrompt, context.getSessionVariables());

            log.info("Calling AI service — provider={} prompt[{}]={}", provider,
                resolvedPrompt.length(), resolvedPrompt.substring(0, Math.min(80, resolvedPrompt.length())));

            // ── 1. Build memory/context if KB is enabled ───────────────────
            String fullSystem = resolvedSystem;
            String botId     = context.getSession() != null ? context.getSession().getBotId()    : null;
            String sessionId = context.getSession() != null ? context.getSession().getId()       : null;

            if (useKb && botId != null) {
                try {
                    Map<String, Object> memReq = Map.of(
                        "bot_id",    botId,
                        "session_id", sessionId != null ? sessionId : "",
                        "recent_turns", 5
                    );
                    Map<String, Object> memResp = aiServiceClient.buildMemory(memReq);
                    String memSystem = (String) memResp.getOrDefault("system_prompt", "");
                    if (!memSystem.isBlank()) {
                        fullSystem = memSystem + (resolvedSystem.isBlank() ? "" : "\n\n" + resolvedSystem);
                    }
                } catch (Exception e) {
                    log.warn("Failed to build memory context, proceeding without KB: {}", e.getMessage());
                }
            }

            // ── 2. Call LLM complete endpoint ──────────────────────────────
            Map<String, Object> aiReq = new HashMap<>();
            aiReq.put("prompt",      resolvedPrompt);
            aiReq.put("provider",    provider);
            aiReq.put("temperature", temperature);
            aiReq.put("max_tokens",  maxTokens);
            if (!fullSystem.isBlank()) {
                aiReq.put("system_prompt", fullSystem);
            }

            Map<String, Object> aiResp = aiServiceClient.complete(aiReq);
            String aiText     = (String)  aiResp.getOrDefault("text",        "");
            int    tokensUsed = ((Number) aiResp.getOrDefault("tokens_used", 0)).intValue();

            log.info("AI response received — tokens={} chars={}", tokensUsed, aiText.length());

            return ExecutionResult.builder()
                .status(ExecutionResult.ExecutionStatus.SUCCESS)
                .statusMessage("AI response generated")
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .nextNodeId(node.has("next") ? node.get("next").asText() : null)
                .build()
                .addMessage("TEXT", aiText)
                .addVariable("_ai_response",   aiText)
                .addVariable("_tokens_used",   String.valueOf(tokensUsed))
                .addVariable("_ai_provider",   provider);

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
