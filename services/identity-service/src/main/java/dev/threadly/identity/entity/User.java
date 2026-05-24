package dev.threadly.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User entity representing an individual account in Threadly.
 * Each user belongs to an organization (tenant).
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_org_id", columnList = "org_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  /**
   * Unique identifier for the user (UUID format).
   */
  @Id
  @Column(columnDefinition = "VARCHAR(36)")
  private String id;

  /**
   * Organization ID (tenant). Enforces multi-tenancy at the entity level.
   */
  @Column(nullable = false, columnDefinition = "VARCHAR(36)")
  private String orgId;

  /**
   * User's email address. Unique across all users.
   */
  @Column(nullable = false, unique = true, length = 255)
  private String email;

  /**
   * Bcrypt hashed password. Never store plaintext passwords.
   */
  @Column(nullable = false, length = 72)
  private String passwordHash;

  /**
   * User's full name.
   */
  @Column(length = 255)
  private String fullName;

  /**
   * URL to user's profile picture.
   */
  @Column(length = 500)
  private String profilePictureUrl;

  /**
   * User's job title or role description.
   */
  @Column(length = 255)
  private String jobTitle;

  /**
   * Whether the user's email has been verified.
   */
  @Column(nullable = false)
  @Builder.Default
  private Boolean emailVerified = false;

  /**
   * Timestamp when email verification was completed.
   */
  private LocalDateTime emailVerifiedAt;

  /**
   * Whether the user account is active. Can be used for soft-deletes.
   */
  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  /**
   * Timestamp when the user account was created.
   */
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Timestamp when the user account was last updated.
   */
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Timestamp when the user last logged in.
   */
  private LocalDateTime lastLoginAt;
}
