package dev.threadly.core.analytics;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.conversation.ConversationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Server-Sent Events endpoint that pushes live dashboard counters every 5 s.
 * Used by the dashboard page for the stat-card live updates.
 */
@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard metrics")
public class LiveCounterController {

  private final ConversationRepository conversationRepository;

  @GetMapping(value = "/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "SSE stream of live dashboard counters (5s interval)")
  public SseEmitter liveCounters() {
    UUID orgId = TenantContext.getOrgId();
    SseEmitter emitter = new SseEmitter(300_000L); // 5-min timeout

    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        for (int i = 0; i < 60; i++) { // 60 ticks × 5s = 5 min max
          long open = conversationRepository.countByOrgIdAndStatus(orgId, "OPEN");
          long handedOff = conversationRepository.countByOrgIdAndStatus(orgId, "HANDED_OFF");
          long total = open + handedOff
              + conversationRepository.countByOrgIdAndStatus(orgId, "CLOSED");
          Map<String, Object> payload = Map.of(
              "openConversations", open,
              "handoffConversations", handedOff,
              "totalConversations", total
          );
          emitter.send(SseEmitter.event().name("stats").data(payload));
          Thread.sleep(5_000);
        }
        emitter.complete();
      } catch (IOException | InterruptedException e) {
        emitter.completeWithError(e);
      }
    });

    return emitter;
  }
}
