package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for BotSettings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotSettingsDto {

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
   * Theme color (hex code)
   */
  @JsonProperty("theme_color")
  private String themeColor;

  /**
   * Avatar URL
   */
  @JsonProperty("avatar")
  private String avatar;

  /**
   * Welcome message
   */
  @JsonProperty("welcome_message")
  private String welcomeMessage;

  /**
   * Maximum token budget
   */
  @JsonProperty("max_token_budget")
  private Integer maxTokenBudget;

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
}
