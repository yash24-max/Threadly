package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.threadly.flow.entity.FlowNode;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for FlowNode entity.
 * Used for API responses containing node information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowNodeDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("node_id")
  private String nodeId;

  @JsonProperty("type")
  private String type;

  @JsonProperty("position_x")
  private Double positionX;

  @JsonProperty("position_y")
  private Double positionY;

  @JsonProperty("data")
  private String dataJson;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  /**
   * Converts a FlowNode entity to a DTO.
   *
   * @param node the flow node entity
   * @return the flow node DTO
   */
  public static FlowNodeDto fromEntity(FlowNode node) {
    return FlowNodeDto.builder()
        .id(node.getId())
        .flowId(node.getFlowId())
        .nodeId(node.getNodeId())
        .type(node.getType())
        .positionX(node.getPositionX())
        .positionY(node.getPositionY())
        .dataJson(node.getDataJson())
        .createdAt(node.getCreatedAt())
        .updatedAt(node.getUpdatedAt())
        .build();
  }
}
