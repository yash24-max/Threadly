package dev.threadly.flow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.threadly.flow.entity.FlowExecutionLog;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for FlowExecutionLog entity.
 * Used for API responses containing execution log information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowExecutionLogDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("flow_id")
  private String flowId;

  @JsonProperty("session_id")
  private String sessionId;

  @JsonProperty("node_id")
  private String nodeId;

  @JsonProperty("status")
  private String status;

  @JsonProperty("execution_time_ms")
  private Long executionTimeMs;

  @JsonProperty("error_message")
  private String errorMessage;

  @JsonProperty("input_data")
  private String inputDataJson;

  @JsonProperty("output_data")
  private String outputDataJson;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  /**
   * Converts a FlowExecutionLog entity to a DTO.
   *
   * @param log the flow execution log entity
   * @return the flow execution log DTO
   */
  public static FlowExecutionLogDto fromEntity(FlowExecutionLog log) {
    return FlowExecutionLogDto.builder()
        .id(log.getId())
        .flowId(log.getFlowId())
        .sessionId(log.getSessionId())
        .nodeId(log.getNodeId())
        .status(log.getStatus().toString())
        .executionTimeMs(log.getExecutionTimeMs())
        .errorMessage(log.getErrorMessage())
        .inputDataJson(log.getInputDataJson())
        .outputDataJson(log.getOutputDataJson())
        .createdAt(log.getCreatedAt())
        .build();
  }
}
