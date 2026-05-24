package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for creating a new flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlowRequest {

  @NotBlank(message = "Bot ID is required")
  @JsonProperty("bot_id")
  private String botId;

  @NotBlank(message = "Flow name is required")
  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;
}
