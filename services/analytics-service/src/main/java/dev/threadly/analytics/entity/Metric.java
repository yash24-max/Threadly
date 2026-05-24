package dev.threadly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a computed metric from analytics events.
 * Metrics are aggregated from raw events and stored for efficient querying.
 * Supports flexible tagging for segmentation and filtering.
 */
@Entity
@Table(
    name = "metrics",
    schema = "analytics_service",
    indexes = {
        @Index(name = "idx_metric_org_timestamp", columnList = "org_id,metric_name,created_at"),
        @Index(name = "idx_metric_bot_timestamp", columnList = "bot_id,metric_name,created_at"),
        @Index(name = "idx_metric_name", columnList = "metric_name"),
        @Index(name = "idx_metric_timestamp", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Metric {

    /**
     * Unique metric record identifier (UUID).
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
     * Metric name (e.g., messages_count, avg_response_time, csat_score).
     */
    @Column(nullable = false, length = 100)
    private String metricName;

    /**
     * Bot/Flow instance ID associated with this metric.
     */
    @Column(nullable = false, length = 36)
    private String botId;

    /**
     * Metric value (numeric representation).
     */
    @Column(nullable = false)
    private Double value;

    /**
     * JSON-serialized tags for flexible segmentation.
     * Enables querying metrics by channel, region, feature flag, etc.
     * Example: {"channel": "web", "region": "us-east"}
     */
    @Column(columnDefinition = "TEXT")
    private String tagsJson;

    /**
     * Metric timestamp (when this metric was recorded).
     */
    @Column(nullable = false)
    private Instant metricTimestamp;

    /**
     * Record creation timestamp (when this record was persisted).
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

}
