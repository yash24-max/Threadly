package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for Metric data transfer.
 * Represents computed metrics from analytics events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("org_id")
    private String orgId;

    @JsonProperty("metric_name")
    private String metricName;

    @JsonProperty("bot_id")
    private String botId;

    @JsonProperty("value")
    private Double value;

    @JsonProperty("tags")
    private Map<String, String> tags;

    @JsonProperty("metric_timestamp")
    private Instant metricTimestamp;

    @JsonProperty("created_at")
    private Instant createdAt;

}
