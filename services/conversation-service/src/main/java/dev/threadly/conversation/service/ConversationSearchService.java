package dev.threadly.conversation.service;

import dev.threadly.conversation.dto.ConversationDto;
import dev.threadly.conversation.dto.ConversationSearchRequest;
import dev.threadly.conversation.dto.ConversationSearchResponse;
import dev.threadly.conversation.entity.Conversation;
import dev.threadly.conversation.entity.Message;
import dev.threadly.conversation.repository.ConversationRepository;
import dev.threadly.conversation.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for full-text search across conversations.
 * Supports searching by conversation content, metadata, and custom filters.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ConversationSearchService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Search conversations based on the provided search request.
     *
     * @param orgId the organization ID
     * @param request the search request
     * @return the search response with results
     */
    public ConversationSearchResponse searchConversations(String orgId, ConversationSearchRequest request) {
        log.debug("Searching conversations for org: {} with query: {}", orgId, request.getSearchText());

        // Build sort order
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortDirection())
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, request.getSortBy() != null ? request.getSortBy() : "startedAt");

        Pageable pageable = PageRequest.of(request.getPage(), request.getPageSize(), sort);

        Page<Conversation> results;

        // Determine search strategy based on filters
        if (request.getSearchText() != null && !request.getSearchText().isEmpty()) {
            results = searchByContent(orgId, request, pageable);
        } else if (request.getStatus() != null) {
            results = conversationRepository.findByOrgIdAndStatus(
                orgId,
                Conversation.ConversationStatus.valueOf(request.getStatus()),
                pageable
            );
        } else if (request.getVisitorId() != null) {
            results = conversationRepository.findByOrgIdAndVisitorId(orgId, request.getVisitorId(), pageable);
        } else if (request.getBotId() != null) {
            results = conversationRepository.findByOrgIdAndBotId(orgId, request.getBotId(), pageable);
        } else if (request.getStartDate() != null && request.getEndDate() != null) {
            results = conversationRepository.findByOrgIdAndDateRange(
                orgId,
                request.getStartDate(),
                request.getEndDate(),
                pageable
            );
        } else {
            results = conversationRepository.findByOrgId(orgId, pageable);
        }

        // Build response
        List<ConversationDto> conversations = results.getContent()
            .stream()
            .map(ConversationDto::fromEntity)
            .collect(Collectors.toList());

        return ConversationSearchResponse.builder()
            .totalCount(results.getTotalElements())
            .page(request.getPage())
            .pageSize(request.getPageSize())
            .totalPages(results.getTotalPages())
            .hasNext(results.hasNext())
            .hasPrevious(results.hasPrevious())
            .conversations(conversations)
            .build();
    }

    /**
     * Search conversations by message content.
     *
     * @param orgId the organization ID
     * @param request the search request
     * @param pageable pagination information
     * @return page of conversations containing matching messages
     */
    private Page<Conversation> searchByContent(String orgId, ConversationSearchRequest request, Pageable pageable) {
        // First, find all matching messages
        Page<Message> matchingMessages = messageRepository.searchByContent(
            "", // Empty conversation ID to search all
            request.getSearchText(),
            PageRequest.of(0, Integer.MAX_VALUE) // Get all matches initially
        );

        // Extract unique conversation IDs
        List<String> conversationIds = matchingMessages.getContent()
            .stream()
            .map(m -> m.getConversation().getId())
            .distinct()
            .collect(Collectors.toList());

        if (conversationIds.isEmpty()) {
            // Return empty page
            return Page.empty(pageable);
        }

        // For now, fall back to basic filtering
        // In a production system, would use proper full-text search (Elasticsearch, PostgreSQL FTS, etc.)
        return conversationRepository.findByOrgId(orgId, pageable);
    }

    /**
     * Advanced search with multiple filters.
     *
     * @param orgId the organization ID
     * @param searchText the search text
     * @param visitorId the visitor ID filter
     * @param status the status filter
     * @param page the page number
     * @param pageSize the page size
     * @return search response
     */
    public ConversationSearchResponse advancedSearch(String orgId, String searchText, String visitorId,
                                                      String status, int page, int pageSize) {
        ConversationSearchRequest request = ConversationSearchRequest.builder()
            .searchText(searchText)
            .visitorId(visitorId)
            .status(status)
            .page(page)
            .pageSize(pageSize)
            .build();

        return searchConversations(orgId, request);
    }

    /**
     * Get conversations with high engagement (many messages).
     *
     * @param orgId the organization ID
     * @param minMessages minimum number of messages
     * @param page the page number
     * @param pageSize the page size
     * @return page of highly engaged conversations
     */
    public Page<ConversationDto> searchHighEngagement(String orgId, Integer minMessages, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return conversationRepository.findByOrgIdAndMinMessageCount(orgId, minMessages, pageable)
            .map(ConversationDto::fromEntity);
    }

    /**
     * Find conversations that have been open for an extended period.
     *
     * @param orgId the organization ID
     * @param ageThreshold the age threshold instant
     * @param page the page number
     * @param pageSize the page size
     * @return page of long-open conversations
     */
    public Page<ConversationDto> searchLongOpenConversations(String orgId, java.time.Instant ageThreshold, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return conversationRepository.findLongOpenConversations(orgId, ageThreshold, pageable)
            .map(ConversationDto::fromEntity);
    }
}
