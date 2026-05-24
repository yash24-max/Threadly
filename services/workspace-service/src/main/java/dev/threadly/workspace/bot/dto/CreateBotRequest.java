package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new bot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBotRequest {

  /**
   * Bot name (required, max 255 characters)
   */
  @NotBlank(message = "Bot name is required")
  @JsonProperty("name")
  private String name;

  /**
   * Bot description (optional)
   */
  @JsonProperty("description")
  private String description;
}
