package dev.threadly.analytics.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.analytics.entity.AnalyticsEvent;
import dev.threadly.analytics.entity.Metric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Processes AI-related events and generates metrics.
 * Handles events: AI_REPLY_REQUESTED, AI_REPLY_COMPLETED, HANDOFF_INITIATED
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiCallMetricProcessor extends MetricProcessor {

    private final ObjectMapper objectMapper;

    private static final String AI_REPLY_REQUESTED = "AI_REPLY_REQUESTED";
    private static final String AI_REPLY_COMPLETED = "AI_REPLY_COMPLETED";
    private static final String HANDOFF_INITIATED = "HANDOFF_INITIATED";

    @Override
    public List<Metric> processEvent(AnalyticsEvent event) {
        List<Metric> metrics = new ArrayList<>();

        if (!isValidEvent(event)) {
            logProcessing(event, "Invalid event data");
            return metrics;
        }

        try {
            JsonNode data = objectMapper.readTree(event.getEventDataJson());

            switch (event.getEventType()) {
                case AI_REPLY_REQUESTED:
                    metrics.addAll(processAiReplyRequested(event, data));
                    break;
                case AI_REPLY_COMPLETED:
                    metrics.addAll(processAiReplyCompleted(event, data));
                    break;
                case HANDOFF_INITIATED:
                    metrics.addAll(processHandoffInitiated(event, data));
                    break;
                default:
                    logProcessing(event, "Unsupported event type");
            }
        } catch (Exception e) {
            log.error("Error processing AI event: {}", event.getId(), e);
        }

        return metrics;
    }

    @Override
    public boolean canHandle(String eventType) {
        return AI_REPLY_REQUESTED.equals(eventType) ||
               AI_REPLY_COMPLETED.equals(eventType) ||
               HANDOFF_INITIATED.equals(eventType);
    }

    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{AI_REPLY_REQUESTED, AI_REPLY_COMPLETED, HANDOFF_INITIATED};
    }

    private List<Metric> processAiReplyRequested(AnalyticsEvent event, JsonNode data) {
        List<Metric> metrics = new ArrayList<>();
        logProcessing(event, "Processing AI reply requested");

        // Increment AI calls counter
        Metric aiCallCounter = Metric.builder()
            .id(UUID.randomUUID().toString())
            .orgId(event.getOrgId())
            .botId(event.getBotId())
            .metricName("ai_calls_count")
            .value(1.0)
            .metricTimestamp(event.getEventTimestamp())
            .createdAt(Instant.now())
            .build();

        metrics.add(aiCallCounter);
        logMetricGenerated("ai_calls_count", 1.0, event.getEventType());

        return metrics;
    }

    private List<Metric> processAiReplyCompleted(AnalyticsEvent event, JsonNode data) {
        List<Metric> metrics = new ArrayList<>();
        logProcessing(event, "Processing AI reply completed");

        // Record tokens consumed if present
        if (data.has("tokens_used")) {
            long tokensUsed = data.get("tokens_used").asLong();
            Metric tokensMetric = Metric.builder()
                .id(UUID.randomUUID().toString())
                .orgId(event.getOrgId())
                .botId(event.getBotId())
                .metricName("ai_tokens_consumed")
                .value((double) tokensUsed)
                .metricTimestamp(event.getEventTimestamp())
                .createdAt(Instant.now())
                .build();
            metrics.add(tokensMetric);
            logMetricGenerated("ai_tokens_consumed", (double) tokensUsed, event.getEventType());
        }

        // Record cost if present
        if (data.has("cost_cents")) {
            long costCents = data.get("cost_cents").asLong();
            Metric costMetric = Metric.builder()
                .id(UUID.randomUUID().toString())
                .orgId(event.getOrgId())
                .botId(event.getBotId())
                .metricName("ai_cost_cents")
                .value((double) costCents)
                .metricTimestamp(event.getEventTimestamp())
                .createdAt(Instant.now())
                .build();
            metrics.add(costMetric);
            logMetricGenerated("ai_cost_cents", (double) costCents, event.getEventType());
        }

        // Record latency if present
        if (data.has("latency_ms")) {
            long latency = data.get("latency_ms").asLong();
            Metric latencyMetric = Metric.builder()
                .id(UUID.randomUUID().toString())
                .orgId(event.getOrgId())
                .botId(event.getBotId())
                .metricName("ai_latency_ms")
                .value((double) latency)
                .metricTimestamp(event.getEventTimestamp())
                .createdAt(Instant.now())
                .build();
            metrics.add(latencyMetric);
            logMetricGenerated("ai_latency_ms", (double) latency, event.getEventType());
        }

        // Track success/failure
        boolean isSuccess = data.has("success") ? data.get("success").asBoolean() : true;
        Metric successMetric = Metric.builder()
            .id(UUID.randomUUID().toString())
            .orgId(event.getOrgId())
            .botId(event.getBotId())
            .metricName("ai_calls_successful")
            .value(isSuccess ? 1.0 : 0.0)
            .metricTimestamp(event.getEventTimestamp())
            .createdAt(Instant.now())
            .build();
        metrics.add(successMetric);
        logMetricGenerated("ai_calls_successful", isSuccess ? 1.0 : 0.0, event.getEventType());

        return metrics;
    }

    private List<Metric> processHandoffInitiated(AnalyticsEvent event, JsonNode data) {
        List<Metric> metrics = new ArrayList<>();
        logProcessing(event, "Processing handoff initiated");

        // Increment handoff counter
        Metric handoffCounter = Metric.builder()
            .id(UUID.randomUUID().toString())
            .orgId(event.getOrgId())
            .botId(event.getBotId())
            .metricName("handoffs_count")
            .value(1.0)
            .metricTimestamp(event.getEventTimestamp())
            .createdAt(Instant.now())
            .build();

        metrics.add(handoffCounter);
        logMetricGenerated("handoffs_count", 1.0, event.getEventType());

        // Record handoff reason if present
        if (data.has("reason")) {
            log.debug("Handoff initiated - Reason: {}", data.get("reason").asText());
        }

        return metrics;
    }

}
