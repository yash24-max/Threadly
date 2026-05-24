package dev.threadly.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for RAG context formatted for LLM input.
 * Provides relevant chunks with citations and formatting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RagContextDto {

  /**
   * Formatted context as a single string for LLM prompt.
   */
  private String formattedContext;

  /**
   * List of source citations.
   */
  private List<CitationDto> citations;

  /**
   * Total number of tokens in context.
   */
  private Integer totalTokens;

  /**
   * Number of chunks included.
   */
  private Integer chunkCount;

  /**
   * Whether context was truncated due to token limits.
   */
  private Boolean truncated;

  /**
   * Original query that generated this context.
   */
  private String query;

  /**
   * Confidence level (average relevance score).
   */
  private Double confidence;

  /**
   * Inner DTO for citation information.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CitationDto {
    private String documentId;
    private String filename;
    private String source;
    private Double relevanceScore;
    private Integer chunkNumber;
  }
}
