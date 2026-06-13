package dev.threadly.core.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.core.centrifugo.CentrifugoClient;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Outbox service — publishes events to Centrifugo.
 * In MVP we publish directly (async). In v2, write to DB outbox first for at-least-once.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

  private final CentrifugoClient centrifugoClient;
  private final ObjectMapper objectMapper;

  @Async
  public void publishChatEvent(UUID botId, String visitorId, String eventType, Map<String, Object> payload) {
    String channel = "chat:" + botId + ":" + visitorId;
    try {
      Map<String, Object> envelope = Map.of("type", eventType, "data", payload);
      centrifugoClient.publish(channel, envelope);
    } catch (Exception e) {
      log.error("Failed to publish {} to channel {}", eventType, channel, e);
    }
  }

  @Async
  public void publishDashboardEvent(UUID orgId, String eventType, Map<String, Object> payload) {
    String channel = "dashboard:" + orgId;
    try {
      Map<String, Object> envelope = Map.of("type", eventType, "data", payload);
      centrifugoClient.publish(channel, envelope);
    } catch (Exception e) {
      log.error("Failed to publish {} to dashboard channel {}", eventType, channel, e);
    }
  }
}
