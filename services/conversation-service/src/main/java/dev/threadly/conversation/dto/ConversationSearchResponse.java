package dev.threadly.conversation.dto;

import lombok.*;

import java.util.List;

/**
 * Response DTO for conversation search results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSearchResponse {
    /**
     * Total number of results matching the search criteria.
     */
    private long totalCount;

    /**
     * Current page number (0-indexed).
     */
    private int page;

    /**
     * Page size used for this result set.
     */
    private int pageSize;

    /**
     * Total number of pages available.
     */
    private int totalPages;

    /**
     * Whether there is a next page available.
     */
    private boolean hasNext;

    /**
     * Whether there is a previous page available.
     */
    private boolean hasPrevious;

    /**
     * List of conversations matching the search criteria.
     */
    private List<ConversationDto> conversations;
}
