package dev.threadly.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Organization entity representing a customer/tenant in Threadly.
 * All users and resources are scoped to an organization.
 */
@Entity
@Table(
    name = "organizations",
    indexes = {
        @Index(name = "idx_owner_id", columnList = "owner_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

  /**
   * Unique identifier for the organization (UUID format).
   */
  @Id
  @Column(columnDefinition = "VARCHAR(36)")
  private String id;

  /**
   * Organization name/display name.
   */
  @Column(nullable = false, length = 255)
  private String name;

  /**
   * Organization owner's user ID (FK to users table).
   */
  @Column(nullable = false, columnDefinition = "VARCHAR(36)")
  private String ownerId;

  /**
   * Billing plan type (e.g., FREE, PRO, ENTERPRISE).
   */
  @Column(nullable = false, length = 50)
  @Builder.Default
  private String plan = "FREE";

  /**
   * Stripe customer ID for billing integration.
   */
  @Column(length = 255)
  private String stripeCustomerId;

  /**
   * Organization description or notes.
   */
  @Column(columnDefinition = "TEXT")
  private String description;

  /**
   * Organization's website URL.
   */
  @Column(length = 500)
  private String website;

  /**
   * Logo URL for the organization.
   */
  @Column(length = 500)
  private String logoUrl;

  /**
   * Whether the organization account is active.
   */
  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  /**
   * Timestamp when the organization was created.
   */
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Timestamp when the organization was last updated.
   */
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
