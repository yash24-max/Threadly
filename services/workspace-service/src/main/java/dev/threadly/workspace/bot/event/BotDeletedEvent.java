package dev.threadly.workspace.bot.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a bot is deleted (soft delete).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotDeletedEvent {

  /**
   * Event type identifier
   */
  @JsonProperty("event_type")
  @Builder.Default
  private String eventType = "bot.deleted";

  /**
   * Bot ID
   */
  @JsonProperty("bot_id")
  private String botId;

  /**
   * Organization ID
   */
  @JsonProperty("org_id")
  private String orgId;

  /**
   * Bot name
   */
  @JsonProperty("name")
  private String name;

  /**
   * User who deleted the bot
   */
  @JsonProperty("deleted_by")
  private String deletedBy;

  /**
   * Timestamp of event
   */
  @JsonProperty("timestamp")
  private Instant timestamp;
}
