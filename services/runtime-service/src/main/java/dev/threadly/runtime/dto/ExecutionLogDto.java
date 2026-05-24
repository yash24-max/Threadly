package dev.threadly.runtime.dto;

import dev.threadly.runtime.model.ExecutionLog;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Execution Log DTO for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionLogDto {

  private String id;
  private String sessionId;
  private String nodeId;
  private String nodeType;
  private String inputJson;
  private String outputJson;
  private Long executionTimeMs;
  private ExecutionLog.ExecutionLogStatus status;
  private String errorDetails;
  private LocalDateTime createdAt;
}
