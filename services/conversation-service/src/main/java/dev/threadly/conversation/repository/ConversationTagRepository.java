package dev.threadly.conversation.repository;

import dev.threadly.conversation.entity.ConversationTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ConversationTag entity.
 * Provides database access for conversation tagging functionality.
 */
@Repository
public interface ConversationTagRepository extends JpaRepository<ConversationTag, String> {

    /**
     * Find all tags for a conversation.
     *
     * @param conversationId the conversation ID
     * @return list of tags
     */
    @Query("SELECT t FROM ConversationTag t WHERE t.conversation.id = :conversationId ORDER BY t.createdAt DESC")
    List<ConversationTag> findByConversationId(@Param("conversationId") String conversationId);

    /**
     * Find tags by name for a conversation.
     *
     * @param conversationId the conversation ID
     * @param tagName the tag name/key
     * @return list of tags with the specified name
     */
    @Query("SELECT t FROM ConversationTag t WHERE t.conversation.id = :conversationId AND t.tagName = :tagName ORDER BY t.createdAt DESC")
    List<ConversationTag> findByConversationIdAndTagName(
        @Param("conversationId") String conversationId,
        @Param("tagName") String tagName
    );

    /**
     * Find a specific tag by conversation and tag name/value.
     *
     * @param conversationId the conversation ID
     * @param tagName the tag name
     * @param tagValue the tag value
     * @return Optional containing the tag if found
     */
    @Query("SELECT t FROM ConversationTag t WHERE t.conversation.id = :conversationId AND t.tagName = :tagName AND t.tagValue = :tagValue")
    Optional<ConversationTag> findByConversationIdAndTag(
        @Param("conversationId") String conversationId,
        @Param("tagName") String tagName,
        @Param("tagValue") String tagValue
    );

    /**
     * Find all conversations with a specific tag.
     *
     * @param tagName the tag name
     * @param tagValue the tag value
     * @param pageable pagination information
     * @return page of tag records
     */
    @Query("SELECT t FROM ConversationTag t WHERE t.tagName = :tagName AND t.tagValue = :tagValue ORDER BY t.createdAt DESC")
    Page<ConversationTag> findByTag(
        @Param("tagName") String tagName,
        @Param("tagValue") String tagValue,
        Pageable pageable
    );

    /**
     * Check if a conversation has a specific tag.
     *
     * @param conversationId the conversation ID
     * @param tagName the tag name
     * @param tagValue the tag value
     * @return true if the tag exists
     */
    @Query("SELECT COUNT(t) > 0 FROM ConversationTag t WHERE t.conversation.id = :conversationId AND t.tagName = :tagName AND t.tagValue = :tagValue")
    boolean existsByConversationIdAndTag(
        @Param("conversationId") String conversationId,
        @Param("tagName") String tagName,
        @Param("tagValue") String tagValue
    );

    /**
     * Count tags for a conversation.
     *
     * @param conversationId the conversation ID
     * @return count of tags
     */
    @Query("SELECT COUNT(t) FROM ConversationTag t WHERE t.conversation.id = :conversationId")
    long countByConversationId(@Param("conversationId") String conversationId);

    /**
     * Delete all tags for a conversation.
     *
     * @param conversationId the conversation ID
     */
    @Query("DELETE FROM ConversationTag t WHERE t.conversation.id = :conversationId")
    void deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * Find unique tag names used in an organization.
     *
     * @param conversationIds list of conversation IDs
     * @return list of unique tag names
     */
    @Query("SELECT DISTINCT t.tagName FROM ConversationTag t WHERE t.conversation.id IN :conversationIds ORDER BY t.tagName")
    List<String> findDistinctTagNamesByConversationIds(@Param("conversationIds") List<String> conversationIds);
}
