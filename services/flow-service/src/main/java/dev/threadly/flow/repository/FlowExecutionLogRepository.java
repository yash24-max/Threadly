package dev.threadly.flow.repository;

import dev.threadly.flow.entity.FlowExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for FlowExecutionLog entity.
 * Provides database access methods for flow execution logging.
 */
@Repository
public interface FlowExecutionLogRepository extends JpaRepository<FlowExecutionLog, String> {

  /**
   * Find execution logs by flow ID with pagination.
   *
   * @param flowId the flow ID
   * @param pageable pagination parameters
   * @return page of execution logs
   */
  Page<FlowExecutionLog> findByFlowIdOrderByCreatedAtDesc(String flowId, Pageable pageable);

  /**
   * Find execution logs by session ID.
   *
   * @param sessionId the execution session ID
   * @return list of execution logs
   */
  List<FlowExecutionLog> findBySessionIdOrderByCreatedAtAsc(String sessionId);

  /**
   * Find execution logs for a specific node in a session.
   *
   * @param sessionId the execution session ID
   * @param nodeId the node ID
   * @return list of execution logs
   */
  List<FlowExecutionLog> findBySessionIdAndNodeIdOrderByCreatedAtAsc(String sessionId, String nodeId);

  /**
   * Find execution logs by status.
   *
   * @param flowId the flow ID
   * @param status the execution status
   * @param pageable pagination parameters
   * @return page of execution logs
   */
  Page<FlowExecutionLog> findByFlowIdAndStatusOrderByCreatedAtDesc(
      String flowId, FlowExecutionLog.ExecutionStatus status, Pageable pageable);

  /**
   * Count failed executions for a flow.
   *
   * @param flowId the flow ID
   * @return count of failed executions
   */
  long countByFlowIdAndStatus(String flowId, FlowExecutionLog.ExecutionStatus status);

  /**
   * Find execution logs created after a specific time.
   *
   * @param flowId the flow ID
   * @param after the time threshold
   * @param pageable pagination parameters
   * @return page of execution logs
   */
  Page<FlowExecutionLog> findByFlowIdAndCreatedAtAfterOrderByCreatedAtDesc(
      String flowId, LocalDateTime after, Pageable pageable);

  /**
   * Get average execution time for a flow.
   *
   * @param flowId the flow ID
   * @return average execution time in milliseconds
   */
  @Query("SELECT AVG(el.executionTimeMs) FROM FlowExecutionLog el WHERE el.flowId = :flowId AND el.executionTimeMs IS NOT NULL")
  Double getAverageExecutionTime(@Param("flowId") String flowId);
}
