package dev.threadly.knowledge.service;

import dev.threadly.knowledge.dto.KbSearchResultDto;
import dev.threadly.knowledge.dto.RagContextDto;
import dev.threadly.knowledge.repository.KbChunkRepository;
import dev.threadly.knowledge.repository.KbDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) Pipeline.
 * Formats retrieved chunks as context for LLM prompts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagPipeline {

  @Value("${rag.max-context-tokens:4000}")
  private Integer maxContextTokens;

  @Value("${rag.chunk-separator:\n---\n}")
  private String chunkSeparator;

  private final KbChunkRepository chunkRepository;
  private final KbDocumentRepository documentRepository;

  /**
   * Build RAG context from search results.
   *
   * @param results the search results
   * @param query the original query
   * @return formatted context for LLM
   */
  public RagContextDto buildContext(List<KbSearchResultDto> results, String query) {
    log.info("Building RAG context from {} results", results.size());

    List<RagContextDto.CitationDto> citations = new ArrayList<>();
    StringBuilder contextBuilder = new StringBuilder();
    int totalTokens = 0;
    boolean truncated = false;

    contextBuilder.append("Context from knowledge base:\n\n");

    for (KbSearchResultDto result : results) {
      if (totalTokens + result.getTokens() > maxContextTokens) {
        truncated = true;
        break;
      }

      // Add chunk content
      contextBuilder.append("Source: ").append(result.getFilename())
          .append(" (Relevance: ").append(String.format("%.2f", result.getRelevanceScore()))
          .append(")\n");

      if (result.getSource() != null) {
        contextBuilder.append("Page/Section: ").append(result.getSource()).append("\n");
      }

      contextBuilder.append("---\n");
      contextBuilder.append(result.getContent()).append("\n");
      contextBuilder.append(chunkSeparator).append("\n");

      totalTokens += result.getTokens();

      // Add citation
      citations.add(RagContextDto.CitationDto.builder()
          .documentId(result.getDocumentId())
          .filename(result.getFilename())
          .source(result.getSource())
          .relevanceScore(result.getRelevanceScore())
          .chunkNumber(result.getChunkNumber())
          .build());
    }

    // Calculate average confidence
    double avgConfidence = results.stream()
        .mapToDouble(r -> r.getRelevanceScore() != null ? r.getRelevanceScore() : 0.0)
        .average()
        .orElse(0.0);

    return RagContextDto.builder()
        .formattedContext(contextBuilder.toString())
        .citations(citations)
        .totalTokens(totalTokens)
        .chunkCount(results.size())
        .truncated(truncated)
        .query(query)
        .confidence(avgConfidence)
        .build();
  }

  /**
   * Build context from chunk IDs directly (for when you have specific chunks).
   *
   * @param chunkIds the chunk IDs to include
   * @param query the original query
   * @return formatted context
   */
  public RagContextDto buildContextFromChunks(List<String> chunkIds, String query) {
    log.info("Building RAG context from {} chunk IDs", chunkIds.size());

    List<KbSearchResultDto> results = chunkIds.stream()
        .map(chunkRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(chunk -> {
          var doc = documentRepository.findById(chunk.getDocumentId());
          return KbSearchResultDto.builder()
              .chunkId(chunk.getId())
              .content(chunk.getContent())
              .documentId(chunk.getDocumentId())
              .filename(doc.map(d -> d.getFilename()).orElse("Unknown"))
              .source(chunk.getSource())
              .tokens(chunk.getTokens())
              .chunkNumber(chunk.getChunkNumber())
              .relevanceScore(1.0)  // Direct inclusion = full relevance
              .build();
        })
        .collect(Collectors.toList());

    return buildContext(results, query);
  }

  /**
   * Format context as a simple text block for direct LLM input.
   *
   * @param context the RAG context
   * @return formatted text string
   */
  public String formatContextAsPrompt(RagContextDto context) {
    return context.getFormattedContext();
  }

  /**
   * Format context with structured sections.
   *
   * @param context the RAG context
   * @return formatted text with sections
   */
  public String formatContextWithSections(RagContextDto context) {
    StringBuilder formatted = new StringBuilder();

    formatted.append("RETRIEVED CONTEXT\n");
    formatted.append("=================\n\n");

    formatted.append(context.getFormattedContext()).append("\n\n");

    if (!context.getCitations().isEmpty()) {
      formatted.append("SOURCES\n");
      formatted.append("-------\n");
      for (int i = 0; i < context.getCitations().size(); i++) {
        RagContextDto.CitationDto citation = context.getCitations().get(i);
        formatted.append(String.format("[%d] %s", i + 1, citation.getFilename()));
        if (citation.getSource() != null) {
          formatted.append(" (").append(citation.getSource()).append(")");
        }
        formatted.append("\n");
      }
    }

    if (context.getTruncated()) {
      formatted.append("\n[WARNING: Context was truncated due to token limits]\n");
    }

    return formatted.toString();
  }

  /**
   * Get token count for context.
   *
   * @param context the context
   * @return total tokens
   */
  public Integer getContextTokenCount(RagContextDto context) {
    return context.getTotalTokens();
  }

  /**
   * Check if context fits within token limits.
   *
   * @param context the context
   * @return true if within limits
   */
  public Boolean fitsInTokenLimit(RagContextDto context) {
    return !context.getTruncated();
  }

  /**
   * Get max context tokens.
   *
   * @return max tokens
   */
  public Integer getMaxContextTokens() {
    return maxContextTokens;
  }
}
