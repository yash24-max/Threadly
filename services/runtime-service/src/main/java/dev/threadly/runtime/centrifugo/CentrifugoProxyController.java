package dev.threadly.runtime.centrifugo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Centrifugo proxy webhook controller.
 *
 * Centrifugo calls these endpoints to delegate authentication decisions back to the app:
 *   POST /api/v1/proxy/connect      — called when a client connects; return user context
 *   POST /api/v1/proxy/subscribe    — called when a client subscribes; validate channel access
 *   POST /api/v1/proxy/publish      — called before a client publishes (server-side validation)
 *
 * Configure in centrifugo config.json:
 *   "proxy_connect_endpoint":   "http://runtime-service:3004/api/v1/proxy/connect"
 *   "proxy_subscribe_endpoint": "http://runtime-service:3004/api/v1/proxy/subscribe"
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/proxy")
@RequiredArgsConstructor
public class CentrifugoProxyController {

    private final CentrifugoProxyService proxyService;

    /**
     * Connection proxy — Centrifugo asks "is this token valid?".
     * Returns user ID and any additional context to embed in the connection.
     */
    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connect(@RequestBody Map<String, Object> payload) {
        log.debug("Centrifugo connect proxy: {}", payload.get("token"));
        Map<String, Object> result = proxyService.handleConnect(payload);
        return ResponseEntity.ok(result);
    }

    /**
     * Subscribe proxy — Centrifugo asks "can this user subscribe to channel X?".
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody Map<String, Object> payload) {
        String channel = (String) payload.get("channel");
        String userId  = (String) payload.get("user");
        log.debug("Centrifugo subscribe proxy: user={} channel={}", userId, channel);
        Map<String, Object> result = proxyService.handleSubscribe(userId, channel, payload);
        return ResponseEntity.ok(result);
    }

    /**
     * Publish proxy — Centrifugo asks "can this user publish to channel X?".
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publish(@RequestBody Map<String, Object> payload) {
        String channel = (String) payload.get("channel");
        String userId  = (String) payload.get("user");
        log.debug("Centrifugo publish proxy: user={} channel={}", userId, channel);
        Map<String, Object> result = proxyService.handlePublish(userId, channel, payload);
        return ResponseEntity.ok(result);
    }
}
