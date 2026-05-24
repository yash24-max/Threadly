package dev.threadly.conversation.service;

import dev.threadly.conversation.dto.ConversationDto;
import dev.threadly.conversation.entity.Conversation;
import dev.threadly.conversation.exception.ConversationNotFoundException;
import dev.threadly.conversation.exception.InvalidConversationStatusException;
import dev.threadly.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing conversation handoffs to human agents.
 * Handles the transition from AI to human support.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HandoffService {
    private final ConversationRepository conversationRepository;

    /**
     * Initiate handoff from AI to human agent.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param agentId the agent ID to assign
     * @param reason the reason for handoff
     * @return the updated conversation DTO
     */
    public ConversationDto initiateHandoff(String conversationId, String orgId, String agentId, String reason) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        if (conversation.getStatus() == Conversation.ConversationStatus.CLOSED) {
            throw new InvalidConversationStatusException(
                conversationId,
                conversation.getStatus().toString(),
                Conversation.ConversationStatus.HANDED_OFF.toString()
            );
        }

        conversation.setStatus(Conversation.ConversationStatus.HANDED_OFF);
        conversation.setAssignedAgentId(agentId);

        // Store handoff reason in metadata if needed
        if (reason != null && !reason.isEmpty()) {
            // Append to or create metadata
            String metadata = conversation.getMetadataJson();
            if (metadata == null) {
                metadata = "{\"handoff_reason\":\"" + escapeJson(reason) + "\"}";
            } else {
                // Simple JSON append (in production, use proper JSON library)
                metadata = metadata.replace("}", ",\"handoff_reason\":\"" + escapeJson(reason) + "\"}");
            }
            conversation.setMetadataJson(metadata);
        }

        Conversation updated = conversationRepository.save(conversation);
        log.info("Handoff initiated for conversation: {} to agent: {}", conversationId, agentId);

        return ConversationDto.fromEntity(updated);
    }

    /**
     * Check if a conversation can be handed off.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @return true if handoff is possible
     */
    public boolean canHandoff(String conversationId, String orgId) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        // Can only handoff open conversations
        return conversation.getStatus() == Conversation.ConversationStatus.OPEN;
    }

    /**
     * Cancel a handoff and return conversation to AI.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @return the updated conversation DTO
     */
    public ConversationDto cancelHandoff(String conversationId, String orgId) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        if (conversation.getStatus() != Conversation.ConversationStatus.HANDED_OFF) {
            throw new InvalidConversationStatusException(
                conversationId,
                conversation.getStatus().toString(),
                Conversation.ConversationStatus.OPEN.toString()
            );
        }

        conversation.setStatus(Conversation.ConversationStatus.OPEN);
        conversation.setAssignedAgentId(null);

        Conversation updated = conversationRepository.save(conversation);
        log.info("Handoff cancelled for conversation: {}", conversationId);

        return ConversationDto.fromEntity(updated);
    }

    /**
     * Get active conversations assigned to an agent.
     *
     * @param orgId the organization ID
     * @param agentId the agent ID
     * @param page the page number
     * @param pageSize the page size
     * @return page of conversations assigned to the agent
     */
    public org.springframework.data.domain.Page<ConversationDto> getAgentConversations(String orgId, String agentId, int page, int pageSize) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
        return conversationRepository.findByOrgIdAndAssignedAgentId(orgId, agentId, pageable)
            .map(ConversationDto::fromEntity);
    }

    /**
     * Count active conversations for an agent.
     *
     * @param orgId the organization ID
     * @param agentId the agent ID
     * @return count of active conversations
     */
    public long countAgentConversations(String orgId, String agentId) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 1);
        return conversationRepository.findByOrgIdAndAssignedAgentId(orgId, agentId, pageable).getTotalElements();
    }

    /**
     * Reassign a conversation to a different agent.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param newAgentId the new agent ID
     * @return the updated conversation DTO
     */
    public ConversationDto reassignConversation(String conversationId, String orgId, String newAgentId) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        if (conversation.getStatus() != Conversation.ConversationStatus.HANDED_OFF) {
            throw new InvalidConversationStatusException(
                conversationId,
                conversation.getStatus().toString(),
                Conversation.ConversationStatus.HANDED_OFF.toString()
            );
        }

        String oldAgentId = conversation.getAssignedAgentId();
        conversation.setAssignedAgentId(newAgentId);

        Conversation updated = conversationRepository.save(conversation);
        log.info("Conversation reassigned from agent: {} to agent: {} for conversation: {}", oldAgentId, newAgentId, conversationId);

        return ConversationDto.fromEntity(updated);
    }

    /**
     * Escape JSON special characters.
     *
     * @param text the text to escape
     * @return escaped JSON text
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
