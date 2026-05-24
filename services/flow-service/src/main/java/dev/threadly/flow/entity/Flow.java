package dev.threadly.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Flow entity representing a workflow definition.
 * Tracks the overall flow with version control and publishing status.
 */
@Entity
@Table(name = "flow", indexes = {
    @Index(name = "idx_flow_bot_id", columnList = "bot_id"),
    @Index(name = "idx_flow_org_id", columnList = "org_id"),
    @Index(name = "idx_flow_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flow {

  @Id
  private String id;

  @Column(name = "bot_id", nullable = false)
  private String botId;

  @Column(name = "org_id", nullable = false)
  private String orgId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private FlowStatus status;

  @Column(name = "current_version_id")
  private String currentVersionId;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Flow status enumeration.
   */
  public enum FlowStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
  }
}
