package dev.threadly.core.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDeliveryService {

  private static final String HMAC_ALGO = "HmacSHA256";

  private final WebhookRepository webhookRepository;
  private final ObjectMapper objectMapper;
  private final WebClient.Builder webClientBuilder;

  /**
   * Dispatches an event to all active webhooks for a bot that subscribe to the given event type.
   */
  @Async
  public void dispatch(UUID botId, String eventType, Map<String, Object> payload) {
    List<Webhook> webhooks = webhookRepository.findAllByBotIdAndActiveTrue(botId);
    for (Webhook webhook : webhooks) {
      if (subscribesTo(webhook, eventType)) {
        deliverWithRetry(webhook, eventType, payload);
      }
    }
  }

  @Retry(name = "webhook-delivery")
  public void deliverWithRetry(Webhook webhook, String eventType, Map<String, Object> payload) {
    try {
      Map<String, Object> envelope =
          Map.of(
              "id", UUID.randomUUID().toString(),
              "eventType", eventType,
              "botId", webhook.getBotId().toString(),
              "orgId", webhook.getOrgId().toString(),
              "data", payload);

      String body = objectMapper.writeValueAsString(envelope);
      String signature = computeHmac(body, webhook.getSecret());

      WebClient client = webClientBuilder.build();
      client
          .post()
          .uri(webhook.getUrl())
          .header("Content-Type", "application/json")
          .header("X-Threadly-Signature", "sha256=" + signature)
          .header("X-Threadly-Event", eventType)
          .bodyValue(body)
          .retrieve()
          .toBodilessEntity()
          .timeout(Duration.ofSeconds(15))
          .block();

      log.debug(
          "Webhook delivered: eventType={} webhookId={} url={}",
          eventType,
          webhook.getId(),
          webhook.getUrl());
    } catch (Exception e) {
      log.error(
          "Webhook delivery failed: webhookId={} url={} error={}",
          webhook.getId(),
          webhook.getUrl(),
          e.getMessage());
      throw new RuntimeException("Webhook delivery failed: " + e.getMessage(), e);
    }
  }

  private boolean subscribesTo(Webhook webhook, String eventType) {
    try {
      List<?> events = objectMapper.readValue(webhook.getEvents(), List.class);
      return events.contains(eventType);
    } catch (Exception e) {
      log.warn("Failed to parse webhook events for webhookId={}", webhook.getId());
      return false;
    }
  }

  private String computeHmac(String data, String secret) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGO);
      SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
      mac.init(keySpec);
      byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : rawHmac) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("HMAC computation failed", e);
    }
  }
}
