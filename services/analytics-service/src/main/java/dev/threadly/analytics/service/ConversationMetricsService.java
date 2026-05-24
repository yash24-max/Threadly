package dev.threadly.analytics.service;

import dev.threadly.analytics.dto.ConversationMetricsDto;
import dev.threadly.analytics.entity.AnalyticsEvent;
import dev.threadly.analytics.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating conversation-level analytics.
 * Aggregates metrics per conversation including duration, message counts, and resolution.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationMetricsService {

    private final AnalyticsEventRepository eventRepository;

    /**
     * Get metrics for a specific conversation.
     *
     * @param conversationId conversation identifier
     * @return conversation metrics DTO
     */
    public ConversationMetricsDto getConversationMetrics(String conversationId) {
        try {
            List<AnalyticsEvent> events = eventRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);

            if (events.isEmpty()) {
                log.warn("No events found for conversation: {}", conversationId);
                return ConversationMetricsDto.builder()
                    .conversationId(conversationId)
                    .build();
            }

            // Calculate metrics from events
            ConversationMetricsDto metrics = ConversationMetricsDto.builder()
                .conversationId(conversationId)
                .build();

            // Process events to calculate metrics
            for (AnalyticsEvent event : events) {
                processEventForMetrics(event, metrics);
            }

            // Calculate duration from first and last events
            if (events.size() > 1) {
                AnalyticsEvent firstEvent = events.get(events.size() - 1);
                AnalyticsEvent lastEvent = events.get(0);
                long durationSeconds = (lastEvent.getCreatedAt().getEpochSecond() -
                                       firstEvent.getCreatedAt().getEpochSecond());
                metrics.setDurationSeconds(durationSeconds);
            }

            log.debug("Calculated metrics for conversation: {}", conversationId);
            return metrics;

        } catch (Exception e) {
            log.error("Error calculating conversation metrics for: {}", conversationId, e);
            throw new RuntimeException("Failed to calculate conversation metrics", e);
        }
    }

    /**
     * Get metrics for multiple conversations.
     *
     * @param conversationIds list of conversation identifiers
     * @return list of conversation metrics
     */
    public List<ConversationMetricsDto> getConversationMetricsBatch(List<String> conversationIds) {
        return conversationIds.stream()
            .map(this::getConversationMetrics)
            .collect(Collectors.toList());
    }

    private void processEventForMetrics(AnalyticsEvent event, ConversationMetricsDto metrics) {
        switch (event.getEventType()) {
            case "CONVERSATION_STARTED":
                metrics.setStartedAt(event.getEventTimestamp());
                break;
            case "CONVERSATION_ENDED":
                metrics.setEndedAt(event.getEventTimestamp());
                break;
            case "MESSAGE_ADDED":
                incrementMessageCount(metrics, event);
                break;
            case "AI_REPLY_REQUESTED":
                break;
            case "HANDOFF_INITIATED":
                metrics.setWasHandoff(true);
                break;
        }
    }

    private void incrementMessageCount(ConversationMetricsDto metrics, AnalyticsEvent event) {
        int currentCount = metrics.getMessagesCount() != null ? metrics.getMessagesCount() : 0;
        metrics.setMessagesCount(currentCount + 1);
    }

}
