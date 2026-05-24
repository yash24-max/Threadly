package dev.threadly.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for a single search result.
 * Represents a chunk with relevance score and citation information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KbSearchResultDto {

  /**
   * The chunk ID.
   */
  private String chunkId;

  /**
   * The chunk content.
   */
  private String content;

  /**
   * Document ID for citation purposes.
   */
  private String documentId;

  /**
   * Original filename for citation.
   */
  private String filename;

  /**
   * Source location in document (e.g., page number).
   */
  private String source;

  /**
   * Relevance score (0.0-1.0).
   */
  private Double relevanceScore;

  /**
   * Chunk metadata.
   */
  private Map<String, Object> metadata;

  /**
   * Total tokens in the chunk.
   */
  private Integer tokens;

  /**
   * Chunk number for ordering.
   */
  private Integer chunkNumber;

  /**
   * Whether this was reranked.
   */
  private Boolean reranked;
}
