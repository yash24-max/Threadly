package dev.threadly.conversation.dto;

import dev.threadly.conversation.entity.Message;
import lombok.*;

import java.time.Instant;

/**
 * Data Transfer Object for Message entity.
 * Used for API responses and data serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private String id;
    private String conversationId;
    private String sender;
    private String senderId;
    private String content;
    private String metadataJson;
    private Long tokensUsed;
    private Instant createdAt;

    /**
     * Convert a Message entity to a DTO.
     *
     * @param message the message entity
     * @return the message DTO
     */
    public static MessageDto fromEntity(Message message) {
        if (message == null) {
            return null;
        }
        return MessageDto.builder()
            .id(message.getId())
            .conversationId(message.getConversation().getId())
            .sender(message.getSender().toString())
            .senderId(message.getSenderId())
            .content(message.getContent())
            .metadataJson(message.getMetadataJson())
            .tokensUsed(message.getTokensUsed())
            .createdAt(message.getCreatedAt())
            .build();
    }
}
