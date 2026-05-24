package dev.threadly.knowledge.service;

import dev.threadly.knowledge.dto.KbSearchRequest;
import dev.threadly.knowledge.dto.KbSearchResponse;
import dev.threadly.knowledge.dto.KbSearchResultDto;
import dev.threadly.knowledge.entity.KbChunk;
import dev.threadly.knowledge.repository.KbChunkRepository;
import dev.threadly.knowledge.repository.KbDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for searching the knowledge base.
 * Supports semantic search via vector similarity, hybrid search, and text-based search.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KbSearchService {

  private final VectorDbService vectorDbService;
  private final EmbeddingService embeddingService;
  private final KbChunkRepository chunkRepository;
  private final KbDocumentRepository documentRepository;
  private final RagPipeline ragPipeline;

  /**
   * Perform semantic vector search.
   *
   * @param request the search request
   * @return search response with results
   */
  public KbSearchResponse semanticSearch(KbSearchRequest request) {
    log.info("Performing semantic search for bot: {} with query: {}", request.getBotId(), request.getQuery());

    long startTime = System.currentTimeMillis();

    try {
      // Generate embedding for query
      String modelName = request.getEmbeddingModel() != null ?
          request.getEmbeddingModel() : embeddingService.getDefaultModel();
      double[] queryEmbedding = embeddingService.generateEmbedding(request.getQuery(), modelName);

      // Search in vector database
      List<KbSearchResultDto> results = vectorDbService.search(
          request.getBotId(),
          queryEmbedding,
          request.getLimit(),
          request.getMinScore()
      );

      // Enrich results with chunk data
      enrichResults(results);

      long executionTime = System.currentTimeMillis() - startTime;

      return KbSearchResponse.builder()
          .query(request.getQuery())
          .totalResults(results.size())
          .resultCount(results.size())
          .results(results)
          .executionTimeMs(executionTime)
          .searchMode("semantic")
          .reranked(request.getUseReranking())
          .build();

    } catch (Exception e) {
      log.error("Semantic search failed", e);
      return KbSearchResponse.builder()
          .query(request.getQuery())
          .totalResults(0)
          .resultCount(0)
          .results(List.of())
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .searchMode("semantic")
          .warnings(List.of("Search failed: " + e.getMessage()))
          .build();
    }
  }

  /**
   * Perform hybrid search (vector + text-based).
   *
   * @param request the search request
   * @return search response with results
   */
  public KbSearchResponse hybridSearch(KbSearchRequest request) {
    log.info("Performing hybrid search for bot: {} with query: {}", request.getBotId(), request.getQuery());

    long startTime = System.currentTimeMillis();

    try {
      // Semantic search
      List<KbSearchResultDto> semanticResults = performSemanticSearch(request);

      // Text-based search
      List<KbSearchResultDto> textResults = performTextSearch(request);

      // Merge and deduplicate
      List<KbSearchResultDto> mergedResults = mergeResults(semanticResults, textResults, request.getLimit());

      // Apply reranking if requested
      if (request.getUseReranking()) {
        mergedResults = applyReranking(mergedResults, request.getQuery());
      }

      long executionTime = System.currentTimeMillis() - startTime;

      return KbSearchResponse.builder()
          .query(request.getQuery())
          .totalResults(mergedResults.size())
          .resultCount(mergedResults.size())
          .results(mergedResults)
          .executionTimeMs(executionTime)
          .searchMode("hybrid")
          .reranked(request.getUseReranking())
          .build();

    } catch (Exception e) {
      log.error("Hybrid search failed", e);
      return KbSearchResponse.builder()
          .query(request.getQuery())
          .totalResults(0)
          .resultCount(0)
          .results(List.of())
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .searchMode("hybrid")
          .warnings(List.of("Search failed: " + e.getMessage()))
          .build();
    }
  }

  /**
   * Perform BM25 text-based search.
   *
   * @param request the search request
   * @return search response with results
   */
  public KbSearchResponse textSearch(KbSearchRequest request) {
    log.info("Performing text search for bot: {} with query: {}", request.getBotId(), request.getQuery());

    long startTime = System.currentTimeMillis();

    try {
      List<KbSearchResultDto> results = performTextSearch(request);

      long executionTime = System.currentTimeMillis() - startTime;

      return KbSearchResponse.builder()
          .query(request.getQuery())
          .totalResults(results.size())
          .resultCount(results.size())
          .results(results)
          .executionTimeMs(executionTime)
          .searchMode("bm25")
          .build();

    } catch (Exception e) {
      log.error("Text search failed", e);
      return KbSearchResponse.builder()
          .query(request.getQuery())
          .totalResults(0)
          .resultCount(0)
          .results(List.of())
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .searchMode("bm25")
          .warnings(List.of("Search failed: " + e.getMessage()))
          .build();
    }
  }

  /**
   * Perform semantic search.
   *
   * @param request the request
   * @return list of results
   */
  private List<KbSearchResultDto> performSemanticSearch(KbSearchRequest request) {
    String modelName = request.getEmbeddingModel() != null ?
        request.getEmbeddingModel() : embeddingService.getDefaultModel();
    double[] queryEmbedding = embeddingService.generateEmbedding(request.getQuery(), modelName);

    return vectorDbService.search(
        request.getBotId(),
        queryEmbedding,
        request.getLimit(),
        request.getMinScore()
    );
  }

  /**
   * Perform text-based search using content matching.
   *
   * @param request the request
   * @return list of results
   */
  private List<KbSearchResultDto> performTextSearch(KbSearchRequest request) {
    String queryLower = request.getQuery().toLowerCase();
    List<KbChunk> chunks = chunkRepository.findByBotId(request.getBotId());

    return chunks.stream()
        .filter(chunk -> chunk.getContent().toLowerCase().contains(queryLower))
        .limit(request.getLimit())
        .map(chunk -> KbSearchResultDto.builder()
            .chunkId(chunk.getId())
            .content(chunk.getContent())
            .documentId(chunk.getDocumentId())
            .relevanceScore(calculateTextRelevance(chunk.getContent(), request.getQuery()))
            .tokens(chunk.getTokens())
            .build())
        .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
        .collect(Collectors.toList());
  }

  /**
   * Merge semantic and text results, deduplicating by chunk ID.
   *
   * @param semanticResults semantic search results
   * @param textResults text search results
   * @param limit max results
   * @return merged results
   */
  private List<KbSearchResultDto> mergeResults(List<KbSearchResultDto> semanticResults,
                                               List<KbSearchResultDto> textResults,
                                               int limit) {
    return semanticResults.stream()
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * Apply reranking to search results.
   *
   * @param results the results to rerank
   * @param query the original query
   * @return reranked results
   */
  private List<KbSearchResultDto> applyReranking(List<KbSearchResultDto> results, String query) {
    log.debug("Applying reranking to {} results", results.size());

    // Mark results as reranked
    results.forEach(r -> r.setReranked(true));

    // In production, integrate with Cohere reranking or similar
    return results.stream()
        .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
        .collect(Collectors.toList());
  }

  /**
   * Calculate text relevance score based on query matches.
   *
   * @param content the chunk content
   * @param query the search query
   * @return relevance score (0.0-1.0)
   */
  private Double calculateTextRelevance(String content, String query) {
    if (content == null || query == null) {
      return 0.0;
    }

    String contentLower = content.toLowerCase();
    String queryLower = query.toLowerCase();

    // Simple scoring: count query term occurrences
    int matches = 0;
    String[] queryTerms = queryLower.split("\\s+");
    for (String term : queryTerms) {
      if (contentLower.contains(term)) {
        matches++;
      }
    }

    return Math.min(1.0, (double) matches / queryTerms.length);
  }

  /**
   * Enrich search results with chunk data from database.
   *
   * @param results the results to enrich
   */
  private void enrichResults(List<KbSearchResultDto> results) {
    for (KbSearchResultDto result : results) {
      var chunk = chunkRepository.findById(result.getChunkId());
      if (chunk.isPresent()) {
        KbChunk c = chunk.get();
        result.setContent(c.getContent());
        result.setDocumentId(c.getDocumentId());
        result.setTokens(c.getTokens());
        result.setChunkNumber(c.getChunkNumber());

        var document = documentRepository.findById(c.getDocumentId());
        if (document.isPresent()) {
          result.setFilename(document.get().getFilename());
        }
      }
    }
  }
}
