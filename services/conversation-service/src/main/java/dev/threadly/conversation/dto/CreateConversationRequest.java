package dev.threadly.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * Request DTO for creating a new conversation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConversationRequest {
    /**
     * The bot/flow instance ID.
     */
    @NotBlank(message = "Bot ID is required")
    @Pattern(regexp = "^[a-f0-9-]{36}$", message = "Invalid bot ID format")
    private String botId;

    /**
     * The flow configuration ID.
     */
    @Pattern(regexp = "^[a-f0-9-]{36}$|^$", message = "Invalid flow ID format")
    private String flowId;

    /**
     * The visitor/session ID.
     */
    @NotBlank(message = "Visitor ID is required")
    @Pattern(regexp = "^[a-f0-9-]{36}$", message = "Invalid visitor ID format")
    private String visitorId;

    /**
     * Optional JSON metadata for the conversation.
     */
    private String metadataJson;
}
