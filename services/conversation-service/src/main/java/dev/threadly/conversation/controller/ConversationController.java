package dev.threadly.conversation.controller;

import dev.threadly.conversation.dto.ConversationDto;
import dev.threadly.conversation.dto.CreateConversationRequest;
import dev.threadly.conversation.entity.Conversation;
import dev.threadly.conversation.service.ConversationService;
import dev.threadly.conversation.service.HandoffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for conversation management endpoints.
 * Provides APIs for creating, retrieving, updating, and closing conversations.
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {
    private final ConversationService conversationService;
    private final HandoffService handoffService;

    /**
     * Create a new conversation.
     * POST /api/v1/conversations
     *
     * @param request the conversation creation request
     * @return the created conversation
     */
    @PostMapping
    public ResponseEntity<ConversationDto> createConversation(
        @RequestHeader("X-Org-ID") String orgId,
        @Valid @RequestBody CreateConversationRequest request) {

        log.info("Creating conversation for org: {}", orgId);
        ConversationDto conversation = conversationService.createConversation(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    /**
     * Get a conversation by ID.
     * GET /api/v1/conversations/{conversationId}
     *
     * @param conversationId the conversation ID
     * @return the conversation
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDto> getConversation(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId) {

        log.debug("Fetching conversation: {} for org: {}", conversationId, orgId);
        ConversationDto conversation = conversationService.getConversation(conversationId, orgId);
        return ResponseEntity.ok(conversation);
    }

    /**
     * List conversations with pagination.
     * GET /api/v1/conversations?page=0&pageSize=20
     *
     * @param page the page number
     * @param pageSize the page size
     * @return page of conversations
     */
    @GetMapping
    public ResponseEntity<Page<ConversationDto>> listConversations(
        @RequestHeader("X-Org-ID") String orgId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        log.debug("Listing conversations for org: {}", orgId);
        Page<ConversationDto> conversations = conversationService.listConversations(orgId, page, pageSize);
        return ResponseEntity.ok(conversations);
    }

    /**
     * List conversations by status.
     * GET /api/v1/conversations/by-status?status=OPEN&page=0&pageSize=20
     *
     * @param status the conversation status
     * @param page the page number
     * @param pageSize the page size
     * @return page of conversations
     */
    @GetMapping("/by-status")
    public ResponseEntity<Page<ConversationDto>> listByStatus(
        @RequestHeader("X-Org-ID") String orgId,
        @RequestParam String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        Conversation.ConversationStatus convStatus = Conversation.ConversationStatus.valueOf(status.toUpperCase());
        Page<ConversationDto> conversations = conversationService.listConversationsByStatus(orgId, convStatus, page, pageSize);
        return ResponseEntity.ok(conversations);
    }

    /**
     * List conversations by visitor.
     * GET /api/v1/conversations/by-visitor/{visitorId}
     *
     * @param visitorId the visitor ID
     * @param page the page number
     * @param pageSize the page size
     * @return page of conversations
     */
    @GetMapping("/by-visitor/{visitorId}")
    public ResponseEntity<Page<ConversationDto>> listByVisitor(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String visitorId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        Page<ConversationDto> conversations = conversationService.listConversationsByVisitor(orgId, visitorId, page, pageSize);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Update conversation metadata.
     * PATCH /api/v1/conversations/{conversationId}
     *
     * @param conversationId the conversation ID
     * @param metadataJson the new metadata
     * @return the updated conversation
     */
    @PatchMapping("/{conversationId}")
    public ResponseEntity<ConversationDto> updateConversation(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @RequestBody String metadataJson) {

        log.debug("Updating conversation: {} for org: {}", conversationId, orgId);
        ConversationDto conversation = conversationService.updateConversationMetadata(conversationId, orgId, metadataJson);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Close a conversation.
     * POST /api/v1/conversations/{conversationId}/close
     *
     * @param conversationId the conversation ID
     * @return the closed conversation
     */
    @PostMapping("/{conversationId}/close")
    public ResponseEntity<ConversationDto> closeConversation(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId) {

        log.info("Closing conversation: {} for org: {}", conversationId, orgId);
        ConversationDto conversation = conversationService.closeConversation(conversationId, orgId);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Hand off conversation to agent.
     * POST /api/v1/conversations/{conversationId}/handoff
     *
     * @param conversationId the conversation ID
     * @param agentId the agent ID
     * @param reason the handoff reason
     * @return the handed-off conversation
     */
    @PostMapping("/{conversationId}/handoff")
    public ResponseEntity<ConversationDto> handoffConversation(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @RequestParam String agentId,
        @RequestParam(required = false) String reason) {

        log.info("Initiating handoff for conversation: {} to agent: {}", conversationId, agentId);
        ConversationDto conversation = handoffService.initiateHandoff(conversationId, orgId, agentId, reason);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Get count of open conversations.
     * GET /api/v1/conversations/stats/open
     *
     * @return count of open conversations
     */
    @GetMapping("/stats/open")
    public ResponseEntity<Long> countOpenConversations(
        @RequestHeader("X-Org-ID") String orgId) {

        long count = conversationService.countOpenConversations(orgId);
        return ResponseEntity.ok(count);
    }
}
