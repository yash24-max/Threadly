package dev.threadly.flow.repository;

import dev.threadly.flow.entity.FlowValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for FlowValidation entity.
 * Provides database access methods for flow validation operations.
 */
@Repository
public interface FlowValidationRepository extends JpaRepository<FlowValidation, String> {

  /**
   * Find validation record by flow ID.
   *
   * @param flowId the flow ID
   * @return optional containing validation if found
   */
  Optional<FlowValidation> findByFlowId(String flowId);

  /**
   * Check if a flow has a valid validation record.
   *
   * @param flowId the flow ID
   * @return true if validation exists and is valid
   */
  boolean existsByFlowIdAndIsValidTrue(String flowId);
}
