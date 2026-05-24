package dev.threadly.conversation.service;

import dev.threadly.conversation.dto.AddMessageRequest;
import dev.threadly.conversation.dto.MessageDto;
import dev.threadly.conversation.entity.Conversation;
import dev.threadly.conversation.entity.Message;
import dev.threadly.conversation.exception.ConversationNotFoundException;
import dev.threadly.conversation.exception.MessageNotFoundException;
import dev.threadly.conversation.repository.ConversationRepository;
import dev.threadly.conversation.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for message management.
 * Handles message creation, retrieval, and search within conversations.
 * Messages are immutable after creation and can only be soft-deleted by admins.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    /**
     * Add a message to a conversation.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param request the add message request
     * @return the created message DTO
     */
    public MessageDto addMessage(String conversationId, String orgId, AddMessageRequest request) {
        // Verify conversation exists and belongs to organization
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        Message message = Message.builder()
            .id(UUID.randomUUID().toString())
            .conversation(conversation)
            .sender(Message.MessageSender.valueOf(request.getSender().toUpperCase()))
            .senderId(request.getSenderId())
            .content(request.getContent())
            .metadataJson(request.getMetadataJson())
            .tokensUsed(request.getTokensUsed())
            .build();

        Message saved = messageRepository.save(message);

        // Update conversation message count and tokens
        conversation.setMessageCount(conversation.getMessageCount() + 1);
        if (request.getTokensUsed() != null) {
            conversation.setTokensUsed(conversation.getTokensUsed() + request.getTokensUsed());
        }
        conversationRepository.save(conversation);

        log.debug("Message added to conversation: {}", conversationId);
        return MessageDto.fromEntity(saved);
    }

    /**
     * Get a message by ID.
     *
     * @param messageId the message ID
     * @param conversationId the conversation ID
     * @return the message DTO
     * @throws MessageNotFoundException if message not found
     */
    @Transactional(readOnly = true)
    public MessageDto getMessage(String messageId, String conversationId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException(messageId, conversationId));

        if (!message.getConversation().getId().equals(conversationId)) {
            throw new MessageNotFoundException(messageId, conversationId);
        }

        return MessageDto.fromEntity(message);
    }

    /**
     * Get all messages in a conversation with pagination.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param page the page number
     * @param pageSize the page size
     * @return page of message DTOs
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessages(String conversationId, String orgId, int page, int pageSize) {
        // Verify conversation exists
        conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        Pageable pageable = PageRequest.of(page, pageSize);
        return messageRepository.findByConversationId(conversationId, pageable)
            .map(MessageDto::fromEntity);
    }

    /**
     * Get all messages in a conversation without pagination.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @return list of message DTOs
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getAllMessages(String conversationId, String orgId) {
        // Verify conversation exists
        conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        return messageRepository.findAllByConversationId(conversationId)
            .stream()
            .map(MessageDto::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Search messages by content.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param searchText the text to search for
     * @param page the page number
     * @param pageSize the page size
     * @return page of matching message DTOs
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> searchMessages(String conversationId, String orgId, String searchText, int page, int pageSize) {
        // Verify conversation exists
        conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        Pageable pageable = PageRequest.of(page, pageSize);
        return messageRepository.searchByContent(conversationId, searchText, pageable)
            .map(MessageDto::fromEntity);
    }

    /**
     * Get messages by sender type.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param sender the sender type
     * @param page the page number
     * @param pageSize the page size
     * @return page of message DTOs
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessagesBySender(String conversationId, String orgId, Message.MessageSender sender, int page, int pageSize) {
        // Verify conversation exists
        conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        Pageable pageable = PageRequest.of(page, pageSize);
        return messageRepository.findByConversationIdAndSender(conversationId, sender, pageable)
            .map(MessageDto::fromEntity);
    }

    /**
     * Delete a message (soft delete - admin only).
     * This does not actually remove the record but marks it as deleted.
     *
     * @param messageId the message ID
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     */
    public void deleteMessage(String messageId, String conversationId, String orgId) {
        // Verify conversation exists
        conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException(messageId, conversationId));

        if (!message.getConversation().getId().equals(conversationId)) {
            throw new MessageNotFoundException(messageId, conversationId);
        }

        message.setDeletedAt(Instant.now());
        messageRepository.save(message);

        log.info("Message soft-deleted: {}", messageId);
    }

    /**
     * Get message count for a conversation.
     *
     * @param conversationId the conversation ID
     * @return count of non-deleted messages
     */
    @Transactional(readOnly = true)
    public long getMessageCount(String conversationId) {
        return messageRepository.countByConversationId(conversationId);
    }

    /**
     * Get total tokens used in a conversation.
     *
     * @param conversationId the conversation ID
     * @return total tokens
     */
    @Transactional(readOnly = true)
    public long getTotalTokens(String conversationId) {
        return messageRepository.sumTokensByConversationId(conversationId);
    }
}
