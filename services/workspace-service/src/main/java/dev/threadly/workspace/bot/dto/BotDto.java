package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Bot entity.
 * Represents a bot workspace with its metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotDto {

  /**
   * Unique identifier
   */
  @JsonProperty("id")
  private String id;

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
   * Bot description
   */
  @JsonProperty("description")
  private String description;

  /**
   * Current status
   */
  @JsonProperty("status")
  private String status;

  /**
   * User who created this bot
   */
  @JsonProperty("created_by")
  private String createdBy;

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
   * User's role on this bot (OWNER, EDITOR, VIEWER)
   */
  @JsonProperty("user_role")
  private String userRole;

  /**
   * Brand accent color for the bot widget (hex, e.g. "#6366F1").
   * Used by the frontend to colorize bot avatars and the chat widget.
   */
  @JsonProperty("accent_color")
  private String accentColor;
}
