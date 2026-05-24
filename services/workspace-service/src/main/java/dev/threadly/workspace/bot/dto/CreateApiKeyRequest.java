package dev.threadly.workspace.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new API key.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateApiKeyRequest {

  /**
   * Name for this API key (required)
   */
  @NotBlank(message = "API key name is required")
  @JsonProperty("name")
  private String name;
}
