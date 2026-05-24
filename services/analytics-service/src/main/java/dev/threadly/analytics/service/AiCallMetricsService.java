package dev.threadly.analytics.service;

import dev.threadly.analytics.entity.Metric;
import dev.threadly.analytics.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Service for tracking AI call metrics.
 * Provides insights into token usage, cost, latency, and success rates.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiCallMetricsService {

    private final MetricRepository metricRepository;

    /**
     * Get total tokens consumed by a bot within a time range.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param startTime start time
     * @param endTime end time
     * @return total tokens
     */
    public Long getTotalTokensConsumed(String orgId, String botId, Instant startTime, Instant endTime) {
        try {
            Double totalTokens = metricRepository.findAverageValue(orgId, botId, "ai_tokens_consumed", startTime, endTime);
            return totalTokens != null ? totalTokens.longValue() : 0L;
        } catch (Exception e) {
            log.error("Error calculating tokens consumed for bot: {}", botId, e);
            return 0L;
        }
    }

    /**
     * Get total AI cost for a bot within a time range.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param startTime start time
     * @param endTime end time
     * @return total cost in cents
     */
    public Long getTotalAiCost(String orgId, String botId, Instant startTime, Instant endTime) {
        try {
            Double totalCost = metricRepository.findAverageValue(orgId, botId, "ai_cost_cents", startTime, endTime);
            return totalCost != null ? totalCost.longValue() : 0L;
        } catch (Exception e) {
            log.error("Error calculating AI cost for bot: {}", botId, e);
            return 0L;
        }
    }

    /**
     * Get average AI latency for a bot within a time range.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param startTime start time
     * @param endTime end time
     * @return average latency in milliseconds
     */
    public Double getAverageLatency(String orgId, String botId, Instant startTime, Instant endTime) {
        try {
            return metricRepository.findAverageValue(orgId, botId, "ai_latency_ms", startTime, endTime);
        } catch (Exception e) {
            log.error("Error calculating average latency for bot: {}", botId, e);
            return 0.0;
        }
    }

    /**
     * Get the latest AI latency for a bot.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @return optional latest latency metric
     */
    public Optional<Metric> getLatestLatency(String orgId, String botId) {
        try {
            return metricRepository.findLatestByBotAndMetricName(orgId, botId, "ai_latency_ms");
        } catch (Exception e) {
            log.error("Error fetching latest latency for bot: {}", botId, e);
            return Optional.empty();
        }
    }

    /**
     * Calculate cost per conversation for a bot.
     *
     * @param orgId organization identifier
     * @param botId bot identifier
     * @param totalCostCents total cost in cents
     * @param totalConversations total conversations
     * @return cost per conversation
     */
    public Double calculateCostPerConversation(String orgId, String botId,
                                               Long totalCostCents, Long totalConversations) {
        if (totalConversations == null || totalConversations == 0) {
            return 0.0;
        }
        return (double) totalCostCents / totalConversations;
    }

    /**
     * Calculate tokens per conversation for a bot.
     *
     * @param totalTokens total tokens consumed
     * @param totalConversations total conversations
     * @return tokens per conversation
     */
    public Double calculateTokensPerConversation(Long totalTokens, Long totalConversations) {
        if (totalConversations == null || totalConversations == 0) {
            return 0.0;
        }
        return (double) totalTokens / totalConversations;
    }

}
