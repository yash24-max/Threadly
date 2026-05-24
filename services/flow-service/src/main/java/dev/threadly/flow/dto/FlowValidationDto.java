package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.threadly.flow.entity.FlowValidation;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for FlowValidation entity.
 * Used for API responses containing validation status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowValidationDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("is_valid")
  private Boolean isValid;

  @JsonProperty("errors")
  private List<ValidationErrorDto> errors;

  @JsonProperty("last_validated_at")
  private LocalDateTime lastValidatedAt;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  /**
   * Converts a FlowValidation entity to a DTO.
   *
   * @param validation the flow validation entity
   * @return the flow validation DTO
   */
  public static FlowValidationDto fromEntity(FlowValidation validation) {
    return FlowValidationDto.builder()
        .id(validation.getId())
        .flowId(validation.getFlowId())
        .isValid(validation.getIsValid())
        .lastValidatedAt(validation.getLastValidatedAt())
        .createdAt(validation.getCreatedAt())
        .build();
  }
}
