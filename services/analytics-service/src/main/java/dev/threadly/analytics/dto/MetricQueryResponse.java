package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * DTO for metric query responses.
 * Contains aggregated metric data and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricQueryResponse {

    @JsonProperty("query_id")
    private String queryId;

    @JsonProperty("metric_name")
    private String metricName;

    @JsonProperty("series")
    private List<MetricDataPoint> series;

    @JsonProperty("summary")
    private MetricSummary summary;

    @JsonProperty("total_records")
    private Long totalRecords;

    @JsonProperty("executed_at")
    private Instant executedAt;

    /**
     * Single data point in a metric series.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricDataPoint {
        @JsonProperty("timestamp")
        private Instant timestamp;

        @JsonProperty("value")
        private Double value;

        @JsonProperty("bot_id")
        private String botId;

        @JsonProperty("tags")
        private java.util.Map<String, String> tags;
    }

    /**
     * Summary statistics for metric query.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricSummary {
        @JsonProperty("average")
        private Double average;

        @JsonProperty("minimum")
        private Double minimum;

        @JsonProperty("maximum")
        private Double maximum;

        @JsonProperty("total")
        private Double total;

        @JsonProperty("count")
        private Long count;
    }

}
