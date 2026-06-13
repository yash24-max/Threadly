package dev.threadly.core.proxy;

import dev.threadly.core.runtime.FlowRuntime;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Centrifugo proxy endpoints.
 * Centrifugo calls these via HTTP when a client connects, subscribes, or publishes.
 * See: https://centrifugal.dev/docs/server/proxy
 */
@Slf4j
@RestController
@RequestMapping("/v1/proxy")
@RequiredArgsConstructor
@Hidden
public class CentrifugoProxyController {

  private final CentrifugoProxyService proxyService;
  private final FlowRuntime flowRuntime;

  /** Called when a client WebSocket connection is established. */
  @PostMapping("/connect")
  public ResponseEntity<ConnectResult> connect(@RequestBody ConnectRequest req) {
    ConnectResult result = proxyService.handleConnect(req);
    return ResponseEntity.ok(result);
  }

  /** Called when a client subscribes to a channel. */
  @PostMapping("/subscribe")
  public ResponseEntity<SubscribeResult> subscribe(@RequestBody SubscribeRequest req) {
    SubscribeResult result = proxyService.handleSubscribe(req);
    return ResponseEntity.ok(result);
  }

  /** Called when a client publishes to a channel (visitor sends a message). */
  @PostMapping("/publish")
  public ResponseEntity<PublishResult> publish(@RequestBody PublishRequest req) {
    try {
      proxyService.handlePublish(req, flowRuntime);
    } catch (Exception e) {
      log.error("Publish proxy error", e);
    }
    return ResponseEntity.ok(new PublishResult());
  }

  /** Called for custom RPC from the widget (e.g. token renewal). */
  @PostMapping("/rpc")
  public ResponseEntity<Map<String, Object>> rpc(@RequestBody Map<String, Object> req) {
    return ResponseEntity.ok(Map.of("result", Map.of()));
  }

  // ── Request/Response shapes (Centrifugo protocol) ────────────────

  @Data public static class ConnectRequest {
    private String clientID, transport, protocol, encoding;
    private String token; // visitor JWT
    private Map<String, Object> data;
  }

  @Data public static class ConnectResult {
    private String userID;
    private Map<String, Object> data;
  }

  @Data public static class SubscribeRequest {
    private String clientID, userID, channel, transport;
  }

  @Data public static class SubscribeResult {
    private boolean allow = true;
  }

  @Data public static class PublishRequest {
    private String clientID, userID, channel, transport;
    private Map<String, Object> data;
  }

  @Data public static class PublishResult {
    // empty = allow publish
  }
}
