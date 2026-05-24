package dev.threadly.flow.service;

import dev.threadly.flow.dto.FlowExecutionLogDto;
import dev.threadly.flow.entity.FlowExecutionLog;
import dev.threadly.flow.repository.FlowExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for Flow Execution tracking and logging.
 * Records execution history for flows and individual nodes.
 * Used for debugging, monitoring, and auditing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowExecutionTracker {

  private final FlowExecutionLogRepository executionLogRepository;

  /**
   * Logs the start of a flow execution.
   *
   * @param flowId the flow ID
   * @param sessionId the execution session ID
   * @return the execution log DTO
   */
  @Transactional
  public FlowExecutionLogDto logFlowStart(String flowId, String sessionId) {
    log.debug("Flow execution started: {} (session: {})", flowId, sessionId);

    FlowExecutionLog log = FlowExecutionLog.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .sessionId(sessionId)
        .status(FlowExecutionLog.ExecutionStatus.STARTED)
        .build();

    FlowExecutionLog saved = executionLogRepository.save(log);
    return FlowExecutionLogDto.fromEntity(saved);
  }

  /**
   * Logs the execution of a node.
   *
   * @param flowId the flow ID
   * @param sessionId the execution session ID
   * @param nodeId the node ID
   * @param status the execution status
   * @param executionTimeMs the time taken in milliseconds
   * @param inputData the input data as JSON string
   * @param outputData the output data as JSON string
   * @param errorMessage the error message if failed
   * @return the execution log DTO
   */
  @Transactional
  public FlowExecutionLogDto logNodeExecution(
      String flowId,
      String sessionId,
      String nodeId,
      FlowExecutionLog.ExecutionStatus status,
      Long executionTimeMs,
      String inputData,
      String outputData,
      String errorMessage) {

    log.debug("Node execution logged: flow={}, node={}, session={}, status={}",
        flowId, nodeId, sessionId, status);

    FlowExecutionLog log = FlowExecutionLog.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .sessionId(sessionId)
        .nodeId(nodeId)
        .status(status)
        .executionTimeMs(executionTimeMs)
        .inputDataJson(inputData)
        .outputDataJson(outputData)
        .errorMessage(errorMessage)
        .build();

    FlowExecutionLog saved = executionLogRepository.save(log);
    return FlowExecutionLogDto.fromEntity(saved);
  }

  /**
   * Logs the completion of a flow execution.
   *
   * @param flowId the flow ID
   * @param sessionId the execution session ID
   * @return the execution log DTO
   */
  @Transactional
  public FlowExecutionLogDto logFlowCompletion(String flowId, String sessionId) {
    log.debug("Flow execution completed: {} (session: {})", flowId, sessionId);

    FlowExecutionLog log = FlowExecutionLog.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .sessionId(sessionId)
        .status(FlowExecutionLog.ExecutionStatus.COMPLETED)
        .build();

    FlowExecutionLog saved = executionLogRepository.save(log);
    return FlowExecutionLogDto.fromEntity(saved);
  }

  /**
   * Gets execution history for a flow.
   *
   * @param flowId the flow ID
   * @param pageable pagination parameters
   * @return page of execution logs
   */
  @Transactional(readOnly = true)
  public Page<FlowExecutionLogDto> getExecutionHistory(String flowId, Pageable pageable) {
    return executionLogRepository.findByFlowIdOrderByCreatedAtDesc(flowId, pageable)
        .map(FlowExecutionLogDto::fromEntity);
  }

  /**
   * Gets execution logs for a specific session.
   *
   * @param sessionId the execution session ID
   * @return list of execution logs
   */
  @Transactional(readOnly = true)
  public List<FlowExecutionLogDto> getSessionLogs(String sessionId) {
    return executionLogRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
        .stream()
        .map(FlowExecutionLogDto::fromEntity)
        .toList();
  }

  /**
   * Gets execution logs for a specific node in a session.
   *
   * @param sessionId the execution session ID
   * @param nodeId the node ID
   * @return list of execution logs
   */
  @Transactional(readOnly = true)
  public List<FlowExecutionLogDto> getNodeSessionLogs(String sessionId, String nodeId) {
    return executionLogRepository.findBySessionIdAndNodeIdOrderByCreatedAtAsc(sessionId, nodeId)
        .stream()
        .map(FlowExecutionLogDto::fromEntity)
        .toList();
  }

  /**
   * Gets failed executions for a flow.
   *
   * @param flowId the flow ID
   * @param pageable pagination parameters
   * @return page of failed execution logs
   */
  @Transactional(readOnly = true)
  public Page<FlowExecutionLogDto> getFailedExecutions(String flowId, Pageable pageable) {
    return executionLogRepository.findByFlowIdAndStatusOrderByCreatedAtDesc(
        flowId, FlowExecutionLog.ExecutionStatus.FAILED, pageable)
        .map(FlowExecutionLogDto::fromEntity);
  }

  /**
   * Gets average execution time for a flow.
   *
   * @param flowId the flow ID
   * @return average execution time in milliseconds
   */
  @Transactional(readOnly = true)
  public Double getAverageExecutionTime(String flowId) {
    return executionLogRepository.getAverageExecutionTime(flowId);
  }

  /**
   * Counts failed executions for a flow.
   *
   * @param flowId the flow ID
   * @return count of failed executions
   */
  @Transactional(readOnly = true)
  public long countFailedExecutions(String flowId) {
    return executionLogRepository.countByFlowIdAndStatus(flowId, FlowExecutionLog.ExecutionStatus.FAILED);
  }

  /**
   * Gets recent execution history (last N hours).
   *
   * @param flowId the flow ID
   * @param hoursBack number of hours back to look
   * @param pageable pagination parameters
   * @return page of execution logs
   */
  @Transactional(readOnly = true)
  public Page<FlowExecutionLogDto> getRecentExecutions(String flowId, int hoursBack, Pageable pageable) {
    LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
    return executionLogRepository.findByFlowIdAndCreatedAtAfterOrderByCreatedAtDesc(flowId, since, pageable)
        .map(FlowExecutionLogDto::fromEntity);
  }
}
