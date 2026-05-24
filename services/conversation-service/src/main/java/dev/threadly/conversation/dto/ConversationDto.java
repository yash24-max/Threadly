package dev.threadly.conversation.dto;

import dev.threadly.conversation.entity.Conversation;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object for Conversation entity.
 * Used for API responses and data serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDto {
    private String id;
    private String orgId;
    private String botId;
    private String flowId;
    private String visitorId;
    private String status;
    private String assignedAgentId;
    private Integer messageCount;
    private Long tokensUsed;
    private String metadataJson;
    private Instant startedAt;
    private Instant endedAt;
    private Instant updatedAt;
    private List<MessageDto> recentMessages;

    /**
     * Convert a Conversation entity to a DTO.
     *
     * @param conversation the conversation entity
     * @return the conversation DTO
     */
    public static ConversationDto fromEntity(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        return ConversationDto.builder()
            .id(conversation.getId())
            .orgId(conversation.getOrgId())
            .botId(conversation.getBotId())
            .flowId(conversation.getFlowId())
            .visitorId(conversation.getVisitorId())
            .status(conversation.getStatus().toString())
            .assignedAgentId(conversation.getAssignedAgentId())
            .messageCount(conversation.getMessageCount())
            .tokensUsed(conversation.getTokensUsed())
            .metadataJson(conversation.getMetadataJson())
            .startedAt(conversation.getStartedAt())
            .endedAt(conversation.getEndedAt())
            .updatedAt(conversation.getUpdatedAt())
            .build();
    }
}
