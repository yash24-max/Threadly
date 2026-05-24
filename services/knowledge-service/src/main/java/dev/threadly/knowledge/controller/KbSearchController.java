package dev.threadly.knowledge.controller;

import dev.threadly.knowledge.dto.KbSearchRequest;
import dev.threadly.knowledge.dto.KbSearchResponse;
import dev.threadly.knowledge.service.KbSearchService;
import dev.threadly.knowledge.service.RagPipeline;
import dev.threadly.knowledge.dto.RagContextDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for knowledge base search operations.
 * Supports semantic, hybrid, and text-based search.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kb/search")
@RequiredArgsConstructor
public class KbSearchController {

  private final KbSearchService searchService;
  private final RagPipeline ragPipeline;

  /**
   * Perform semantic vector search.
   *
   * @param request the search request
   * @return search response with results
   */
  @PostMapping
  public ResponseEntity<KbSearchResponse> search(@RequestBody KbSearchRequest request) {
    log.info("Performing semantic search: {} for bot: {}", request.getQuery(), request.getBotId());

    KbSearchResponse response = searchService.semanticSearch(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Perform hybrid search (vector + text).
   *
   * @param request the search request
   * @return search response with results
   */
  @PostMapping("/hybrid")
  public ResponseEntity<KbSearchResponse> hybridSearch(@RequestBody KbSearchRequest request) {
    log.info("Performing hybrid search: {} for bot: {}", request.getQuery(), request.getBotId());

    KbSearchResponse response = searchService.hybridSearch(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Perform text-based search (BM25).
   *
   * @param request the search request
   * @return search response with results
   */
  @PostMapping("/text")
  public ResponseEntity<KbSearchResponse> textSearch(@RequestBody KbSearchRequest request) {
    log.info("Performing text search: {} for bot: {}", request.getQuery(), request.getBotId());

    KbSearchResponse response = searchService.textSearch(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Build RAG context from search results.
   * Returns formatted context suitable for LLM prompt injection.
   *
   * @param request the search request
   * @param format optional format ("text", "sections")
   * @return RAG context DTO
   */
  @PostMapping("/rag-context")
  public ResponseEntity<RagContextDto> getRagContext(
      @RequestBody KbSearchRequest request,
      @RequestParam(defaultValue = "text") String format) {

    log.info("Building RAG context for query: {}", request.getQuery());

    KbSearchResponse searchResults = searchService.semanticSearch(request);
    RagContextDto context = ragPipeline.buildContext(searchResults.getResults(), request.getQuery());

    return ResponseEntity.ok(context);
  }

  /**
   * Get formatted RAG context as plain text.
   *
   * @param request the search request
   * @return formatted text
   */
  @PostMapping("/rag-prompt")
  public ResponseEntity<String> getRagPrompt(@RequestBody KbSearchRequest request) {
    log.info("Getting RAG prompt for query: {}", request.getQuery());

    KbSearchResponse searchResults = searchService.semanticSearch(request);
    RagContextDto context = ragPipeline.buildContext(searchResults.getResults(), request.getQuery());
    String prompt = ragPipeline.formatContextWithSections(context);

    return ResponseEntity.ok(prompt);
  }
}
