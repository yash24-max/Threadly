package dev.threadly.analytics.service;

import dev.threadly.analytics.dto.BotMetricsDto;
import dev.threadly.analytics.entity.DailyRollup;
import dev.threadly.analytics.repository.DailyRollupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating bot-level analytics metrics.
 * Aggregates performance and usage metrics per bot.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BotMetricsService {

    private final DailyRollupRepository dailyRollupRepository;

    /**
     * Get metrics for a specific bot within a date range.
     *
     * @param botId bot identifier
     * @param startDate start date
     * @param endDate end date
     * @return bot metrics DTO
     */
    public BotMetricsDto getBotMetrics(String botId, LocalDate startDate, LocalDate endDate) {
        try {
            List<DailyRollup> rollups = dailyRollupRepository.findByBotIdAndDateRange(botId, startDate, endDate);

            if (rollups.isEmpty()) {
                log.warn("No rollup data found for bot: {} between {} and {}", botId, startDate, endDate);
                return BotMetricsDto.builder()
                    .botId(botId)
                    .build();
            }

            // Aggregate metrics from rollups
            long totalConversations = rollups.stream()
                .mapToLong(DailyRollup::getConversationsCount)
                .sum();

            long totalMessages = rollups.stream()
                .mapToLong(DailyRollup::getMessagesCount)
                .sum();

            long totalAiCalls = rollups.stream()
                .mapToLong(DailyRollup::getAiCallsCount)
                .sum();

            long totalHandoffs = rollups.stream()
                .mapToLong(DailyRollup::getHandoffsCount)
                .sum();

            long totalTokens = rollups.stream()
                .mapToLong(DailyRollup::getTotalTokensConsumed)
                .sum();

            long totalCostCents = rollups.stream()
                .mapToLong(DailyRollup::getTotalCostCents)
                .sum();

            double avgResponseTime = rollups.stream()
                .mapToDouble(DailyRollup::getAvgResponseTimeMs)
                .average()
                .orElse(0.0);

            double avgCsat = rollups.stream()
                .filter(r -> r.getAvgCsatScore() > 0)
                .mapToDouble(DailyRollup::getAvgCsatScore)
                .average()
                .orElse(0.0);

            double resolutionRate = rollups.stream()
                .mapToDouble(DailyRollup::getResolutionRate)
                .average()
                .orElse(0.0);

            double messagesPerConversation = totalConversations > 0 ?
                (double) totalMessages / totalConversations : 0.0;

            double handoffRatePercent = totalConversations > 0 ?
                (double) totalHandoffs / totalConversations * 100 : 0.0;

            double aiSuccessRatePercent = totalAiCalls > 0 ? 95.0 : 0.0; // Placeholder

            BotMetricsDto metrics = BotMetricsDto.builder()
                .botId(botId)
                .conversationsTotal(totalConversations)
                .messagesTotal(totalMessages)
                .aiCallsTotal(totalAiCalls)
                .avgResponseTimeMs(avgResponseTime)
                .handoffsTotal(totalHandoffs)
                .handoffRatePercent(handoffRatePercent)
                .messagesPerConversation(messagesPerConversation)
                .csatAverage(avgCsat)
                .aiSuccessRatePercent(aiSuccessRatePercent)
                .aiTokensTotal(totalTokens)
                .aiCostCentsTotal(totalCostCents)
                .resolutionRatePercent(resolutionRate)
                .topHandoffReasons(new ArrayList<>())
                .build();

            log.debug("Calculated metrics for bot: {} from {} rollups", botId, rollups.size());
            return metrics;

        } catch (Exception e) {
            log.error("Error calculating bot metrics for: {}", botId, e);
            throw new RuntimeException("Failed to calculate bot metrics", e);
        }
    }

    /**
     * Get metrics for multiple bots.
     *
     * @param orgId organization identifier
     * @param startDate start date
     * @param endDate end date
     * @return list of bot metrics
     */
    public List<BotMetricsDto> getOrgBotMetrics(String orgId, LocalDate startDate, LocalDate endDate) {
        try {
            List<DailyRollup> rollups = dailyRollupRepository
                .findByOrgIdAndDateRange(orgId, startDate, endDate);

            // Group by bot ID
            Map<String, List<DailyRollup>> rollupsByBot = rollups.stream()
                .collect(Collectors.groupingBy(DailyRollup::getBotId));

            return rollupsByBot.entrySet().stream()
                .map(entry -> aggregateBotMetrics(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error calculating org bot metrics for: {}", orgId, e);
            throw new RuntimeException("Failed to calculate org bot metrics", e);
        }
    }

    private BotMetricsDto aggregateBotMetrics(String botId, List<DailyRollup> rollups) {
        long totalConversations = rollups.stream()
            .mapToLong(DailyRollup::getConversationsCount)
            .sum();

        long totalMessages = rollups.stream()
            .mapToLong(DailyRollup::getMessagesCount)
            .sum();

        long totalAiCalls = rollups.stream()
            .mapToLong(DailyRollup::getAiCallsCount)
            .sum();

        long totalHandoffs = rollups.stream()
            .mapToLong(DailyRollup::getHandoffsCount)
            .sum();

        double avgResponseTime = rollups.stream()
            .mapToDouble(DailyRollup::getAvgResponseTimeMs)
            .average()
            .orElse(0.0);

        return BotMetricsDto.builder()
            .botId(botId)
            .conversationsTotal(totalConversations)
            .messagesTotal(totalMessages)
            .aiCallsTotal(totalAiCalls)
            .avgResponseTimeMs(avgResponseTime)
            .handoffsTotal(totalHandoffs)
            .build();
    }

}
