package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for TeamMember.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberDto {

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
   * User ID
   */
  @JsonProperty("user_id")
  private String userId;

  /**
   * User's role (OWNER, EDITOR, VIEWER)
   */
  @JsonProperty("role")
  private String role;

  /**
   * Timestamp when added to team
   */
  @JsonProperty("created_at")
  private Instant createdAt;

  /**
   * Last modification timestamp
   */
  @JsonProperty("updated_at")
  private Instant updatedAt;
}
