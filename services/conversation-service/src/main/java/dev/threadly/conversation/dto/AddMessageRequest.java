package dev.threadly.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for adding a message to a conversation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMessageRequest {
    /**
     * The sender type: visitor, ai, human.
     */
    @NotBlank(message = "Sender type is required")
    private String sender;

    /**
     * The ID of the sender.
     */
    private String senderId;

    /**
     * The message content.
     */
    @NotBlank(message = "Message content is required")
    private String content;

    /**
     * Optional JSON metadata.
     */
    private String metadataJson;

    /**
     * Optional tokens used count.
     */
    private Long tokensUsed;
}
