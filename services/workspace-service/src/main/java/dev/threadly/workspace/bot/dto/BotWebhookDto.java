package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for BotWebhook.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotWebhookDto {

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
   * Webhook URL
   */
  @JsonProperty("url")
  private String url;

  /**
   * Subscribed events
   */
  @JsonProperty("events")
  private String events;

  /**
   * Whether this webhook is active
   */
  @JsonProperty("is_active")
  private Boolean isActive;

  /**
   * Creation timestamp
   */
  @JsonProperty("created_at")
  private Instant createdAt;

  /**
   * Last modification timestamp
   */
  @JsonProperty("updated_at")
  private Instant updatedAt;

  /**
   * Last successful delivery timestamp
   */
  @JsonProperty("last_delivered_at")
  private Instant lastDeliveredAt;
}
