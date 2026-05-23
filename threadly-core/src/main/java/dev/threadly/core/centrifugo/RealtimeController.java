package dev.threadly.core.centrifugo;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.runtime.FlowRuntime;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Realtime", description = "Centrifugo token issuance")
public class RealtimeController {

  @Value("${threadly.centrifugo.token-secret}")
  private String tokenSecret;

  @Value("${threadly.centrifugo.token-expiry-seconds:3600}")
  private long tokenExpirySeconds;

  private final FlowRuntime flowRuntime;

  @GetMapping("/realtime/token")
  @PostMapping("/realtime/token")
  @Operation(summary = "Issue Centrifugo JWT for authenticated dashboard user")
  public ResponseEntity<Map<String, String>> getDashboardToken() {
    UUID userId = TenantContext.getUserId();
    String token = buildCentrifugoJwt(userId.toString());
    return ResponseEntity.ok(Map.of("token", token));
  }

  @GetMapping("/widget/token")
  @PostMapping("/widget/token")
  @Operation(summary = "Issue Centrifugo JWT for visitor widget (public)")
  public ResponseEntity<Map<String, String>> getVisitorToken(
      @RequestParam String botId,
      @RequestParam(required = false) String visitorId) {
    // Generate or reuse visitorId
    String vid = (visitorId != null && !visitorId.isBlank())
        ? visitorId
        : UUID.randomUUID().toString();
    String token = buildCentrifugoJwt(vid);
    return ResponseEntity.ok(Map.of("token", token, "visitorId", vid));
  }

  /** HTTP fallback — used when Centrifugo publish fails from the widget. */
  @PostMapping("/widget/message")
  @Operation(summary = "Widget HTTP fallback message endpoint (public)")
  public ResponseEntity<Void> receiveWidgetMessage(@RequestBody WidgetMessageRequest req) {
    if (req.getBotId() == null || req.getVisitorId() == null || req.getText() == null) {
      return ResponseEntity.badRequest().build();
    }
    // orgId unknown for anonymous widget — runtime resolves via botId
    flowRuntime.handleVisitorMessage(
        UUID.fromString(req.getBotId()),
        req.getVisitorId(),
        req.getText(),
        null  // orgId resolved from bot inside runtime
    );
    return ResponseEntity.noContent().build();
  }

  @Data
  public static class WidgetMessageRequest {
    private String botId;
    private String visitorId;
    private String text;
  }

  private String buildCentrifugoJwt(String subject) {
    byte[] key = tokenSecret.getBytes(StandardCharsets.UTF_8);
    // Pad key to 256 bits if needed
    if (key.length < 32) key = java.util.Arrays.copyOf(key, 32);
    return Jwts.builder()
        .subject(subject)
        .expiration(new Date(System.currentTimeMillis() + tokenExpirySeconds * 1000))
        .signWith(Keys.hmacShaKeyFor(key))
        .compact();
  }
}
