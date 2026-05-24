package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for BotVersion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotVersionDto {

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
   * Version number
   */
  @JsonProperty("version_number")
  private Integer versionNumber;

  /**
   * Configuration snapshot as JSON
   */
  @JsonProperty("config_snapshot")
  private String configSnapshot;

  /**
   * Publication timestamp
   */
  @JsonProperty("published_at")
  private Instant publishedAt;

  /**
   * User who published this version
   */
  @JsonProperty("published_by")
  private String publishedBy;

  /**
   * Release notes
   */
  @JsonProperty("release_notes")
  private String releaseNotes;
}
