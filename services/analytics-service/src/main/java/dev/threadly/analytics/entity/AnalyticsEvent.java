package dev.threadly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Represents a raw analytics event captured from domain events.
 * Stores event details in JSON format for flexible event tracking.
 * Designed for time-series queries with efficient retention policies.
 */
@Entity
@Table(
    name = "analytics_events",
    schema = "analytics_service",
    indexes = {
        @Index(name = "idx_org_id_timestamp", columnList = "org_id,created_at"),
        @Index(name = "idx_bot_id_timestamp", columnList = "bot_id,created_at"),
        @Index(name = "idx_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_session_id", columnList = "session_id"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEvent {

    /**
     * Unique event identifier (UUID).
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
     * Event type enumeration (e.g., CONVERSATION_STARTED, MESSAGE_ADDED, AI_REPLY_REQUESTED).
     */
    @Column(nullable = false, length = 50)
    private String eventType;

    /**
     * Bot/Flow instance ID associated with this event.
     */
    @Column(nullable = false, length = 36)
    private String botId;

    /**
     * Conversation ID associated with this event (optional, may be null for system events).
     */
    @Column(length = 36)
    private String conversationId;

    /**
     * Session ID for visitor session tracking.
     */
    @Column(length = 36)
    private String sessionId;

    /**
     * JSON-serialized event data containing event-specific attributes.
     * Stored as TEXT to support variable payload sizes.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String eventDataJson;

    /**
     * Event timestamp (when the event occurred in the domain).
     */
    @Column(nullable = false)
    private Instant eventTimestamp;

    /**
     * Record creation timestamp (when this event was stored in the system).
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

}
