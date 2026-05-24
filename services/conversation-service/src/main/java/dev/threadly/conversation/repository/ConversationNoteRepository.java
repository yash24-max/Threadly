package dev.threadly.conversation.repository;

import dev.threadly.conversation.entity.ConversationNote;
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
 * Repository interface for ConversationNote entity.
 * Provides database access for agent notes on conversations.
 */
@Repository
public interface ConversationNoteRepository extends JpaRepository<ConversationNote, String> {

    /**
     * Find a note by ID.
     *
     * @param id the note ID
     * @return Optional containing the note if found and not deleted
     */
    @Query("SELECT n FROM ConversationNote n WHERE n.id = :id AND n.deletedAt IS NULL")
    Optional<ConversationNote> findById(@Param("id") String id);

    /**
     * Find all notes for a conversation with pagination.
     *
     * @param conversationId the conversation ID
     * @param pageable pagination information
     * @return page of notes ordered by creation time (newest first)
     */
    @Query("SELECT n FROM ConversationNote n WHERE n.conversation.id = :conversationId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<ConversationNote> findByConversationId(@Param("conversationId") String conversationId, Pageable pageable);

    /**
     * Find all notes for a conversation (no pagination).
     *
     * @param conversationId the conversation ID
     * @return list of notes ordered by creation time
     */
    @Query("SELECT n FROM ConversationNote n WHERE n.conversation.id = :conversationId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<ConversationNote> findAllByConversationId(@Param("conversationId") String conversationId);

    /**
     * Find notes by conversation and agent ID.
     *
     * @param conversationId the conversation ID
     * @param agentId the agent ID
     * @param pageable pagination information
     * @return page of notes created by the agent
     */
    @Query("SELECT n FROM ConversationNote n WHERE n.conversation.id = :conversationId AND n.agentId = :agentId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<ConversationNote> findByConversationIdAndAgentId(
        @Param("conversationId") String conversationId,
        @Param("agentId") String agentId,
        Pageable pageable
    );

    /**
     * Search notes by content.
     *
     * @param conversationId the conversation ID
     * @param searchText the text to search for
     * @param pageable pagination information
     * @return page of matching notes
     */
    @Query("SELECT n FROM ConversationNote n WHERE n.conversation.id = :conversationId AND LOWER(n.content) LIKE LOWER(CONCAT('%', :searchText, '%')) AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<ConversationNote> searchByContent(
        @Param("conversationId") String conversationId,
        @Param("searchText") String searchText,
        Pageable pageable
    );

    /**
     * Find notes within a date range.
     *
     * @param conversationId the conversation ID
     * @param startTime the start time
     * @param endTime the end time
     * @param pageable pagination information
     * @return page of notes within the date range
     */
    @Query("SELECT n FROM ConversationNote n WHERE n.conversation.id = :conversationId AND n.createdAt >= :startTime AND n.createdAt <= :endTime AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<ConversationNote> findByConversationIdAndTimeRange(
        @Param("conversationId") String conversationId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        Pageable pageable
    );

    /**
     * Count notes for a conversation.
     *
     * @param conversationId the conversation ID
     * @return count of non-deleted notes
     */
    @Query("SELECT COUNT(n) FROM ConversationNote n WHERE n.conversation.id = :conversationId AND n.deletedAt IS NULL")
    long countByConversationId(@Param("conversationId") String conversationId);

    /**
     * Count notes created by an agent.
     *
     * @param agentId the agent ID
     * @return count of notes created by the agent
     */
    @Query("SELECT COUNT(n) FROM ConversationNote n WHERE n.agentId = :agentId AND n.deletedAt IS NULL")
    long countByAgentId(@Param("agentId") String agentId);

    /**
     * Find the most recent note for a conversation.
     *
     * @param conversationId the conversation ID
     * @return Optional containing the most recent note
     */
    @Query(value = "SELECT n FROM ConversationNote n WHERE n.conversation.id = :conversationId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC LIMIT 1")
    Optional<ConversationNote> findLatestByConversationId(@Param("conversationId") String conversationId);

    /**
     * Delete all notes for a conversation (soft delete).
     *
     * @param conversationId the conversation ID
     */
    @Query("UPDATE ConversationNote n SET n.deletedAt = CURRENT_TIMESTAMP WHERE n.conversation.id = :conversationId AND n.deletedAt IS NULL")
    void softDeleteByConversationId(@Param("conversationId") String conversationId);
}
