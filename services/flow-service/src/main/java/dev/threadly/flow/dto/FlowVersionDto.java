package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.threadly.flow.entity.FlowVersion;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for FlowVersion entity.
 * Used for API responses containing version information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowVersionDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("version_number")
  private Integer versionNumber;

  @JsonProperty("definition")
  private String definitionJson;

  @JsonProperty("is_active")
  private Boolean isActive;

  @JsonProperty("published_at")
  private LocalDateTime publishedAt;

  @JsonProperty("published_by")
  private String publishedBy;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  /**
   * Converts a FlowVersion entity to a DTO.
   *
   * @param version the flow version entity
   * @return the flow version DTO
   */
  public static FlowVersionDto fromEntity(FlowVersion version) {
    return FlowVersionDto.builder()
        .id(version.getId())
        .flowId(version.getFlowId())
        .versionNumber(version.getVersionNumber())
        .definitionJson(version.getDefinitionJson())
        .isActive(version.getIsActive())
        .publishedAt(version.getPublishedAt())
        .publishedBy(version.getPublishedBy())
        .createdAt(version.getCreatedAt())
        .build();
  }
}
