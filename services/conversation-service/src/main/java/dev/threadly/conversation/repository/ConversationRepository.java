package dev.threadly.conversation.repository;

import dev.threadly.conversation.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Conversation entity.
 * Provides database access and query methods with multi-tenancy support.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    /**
     * Find a conversation by ID and organization ID (multi-tenancy).
     *
     * @param id the conversation ID
     * @param orgId the organization ID
     * @return Optional containing the conversation if found
     */
    @Query("SELECT c FROM Conversation c WHERE c.id = :id AND c.orgId = :orgId AND c.deletedAt IS NULL")
    Optional<Conversation> findByIdAndOrgId(@Param("id") String id, @Param("orgId") String orgId);

    /**
     * Find all conversations for an organization with pagination.
     *
     * @param orgId the organization ID
     * @param pageable pagination information
     * @return page of conversations
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.deletedAt IS NULL ORDER BY c.startedAt DESC")
    Page<Conversation> findByOrgId(@Param("orgId") String orgId, Pageable pageable);

    /**
     * Find conversations by organization and status.
     *
     * @param orgId the organization ID
     * @param status the conversation status
     * @param pageable pagination information
     * @return page of conversations with the specified status
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.status = :status AND c.deletedAt IS NULL ORDER BY c.startedAt DESC")
    Page<Conversation> findByOrgIdAndStatus(
        @Param("orgId") String orgId,
        @Param("status") Conversation.ConversationStatus status,
        Pageable pageable
    );

    /**
     * Find conversations by organization and visitor ID.
     *
     * @param orgId the organization ID
     * @param visitorId the visitor ID
     * @param pageable pagination information
     * @return page of conversations for the visitor
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.visitorId = :visitorId AND c.deletedAt IS NULL ORDER BY c.startedAt DESC")
    Page<Conversation> findByOrgIdAndVisitorId(
        @Param("orgId") String orgId,
        @Param("visitorId") String visitorId,
        Pageable pageable
    );

    /**
     * Find conversations by organization and bot ID.
     *
     * @param orgId the organization ID
     * @param botId the bot ID
     * @param pageable pagination information
     * @return page of conversations
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.botId = :botId AND c.deletedAt IS NULL ORDER BY c.startedAt DESC")
    Page<Conversation> findByOrgIdAndBotId(
        @Param("orgId") String orgId,
        @Param("botId") String botId,
        Pageable pageable
    );

    /**
     * Find conversations assigned to a specific agent.
     *
     * @param orgId the organization ID
     * @param agentId the agent ID
     * @param pageable pagination information
     * @return page of conversations assigned to the agent
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.assignedAgentId = :agentId AND c.deletedAt IS NULL ORDER BY c.startedAt DESC")
    Page<Conversation> findByOrgIdAndAssignedAgentId(
        @Param("orgId") String orgId,
        @Param("agentId") String agentId,
        Pageable pageable
    );

    /**
     * Find conversations within a date range.
     *
     * @param orgId the organization ID
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of conversations within the date range
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.startedAt >= :startDate AND c.startedAt <= :endDate AND c.deletedAt IS NULL ORDER BY c.startedAt DESC")
    Page<Conversation> findByOrgIdAndDateRange(
        @Param("orgId") String orgId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );

    /**
     * Count active (open) conversations for an organization.
     *
     * @param orgId the organization ID
     * @return count of open conversations
     */
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.orgId = :orgId AND c.status = 'OPEN' AND c.deletedAt IS NULL")
    long countOpenConversations(@Param("orgId") String orgId);

    /**
     * Find conversations by organization and message count filter.
     *
     * @param orgId the organization ID
     * @param minMessages minimum number of messages
     * @param pageable pagination information
     * @return page of conversations with at least minMessages
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.messageCount >= :minMessages AND c.deletedAt IS NULL ORDER BY c.messageCount DESC")
    Page<Conversation> findByOrgIdAndMinMessageCount(
        @Param("orgId") String orgId,
        @Param("minMessages") Integer minMessages,
        Pageable pageable
    );

    /**
     * Find conversations that have been open longer than a specified duration.
     *
     * @param orgId the organization ID
     * @param threshold the time threshold
     * @param pageable pagination information
     * @return page of conversations open longer than threshold
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.status = 'OPEN' AND c.startedAt < :threshold AND c.deletedAt IS NULL ORDER BY c.startedAt ASC")
    Page<Conversation> findLongOpenConversations(
        @Param("orgId") String orgId,
        @Param("threshold") Instant threshold,
        Pageable pageable
    );

    /**
     * Find all conversations by organization (for bulk operations).
     *
     * @param orgId the organization ID
     * @return list of all conversations
     */
    @Query("SELECT c FROM Conversation c WHERE c.orgId = :orgId AND c.deletedAt IS NULL ORDER BY c.startedAt DESC")
    List<Conversation> findAllByOrgId(@Param("orgId") String orgId);
}
