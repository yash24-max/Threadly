package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.threadly.flow.entity.FlowEdge;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for FlowEdge entity.
 * Used for API responses containing edge information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowEdgeDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("edge_id")
  private String edgeId;

  @JsonProperty("source_node_id")
  private String sourceNodeId;

  @JsonProperty("target_node_id")
  private String targetNodeId;

  @JsonProperty("source_handle")
  private String sourceHandle;

  @JsonProperty("target_handle")
  private String targetHandle;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  /**
   * Converts a FlowEdge entity to a DTO.
   *
   * @param edge the flow edge entity
   * @return the flow edge DTO
   */
  public static FlowEdgeDto fromEntity(FlowEdge edge) {
    return FlowEdgeDto.builder()
        .id(edge.getId())
        .flowId(edge.getFlowId())
        .edgeId(edge.getEdgeId())
        .sourceNodeId(edge.getSourceNodeId())
        .targetNodeId(edge.getTargetNodeId())
        .sourceHandle(edge.getSourceHandle())
        .targetHandle(edge.getTargetHandle())
        .createdAt(edge.getCreatedAt())
        .updatedAt(edge.getUpdatedAt())
        .build();
  }
}
