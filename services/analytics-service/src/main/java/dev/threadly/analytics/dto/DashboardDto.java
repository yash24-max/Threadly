package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for DashboardView data transfer.
 * Represents user-created custom dashboards with widget configurations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("org_id")
    private String orgId;

    @JsonProperty("view_name")
    private String viewName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("widgets")
    private List<DashboardWidget> widgets;

    @JsonProperty("filters")
    private Map<String, Object> filters;

    @JsonProperty("is_default")
    private Boolean isDefault;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("last_viewed_at")
    private Instant lastViewedAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    /**
     * Configuration for a single dashboard widget.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardWidget {
        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private String type; // kpi, trend, chart, table

        @JsonProperty("metric")
        private String metric;

        @JsonProperty("title")
        private String title;

        @JsonProperty("config")
        private Map<String, Object> config;
    }

}
