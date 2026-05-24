package dev.threadly.runtime.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ExecutionState tracks the current execution state of a flow session.
 * Maintains execution stack, current node position, and error information.
 */
@Entity
@Table(name = "execution_states", indexes = {
    @Index(name = "idx_exec_state_session_id", columnList = "session_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionState {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 36)
  private String sessionId;

  @Column(length = 36)
  private String currentNodeId;

  @Column(columnDefinition = "TEXT")
  private String executionStackJson; // Stack of executed node IDs

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ExecutionStatus status;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Version
  private Long version;

  /**
   * Execution status enumeration
   */
  public enum ExecutionStatus {
    RUNNING,      // Currently executing
    PAUSED,       // Waiting for input
    COMPLETED,    // Flow completed successfully
    ERROR,        // Execution error
    TIMEOUT       // Execution timeout
  }
}
