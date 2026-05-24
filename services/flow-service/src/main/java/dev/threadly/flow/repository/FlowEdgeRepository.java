package dev.threadly.flow.repository;

import dev.threadly.flow.entity.FlowEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FlowEdge entity.
 * Provides database access methods for flow edge operations.
 */
@Repository
public interface FlowEdgeRepository extends JpaRepository<FlowEdge, String> {

  /**
   * Find all edges in a flow.
   *
   * @param flowId the flow ID
   * @return list of edges
   */
  List<FlowEdge> findByFlowId(String flowId);

  /**
   * Find an edge by flow ID and edge ID.
   *
   * @param flowId the flow ID
   * @param edgeId the edge ID
   * @return optional containing edge if found
   */
  Optional<FlowEdge> findByFlowIdAndEdgeId(String flowId, String edgeId);

  /**
   * Find all edges from a source node.
   *
   * @param flowId the flow ID
   * @param sourceNodeId the source node ID
   * @return list of edges
   */
  List<FlowEdge> findByFlowIdAndSourceNodeId(String flowId, String sourceNodeId);

  /**
   * Find all edges to a target node.
   *
   * @param flowId the flow ID
   * @param targetNodeId the target node ID
   * @return list of edges
   */
  List<FlowEdge> findByFlowIdAndTargetNodeId(String flowId, String targetNodeId);

  /**
   * Find a direct edge between two nodes.
   *
   * @param flowId the flow ID
   * @param sourceNodeId the source node ID
   * @param targetNodeId the target node ID
   * @return optional containing edge if found
   */
  Optional<FlowEdge> findByFlowIdAndSourceNodeIdAndTargetNodeId(
      String flowId, String sourceNodeId, String targetNodeId);

  /**
   * Check if an edge exists in a flow.
   *
   * @param flowId the flow ID
   * @param edgeId the edge ID
   * @return true if edge exists
   */
  boolean existsByFlowIdAndEdgeId(String flowId, String edgeId);

  /**
   * Count edges in a flow.
   *
   * @param flowId the flow ID
   * @return number of edges
   */
  long countByFlowId(String flowId);

  /**
   * Delete all edges in a flow.
   *
   * @param flowId the flow ID
   */
  void deleteByFlowId(String flowId);

  /**
   * Find all edges connected to a node (incoming and outgoing).
   *
   * @param flowId the flow ID
   * @param nodeId the node ID
   * @return list of edges
   */
  @Query("SELECT e FROM FlowEdge e WHERE e.flowId = :flowId AND (e.sourceNodeId = :nodeId OR e.targetNodeId = :nodeId)")
  List<FlowEdge> findAllConnectedEdges(@Param("flowId") String flowId, @Param("nodeId") String nodeId);
}
