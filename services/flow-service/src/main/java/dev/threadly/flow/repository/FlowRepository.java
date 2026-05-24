package dev.threadly.flow.repository;

import dev.threadly.flow.entity.Flow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Flow entity.
 * Provides database access methods for flow operations.
 */
@Repository
public interface FlowRepository extends JpaRepository<Flow, String> {

  /**
   * Find flows by bot ID with pagination.
   *
   * @param botId the bot ID
   * @param orgId the organization ID
   * @param pageable pagination parameters
   * @return page of flows
   */
  Page<Flow> findByBotIdAndOrgId(String botId, String orgId, Pageable pageable);

  /**
   * Find all flows for an organization.
   *
   * @param orgId the organization ID
   * @param pageable pagination parameters
   * @return page of flows
   */
  Page<Flow> findByOrgId(String orgId, Pageable pageable);

  /**
   * Find flows by status.
   *
   * @param orgId the organization ID
   * @param status the flow status
   * @param pageable pagination parameters
   * @return page of flows
   */
  Page<Flow> findByOrgIdAndStatus(String orgId, Flow.FlowStatus status, Pageable pageable);

  /**
   * Find a flow by ID and organization ID (enforces org isolation).
   *
   * @param id the flow ID
   * @param orgId the organization ID
   * @return optional containing flow if found
   */
  Optional<Flow> findByIdAndOrgId(String id, String orgId);

  /**
   * Check if a flow exists for a given bot.
   *
   * @param botId the bot ID
   * @param orgId the organization ID
   * @return true if flows exist for this bot
   */
  boolean existsByBotIdAndOrgId(String botId, String orgId);

  /**
   * Count flows by status for an organization.
   *
   * @param orgId the organization ID
   * @param status the flow status
   * @return count of flows
   */
  long countByOrgIdAndStatus(String orgId, Flow.FlowStatus status);

  /**
   * Find flows created by a specific user.
   *
   * @param createdBy the user who created the flows
   * @param orgId the organization ID
   * @param pageable pagination parameters
   * @return page of flows
   */
  Page<Flow> findByCreatedByAndOrgId(String createdBy, String orgId, Pageable pageable);
}
