package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Request DTO for updating an existing flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFlowRequest {

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("status")
  private String status;
}
