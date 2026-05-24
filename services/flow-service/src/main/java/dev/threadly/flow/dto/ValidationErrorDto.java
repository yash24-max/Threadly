package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO for validation errors.
 * Used to represent individual validation error details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationErrorDto {

  @JsonProperty("code")
  private String code;

  @JsonProperty("message")
  private String message;

  @JsonProperty("field")
  private String field;

  @JsonProperty("details")
  private String details;
}
