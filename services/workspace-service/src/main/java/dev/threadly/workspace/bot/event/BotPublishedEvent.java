package dev.threadly.workspace.bot.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a bot version is published (goes live).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotPublishedEvent {

  /**
   * Event type identifier
   */
  @JsonProperty("event_type")
  @Builder.Default
  private String eventType = "bot.published";

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
   * Published version number
   */
  @JsonProperty("version_number")
  private Integer versionNumber;

  /**
   * User who published
   */
  @JsonProperty("published_by")
  private String publishedBy;

  /**
   * Release notes
   */
  @JsonProperty("release_notes")
  private String releaseNotes;

  /**
   * Timestamp of event
   */
  @JsonProperty("timestamp")
  private Instant timestamp;
}
