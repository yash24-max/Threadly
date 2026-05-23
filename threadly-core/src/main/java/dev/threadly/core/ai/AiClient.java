package dev.threadly.core.ai;

import java.util.UUID;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * HTTP client for threadly-ai sidecar.
 * Supports streaming completions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

  @Value("${threadly.ai.url}")
  private String aiUrl;

  @Value("${threadly.ai.shared-secret}")
  private String sharedSecret;

  private WebClient webClient() {
    return WebClient.builder()
        .baseUrl(aiUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("X-Service-Secret", sharedSecret)
        .build();
  }

  /**
   * Complete an AI request.
   * Each token is delivered via the tokenConsumer callback (for streaming to Centrifugo).
   * Returns the full response text.
   */
  public String complete(AiRequest request, Consumer<String> tokenConsumer) {
    StringBuilder full = new StringBuilder();
    try {
      webClient().post()
          .uri("/ai/complete")
          .bodyValue(request)
          .retrieve()
          .bodyToFlux(String.class) // SSE token stream
          .doOnNext(token -> {
            if (!token.isBlank()) {
              full.append(token);
              tokenConsumer.accept(token);
            }
          })
          .blockLast();
    } catch (Exception e) {
      log.error("AI completion failed", e);
      String fallback = "I'm sorry, I couldn't process that request. Please try again.";
      tokenConsumer.accept(fallback);
      return fallback;
    }
    return full.toString();
  }

  @Data
  @Builder
  public static class AiRequest {
    private UUID botId;
    private UUID orgId;
    private UUID conversationId;
    private String systemPrompt;
    private String userMessage;
    private boolean useKb;
    private int maxTokens;
    private String provider;
  }
}
