package dev.threadly.runtime.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for sending a message in a session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {

  @NotBlank(message = "Message content is required")
  private String message;

  private String userId;
}
