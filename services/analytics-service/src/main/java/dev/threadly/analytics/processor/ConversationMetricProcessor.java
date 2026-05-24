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
 * Processes conversation-related events and generates metrics.
 * Handles events: CONVERSATION_STARTED, CONVERSATION_ENDED, MESSAGE_ADDED
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ConversationMetricProcessor extends MetricProcessor {

    private final ObjectMapper objectMapper;

    private static final String CONVERSATION_STARTED = "CONVERSATION_STARTED";
    private static final String CONVERSATION_ENDED = "CONVERSATION_ENDED";
    private static final String MESSAGE_ADDED = "MESSAGE_ADDED";

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
                case CONVERSATION_STARTED:
                    metrics.addAll(processConversationStarted(event, data));
                    break;
                case CONVERSATION_ENDED:
                    metrics.addAll(processConversationEnded(event, data));
                    break;
                case MESSAGE_ADDED:
                    metrics.addAll(processMessageAdded(event, data));
                    break;
                default:
                    logProcessing(event, "Unsupported event type");
            }
        } catch (Exception e) {
            log.error("Error processing conversation event: {}", event.getId(), e);
        }

        return metrics;
    }

    @Override
    public boolean canHandle(String eventType) {
        return CONVERSATION_STARTED.equals(eventType) ||
               CONVERSATION_ENDED.equals(eventType) ||
               MESSAGE_ADDED.equals(eventType);
    }

    @Override
    public String[] getSupportedEventTypes() {
        return new String[]{CONVERSATION_STARTED, CONVERSATION_ENDED, MESSAGE_ADDED};
    }

    private List<Metric> processConversationStarted(AnalyticsEvent event, JsonNode data) {
        List<Metric> metrics = new ArrayList<>();
        logProcessing(event, "Processing conversation started");

        // Increment conversation counter
        Metric conversationCounter = Metric.builder()
            .id(UUID.randomUUID().toString())
            .orgId(event.getOrgId())
            .botId(event.getBotId())
            .metricName("conversations_started_count")
            .value(1.0)
            .metricTimestamp(event.getEventTimestamp())
            .createdAt(Instant.now())
            .build();

        metrics.add(conversationCounter);
        logMetricGenerated("conversations_started_count", 1.0, event.getEventType());

        return metrics;
    }

    private List<Metric> processConversationEnded(AnalyticsEvent event, JsonNode data) {
        List<Metric> metrics = new ArrayList<>();
        logProcessing(event, "Processing conversation ended");

        // Check if conversation was resolved
        boolean isResolved = data.has("is_resolved") && data.get("is_resolved").asBoolean();
        if (isResolved) {
            Metric resolvedMetric = Metric.builder()
                .id(UUID.randomUUID().toString())
                .orgId(event.getOrgId())
                .botId(event.getBotId())
                .metricName("conversations_resolved_count")
                .value(1.0)
                .metricTimestamp(event.getEventTimestamp())
                .createdAt(Instant.now())
                .build();
            metrics.add(resolvedMetric);
            logMetricGenerated("conversations_resolved_count", 1.0, event.getEventType());
        }

        // Record CSAT score if present
        if (data.has("csat_score")) {
            int csatScore = data.get("csat_score").asInt();
            Metric csatMetric = Metric.builder()
                .id(UUID.randomUUID().toString())
                .orgId(event.getOrgId())
                .botId(event.getBotId())
                .metricName("csat_score")
                .value((double) csatScore)
                .metricTimestamp(event.getEventTimestamp())
                .createdAt(Instant.now())
                .build();
            metrics.add(csatMetric);
            logMetricGenerated("csat_score", (double) csatScore, event.getEventType());
        }

        // Record conversation duration if present
        if (data.has("duration_seconds")) {
            long duration = data.get("duration_seconds").asLong();
            Metric durationMetric = Metric.builder()
                .id(UUID.randomUUID().toString())
                .orgId(event.getOrgId())
                .botId(event.getBotId())
                .metricName("conversation_duration_seconds")
                .value((double) duration)
                .metricTimestamp(event.getEventTimestamp())
                .createdAt(Instant.now())
                .build();
            metrics.add(durationMetric);
            logMetricGenerated("conversation_duration_seconds", (double) duration, event.getEventType());
        }

        return metrics;
    }

    private List<Metric> processMessageAdded(AnalyticsEvent event, JsonNode data) {
        List<Metric> metrics = new ArrayList<>();
        logProcessing(event, "Processing message added");

        // Increment message counter
        Metric messageCounter = Metric.builder()
            .id(UUID.randomUUID().toString())
            .orgId(event.getOrgId())
            .botId(event.getBotId())
            .metricName("messages_count")
            .value(1.0)
            .metricTimestamp(event.getEventTimestamp())
            .createdAt(Instant.now())
            .build();

        metrics.add(messageCounter);
        logMetricGenerated("messages_count", 1.0, event.getEventType());

        // Track response time if available
        if (data.has("response_time_ms")) {
            long responseTime = data.get("response_time_ms").asLong();
            Metric responseMetric = Metric.builder()
                .id(UUID.randomUUID().toString())
                .orgId(event.getOrgId())
                .botId(event.getBotId())
                .metricName("response_time_ms")
                .value((double) responseTime)
                .metricTimestamp(event.getEventTimestamp())
                .createdAt(Instant.now())
                .build();
            metrics.add(responseMetric);
            logMetricGenerated("response_time_ms", (double) responseTime, event.getEventType());
        }

        return metrics;
    }

}
