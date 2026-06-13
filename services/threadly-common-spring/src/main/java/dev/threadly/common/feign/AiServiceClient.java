package dev.threadly.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for the threadly-ai FastAPI service (:8081).
 *
 * Covers the synchronous endpoints:
 *   POST /llm/complete       — single LLM completion (no streaming)
 *   POST /llm/classify       — intent / category classification
 *   POST /chat/memory/build  — assemble conversation memory context
 *
 * Note: the streaming POST /chat/rag-reply endpoint uses SSE and must be
 * consumed via RestTemplate + ResponseExtractor, not Feign.
 */
@FeignClient(
    name = "threadly-ai",
    url = "${threadly.services.ai-service.url:http://threadly-ai:8081}"
)
public interface AiServiceClient {

    /**
     * Single (non-streaming) LLM completion.
     *
     * Request body fields:
     *   prompt        (required) user prompt
     *   system_prompt (optional) system context
     *   provider      (optional) anthropic | openai | gemini
     *   temperature   (optional, default 0.7)
     *   max_tokens    (optional, default 2000)
     *
     * Response fields:
     *   text          completed text
     *   provider      provider used
     *   model         model used
     *   tokens_used   total tokens consumed
     */
    @PostMapping("/llm/complete")
    Map<String, Object> complete(@RequestBody Map<String, Object> request);

    /**
     * Intent / category classification.
     *
     * Request body fields:
     *   text          (required) text to classify
     *   categories    (required) list of category strings
     *   provider      (optional)
     *
     * Response fields:
     *   category      top predicted category
     *   confidence    confidence score 0-1
     *   all_scores    map of category → score
     */
    @PostMapping("/llm/classify")
    Map<String, Object> classify(@RequestBody Map<String, Object> request);

    /**
     * Build conversation memory context (system prompt + KB passages).
     *
     * Request body fields:
     *   bot_id        (required)
     *   session_id    (required)
     *   recent_turns  (optional, default 5)
     *
     * Response fields:
     *   system_prompt assembled system prompt
     *   context_text  full context string
     *   token_count   estimated token count
     */
    @PostMapping("/chat/memory/build")
    Map<String, Object> buildMemory(@RequestBody Map<String, Object> request);
}
