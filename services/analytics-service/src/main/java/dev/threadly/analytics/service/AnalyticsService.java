package dev.threadly.analytics.service;

import dev.threadly.analytics.dto.AnalyticsEventDto;
import dev.threadly.analytics.dto.MetricDto;
import dev.threadly.analytics.entity.AnalyticsEvent;
import dev.threadly.analytics.entity.Metric;
import dev.threadly.analytics.repository.AnalyticsEventRepository;
import dev.threadly.analytics.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core analytics service providing high-level analytics operations.
 * Handles event queries, metric retrieval, and data export.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsEventRepository eventRepository;
    private final MetricRepository metricRepository;

    /**
     * Query metrics with pagination.
     *
     * @param orgId organization identifier
     * @param metricName metric name to query
     * @param startTime start time
     * @param endTime end time
     * @param pageable pagination settings
     * @return page of metrics
     */
    public Page<MetricDto> queryMetrics(
        String orgId,
        String metricName,
        Instant startTime,
        Instant endTime,
        Pageable pageable
    ) {
        try {
            List<Metric> metrics = metricRepository.findByMetricName(orgId, metricName, startTime, endTime);

            // Apply pagination
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), metrics.size());
            List<MetricDto> pageContent = metrics.subList(start, end).stream()
                .map(this::convertToMetricDto)
                .collect(Collectors.toList());

            return new PageImpl<>(pageContent, pageable, metrics.size());
        } catch (Exception e) {
            log.error("Error querying metrics for org: {} metric: {}", orgId, metricName, e);
            throw new RuntimeException("Failed to query metrics", e);
        }
    }

    /**
     * Get analytics events with pagination.
     *
     * @param orgId organization identifier
     * @param startTime start time
     * @param endTime end time
     * @param pageable pagination settings
     * @return page of events
     */
    public Page<AnalyticsEventDto> getEvents(
        String orgId,
        Instant startTime,
        Instant endTime,
        Pageable pageable
    ) {
        try {
            List<AnalyticsEvent> events = eventRepository.findByOrgIdAndTimeRange(orgId, startTime, endTime);

            // Apply pagination
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), events.size());
            List<AnalyticsEventDto> pageContent = events.subList(start, end).stream()
                .map(this::convertToEventDto)
                .collect(Collectors.toList());

            return new PageImpl<>(pageContent, pageable, events.size());
        } catch (Exception e) {
            log.error("Error retrieving events for org: {}", orgId, e);
            throw new RuntimeException("Failed to retrieve events", e);
        }
    }

    /**
     * Get events for a specific conversation.
     *
     * @param conversationId conversation identifier
     * @return list of events
     */
    public List<AnalyticsEventDto> getConversationEvents(String conversationId) {
        try {
            return eventRepository.findByConversationIdOrderByCreatedAtDesc(conversationId).stream()
                .map(this::convertToEventDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving events for conversation: {}", conversationId, e);
            throw new RuntimeException("Failed to retrieve conversation events", e);
        }
    }

    /**
     * Export metrics to CSV format.
     *
     * @param orgId organization identifier
     * @param metricName metric name
     * @param startTime start time
     * @param endTime end time
     * @return CSV string representation
     */
    public String exportMetricsAsCsv(
        String orgId,
        String metricName,
        Instant startTime,
        Instant endTime
    ) {
        try {
            List<Metric> metrics = metricRepository.findByMetricName(orgId, metricName, startTime, endTime);

            StringBuilder csv = new StringBuilder();
            csv.append("timestamp,bot_id,metric_name,value,tags\n");

            for (Metric metric : metrics) {
                csv.append(metric.getMetricTimestamp()).append(",")
                    .append(metric.getBotId()).append(",")
                    .append(metric.getMetricName()).append(",")
                    .append(metric.getValue()).append(",")
                    .append(metric.getTagsJson() != null ? metric.getTagsJson() : "").append("\n");
            }

            log.debug("Exported {} metrics as CSV for metric: {}", metrics.size(), metricName);
            return csv.toString();

        } catch (Exception e) {
            log.error("Error exporting metrics as CSV", e);
            throw new RuntimeException("Failed to export metrics", e);
        }
    }

    /**
     * Convert AnalyticsEvent entity to DTO.
     *
     * @param event the event entity
     * @return event DTO
     */
    private AnalyticsEventDto convertToEventDto(AnalyticsEvent event) {
        try {
            return AnalyticsEventDto.builder()
                .id(event.getId())
                .orgId(event.getOrgId())
                .eventType(event.getEventType())
                .botId(event.getBotId())
                .conversationId(event.getConversationId())
                .sessionId(event.getSessionId())
                .eventTimestamp(event.getEventTimestamp())
                .createdAt(event.getCreatedAt())
                .build();
        } catch (Exception e) {
            log.error("Error converting event to DTO: {}", event.getId(), e);
            return null;
        }
    }

    /**
     * Convert Metric entity to DTO.
     *
     * @param metric the metric entity
     * @return metric DTO
     */
    private MetricDto convertToMetricDto(Metric metric) {
        return MetricDto.builder()
            .id(metric.getId())
            .orgId(metric.getOrgId())
            .metricName(metric.getMetricName())
            .botId(metric.getBotId())
            .value(metric.getValue())
            .metricTimestamp(metric.getMetricTimestamp())
            .createdAt(metric.getCreatedAt())
            .build();
    }

}
