package dev.threadly.flow.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Event published when a flow is created.
 * Used for downstream event processing and notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowCreatedEvent {

  @JsonProperty("event_type")
  private String eventType = "flow.created";

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("bot_id")
  private String botId;

  @JsonProperty("org_id")
  private String orgId;

  @JsonProperty("flow_name")
  private String flowName;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("timestamp")
  private Long timestamp = System.currentTimeMillis();
}
