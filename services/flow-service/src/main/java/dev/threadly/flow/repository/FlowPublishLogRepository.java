package dev.threadly.flow.repository;

import dev.threadly.flow.entity.FlowPublishLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for FlowPublishLog entity.
 * Provides database access methods for flow publish history operations.
 */
@Repository
public interface FlowPublishLogRepository extends JpaRepository<FlowPublishLog, String> {

  /**
   * Find publish history for a flow with pagination.
   *
   * @param flowId the flow ID
   * @param pageable pagination parameters
   * @return page of publish logs
   */
  Page<FlowPublishLog> findByFlowIdOrderByCreatedAtDesc(String flowId, Pageable pageable);

  /**
   * Find all publish history for a flow.
   *
   * @param flowId the flow ID
   * @return list of publish logs sorted by creation date descending
   */
  List<FlowPublishLog> findByFlowIdOrderByCreatedAtDesc(String flowId);

  /**
   * Find publish logs by event type.
   *
   * @param flowId the flow ID
   * @param eventType the event type
   * @return list of publish logs
   */
  List<FlowPublishLog> findByFlowIdAndEventType(String flowId, FlowPublishLog.EventType eventType);

  /**
   * Find logs published by a specific user.
   *
   * @param publishedBy the user ID
   * @param pageable pagination parameters
   * @return page of publish logs
   */
  Page<FlowPublishLog> findByPublishedByOrderByCreatedAtDesc(String publishedBy, Pageable pageable);

  /**
   * Get the most recent publish event for a flow.
   *
   * @param flowId the flow ID
   * @return optional containing the most recent publish log
   */
  @Query("SELECT pl FROM FlowPublishLog pl WHERE pl.flowId = :flowId ORDER BY pl.createdAt DESC LIMIT 1")
  java.util.Optional<FlowPublishLog> getMostRecentPublish(@Param("flowId") String flowId);

  /**
   * Count publications for a flow.
   *
   * @param flowId the flow ID
   * @return number of publish events
   */
  long countByFlowIdAndEventType(String flowId, FlowPublishLog.EventType eventType);

  /**
   * Find publish logs created after a specific time.
   *
   * @param flowId the flow ID
   * @param after the time threshold
   * @return list of publish logs
   */
  List<FlowPublishLog> findByFlowIdAndCreatedAtAfterOrderByCreatedAtDesc(String flowId, LocalDateTime after);
}
