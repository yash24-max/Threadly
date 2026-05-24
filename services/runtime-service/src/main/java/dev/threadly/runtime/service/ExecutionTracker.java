package dev.threadly.runtime.service;

import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.model.ExecutionLog;
import dev.threadly.runtime.repository.ExecutionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ExecutionTracker logs node executions for debugging, monitoring, and audit trails.
 * Tracks execution times, status, inputs, outputs, and errors.
 */
@Service
@Slf4j
public class ExecutionTracker {

  @Autowired
  private ExecutionLogRepository executionLogRepository;

  /**
   * Log a node execution
   */
  @Transactional
  public void logExecution(String sessionId, String nodeId, String nodeType,
                          Object input, ExecutionResult result) {
    try {
      ExecutionLog executionLog = ExecutionLog.builder()
          .id(UUID.randomUUID().toString())
          .sessionId(sessionId)
          .nodeId(nodeId)
          .nodeType(nodeType)
          .inputJson(serializeInput(input))
          .outputJson(serializeOutput(result))
          .executionTimeMs(result.getExecutionTimeMs() != null ? result.getExecutionTimeMs() : 0)
          .status(mapStatus(result.getStatus()))
          .errorDetails(result.getErrorMessage())
          .build();

      executionLogRepository.save(executionLog);
      log.debug("Logged execution for node {} in session {}", nodeId, sessionId);

    } catch (Exception e) {
      log.error("Failed to log execution", e);
    }
  }

  /**
   * Get average execution time for a node
   */
  public Long getAverageExecutionTime(String nodeId) {
    return executionLogRepository.calculateAverageExecutionTime(nodeId);
  }

  /**
   * Serialize input to JSON string
   */
  private String serializeInput(Object input) {
    if (input == null) {
      return null;
    }
    try {
      return input.toString();
    } catch (Exception e) {
      log.warn("Failed to serialize input", e);
      return null;
    }
  }

  /**
   * Serialize execution result to JSON string
   */
  private String serializeOutput(ExecutionResult result) {
    if (result == null) {
      return null;
    }
    try {
      // Simple serialization - could use ObjectMapper for complex objects
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("\"status\":\"").append(result.getStatus()).append("\"");
      if (result.getNextNodeId() != null) {
        sb.append(",\"nextNodeId\":\"").append(result.getNextNodeId()).append("\"");
      }
      sb.append("}");
      return sb.toString();
    } catch (Exception e) {
      log.warn("Failed to serialize output", e);
      return null;
    }
  }

  /**
   * Map ExecutionResult status to ExecutionLog status
   */
  private ExecutionLog.ExecutionLogStatus mapStatus(ExecutionResult.ExecutionStatus status) {
    if (status == null) {
      return ExecutionLog.ExecutionLogStatus.FAILURE;
    }

    return switch (status) {
      case SUCCESS -> ExecutionLog.ExecutionLogStatus.SUCCESS;
      case FAILURE -> ExecutionLog.ExecutionLogStatus.FAILURE;
      case TIMEOUT -> ExecutionLog.ExecutionLogStatus.TIMEOUT;
      case PAUSED -> ExecutionLog.ExecutionLogStatus.SKIPPED;
      case SKIPPED -> ExecutionLog.ExecutionLogStatus.SKIPPED;
    };
  }
}
