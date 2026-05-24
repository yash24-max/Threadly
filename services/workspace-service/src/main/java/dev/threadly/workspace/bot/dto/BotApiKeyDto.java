package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for BotApiKey.
 * Note: The actual key value is never included in DTOs for security.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotApiKeyDto {

  /**
   * Unique identifier
   */
  @JsonProperty("id")
  private String id;

  /**
   * Bot ID
   */
  @JsonProperty("bot_id")
  private String botId;

  /**
   * Key name
   */
  @JsonProperty("name")
  private String name;

  /**
   * Creation timestamp
   */
  @JsonProperty("created_at")
  private Instant createdAt;

  /**
   * Revocation timestamp (null if active)
   */
  @JsonProperty("revoked_at")
  private Instant revokedAt;

  /**
   * User who created this key
   */
  @JsonProperty("created_by")
  private String createdBy;

  /**
   * Last usage timestamp
   */
  @JsonProperty("last_used_at")
  private Instant lastUsedAt;

  /**
   * Whether this key is active (not revoked)
   */
  @JsonProperty("is_active")
  private Boolean isActive;
}
