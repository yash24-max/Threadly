package dev.threadly.runtime.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ExecutionLog records detailed information about each node execution.
 * Used for debugging, performance monitoring, and audit trails.
 */
@Entity
@Table(name = "execution_logs", indexes = {
    @Index(name = "idx_exec_log_session_id", columnList = "session_id"),
    @Index(name = "idx_exec_log_node_id", columnList = "node_id"),
    @Index(name = "idx_exec_log_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionLog {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 36)
  private String sessionId;

  @Column(nullable = false, length = 36)
  private String nodeId;

  @Column(nullable = false, length = 100)
  private String nodeType;

  @Column(columnDefinition = "TEXT")
  private String inputJson;

  @Column(columnDefinition = "TEXT")
  private String outputJson;

  @Column(nullable = false)
  private Long executionTimeMs;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ExecutionLogStatus status;

  @Column(columnDefinition = "TEXT")
  private String errorDetails;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Version
  private Long version;

  /**
   * Execution log status
   */
  public enum ExecutionLogStatus {
    SUCCESS,
    FAILURE,
    TIMEOUT,
    SKIPPED
  }
}
