package dev.threadly.conversation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;

import java.time.Instant;

/**
 * ConversationNote entity for internal agent notes on conversations.
 * These are not visible to visitors, only to support agents.
 */
@Entity
@Table(
    name = "conversation_notes",
    indexes = {
        @Index(name = "idx_conversation_id_notes", columnList = "conversation_id"),
        @Index(name = "idx_agent_id", columnList = "agent_id"),
        @Index(name = "idx_created_at_notes", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SoftDelete
public class ConversationNote {
    /**
     * Unique note identifier (UUID).
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * Reference to the conversation this note belongs to.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "conversation_id", nullable = false)
    @ToString.Exclude
    private Conversation conversation;

    /**
     * ID of the agent who created this note.
     */
    @Column(nullable = false, length = 36)
    private String agentId;

    /**
     * The note content/text.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Timestamp when the note was created.
     */
    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Timestamp when the note was soft-deleted.
     */
    @Column
    private Instant deletedAt;
}
