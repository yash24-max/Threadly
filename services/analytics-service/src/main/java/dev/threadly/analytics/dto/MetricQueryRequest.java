package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for custom metric query requests.
 * Allows flexible filtering and aggregation of metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricQueryRequest {

    @JsonProperty("metric_names")
    private List<String> metricNames;

    @JsonProperty("bot_ids")
    private List<String> botIds;

    @JsonProperty("start_time")
    private Instant startTime;

    @JsonProperty("end_time")
    private Instant endTime;

    @JsonProperty("aggregation")
    private String aggregation; // NONE, AVG, SUM, MIN, MAX, COUNT

    @JsonProperty("period")
    private String period; // 1h, 1d, 1w, 1m

    @JsonProperty("filters")
    private Map<String, Object> filters;

    @JsonProperty("limit")
    @Builder.Default
    private Integer limit = 1000;

    @JsonProperty("offset")
    @Builder.Default
    private Integer offset = 0;

}
