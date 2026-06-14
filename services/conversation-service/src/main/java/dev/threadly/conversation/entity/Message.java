package dev.threadly.conversation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;

import java.time.Instant;

/**
 * Message entity representing a single message within a conversation.
 * Messages are immutable after creation - only soft delete is allowed.
 */
@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(name = "idx_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_sender", columnList = "sender"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_conversation_created", columnList = "conversation_id,created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SoftDelete
public class Message {
    /**
     * Unique message identifier (UUID).
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * Reference to the parent conversation.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "conversation_id", nullable = false)
    @ToString.Exclude
    private Conversation conversation;

    /**
     * Message sender type: visitor, ai, human.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MessageSender sender;

    /**
     * ID of the sender (visitor_id, agent_id, or system).
     */
    @Column(length = 36)
    private String senderId;

    /**
     * Message content/text.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * JSON metadata field (e.g., attachments, rich content, sentiment).
     */
    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    /**
     * Number of tokens used by this message (for AI usage tracking).
     */
    @Column
    private Long tokensUsed;

    /**
     * Timestamp when the message was created.
     */
    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Timestamp when the message was soft-deleted (only for admins).
     */
    @Column
    private Instant deletedAt;

    /**
     * Message sender type enumeration.
     */
    public enum MessageSender {
        VISITOR,
        AI,
        HUMAN
    }
}
