package dev.threadly.flow.repository;

import dev.threadly.flow.entity.FlowVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for FlowVersion entity.
 * Provides database access methods for flow version operations.
 */
@Repository
public interface FlowVersionRepository extends JpaRepository<FlowVersion, String> {

  /**
   * Find all versions of a flow with pagination.
   *
   * @param flowId the flow ID
   * @param pageable pagination parameters
   * @return page of versions
   */
  Page<FlowVersion> findByFlowIdOrderByVersionNumberDesc(String flowId, Pageable pageable);

  /**
   * Find a specific version by flow ID and version number.
   *
   * @param flowId the flow ID
   * @param versionNumber the version number
   * @return optional containing version if found
   */
  Optional<FlowVersion> findByFlowIdAndVersionNumber(String flowId, Integer versionNumber);

  /**
   * Find the active/published version of a flow.
   *
   * @param flowId the flow ID
   * @return optional containing active version if found
   */
  Optional<FlowVersion> findByFlowIdAndIsActiveTrue(String flowId);

  /**
   * Get the latest version number for a flow.
   *
   * @param flowId the flow ID
   * @return the highest version number or 0 if no versions exist
   */
  @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM FlowVersion v WHERE v.flowId = :flowId")
  Integer getLatestVersionNumber(@Param("flowId") String flowId);

  /**
   * Check if a specific version exists.
   *
   * @param flowId the flow ID
   * @param versionNumber the version number
   * @return true if version exists
   */
  boolean existsByFlowIdAndVersionNumber(String flowId, Integer versionNumber);

  /**
   * Find all versions of a flow in descending order.
   *
   * @param flowId the flow ID
   * @return list of versions sorted by version number descending
   */
  @Query("SELECT v FROM FlowVersion v WHERE v.flowId = :flowId ORDER BY v.versionNumber DESC")
  java.util.List<FlowVersion> findAllByFlowId(@Param("flowId") String flowId);
}
