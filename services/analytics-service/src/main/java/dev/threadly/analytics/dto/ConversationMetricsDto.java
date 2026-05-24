package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

/**
 * DTO for conversation-level analytics metrics.
 * Aggregates metrics for a single conversation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMetricsDto {

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("bot_id")
    private String botId;

    @JsonProperty("duration_seconds")
    private Long durationSeconds;

    @JsonProperty("messages_count")
    private Integer messagesCount;

    @JsonProperty("ai_messages_count")
    private Integer aiMessagesCount;

    @JsonProperty("user_messages_count")
    private Integer userMessagesCount;

    @JsonProperty("avg_response_time_ms")
    private Double avgResponseTimeMs;

    @JsonProperty("is_resolved")
    private Boolean isResolved;

    @JsonProperty("csat_score")
    private Integer csatScore;

    @JsonProperty("was_handoff")
    private Boolean wasHandoff;

    @JsonProperty("handoff_reason")
    private String handoffReason;

    @JsonProperty("ai_tokens_used")
    private Long aiTokensUsed;

    @JsonProperty("ai_cost_cents")
    private Long aiCostCents;

    @JsonProperty("started_at")
    private Instant startedAt;

    @JsonProperty("ended_at")
    private Instant endedAt;

}
