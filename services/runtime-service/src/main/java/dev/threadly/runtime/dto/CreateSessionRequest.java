package dev.threadly.runtime.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for creating a new session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionRequest {

  @NotBlank(message = "Bot ID is required")
  private String botId;

  @NotBlank(message = "Flow ID is required")
  private String flowId;

  @NotBlank(message = "Visitor ID is required")
  private String visitorId;
}
