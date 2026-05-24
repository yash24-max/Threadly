package dev.threadly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Daily aggregated metrics for efficient dashboard queries.
 * Pre-computed rollups of key metrics on a daily basis to support fast analytics queries.
 * Enables cost-effective retention and querying of historical data.
 */
@Entity
@Table(
    name = "daily_rollups",
    schema = "analytics_service",
    indexes = {
        @Index(name = "idx_rollup_org_date", columnList = "org_id,rollup_date"),
        @Index(name = "idx_rollup_bot_date", columnList = "bot_id,rollup_date"),
        @Index(name = "idx_rollup_date", columnList = "rollup_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRollup {

    /**
     * Unique rollup record identifier (UUID).
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
     * Bot/Flow instance ID associated with this rollup.
     */
    @Column(nullable = false, length = 36)
    private String botId;

    /**
     * Rollup date (the day these metrics represent).
     */
    @Column(nullable = false)
    private LocalDate rollupDate;

    /**
     * Total number of conversations started on this date.
     */
    @Column(nullable = false)
    @Builder.Default
    private Long conversationsCount = 0L;

    /**
     * Total number of messages processed on this date.
     */
    @Column(nullable = false)
    @Builder.Default
    private Long messagesCount = 0L;

    /**
     * Total number of AI reply requests on this date.
     */
    @Column(nullable = false)
    @Builder.Default
    private Long aiCallsCount = 0L;

    /**
     * Average response time in milliseconds.
     */
    @Column(nullable = false)
    @Builder.Default
    private Double avgResponseTimeMs = 0.0;

    /**
     * Conversation resolution rate (0-100 percent).
     */
    @Column(nullable = false)
    @Builder.Default
    private Double resolutionRate = 0.0;

    /**
     * Average CSAT score (1-5 scale).
     */
    @Column
    @Builder.Default
    private Double avgCsatScore = 0.0;

    /**
     * Total tokens consumed by AI calls.
     */
    @Column(nullable = false)
    @Builder.Default
    private Long totalTokensConsumed = 0L;

    /**
     * Total estimated cost of AI calls in cents (USD).
     */
    @Column(nullable = false)
    @Builder.Default
    private Long totalCostCents = 0L;

    /**
     * Number of handoffs to human agents.
     */
    @Column(nullable = false)
    @Builder.Default
    private Long handoffsCount = 0L;

    /**
     * Record creation timestamp.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Last update timestamp.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

}
