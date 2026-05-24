package dev.threadly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * User-created custom dashboard view.
 * Allows users to configure and save personalized analytics dashboards
 * with custom widget selections and layout configurations.
 */
@Entity
@Table(
    name = "dashboard_views",
    schema = "analytics_service",
    indexes = {
        @Index(name = "idx_dashboard_org", columnList = "org_id"),
        @Index(name = "idx_dashboard_created", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardView {

    /**
     * Unique dashboard view identifier (UUID).
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
     * Dashboard view name provided by the user.
     */
    @Column(nullable = false, length = 200)
    private String viewName;

    /**
     * Optional description of the dashboard view.
     */
    @Column(length = 500)
    private String description;

    /**
     * JSON-serialized widget configurations.
     * Example structure:
     * {
     *   "widgets": [
     *     {"id": "w1", "type": "kpi", "metric": "messages_count", "title": "Total Messages"},
     *     {"id": "w2", "type": "trend", "metric": "avg_response_time", "period": "30days"}
     *   ]
     * }
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String widgetsJson;

    /**
     * JSON-serialized filter configurations.
     * Example: {"botIds": ["bot1", "bot2"], "dateRange": "last_30_days"}
     */
    @Column(columnDefinition = "TEXT")
    private String filtersJson;

    /**
     * Whether this is a default/system dashboard.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Dashboard creation timestamp.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Last time this dashboard was viewed.
     */
    @Column
    private Instant lastViewedAt;

    /**
     * Last update timestamp.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

}
