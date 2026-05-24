package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for AnalyticsEvent data transfer.
 * Represents raw analytics events captured from domain events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEventDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("org_id")
    private String orgId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("bot_id")
    private String botId;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("event_data")
    private Map<String, Object> eventData;

    @JsonProperty("event_timestamp")
    private Instant eventTimestamp;

    @JsonProperty("created_at")
    private Instant createdAt;

}
