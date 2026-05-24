package dev.threadly.knowledge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entity representing a semantic chunk of a knowledge base document.
 * Documents are split into chunks for efficient embedding and retrieval.
 */
@Entity
@Table(name = "kb_chunk", indexes = {
    @Index(name = "idx_chunk_document_id", columnList = "document_id"),
    @Index(name = "idx_chunk_bot_id", columnList = "bot_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KbChunk {

  /**
   * Unique identifier for the chunk (UUID).
   */
  @Id
  @Column(nullable = false, length = 36)
  private String id;

  /**
   * Reference to parent document.
   */
  @Column(nullable = false, length = 36)
  private String documentId;

  /**
   * Bot ID for multi-tenant isolation.
   */
  @Column(nullable = false, length = 36)
  private String botId;

  /**
   * Sequential chunk number within the document.
   */
  @Column(nullable = false)
  private Integer chunkNumber;

  /**
   * Text content of the chunk.
   */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /**
   * Number of tokens in this chunk (for tracking token usage).
   */
  @Column(nullable = false)
  private Integer tokens;

  /**
   * Embedded vector as JSON array for vector similarity search.
   * Stored as BYTEA or TEXT depending on database.
   */
  @Column(columnDefinition = "BYTEA")
  private byte[] embeddingVector;

  /**
   * Metadata as JSON (source page numbers, section headers, etc).
   */
  @Column(columnDefinition = "TEXT")
  private String metadata;

  /**
   * Whether this chunk has been embedded.
   */
  @Column(nullable = false)
  private Boolean isEmbedded;

  /**
   * Timestamp when chunk was created.
   */
  @CreationTimestamp
  @Column(nullable = false)
  private Instant createdAt;

  /**
   * Source page or location within document (for citation).
   */
  @Column(length = 500)
  private String source;
}
