package dev.threadly.conversation.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.Instant;

/**
 * Request DTO for searching conversations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSearchRequest {
    /**
     * Search text to find in conversation messages.
     */
    private String searchText;

    /**
     * Filter by conversation status.
     */
    private String status;

    /**
     * Filter by visitor ID.
     */
    private String visitorId;

    /**
     * Filter by bot ID.
     */
    private String botId;

    /**
     * Filter by assigned agent ID.
     */
    private String assignedAgentId;

    /**
     * Filter by tag name.
     */
    private String tagName;

    /**
     * Filter by tag value.
     */
    private String tagValue;

    /**
     * Start date for date range filter.
     */
    private Instant startDate;

    /**
     * End date for date range filter.
     */
    private Instant endDate;

    /**
     * Minimum message count filter.
     */
    @Min(0)
    private Integer minMessageCount;

    /**
     * Page number for pagination (0-indexed).
     */
    @Min(0)
    @Builder.Default
    private Integer page = 0;

    /**
     * Page size for pagination.
     */
    @Min(1)
    @Builder.Default
    private Integer pageSize = 20;

    /**
     * Sort field (e.g., "startedAt", "messageCount").
     */
    @Builder.Default
    private String sortBy = "startedAt";

    /**
     * Sort direction: "asc" or "desc".
     */
    @Builder.Default
    private String sortDirection = "desc";
}
