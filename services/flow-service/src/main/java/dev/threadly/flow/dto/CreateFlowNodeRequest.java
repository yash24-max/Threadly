package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for creating a new node in a flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlowNodeRequest {

  @NotBlank(message = "Node ID is required")
  @JsonProperty("node_id")
  private String nodeId;

  @NotBlank(message = "Node type is required")
  @JsonProperty("type")
  private String type;

  @NotNull(message = "Position X is required")
  @JsonProperty("position_x")
  private Double positionX;

  @NotNull(message = "Position Y is required")
  @JsonProperty("position_y")
  private Double positionY;

  @JsonProperty("data")
  private String dataJson;
}
