package dev.threadly.conversation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

/**
 * Conversation entity representing a conversation between a visitor and the bot/agent.
 * Tracks conversation metadata, status, and messages.
 */
@Entity
@Table(
    name = "conversations",
    indexes = {
        @Index(name = "idx_org_id", columnList = "org_id"),
        @Index(name = "idx_bot_id", columnList = "bot_id"),
        @Index(name = "idx_visitor_id", columnList = "visitor_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_started_at", columnList = "started_at"),
        @Index(name = "idx_org_status", columnList = "org_id,status"),
        @Index(name = "idx_org_visitor", columnList = "org_id,visitor_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SoftDelete
public class Conversation {
    /**
     * Unique conversation identifier (UUID).
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * Organization ID for multi-tenancy isolation.
     */
    @Column(nullable = false, length = 36)
    private String orgId;

    /**
     * Bot/Flow instance ID that initiated this conversation.
     */
    @Column(nullable = false, length = 36)
    private String botId;

    /**
     * Flow configuration ID for this conversation.
     */
    @Column(length = 36)
    private String flowId;

    /**
     * Visitor/Session ID associated with this conversation.
     */
    @Column(nullable = false, length = 36)
    private String visitorId;

    /**
     * Conversation status: open, closed, handed_off.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ConversationStatus status;

    /**
     * ID of the agent this conversation is assigned to (if handed off).
     */
    @Column(length = 36)
    private String assignedAgentId;

    /**
     * Total number of messages in this conversation.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer messageCount = 0;

    /**
     * Total tokens used in this conversation (for billing/analytics).
     */
    @Column(nullable = false)
    @Builder.Default
    private Long tokensUsed = 0L;

    /**
     * JSON metadata field for conversation-specific data.
     */
    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    /**
     * Timestamp when the conversation started.
     */
    @CreationTimestamp
    @Column(nullable = false)
    private Instant startedAt;

    /**
     * Timestamp when the conversation ended (closed or handed off).
     */
    @Column
    private Instant endedAt;

    /**
     * Timestamp when the conversation was soft-deleted.
     */
    @Column
    private Instant deletedAt;

    /**
     * Last update timestamp.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * One-to-many relationship with messages.
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Message> messages;

    /**
     * One-to-many relationship with conversation tags.
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ConversationTag> tags;

    /**
     * One-to-many relationship with conversation notes.
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ConversationNote> notes;

    /**
     * Conversation status enumeration.
     */
    public enum ConversationStatus {
        OPEN,
        CLOSED,
        HANDED_OFF
    }
}
