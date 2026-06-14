package dev.threadly.analytics.controller;

import dev.threadly.analytics.common.TenantContext;
import dev.threadly.analytics.repository.AnalyticsEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Server-Sent Events endpoint streaming live dashboard counters every 5 seconds.
 * Matches the monolith's LiveCounterController contract: GET /v1/analytics/live
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Live dashboard counters")
public class LiveCounterController {

    private final AnalyticsEventRepository eventRepository;

    /**
     * SSE stream pushing live stats every 5 s for up to 5 minutes.
     * Frontend subscribes with EventSource("/v1/analytics/live").
     */
    @GetMapping(value = "/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE stream of live dashboard counters (5 s interval)")
    public SseEmitter liveCounters(
            @RequestHeader(value = "X-Org-ID", required = false) String orgIdHeader) {

        String orgId;
        try {
            var tc = TenantContext.getOrgIdOptional();
            orgId = tc != null ? tc.toString() : orgIdHeader;
        } catch (Exception e) {
            orgId = orgIdHeader;
        }

        final String finalOrgId = orgId;
        SseEmitter emitter = new SseEmitter(300_000L); // 5-min max

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (int tick = 0; tick < 60; tick++) {
                    Instant epoch = Instant.EPOCH;
                    Instant now   = Instant.now();

                    long total     = eventRepository.countByOrgIdAndTimeRange(finalOrgId, epoch, now);
                    long open      = eventRepository.countByEventType(finalOrgId, "CONVERSATION_STARTED", epoch, now);
                    long handedOff = eventRepository.countByEventType(finalOrgId, "HANDOFF_INITIATED",    epoch, now);
                    long closed    = eventRepository.countByEventType(finalOrgId, "CONVERSATION_ENDED",   epoch, now);

                    Map<String, Object> payload = Map.of(
                            "totalConversations",   total,
                            "openConversations",    Math.max(0, open - handedOff - closed),
                            "handoffConversations", handedOff
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
