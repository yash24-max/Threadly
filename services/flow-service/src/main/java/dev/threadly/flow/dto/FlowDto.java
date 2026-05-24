package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.threadly.flow.entity.Flow;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Flow entity.
 * Used for API responses containing flow information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("bot_id")
  private String botId;

  @JsonProperty("org_id")
  private String orgId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("status")
  private String status;

  @JsonProperty("current_version_id")
  private String currentVersionId;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  /**
   * Converts a Flow entity to a DTO.
   *
   * @param flow the flow entity
   * @return the flow DTO
   */
  public static FlowDto fromEntity(Flow flow) {
    return FlowDto.builder()
        .id(flow.getId())
        .botId(flow.getBotId())
        .orgId(flow.getOrgId())
        .name(flow.getName())
        .description(flow.getDescription())
        .status(flow.getStatus().toString())
        .currentVersionId(flow.getCurrentVersionId())
        .createdBy(flow.getCreatedBy())
        .createdAt(flow.getCreatedAt())
        .updatedAt(flow.getUpdatedAt())
        .build();
  }
}
