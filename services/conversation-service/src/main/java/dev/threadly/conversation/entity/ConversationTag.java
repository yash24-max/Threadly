package dev.threadly.conversation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * ConversationTag entity for tagging conversations with custom labels.
 * Supports key-value metadata for flexible tagging.
 */
@Entity
@Table(
    name = "conversation_tags",
    indexes = {
        @Index(name = "idx_conversation_id_tags", columnList = "conversation_id"),
        @Index(name = "idx_tag_name", columnList = "tag_name"),
        @Index(name = "idx_tag_value", columnList = "tag_value")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationTag {
    /**
     * Unique tag identifier (UUID).
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * Reference to the conversation being tagged.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "conversation_id", nullable = false)
    @ToString.Exclude
    private Conversation conversation;

    /**
     * Tag name/key (e.g., "priority", "category", "segment").
     */
    @Column(nullable = false, length = 100)
    private String tagName;

    /**
     * Tag value (e.g., "high", "sales", "enterprise").
     */
    @Column(nullable = false, length = 255)
    private String tagValue;

    /**
     * Timestamp when the tag was created.
     */
    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdAt;
}
