package dev.threadly.knowledge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing a knowledge base document.
 * Stores metadata about uploaded documents for RAG pipeline.
 */
@Entity
@Table(name = "kb_document", indexes = {
    @Index(name = "idx_bot_id", columnList = "bot_id"),
    @Index(name = "idx_org_id", columnList = "org_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KbDocument {

  /**
   * Unique identifier for the document (UUID).
   */
  @Id
  @Column(nullable = false, length = 36)
  private String id;

  /**
   * Bot ID for multi-tenant isolation.
   */
  @Column(nullable = false, length = 36)
  private String botId;

  /**
   * Organization ID for multi-tenant isolation.
   */
  @Column(nullable = false, length = 36)
  private String orgId;

  /**
   * Original filename of the uploaded document.
   */
  @Column(nullable = false, length = 500)
  private String filename;

  /**
   * URL or file path to access the document.
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String fileUrl;

  /**
   * Size of the document in bytes.
   */
  @Column(nullable = false)
  private Long fileSize;

  /**
   * MIME type of the document (e.g., "application/pdf", "text/plain").
   */
  @Column(nullable = false, length = 100)
  private String contentType;

  /**
   * Current status of document indexing.
   * Values: pending, indexed, failed
   */
  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private DocumentStatus status;

  /**
   * Free-form metadata for the document (key-value pairs as JSON).
   */
  @Column(columnDefinition = "TEXT")
  private String metadata;

  /**
   * Number of chunks created from this document.
   */
  @Column(nullable = false)
  private Integer chunkCount;

  /**
   * Timestamp when document was uploaded.
   */
  @CreationTimestamp
  @Column(nullable = false)
  private Instant uploadDate;

  /**
   * Timestamp when document was last updated.
   */
  @UpdateTimestamp
  @Column(nullable = false)
  private Instant updatedDate;

  /**
   * Status enum for document indexing workflow.
   */
  public enum DocumentStatus {
    PENDING,    // Awaiting ingestion
    INDEXED,    // Successfully indexed
    FAILED      // Indexing failed
  }
}
