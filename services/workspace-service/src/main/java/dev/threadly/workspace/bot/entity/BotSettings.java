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
 * Stores customizable settings and configuration for a bot.
 * Includes theming, avatar, welcome message, and token budget limits.
 */
@Entity
@Table(name = "bot_settings", indexes = {@Index(name = "idx_bot_settings_bot_id", columnList = "bot_id")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotSettings {
  /**
   * Unique identifier (UUID)
   */
  @Id
  @Column(name = "id", length = 36, nullable = false)
  private String id;

  /**
   * Reference to the Bot this settings belong to
   */
  @Column(name = "bot_id", length = 36, nullable = false)
  private String botId;

  /**
   * Hex color code for the bot's theme (e.g., #3B82F6)
   */
  @Column(name = "theme_color", length = 7)
  @Builder.Default
  private String themeColor = "#3B82F6";

  /**
   * URL to the bot's avatar image
   */
  @Column(name = "avatar", columnDefinition = "TEXT")
  private String avatar;

  /**
   * Welcome message shown when users interact with the bot
   */
  @Column(name = "welcome_message", columnDefinition = "TEXT")
  private String welcomeMessage;

  /**
   * Maximum token budget per conversation (0 = unlimited)
   */
  @Column(name = "max_token_budget", nullable = false)
  @Builder.Default
  private Integer maxTokenBudget = 0;

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
}
