package dev.threadly.conversation.controller;

import dev.threadly.conversation.dto.AddMessageRequest;
import dev.threadly.conversation.dto.MessageDto;
import dev.threadly.conversation.entity.Message;
import dev.threadly.conversation.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for message management endpoints.
 * Provides APIs for adding, retrieving, and deleting messages within conversations.
 */
@RestController
@RequestMapping("/api/v1/conversations/{conversationId}/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final MessageService messageService;

    /**
     * Add a message to a conversation.
     * POST /api/v1/conversations/{conversationId}/messages
     *
     * @param conversationId the conversation ID
     * @param request the add message request
     * @return the created message
     */
    @PostMapping
    public ResponseEntity<MessageDto> addMessage(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @Valid @RequestBody AddMessageRequest request) {

        log.debug("Adding message to conversation: {}", conversationId);
        MessageDto message = messageService.addMessage(conversationId, orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * Get messages in a conversation with pagination.
     * GET /api/v1/conversations/{conversationId}/messages?page=0&pageSize=20
     *
     * @param conversationId the conversation ID
     * @param page the page number
     * @param pageSize the page size
     * @return page of messages
     */
    @GetMapping
    public ResponseEntity<Page<MessageDto>> getMessages(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        log.debug("Fetching messages for conversation: {}", conversationId);
        Page<MessageDto> messages = messageService.getMessages(conversationId, orgId, page, pageSize);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get a specific message.
     * GET /api/v1/conversations/{conversationId}/messages/{messageId}
     *
     * @param conversationId the conversation ID
     * @param messageId the message ID
     * @return the message
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageDto> getMessage(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @PathVariable String messageId) {

        log.debug("Fetching message: {} from conversation: {}", messageId, conversationId);
        MessageDto message = messageService.getMessage(messageId, conversationId);
        return ResponseEntity.ok(message);
    }

    /**
     * Search messages by content.
     * GET /api/v1/conversations/{conversationId}/messages/search?q=text
     *
     * @param conversationId the conversation ID
     * @param searchText the search text
     * @param page the page number
     * @param pageSize the page size
     * @return page of matching messages
     */
    @GetMapping("/search")
    public ResponseEntity<Page<MessageDto>> searchMessages(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @RequestParam String searchText,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        log.debug("Searching messages in conversation: {} for text: {}", conversationId, searchText);
        Page<MessageDto> messages = messageService.searchMessages(conversationId, orgId, searchText, page, pageSize);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get messages by sender type.
     * GET /api/v1/conversations/{conversationId}/messages/by-sender?sender=VISITOR
     *
     * @param conversationId the conversation ID
     * @param sender the sender type
     * @param page the page number
     * @param pageSize the page size
     * @return page of messages from the sender
     */
    @GetMapping("/by-sender")
    public ResponseEntity<Page<MessageDto>> getMessagesBySender(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @RequestParam String sender,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        Message.MessageSender msgSender = Message.MessageSender.valueOf(sender.toUpperCase());
        Page<MessageDto> messages = messageService.getMessagesBySender(conversationId, orgId, msgSender, page, pageSize);
        return ResponseEntity.ok(messages);
    }

    /**
     * Delete a message (soft delete - admin only).
     * DELETE /api/v1/conversations/{conversationId}/messages/{messageId}
     *
     * @param conversationId the conversation ID
     * @param messageId the message ID
     * @return no content
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @PathVariable String messageId) {

        log.info("Deleting message: {} from conversation: {}", messageId, conversationId);
        messageService.deleteMessage(messageId, conversationId, orgId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get message count for conversation.
     * GET /api/v1/conversations/{conversationId}/messages/stats/count
     *
     * @param conversationId the conversation ID
     * @return count of messages
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getMessageCount(
        @PathVariable String conversationId) {

        long count = messageService.getMessageCount(conversationId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get total tokens for conversation.
     * GET /api/v1/conversations/{conversationId}/messages/stats/tokens
     *
     * @param conversationId the conversation ID
     * @return total tokens
     */
    @GetMapping("/stats/tokens")
    public ResponseEntity<Long> getTotalTokens(
        @PathVariable String conversationId) {

        long tokens = messageService.getTotalTokens(conversationId);
        return ResponseEntity.ok(tokens);
    }
}
