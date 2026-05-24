package dev.threadly.conversation.service;

import dev.threadly.conversation.dto.ConversationDto;
import dev.threadly.conversation.dto.CreateConversationRequest;
import dev.threadly.conversation.entity.Conversation;
import dev.threadly.conversation.exception.ConversationNotFoundException;
import dev.threadly.conversation.exception.InvalidConversationStatusException;
import dev.threadly.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service class for conversation management.
 * Handles CRUD operations, status transitions, and conversation lifecycle.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConversationService {
    private final ConversationRepository conversationRepository;

    /**
     * Create a new conversation.
     *
     * @param orgId the organization ID
     * @param request the conversation creation request
     * @return the created conversation DTO
     */
    public ConversationDto createConversation(String orgId, CreateConversationRequest request) {
        log.info("Creating conversation for org: {}, bot: {}", orgId, request.getBotId());

        Conversation conversation = Conversation.builder()
            .id(UUID.randomUUID().toString())
            .orgId(orgId)
            .botId(request.getBotId())
            .flowId(request.getFlowId())
            .visitorId(request.getVisitorId())
            .status(Conversation.ConversationStatus.OPEN)
            .messageCount(0)
            .tokensUsed(0L)
            .metadataJson(request.getMetadataJson())
            .build();

        Conversation saved = conversationRepository.save(conversation);
        log.debug("Conversation created: {}", saved.getId());

        return ConversationDto.fromEntity(saved);
    }

    /**
     * Get a conversation by ID.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @return the conversation DTO
     * @throws ConversationNotFoundException if conversation not found
     */
    @Transactional(readOnly = true)
    public ConversationDto getConversation(String conversationId, String orgId) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        log.debug("Retrieved conversation: {}", conversationId);
        return ConversationDto.fromEntity(conversation);
    }

    /**
     * Update conversation metadata.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param metadataJson the new metadata JSON
     * @return the updated conversation DTO
     */
    public ConversationDto updateConversationMetadata(String conversationId, String orgId, String metadataJson) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        conversation.setMetadataJson(metadataJson);
        Conversation updated = conversationRepository.save(conversation);

        log.debug("Updated conversation metadata: {}", conversationId);
        return ConversationDto.fromEntity(updated);
    }

    /**
     * Close a conversation.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @return the updated conversation DTO
     */
    public ConversationDto closeConversation(String conversationId, String orgId) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        if (conversation.getStatus() == Conversation.ConversationStatus.CLOSED) {
            throw new InvalidConversationStatusException(
                conversationId,
                conversation.getStatus().toString(),
                Conversation.ConversationStatus.CLOSED.toString()
            );
        }

        conversation.setStatus(Conversation.ConversationStatus.CLOSED);
        conversation.setEndedAt(Instant.now());
        Conversation updated = conversationRepository.save(conversation);

        log.info("Conversation closed: {}", conversationId);
        return ConversationDto.fromEntity(updated);
    }

    /**
     * Hand off a conversation to a human agent.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param agentId the agent ID
     * @return the updated conversation DTO
     */
    public ConversationDto handoffConversation(String conversationId, String orgId, String agentId) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        conversation.setStatus(Conversation.ConversationStatus.HANDED_OFF);
        conversation.setAssignedAgentId(agentId);
        Conversation updated = conversationRepository.save(conversation);

        log.info("Conversation handed off: {} to agent: {}", conversationId, agentId);
        return ConversationDto.fromEntity(updated);
    }

    /**
     * List conversations for an organization with pagination.
     *
     * @param orgId the organization ID
     * @param page the page number (0-indexed)
     * @param pageSize the page size
     * @return page of conversation DTOs
     */
    @Transactional(readOnly = true)
    public Page<ConversationDto> listConversations(String orgId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return conversationRepository.findByOrgId(orgId, pageable)
            .map(ConversationDto::fromEntity);
    }

    /**
     * List conversations by status.
     *
     * @param orgId the organization ID
     * @param status the conversation status
     * @param page the page number
     * @param pageSize the page size
     * @return page of conversation DTOs
     */
    @Transactional(readOnly = true)
    public Page<ConversationDto> listConversationsByStatus(String orgId, Conversation.ConversationStatus status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return conversationRepository.findByOrgIdAndStatus(orgId, status, pageable)
            .map(ConversationDto::fromEntity);
    }

    /**
     * List conversations for a specific visitor.
     *
     * @param orgId the organization ID
     * @param visitorId the visitor ID
     * @param page the page number
     * @param pageSize the page size
     * @return page of conversation DTOs
     */
    @Transactional(readOnly = true)
    public Page<ConversationDto> listConversationsByVisitor(String orgId, String visitorId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return conversationRepository.findByOrgIdAndVisitorId(orgId, visitorId, pageable)
            .map(ConversationDto::fromEntity);
    }

    /**
     * List conversations for a specific bot.
     *
     * @param orgId the organization ID
     * @param botId the bot ID
     * @param page the page number
     * @param pageSize the page size
     * @return page of conversation DTOs
     */
    @Transactional(readOnly = true)
    public Page<ConversationDto> listConversationsByBot(String orgId, String botId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return conversationRepository.findByOrgIdAndBotId(orgId, botId, pageable)
            .map(ConversationDto::fromEntity);
    }

    /**
     * Count open conversations for an organization.
     *
     * @param orgId the organization ID
     * @return count of open conversations
     */
    @Transactional(readOnly = true)
    public long countOpenConversations(String orgId) {
        return conversationRepository.countOpenConversations(orgId);
    }

    /**
     * Increment message count for a conversation.
     *
     * @param conversationId the conversation ID
     */
    protected void incrementMessageCount(String conversationId) {
        // This is called by MessageService after adding a message
        // Implementation will be optimized in a separate update query
    }

    /**
     * Add tokens to a conversation's total.
     *
     * @param conversationId the conversation ID
     * @param tokens the number of tokens to add
     */
    protected void addTokens(String conversationId, long tokens) {
        // This is called by MessageService after adding a message
        // Implementation will be optimized in a separate update query
    }
}
