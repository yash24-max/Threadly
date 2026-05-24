package dev.threadly.flow.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Event published when a flow is published.
 * Notifies other services that a flow version is now live.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowPublishedEvent {

  @JsonProperty("event_type")
  private String eventType = "flow.published";

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("version_number")
  private Integer versionNumber;

  @JsonProperty("version_id")
  private String versionId;

  @JsonProperty("org_id")
  private String orgId;

  @JsonProperty("bot_id")
  private String botId;

  @JsonProperty("published_by")
  private String publishedBy;

  @JsonProperty("previous_version_id")
  private String previousVersionId;

  @JsonProperty("published_at")
  private LocalDateTime publishedAt;

  @JsonProperty("timestamp")
  private Long timestamp = System.currentTimeMillis();
}
