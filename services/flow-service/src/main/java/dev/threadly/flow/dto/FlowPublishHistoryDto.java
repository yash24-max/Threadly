package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.threadly.flow.entity.FlowPublishLog;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for FlowPublishLog entity.
 * Used for API responses containing publish history information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowPublishHistoryDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("event_type")
  private String eventType;

  @JsonProperty("published_by")
  private String publishedBy;

  @JsonProperty("previous_version_id")
  private String previousVersionId;

  @JsonProperty("current_version_id")
  private String currentVersionId;

  @JsonProperty("rollback_reason")
  private String rollbackReason;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  /**
   * Converts a FlowPublishLog entity to a DTO.
   *
   * @param log the flow publish log entity
   * @return the flow publish history DTO
   */
  public static FlowPublishHistoryDto fromEntity(FlowPublishLog log) {
    return FlowPublishHistoryDto.builder()
        .id(log.getId())
        .flowId(log.getFlowId())
        .eventType(log.getEventType().toString())
        .publishedBy(log.getPublishedBy())
        .previousVersionId(log.getPreviousVersionId())
        .currentVersionId(log.getCurrentVersionId())
        .rollbackReason(log.getRollbackReason())
        .createdAt(log.getCreatedAt())
        .build();
  }
}
