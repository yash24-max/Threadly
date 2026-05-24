package dev.threadly.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Team entity representing a group within an organization.
 * Teams allow organizations to structure users into logical groups.
 */
@Entity
@Table(
    name = "teams",
    indexes = {
        @Index(name = "idx_org_id_teams", columnList = "org_id"),
        @Index(name = "idx_created_at_teams", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

  /**
   * Unique identifier for the team (UUID format).
   */
  @Id
  @Column(columnDefinition = "VARCHAR(36)")
  private String id;

  /**
   * Organization ID (tenant). Enforces team isolation by organization.
   */
  @Column(nullable = false, columnDefinition = "VARCHAR(36)")
  private String orgId;

  /**
   * Team name.
   */
  @Column(nullable = false, length = 255)
  private String name;

  /**
   * Team description or purpose.
   */
  @Column(columnDefinition = "TEXT")
  private String description;

  /**
   * Whether the team is active. Allows soft deletion.
   */
  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  /**
   * Timestamp when the team was created.
   */
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Timestamp when the team was last updated.
   */
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
