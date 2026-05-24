package dev.threadly.analytics.service;

import dev.threadly.analytics.entity.AnalyticsEvent;
import dev.threadly.analytics.entity.DailyRollup;
import dev.threadly.analytics.entity.Metric;
import dev.threadly.analytics.processor.MetricProcessor;
import dev.threadly.analytics.repository.DailyRollupRepository;
import dev.threadly.analytics.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for aggregating metrics from analytics events.
 * Computes daily rollups and maintains time-series metrics.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetricAggregationService {

    private final MetricRepository metricRepository;
    private final DailyRollupRepository dailyRollupRepository;
    private final List<MetricProcessor> metricProcessors;

    /**
     * Process an event and generate metrics asynchronously.
     *
     * @param event the analytics event to process
     */
    @Async
    public void processEvent(AnalyticsEvent event) {
        try {
            log.debug("Processing event for metrics aggregation: {}", event.getId());

            // Find appropriate processor for this event type
            for (MetricProcessor processor : metricProcessors) {
                if (processor.canHandle(event.getEventType())) {
                    List<Metric> metrics = processor.processEvent(event);

                    // Save generated metrics
                    if (!metrics.isEmpty()) {
                        metricRepository.saveAll(metrics);
                        log.debug("Saved {} metrics from event: {}", metrics.size(), event.getId());
                    }
                    break;
                }
            }

            // Update daily rollup
            updateDailyRollup(event);

        } catch (Exception e) {
            log.error("Error aggregating metrics for event: {}", event.getId(), e);
        }
    }

    /**
     * Update daily rollup for an event.
     *
     * @param event the analytics event
     */
    private void updateDailyRollup(AnalyticsEvent event) {
        try {
            LocalDate rollupDate = event.getEventTimestamp()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

            Optional<DailyRollup> existingRollup = dailyRollupRepository
                .findByOrgIdAndRollupDate(event.getOrgId(), rollupDate);

            DailyRollup rollup = existingRollup.orElse(DailyRollup.builder()
                .id(java.util.UUID.randomUUID().toString())
                .orgId(event.getOrgId())
                .botId(event.getBotId())
                .rollupDate(rollupDate)
                .build());

            // Update counts based on event type
            switch (event.getEventType()) {
                case "CONVERSATION_STARTED":
                    rollup.setConversationsCount(rollup.getConversationsCount() + 1);
                    break;
                case "MESSAGE_ADDED":
                    rollup.setMessagesCount(rollup.getMessagesCount() + 1);
                    break;
                case "AI_REPLY_REQUESTED":
                    rollup.setAiCallsCount(rollup.getAiCallsCount() + 1);
                    break;
                case "HANDOFF_INITIATED":
                    rollup.setHandoffsCount(rollup.getHandoffsCount() + 1);
                    break;
            }

            dailyRollupRepository.save(rollup);
            log.debug("Updated daily rollup for date: {}", rollupDate);

        } catch (Exception e) {
            log.error("Error updating daily rollup for event: {}", event.getId(), e);
        }
    }

    /**
     * Get metrics for an organization within a date range.
     *
     * @param orgId organization identifier
     * @param metricName metric name to query
     * @param startTime start time
     * @param endTime end time
     * @return list of metrics
     */
    public List<Metric> getMetrics(
        String orgId,
        String metricName,
        Instant startTime,
        Instant endTime
    ) {
        return metricRepository.findByMetricName(orgId, metricName, startTime, endTime);
    }

    /**
     * Calculate average for a metric over a period.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param metricName metric name
     * @param startTime start time
     * @param endTime end time
     * @return average value or null
     */
    public Double getMetricAverage(
        String orgId,
        String botId,
        String metricName,
        Instant startTime,
        Instant endTime
    ) {
        return metricRepository.findAverageValue(orgId, botId, metricName, startTime, endTime);
    }

    /**
     * Get the latest metric value for a bot.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param metricName metric name
     * @return optional metric
     */
    public Optional<Metric> getLatestMetric(String orgId, String botId, String metricName) {
        return metricRepository.findLatestByBotAndMetricName(orgId, botId, metricName);
    }

}
