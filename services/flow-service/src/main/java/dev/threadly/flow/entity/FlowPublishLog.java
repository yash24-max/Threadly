package dev.threadly.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * FlowPublishLog entity recording all flow publication and rollback events.
 * Provides an audit trail of flow version changes.
 */
@Entity
@Table(name = "flow_publish_log", indexes = {
    @Index(name = "idx_flow_pub_flow_id", columnList = "flow_id"),
    @Index(name = "idx_flow_pub_published_by", columnList = "published_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowPublishLog {

  @Id
  private String id;

  @Column(name = "flow_id", nullable = false)
  private String flowId;

  @Column(name = "published_by", nullable = false)
  private String publishedBy;

  @Column(name = "event_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private EventType eventType;

  @Column(name = "previous_version_id")
  private String previousVersionId;

  @Column(name = "current_version_id", nullable = false)
  private String currentVersionId;

  @Column(name = "rollback_reason", columnDefinition = "text")
  private String rollbackReason;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Event type enumeration.
   */
  public enum EventType {
    PUBLISHED,
    ROLLBACK,
    DRAFT_UPDATED
  }
}
