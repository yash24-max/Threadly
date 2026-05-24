package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Request DTO for updating an existing node in a flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFlowNodeRequest {

  @JsonProperty("position_x")
  private Double positionX;

  @JsonProperty("position_y")
  private Double positionY;

  @JsonProperty("data")
  private String dataJson;
}
