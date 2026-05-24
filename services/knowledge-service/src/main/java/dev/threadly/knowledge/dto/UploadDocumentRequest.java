package dev.threadly.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for uploading documents to knowledge base.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadDocumentRequest {

  /**
   * The bot ID that owns this document.
   */
  private String botId;

  /**
   * The organization ID for multi-tenant isolation.
   */
  private String orgId;

  /**
   * Original filename of the document.
   */
  private String filename;

  /**
   * File URL or path (for remote files) or upload will handle stream.
   */
  private String fileUrl;

  /**
   * Optional metadata for the document (e.g., tags, categories).
   */
  private Map<String, Object> metadata;

  /**
   * Optional hint about content language (e.g., "en", "es").
   */
  private String language;

  /**
   * Optional chunking strategy override ("semantic", "fixed", "sentence").
   */
  private String chunkingStrategy;

  /**
   * Optional maximum chunk size in tokens (default: 1000).
   */
  private Integer maxChunkSize;
}
