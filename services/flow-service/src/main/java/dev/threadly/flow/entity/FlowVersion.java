package dev.threadly.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * FlowVersion entity representing an immutable version of a flow definition.
 * Each published flow creates a new version record that cannot be modified.
 */
@Entity
@Table(name = "flow_version", indexes = {
    @Index(name = "idx_flow_version_flow_id", columnList = "flow_id"),
    @Index(name = "idx_flow_version_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowVersion {

  @Id
  private String id;

  @Column(name = "flow_id", nullable = false)
  private String flowId;

  @Column(name = "version_number", nullable = false)
  private Integer versionNumber;

  @Column(name = "definition_json", nullable = false, columnDefinition = "text")
  private String definitionJson;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  @Column(name = "published_by")
  private String publishedBy;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Marks this version as the active published version.
   */
  public void markAsActive() {
    this.isActive = true;
    this.publishedAt = LocalDateTime.now();
  }
}
