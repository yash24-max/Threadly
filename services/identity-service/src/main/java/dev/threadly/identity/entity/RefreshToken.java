package dev.threadly.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Refresh Token entity for managing JWT refresh token rotation.
 * Refresh tokens have a longer expiry than access tokens and are used to obtain new access tokens.
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_user_id_refresh", columnList = "user_id"),
        @Index(name = "idx_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_expires_at", columnList = "expires_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

  /**
   * Unique identifier for the refresh token (UUID format).
   */
  @Id
  @Column(columnDefinition = "VARCHAR(36)")
  private String id;

  /**
   * User ID (FK to users table) that this token belongs to.
   */
  @Column(nullable = false, columnDefinition = "VARCHAR(36)")
  private String userId;

  /**
   * Hash of the actual refresh token. Never store plaintext tokens.
   * Uses SHA256 or similar algorithm for security.
   */
  @Column(nullable = false, length = 255)
  private String tokenHash;

  /**
   * IP address where the token was issued from (for audit trail).
   */
  @Column(length = 45)
  private String issuedFromIp;

  /**
   * User agent string of the client that obtained the token (for audit trail).
   */
  @Column(columnDefinition = "TEXT")
  private String userAgent;

  /**
   * Whether the token has been revoked (e.g., user logged out).
   */
  @Column(nullable = false)
  @Builder.Default
  private Boolean revoked = false;

  /**
   * Timestamp when the refresh token expires.
   */
  @Column(nullable = false)
  private LocalDateTime expiresAt;

  /**
   * Timestamp when the refresh token was created.
   */
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
