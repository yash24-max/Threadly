package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing bot.
 * All fields are optional; only provided fields are updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBotRequest {

  /**
   * Bot name (optional)
   */
  @JsonProperty("name")
  private String name;

  /**
   * Bot description (optional)
   */
  @JsonProperty("description")
  private String description;

  /**
   * Bot status (optional, e.g., DRAFT, PUBLISHED, ARCHIVED)
   */
  @JsonProperty("status")
  private String status;
}
