package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for creating an edge between nodes in a flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlowEdgeRequest {

  @NotBlank(message = "Edge ID is required")
  @JsonProperty("edge_id")
  private String edgeId;

  @NotBlank(message = "Source node ID is required")
  @JsonProperty("source_node_id")
  private String sourceNodeId;

  @NotBlank(message = "Target node ID is required")
  @JsonProperty("target_node_id")
  private String targetNodeId;

  @JsonProperty("source_handle")
  private String sourceHandle;

  @JsonProperty("target_handle")
  private String targetHandle;
}
