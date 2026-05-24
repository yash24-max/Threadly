package dev.threadly.conversation.dto;

import lombok.*;

import java.time.Duration;

/**
 * Data Transfer Object for conversation analytics data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationAnalyticsDto {
    /**
     * Conversation ID.
     */
    private String conversationId;

    /**
     * Total message count.
     */
    private Integer messageCount;

    /**
     * Count of visitor messages.
     */
    private Integer visitorMessageCount;

    /**
     * Count of AI messages.
     */
    private Integer aiMessageCount;

    /**
     * Count of human agent messages.
     */
    private Integer humanMessageCount;

    /**
     * Total tokens used.
     */
    private Long totalTokensUsed;

    /**
     * Average tokens per message.
     */
    private Double averageTokensPerMessage;

    /**
     * Conversation duration.
     */
    private Duration duration;

    /**
     * Time to first human response (if applicable).
     */
    private Duration timeToFirstHumanResponse;

    /**
     * Average response time between messages.
     */
    private Duration averageResponseTime;

    /**
     * Sentiment score (-1.0 to 1.0).
     */
    private Double sentimentScore;

    /**
     * Resolution status: resolved, unresolved, escalated.
     */
    private String resolutionStatus;

    /**
     * Customer satisfaction score (0-5).
     */
    private Double satisfactionScore;

    /**
     * Conversation status.
     */
    private String status;
}
