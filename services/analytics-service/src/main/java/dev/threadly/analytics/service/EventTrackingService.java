package dev.threadly.analytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.analytics.entity.AnalyticsEvent;
import dev.threadly.analytics.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Service for tracking and storing analytics events.
 * Handles event persistence and aggregation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventTrackingService {

    private final AnalyticsEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final MetricAggregationService metricAggregationService;

    /**
     * Track an analytics event.
     *
     * @param orgId organization identifier
     * @param eventType type of event
     * @param botId bot identifier
     * @param conversationId conversation identifier (optional)
     * @param sessionId session identifier (optional)
     * @param eventData event data as map
     */
    public void trackEvent(
        String orgId,
        String eventType,
        String botId,
        String conversationId,
        String sessionId,
        Map<String, Object> eventData
    ) {
        try {
            String eventDataJson = objectMapper.writeValueAsString(eventData);

            AnalyticsEvent event = AnalyticsEvent.builder()
                .id(UUID.randomUUID().toString())
                .orgId(orgId)
                .eventType(eventType)
                .botId(botId)
                .conversationId(conversationId)
                .sessionId(sessionId)
                .eventDataJson(eventDataJson)
                .eventTimestamp(Instant.now())
                .createdAt(Instant.now())
                .build();

            AnalyticsEvent savedEvent = eventRepository.save(event);
            log.debug("Event tracked successfully: {} (ID: {})", eventType, savedEvent.getId());

            // Trigger metric aggregation asynchronously
            metricAggregationService.processEvent(savedEvent);

        } catch (Exception e) {
            log.error("Error tracking event: {}", eventType, e);
            throw new RuntimeException("Failed to track event: " + eventType, e);
        }
    }

    /**
     * Get event count for an organization within a time range.
     *
     * @param orgId organization identifier
     * @param startTime start time
     * @param endTime end time
     * @return count of events
     */
    public long getEventCount(String orgId, Instant startTime, Instant endTime) {
        return eventRepository.countByOrgIdAndTimeRange(orgId, startTime, endTime);
    }

    /**
     * Get event count by type for an organization.
     *
     * @param orgId organization identifier
     * @param eventType event type
     * @param startTime start time
     * @param endTime end time
     * @return count of events
     */
    public long getEventCountByType(String orgId, String eventType, Instant startTime, Instant endTime) {
        return eventRepository.countByEventType(orgId, eventType, startTime, endTime);
    }

}
