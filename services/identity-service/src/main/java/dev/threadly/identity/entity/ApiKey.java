package dev.threadly.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * API Key entity for programmatic access to Threadly APIs.
 * API keys are scoped to an organization and used for authentication.
 */
@Entity
@Table(
    name = "api_keys",
    indexes = {
        @Index(name = "idx_org_id_api_keys", columnList = "org_id"),
        @Index(name = "idx_key_hash", columnList = "key_hash", unique = true),
        @Index(name = "idx_created_at_api_keys", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

  /**
   * Unique identifier for the API key (UUID format).
   */
  @Id
  @Column(columnDefinition = "VARCHAR(36)")
  private String id;

  /**
   * Organization ID (tenant). API keys are scoped to organizations.
   */
  @Column(nullable = false, columnDefinition = "VARCHAR(36)")
  private String orgId;

  /**
   * Human-readable name for the API key (e.g., "Production API Key").
   */
  @Column(nullable = false, length = 255)
  private String name;

  /**
   * Bcrypt or SHA256 hash of the actual API key. Never store plaintext keys.
   */
  @Column(nullable = false, length = 255)
  private String keyHash;

  /**
   * Prefix of the API key shown to users (for identification without exposing full key).
   */
  @Column(length = 50)
  private String keyPrefix;

  /**
   * Timestamp when the API key was last used.
   */
  private LocalDateTime lastUsedAt;

  /**
   * Timestamp when the API key will expire (NULL for non-expiring keys).
   */
  private LocalDateTime expiresAt;

  /**
   * Whether the API key is revoked/disabled.
   */
  @Column(nullable = false)
  @Builder.Default
  private Boolean revoked = false;

  /**
   * Timestamp when the API key was revoked.
   */
  private LocalDateTime revokedAt;

  /**
   * Comma-separated list of scopes/permissions the API key has (e.g., "read,write,delete").
   */
  @Column(columnDefinition = "TEXT")
  @Builder.Default
  private String scopes = "read,write";

  /**
   * Timestamp when the API key was created.
   */
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Timestamp when the API key was last updated.
   */
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
