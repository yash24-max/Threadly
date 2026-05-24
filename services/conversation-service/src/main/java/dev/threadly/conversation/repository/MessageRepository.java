package dev.threadly.conversation.repository;

import dev.threadly.conversation.entity.Message;
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
 * Repository interface for Message entity.
 * Provides database access and query methods for conversation messages.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * Find a message by ID.
     *
     * @param id the message ID
     * @return Optional containing the message if found and not deleted
     */
    @Query("SELECT m FROM Message m WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Message> findById(@Param("id") String id);

    /**
     * Find all messages in a conversation with pagination.
     *
     * @param conversationId the conversation ID
     * @param pageable pagination information
     * @return page of messages ordered by creation time
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL ORDER BY m.createdAt ASC")
    Page<Message> findByConversationId(@Param("conversationId") String conversationId, Pageable pageable);

    /**
     * Find all messages in a conversation (no pagination).
     *
     * @param conversationId the conversation ID
     * @return list of messages ordered by creation time
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL ORDER BY m.createdAt ASC")
    List<Message> findAllByConversationId(@Param("conversationId") String conversationId);

    /**
     * Find messages by conversation and sender type.
     *
     * @param conversationId the conversation ID
     * @param sender the sender type
     * @param pageable pagination information
     * @return page of messages from the specified sender
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sender = :sender AND m.deletedAt IS NULL ORDER BY m.createdAt ASC")
    Page<Message> findByConversationIdAndSender(
        @Param("conversationId") String conversationId,
        @Param("sender") Message.MessageSender sender,
        Pageable pageable
    );

    /**
     * Search messages by content (full-text search simulation).
     *
     * @param conversationId the conversation ID
     * @param searchText the text to search for
     * @param pageable pagination information
     * @return page of matching messages
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchText, '%')) AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    Page<Message> searchByContent(
        @Param("conversationId") String conversationId,
        @Param("searchText") String searchText,
        Pageable pageable
    );

    /**
     * Find messages within a date range.
     *
     * @param conversationId the conversation ID
     * @param startTime the start time
     * @param endTime the end time
     * @param pageable pagination information
     * @return page of messages within the date range
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.createdAt >= :startTime AND m.createdAt <= :endTime AND m.deletedAt IS NULL ORDER BY m.createdAt ASC")
    Page<Message> findByConversationIdAndTimeRange(
        @Param("conversationId") String conversationId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        Pageable pageable
    );

    /**
     * Count messages in a conversation.
     *
     * @param conversationId the conversation ID
     * @return count of non-deleted messages
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL")
    long countByConversationId(@Param("conversationId") String conversationId);

    /**
     * Sum tokens used in a conversation.
     *
     * @param conversationId the conversation ID
     * @return total tokens used
     */
    @Query("SELECT COALESCE(SUM(m.tokensUsed), 0) FROM Message m WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL")
    long sumTokensByConversationId(@Param("conversationId") String conversationId);

    /**
     * Find messages by sender ID.
     *
     * @param conversationId the conversation ID
     * @param senderId the sender ID
     * @param pageable pagination information
     * @return page of messages from the sender
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.senderId = :senderId AND m.deletedAt IS NULL ORDER BY m.createdAt ASC")
    Page<Message> findByConversationIdAndSenderId(
        @Param("conversationId") String conversationId,
        @Param("senderId") String senderId,
        Pageable pageable
    );

    /**
     * Find the last message in a conversation.
     *
     * @param conversationId the conversation ID
     * @return Optional containing the last message if exists
     */
    @Query(value = "SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLastMessageByConversationId(@Param("conversationId") String conversationId);

    /**
     * Delete all messages in a conversation (soft delete).
     *
     * @param conversationId the conversation ID
     */
    @Query("UPDATE Message m SET m.deletedAt = CURRENT_TIMESTAMP WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL")
    void softDeleteAllByConversationId(@Param("conversationId") String conversationId);
}
