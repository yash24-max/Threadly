package dev.threadly.core.centrifugo;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.runtime.FlowRuntime;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  /**
   * Issues a Centrifugo JWT for the requesting principal.
   *
   * <p>Supports two authentication modes:
   * <ul>
   *   <li><b>JWT (dashboard user)</b>: standard Bearer JWT — issues a personal token for the
   *       authenticated user, subscribed to their org dashboard channel.</li>
   *   <li><b>API Key (widget/bot)</b>: {@code Authorization: Bearer tly_live_xxx} — issues a
   *       token scoped to the bot's channel only. The subject is the botId resolved from the
   *       API key. This allows the widget to connect to Centrifugo using only a botId and the
   *       secret API key, without requiring user credentials.</li>
   * </ul>
   */
  @GetMapping("/realtime/token")
  @PostMapping("/realtime/token")
  @Operation(summary = "Issue Centrifugo JWT for authenticated dashboard user or API key holder")
  public ResponseEntity<Map<String, String>> getDashboardToken(
      @RequestParam(required = false) String botId) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isApiKey = auth != null && auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_API_KEY"::equals);

    if (isApiKey) {
      // API key auth — subject is the botId stored in TenantContext as "userId"
      UUID resolvedBotId = TenantContext.getUserId();
      String channel = "bot:" + resolvedBotId;
      String token = buildCentrifugoJwt(resolvedBotId.toString(), List.of(channel));
      return ResponseEntity.ok(Map.of("token", token, "channel", channel));
    }

    // Standard JWT dashboard user
    UUID userId = TenantContext.getUserId();
    String token = buildCentrifugoJwt(userId.toString(), List.of());
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
    String channel = "chat:" + botId + ":" + vid;
    String token = buildCentrifugoJwt(vid, List.of(channel));
    return ResponseEntity.ok(Map.of("token", token, "visitorId", vid, "channel", channel));
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

  /**
   * Builds a Centrifugo-compatible JWT.
   *
   * @param subject    the JWT subject (userId or botId)
   * @param channels   optional list of channels to embed in the token's "channels" claim
   *                   (Centrifugo uses this for channel-scoped tokens)
   */
  private String buildCentrifugoJwt(String subject, List<String> channels) {
    byte[] key = tokenSecret.getBytes(StandardCharsets.UTF_8);
    // Pad key to 256 bits if needed
    if (key.length < 32) key = Arrays.copyOf(key, 32);

    var builder = Jwts.builder()
        .subject(subject)
        .expiration(new Date(System.currentTimeMillis() + tokenExpirySeconds * 1000))
        .signWith(Keys.hmacShaKeyFor(key));

    if (channels != null && !channels.isEmpty()) {
      builder.claim("channels", channels);
    }

    return builder.compact();
  }
}
