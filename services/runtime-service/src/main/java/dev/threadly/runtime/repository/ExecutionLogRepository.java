package dev.threadly.runtime.repository;

import dev.threadly.runtime.model.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for ExecutionLog entity.
 * Provides database access for execution log management and queries.
 */
@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, String> {

  /**
   * Find all execution logs for a session
   */
  @Query("SELECT el FROM ExecutionLog el WHERE el.sessionId = ?1 ORDER BY el.createdAt DESC")
  List<ExecutionLog> findBySessionId(String sessionId);

  /**
   * Find execution logs by node ID
   */
  @Query("SELECT el FROM ExecutionLog el WHERE el.nodeId = ?1 ORDER BY el.createdAt DESC")
  List<ExecutionLog> findByNodeId(String nodeId);

  /**
   * Find execution logs by session ID and node ID
   */
  @Query("SELECT el FROM ExecutionLog el WHERE el.sessionId = ?1 AND el.nodeId = ?2 ORDER BY el.createdAt DESC")
  List<ExecutionLog> findBySessionIdAndNodeId(String sessionId, String nodeId);

  /**
   * Find failed execution logs for a session
   */
  @Query("SELECT el FROM ExecutionLog el WHERE el.sessionId = ?1 AND el.status = 'FAILURE' ORDER BY el.createdAt DESC")
  List<ExecutionLog> findFailedLogsBySessionId(String sessionId);

  /**
   * Calculate average execution time for a node
   */
  @Query("SELECT AVG(el.executionTimeMs) FROM ExecutionLog el WHERE el.nodeId = ?1 AND el.status = 'SUCCESS'")
  Long calculateAverageExecutionTime(String nodeId);

  /**
   * Delete old execution logs (for cleanup)
   */
  @Query("DELETE FROM ExecutionLog el WHERE el.createdAt < ?1")
  void deleteLogsOlderThan(LocalDateTime cutoffTime);
}
