package dev.threadly.flow.repository;

import dev.threadly.flow.entity.FlowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FlowNode entity.
 * Provides database access methods for flow node operations.
 */
@Repository
public interface FlowNodeRepository extends JpaRepository<FlowNode, String> {

  /**
   * Find all nodes in a flow.
   *
   * @param flowId the flow ID
   * @return list of nodes
   */
  List<FlowNode> findByFlowId(String flowId);

  /**
   * Find a node by flow ID and node ID.
   *
   * @param flowId the flow ID
   * @param nodeId the node ID
   * @return optional containing node if found
   */
  Optional<FlowNode> findByFlowIdAndNodeId(String flowId, String nodeId);

  /**
   * Find nodes by type in a flow.
   *
   * @param flowId the flow ID
   * @param type the node type
   * @return list of nodes of specified type
   */
  List<FlowNode> findByFlowIdAndType(String flowId, String type);

  /**
   * Check if a node exists in a flow.
   *
   * @param flowId the flow ID
   * @param nodeId the node ID
   * @return true if node exists
   */
  boolean existsByFlowIdAndNodeId(String flowId, String nodeId);

  /**
   * Count nodes in a flow.
   *
   * @param flowId the flow ID
   * @return number of nodes
   */
  long countByFlowId(String flowId);

  /**
   * Delete all nodes in a flow.
   *
   * @param flowId the flow ID
   */
  void deleteByFlowId(String flowId);

  /**
   * Find nodes by IDs.
   *
   * @param nodeIds the list of node IDs
   * @return list of nodes
   */
  @Query("SELECT n FROM FlowNode n WHERE n.nodeId IN :nodeIds")
  List<FlowNode> findByNodeIds(@Param("nodeIds") List<String> nodeIds);
}
