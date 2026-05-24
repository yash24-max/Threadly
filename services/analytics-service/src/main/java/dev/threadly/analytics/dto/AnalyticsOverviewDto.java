package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for analytics overview dashboard.
 * Provides high-level summary metrics for an organization or bot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsOverviewDto {

    @JsonProperty("period")
    private PeriodMetrics period;

    @JsonProperty("comparison")
    private ComparisonMetrics comparison;

    @JsonProperty("bots")
    private List<BotSummary> bots;

    @JsonProperty("generated_at")
    private Long generatedAt;

    /**
     * Metrics for a specific time period.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodMetrics {
        @JsonProperty("start_date")
        private LocalDate startDate;

        @JsonProperty("end_date")
        private LocalDate endDate;

        @JsonProperty("total_conversations")
        private Long totalConversations;

        @JsonProperty("total_messages")
        private Long totalMessages;

        @JsonProperty("total_ai_calls")
        private Long totalAiCalls;

        @JsonProperty("avg_response_time_ms")
        private Double avgResponseTimeMs;

        @JsonProperty("resolution_rate")
        private Double resolutionRate;

        @JsonProperty("avg_csat_score")
        private Double avgCsatScore;

        @JsonProperty("total_tokens_consumed")
        private Long totalTokensConsumed;

        @JsonProperty("total_cost_cents")
        private Long totalCostCents;

        @JsonProperty("handoffs_count")
        private Long handoffsCount;
    }

    /**
     * Comparison metrics between two periods.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComparisonMetrics {
        @JsonProperty("conversations_change_percent")
        private Double conversationsChangePercent;

        @JsonProperty("messages_change_percent")
        private Double messagesChangePercent;

        @JsonProperty("ai_calls_change_percent")
        private Double aiCallsChangePercent;

        @JsonProperty("response_time_change_percent")
        private Double responseTimeChangePercent;

        @JsonProperty("csat_change_percent")
        private Double csatChangePercent;
    }

    /**
     * Summary metrics for a single bot.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BotSummary {
        @JsonProperty("bot_id")
        private String botId;

        @JsonProperty("bot_name")
        private String botName;

        @JsonProperty("conversations_count")
        private Long conversationsCount;

        @JsonProperty("messages_count")
        private Long messagesCount;

        @JsonProperty("ai_calls_count")
        private Long aiCallsCount;

        @JsonProperty("avg_response_time_ms")
        private Double avgResponseTimeMs;

        @JsonProperty("resolution_rate")
        private Double resolutionRate;

        @JsonProperty("avg_csat_score")
        private Double avgCsatScore;
    }

}
