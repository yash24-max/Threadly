package dev.threadly.workspace.bot.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Represents a webhook subscription for bot lifecycle events.
 * Webhooks are triggered on events like bot creation, publication, deletion, etc.
 */
@Entity
@Table(name = "bot_webhook", indexes = {@Index(name = "idx_bot_webhook_bot_id", columnList = "bot_id")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotWebhook {
  /**
   * Unique identifier (UUID)
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
   * HTTPS endpoint URL where webhook events are posted
   */
  @Column(name = "url", nullable = false, columnDefinition = "TEXT")
  private String url;

  /**
   * Comma-separated event types to subscribe to
   * (e.g., "bot.published,bot.deleted,bot.settings_updated")
   */
  @Column(name = "events", columnDefinition = "TEXT")
  private String events;

  /**
   * Whether this webhook is currently active
   */
  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  /**
   * Secret key for HMAC signature validation of webhook payloads
   */
  @Column(name = "secret", length = 255)
  private String secret;

  /**
   * Timestamp of creation
   */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /**
   * Timestamp of last update
   */
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /**
   * Timestamp of last successful delivery
   */
  @Column(name = "last_delivered_at")
  private Instant lastDeliveredAt;
}
