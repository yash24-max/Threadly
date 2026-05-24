package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * DTO for bot-level performance and usage metrics.
 * Aggregates metrics for a single bot across time period.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotMetricsDto {

    @JsonProperty("bot_id")
    private String botId;

    @JsonProperty("bot_name")
    private String botName;

    @JsonProperty("conversations_total")
    private Long conversationsTotal;

    @JsonProperty("conversations_resolved")
    private Long conversationsResolved;

    @JsonProperty("resolution_rate_percent")
    private Double resolutionRatePercent;

    @JsonProperty("messages_total")
    private Long messagesTotal;

    @JsonProperty("messages_per_conversation")
    private Double messagesPerConversation;

    @JsonProperty("ai_calls_total")
    private Long aiCallsTotal;

    @JsonProperty("ai_success_rate_percent")
    private Double aiSuccessRatePercent;

    @JsonProperty("avg_response_time_ms")
    private Double avgResponseTimeMs;

    @JsonProperty("p95_response_time_ms")
    private Double p95ResponseTimeMs;

    @JsonProperty("p99_response_time_ms")
    private Double p99ResponseTimeMs;

    @JsonProperty("handoffs_total")
    private Long handoffsTotal;

    @JsonProperty("handoff_rate_percent")
    private Double handoffRatePercent;

    @JsonProperty("csat_average")
    private Double csatAverage;

    @JsonProperty("csat_responses_count")
    private Long csatResponsesCount;

    @JsonProperty("ai_tokens_total")
    private Long aiTokensTotal;

    @JsonProperty("ai_cost_cents_total")
    private Long aiCostCentsTotal;

    @JsonProperty("top_handoff_reasons")
    private List<ReasonStats> topHandoffReasons;

    /**
     * Statistics for a specific reason/category.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReasonStats {
        @JsonProperty("reason")
        private String reason;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("percent")
        private Double percent;
    }

}
