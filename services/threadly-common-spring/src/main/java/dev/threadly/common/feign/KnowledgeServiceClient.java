package dev.threadly.common.feign;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Knowledge Service (:3006).
 *
 * Endpoints:
 * - KB document management
 * - RAG query interface
 * - Ingestion job tracking
 */
@FeignClient(
    name = "knowledge-service",
    url = "${threadly.services.knowledge-service.url:http://knowledge-service:3006}"
)
public interface KnowledgeServiceClient {

  /**
   * GET /bots/{botId}/kb/documents — List KB documents.
   */
  @GetMapping("/bots/{botId}/kb/documents")
  DocumentsListResponse listDocuments(
      @PathVariable UUID botId,
      @RequestHeader("Authorization") String token
  );

  /**
   * DELETE /bots/{botId}/kb/documents/{docId} — Delete document.
   */
  @DeleteMapping("/bots/{botId}/kb/documents/{docId}")
  void deleteDocument(
      @PathVariable UUID botId,
      @PathVariable UUID docId,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /bots/{botId}/kb/jobs — Get ingestion job status.
   */
  @GetMapping("/bots/{botId}/kb/jobs")
  JobsListResponse listIngestionJobs(
      @PathVariable UUID botId,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /bots/{botId}/kb/query — RAG query (retrieve + rerank).
   */
  @PostMapping("/bots/{botId}/kb/query")
  RagQueryResponse queryKnowledgeBase(
      @PathVariable UUID botId,
      @RequestBody RagQueryRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /bots/{botId}/kb/reindex — Reindex all documents.
   */
  @PostMapping("/bots/{botId}/kb/reindex")
  void reindexKnowledgeBase(
      @PathVariable UUID botId,
      @RequestHeader("Authorization") String token
  );

  // DTOs

  record DocumentsListResponse(List<KbDocumentDTO> documents, int total) {}

  record KbDocumentDTO(
      UUID documentId,
      UUID botId,
      String documentName,
      String documentType,
      String sourceUrl,
      int fileSizeBytes,
      int pageCount,
      String status,
      int chunkCount,
      java.time.Instant indexedAt,
      java.time.Instant createdAt
  ) {}

  record JobsListResponse(List<IngestionJobDTO> jobs, int total) {}

  record IngestionJobDTO(
      UUID jobId,
      UUID documentId,
      String jobType,
      String status,
      int progressPercent,
      String errorMessage,
      java.time.Instant startedAt,
      java.time.Instant completedAt
  ) {}

  record RagQueryRequest(
      String query,
      int topK,
      boolean includeMetadata,
      String rerankerModel
  ) {}

  record RagQueryResponse(
      String query,
      List<RagResultDTO> results,
      int totalResults
  ) {}

  record RagResultDTO(
      UUID chunkId,
      UUID documentId,
      String documentName,
      String content,
      double relevanceScore,
      java.util.Map<String, Object> metadata
  ) {}
}
