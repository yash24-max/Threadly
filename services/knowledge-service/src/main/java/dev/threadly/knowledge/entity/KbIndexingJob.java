package dev.threadly.knowledge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entity representing an asynchronous indexing job for document ingestion.
 * Tracks the progress of parsing, chunking, and embedding a document.
 */
@Entity
@Table(name = "kb_indexing_job", indexes = {
    @Index(name = "idx_job_document_id", columnList = "document_id"),
    @Index(name = "idx_job_status", columnList = "status"),
    @Index(name = "idx_job_bot_id", columnList = "bot_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KbIndexingJob {

  /**
   * Unique identifier for the indexing job (UUID).
   */
  @Id
  @Column(nullable = false, length = 36)
  private String id;

  /**
   * Reference to the document being indexed.
   */
  @Column(nullable = false, length = 36)
  private String documentId;

  /**
   * Bot ID for multi-tenant isolation.
   */
  @Column(nullable = false, length = 36)
  private String botId;

  /**
   * Current status of the indexing job.
   */
  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private JobStatus status;

  /**
   * Progress percentage (0-100).
   */
  @Column(nullable = false)
  @Builder.Default
  private Integer progress = 0;

  /**
   * Total number of chunks to process.
   */
  @Column
  private Integer totalChunks;

  /**
   * Number of chunks processed so far.
   */
  @Column(nullable = false)
  @Builder.Default
  private Integer processedChunks = 0;

  /**
   * Number of chunks embedded.
   */
  @Column(nullable = false)
  @Builder.Default
  private Integer embeddedChunks = 0;

  /**
   * Timestamp when the job was created.
   */
  @CreationTimestamp
  @Column(nullable = false)
  private Instant createdAt;

  /**
   * Timestamp when processing started.
   */
  @Column
  private Instant startedAt;

  /**
   * Timestamp when processing completed or failed.
   */
  @Column
  private Instant completedAt;

  /**
   * Error message if the job failed.
   */
  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  /**
   * Stack trace for debugging (if failed).
   */
  @Column(columnDefinition = "TEXT")
  private String errorStackTrace;

  /**
   * Job status enum.
   */
  public enum JobStatus {
    PENDING,      // Waiting to start
    PROCESSING,   // Currently processing
    COMPLETE,     // Successfully completed
    FAILED        // Failed with error
  }
}
