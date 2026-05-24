package dev.threadly.workspace.bot.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Stores API keys for programmatic access to a bot.
 * Keys are hashed for security; the plain key is never stored.
 */
@Entity
@Table(name = "bot_api_key", indexes = {@Index(name = "idx_bot_api_key_bot_id", columnList = "bot_id"),
    @Index(name = "idx_bot_api_key_key_hash", columnList = "key_hash")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotApiKey {
  /**
   * Unique identifier for this API key (UUID)
   */
  @Id
  @Column(name = "id", length = 36, nullable = false)
  private String id;

  /**
   * Reference to the Bot
   */
  @Column(name = "bot_id", length = 36, nullable = false)
  private String botId;

  /**
   * Human-readable name for this key
   */
  @Column(name = "name", nullable = false, length = 255)
  private String name;

  /**
   * SHA256 hash of the API key (NEVER store the plain key)
   */
  @Column(name = "key_hash", nullable = false, columnDefinition = "CHAR(64)")
  private String keyHash;

  /**
   * Timestamp when this key was generated
   */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /**
   * Timestamp when this key was revoked (null = active)
   */
  @Column(name = "revoked_at")
  private Instant revokedAt;

  /**
   * User ID who created this key
   */
  @Column(name = "created_by", length = 36, nullable = false)
  private String createdBy;

  /**
   * Last time this key was used
   */
  @Column(name = "last_used_at")
  private Instant lastUsedAt;
}
