package dev.threadly.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for knowledge base search operations.
 * Contains search results and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KbSearchResponse {

  /**
   * The original search query.
   */
  private String query;

  /**
   * Total number of results found.
   */
  private Integer totalResults;

  /**
   * Number of results returned (may be less than limit).
   */
  private Integer resultCount;

  /**
   * List of search results ordered by relevance.
   */
  private List<KbSearchResultDto> results;

  /**
   * Search execution time in milliseconds.
   */
  private Long executionTimeMs;

  /**
   * Search mode used ("semantic", "hybrid", "bm25").
   */
  private String searchMode;

  /**
   * Whether reranking was applied.
   */
  private Boolean reranked;

  /**
   * Warning messages if any.
   */
  private List<String> warnings;
}
