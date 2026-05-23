package dev.threadly.core.centrifugo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for Centrifugo HTTP API.
 * Publishes messages to channels.
 */
@Slf4j
@Component
public class CentrifugoClient {

  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  public CentrifugoClient(
      @Value("${threadly.centrifugo.url}") String centrifugoUrl,
      @Value("${threadly.centrifugo.api-key}") String apiKey,
      ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.webClient = WebClient.builder()
        .baseUrl(centrifugoUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Authorization", "apikey " + apiKey)
        .build();
  }

  @CircuitBreaker(name = "centrifugo", fallbackMethod = "publishFallback")
  public void publish(String channel, Map<String, Object> data) {
    try {
      Map<String, Object> body = new HashMap<>();
      body.put("channel", channel);
      body.put("data", data);

      webClient.post()
          .uri("/api/publish")
          .bodyValue(body)
          .retrieve()
          .toBodilessEntity()
          .block();
    } catch (Exception e) {
      log.warn("Centrifugo publish failed for channel {}: {}", channel, e.getMessage());
      throw e;
    }
  }

  public void publishFallback(String channel, Map<String, Object> data, Exception ex) {
    log.warn("Centrifugo circuit open — dropping publish to channel {}", channel);
  }

  /** Issue a connection token JWT for a visitor or dashboard user. */
  public String issueToken(String subject, long expirySeconds) {
    // This is handled by JwtService with the Centrifugo HMAC secret
    // Left as extension point
    return "";
  }
}
