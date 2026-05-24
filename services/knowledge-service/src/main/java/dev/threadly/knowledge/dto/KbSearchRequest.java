package dev.threadly.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for knowledge base search operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KbSearchRequest {

  /**
   * The bot ID to search within.
   */
  private String botId;

  /**
   * Search query text (for semantic search or BM25).
   */
  private String query;

  /**
   * Maximum number of results to return (default: 10).
   */
  @Builder.Default
  private Integer limit = 10;

  /**
   * Minimum relevance score threshold (0.0-1.0, default: 0.0).
   */
  @Builder.Default
  private Double minScore = 0.0;

  /**
   * Whether to use reranking for top-K results (default: false).
   */
  @Builder.Default
  private Boolean useReranking = false;

  /**
   * Optional filter by document IDs.
   */
  private List<String> documentIds;

  /**
   * Optional semantic search mode ("semantic", "hybrid", "bm25").
   */
  @Builder.Default
  private String searchMode = "semantic";

  /**
   * Optional metadata filter (e.g., {"category": "faq"}).
   */
  private String metadataFilter;

  /**
   * Optional embedding model override.
   */
  private String embeddingModel;
}
