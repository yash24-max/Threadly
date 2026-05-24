package dev.threadly.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Membership entity representing a user's membership in an organization.
 * Tracks the user's role and team associations within the organization.
 */
@Entity
@Table(
    name = "memberships",
    indexes = {
        @Index(name = "idx_user_id_org_id", columnList = "user_id,org_id", unique = true),
        @Index(name = "idx_org_id_memberships", columnList = "org_id"),
        @Index(name = "idx_role", columnList = "role")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

  /**
   * Unique identifier for the membership (UUID format).
   */
  @Id
  @Column(columnDefinition = "VARCHAR(36)")
  private String id;

  /**
   * User ID (FK to users table).
   */
  @Column(nullable = false, columnDefinition = "VARCHAR(36)")
  private String userId;

  /**
   * Organization ID (FK to organizations table).
   */
  @Column(nullable = false, columnDefinition = "VARCHAR(36)")
  private String orgId;

  /**
   * User's role within the organization (e.g., OWNER, ADMIN, MEMBER, GUEST).
   * Controls permission levels and capabilities.
   */
  @Column(nullable = false, length = 50)
  private String role;

  /**
   * Comma-separated list of team IDs the user belongs to within the organization.
   * Empty string if user is not in any teams.
   */
  @Column(columnDefinition = "TEXT")
  @Builder.Default
  private String teamIds = "";

  /**
   * Whether the membership is active. Can be used for soft-removal from organization.
   */
  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  /**
   * Timestamp when the membership was created (user joined the organization).
   */
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Timestamp when the membership was last updated.
   */
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
