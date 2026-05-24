package dev.threadly.flow.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when flow validation fails.
 * Alerts operators about validation issues.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowValidationFailedEvent {

  @JsonProperty("event_type")
  private String eventType = "flow.validation_failed";

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("org_id")
  private String orgId;

  @JsonProperty("bot_id")
  private String botId;

  @JsonProperty("validation_errors")
  private List<String> validationErrors;

  @JsonProperty("error_count")
  private Integer errorCount;

  @JsonProperty("validated_at")
  private LocalDateTime validatedAt;

  @JsonProperty("timestamp")
  private Long timestamp = System.currentTimeMillis();
}
