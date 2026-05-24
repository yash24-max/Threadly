package dev.threadly.flow.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Event published when a flow is rolled back to a previous version.
 * Notifies services about version changes and rollbacks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowRolledBackEvent {

  @JsonProperty("event_type")
  private String eventType = "flow.rolled_back";

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("org_id")
  private String orgId;

  @JsonProperty("bot_id")
  private String botId;

  @JsonProperty("from_version_id")
  private String fromVersionId;

  @JsonProperty("from_version_number")
  private Integer fromVersionNumber;

  @JsonProperty("to_version_id")
  private String toVersionId;

  @JsonProperty("to_version_number")
  private Integer toVersionNumber;

  @JsonProperty("rollback_reason")
  private String rollbackReason;

  @JsonProperty("rolled_back_by")
  private String rolledBackBy;

  @JsonProperty("rolled_back_at")
  private LocalDateTime rolledBackAt;

  @JsonProperty("timestamp")
  private Long timestamp = System.currentTimeMillis();
}
