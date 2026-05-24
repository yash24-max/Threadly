package dev.threadly.conversation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Lead entity capturing prospect information from conversations.
 * Extracted from conversation content or explicitly provided.
 */
@Entity
@Table(
    name = "leads",
    indexes = {
        @Index(name = "idx_org_id_leads", columnList = "org_id"),
        @Index(name = "idx_conversation_id_leads", columnList = "conversation_id"),
        @Index(name = "idx_visitor_id_leads", columnList = "visitor_id"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_phone", columnList = "phone"),
        @Index(name = "idx_status_leads", columnList = "status"),
        @Index(name = "idx_org_status_leads", columnList = "org_id,status"),
        @Index(name = "idx_captured_at", columnList = "captured_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SoftDelete
public class Lead {
    /**
     * Unique lead identifier (UUID).
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
     * Reference to the source conversation.
     */
    @Column(nullable = false, length = 36)
    private String conversationId;

    /**
     * Visitor ID that this lead is associated with.
     */
    @Column(length = 36)
    private String visitorId;

    /**
     * Lead email address.
     */
    @Column(length = 255)
    private String email;

    /**
     * Lead phone number.
     */
    @Column(length = 20)
    private String phone;

    /**
     * Lead full name.
     */
    @Column(length = 255)
    private String name;

    /**
     * Lead company name (if available).
     */
    @Column(length = 255)
    private String company;

    /**
     * Custom fields as JSON (flexible schema for additional lead data).
     */
    @Column(columnDefinition = "TEXT")
    private String customFieldsJson;

    /**
     * Lead status: new, contacted, converted, lost, duplicate.
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    /**
     * Lead quality score (0-100).
     */
    @Column
    private Integer qualityScore;

    /**
     * Timestamp when the lead was first captured.
     */
    @CreationTimestamp
    @Column(nullable = false)
    private Instant capturedAt;

    /**
     * Timestamp when the lead status was last updated.
     */
    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    /**
     * Timestamp when the lead was soft-deleted.
     */
    @Column
    private Instant deletedAt;

    /**
     * Lead status enumeration.
     */
    public enum LeadStatus {
        NEW,
        CONTACTED,
        CONVERTED,
        LOST,
        DUPLICATE
    }
}
