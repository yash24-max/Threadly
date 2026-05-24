package dev.threadly.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * FlowExecutionLog entity tracking execution of flows and individual nodes.
 * Used for auditing, debugging, and performance monitoring.
 */
@Entity
@Table(name = "flow_execution_log", indexes = {
    @Index(name = "idx_flow_exec_flow_id", columnList = "flow_id"),
    @Index(name = "idx_flow_exec_session_id", columnList = "session_id"),
    @Index(name = "idx_flow_exec_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowExecutionLog {

  @Id
  private String id;

  @Column(name = "flow_id", nullable = false)
  private String flowId;

  @Column(name = "session_id", nullable = false)
  private String sessionId;

  @Column(name = "node_id")
  private String nodeId;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private ExecutionStatus status;

  @Column(name = "execution_time_ms")
  private Long executionTimeMs;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  @Column(name = "input_data_json", columnDefinition = "text")
  private String inputDataJson;

  @Column(name = "output_data_json", columnDefinition = "text")
  private String outputDataJson;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Execution status enumeration.
   */
  public enum ExecutionStatus {
    STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    SKIPPED,
    TIMEOUT
  }
}
