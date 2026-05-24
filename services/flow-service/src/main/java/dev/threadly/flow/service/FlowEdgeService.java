package dev.threadly.flow.service;

import dev.threadly.flow.dto.CreateFlowEdgeRequest;
import dev.threadly.flow.dto.FlowEdgeDto;
import dev.threadly.flow.entity.FlowEdge;
import dev.threadly.flow.exception.CyclicFlowException;
import dev.threadly.flow.repository.FlowEdgeRepository;
import dev.threadly.flow.repository.FlowNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for Flow Edge operations.
 * Handles CRUD operations for edges within flows.
 * Detects and prevents cyclic dependencies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowEdgeService {

  private final FlowEdgeRepository flowEdgeRepository;
  private final FlowNodeRepository flowNodeRepository;

  /**
   * Adds a new edge between two nodes.
   * Detects and prevents cyclic dependencies.
   *
   * @param flowId the flow ID
   * @param request the edge creation request
   * @return the created edge DTO
   * @throws CyclicFlowException if adding this edge creates a cycle
   */
  @Transactional
  public FlowEdgeDto addEdge(String flowId, CreateFlowEdgeRequest request) {
    log.info("Adding edge {} to flow {}: {} -> {}",
        request.getEdgeId(), flowId, request.getSourceNodeId(), request.getTargetNodeId());

    // Validate nodes exist
    if (!flowNodeRepository.existsByFlowIdAndNodeId(flowId, request.getSourceNodeId())) {
      throw new IllegalArgumentException("Source node not found: " + request.getSourceNodeId());
    }
    if (!flowNodeRepository.existsByFlowIdAndNodeId(flowId, request.getTargetNodeId())) {
      throw new IllegalArgumentException("Target node not found: " + request.getTargetNodeId());
    }

    // Detect cycles before adding the edge
    if (wouldCreateCycle(flowId, request.getSourceNodeId(), request.getTargetNodeId())) {
      throw new CyclicFlowException(flowId,
          "Adding edge from " + request.getSourceNodeId() + " to " + request.getTargetNodeId() + " would create a cycle");
    }

    FlowEdge edge = FlowEdge.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .edgeId(request.getEdgeId())
        .sourceNodeId(request.getSourceNodeId())
        .targetNodeId(request.getTargetNodeId())
        .sourceHandle(request.getSourceHandle())
        .targetHandle(request.getTargetHandle())
        .build();

    FlowEdge saved = flowEdgeRepository.save(edge);
    log.info("Edge added successfully: {}", saved.getId());
    return FlowEdgeDto.fromEntity(saved);
  }

  /**
   * Updates an edge.
   *
   * @param flowId the flow ID
   * @param edgeId the edge ID
   * @param request the update request
   * @return the updated edge DTO
   */
  @Transactional
  public FlowEdgeDto updateEdge(String flowId, String edgeId, CreateFlowEdgeRequest request) {
    log.info("Updating edge {} in flow {}", edgeId, flowId);

    FlowEdge edge = flowEdgeRepository.findByFlowIdAndEdgeId(flowId, edgeId)
        .orElseThrow(() -> new IllegalArgumentException("Edge not found: " + edgeId));

    // If source/target changed, check for cycles
    if (!edge.getSourceNodeId().equals(request.getSourceNodeId()) ||
        !edge.getTargetNodeId().equals(request.getTargetNodeId())) {
      if (wouldCreateCycle(flowId, request.getSourceNodeId(), request.getTargetNodeId())) {
        throw new CyclicFlowException(flowId,
            "Edge update would create a cycle from " + request.getSourceNodeId() + " to " + request.getTargetNodeId());
      }
    }

    edge.setSourceNodeId(request.getSourceNodeId());
    edge.setTargetNodeId(request.getTargetNodeId());
    edge.setSourceHandle(request.getSourceHandle());
    edge.setTargetHandle(request.getTargetHandle());

    FlowEdge updated = flowEdgeRepository.save(edge);
    log.info("Edge updated successfully: {}", edgeId);
    return FlowEdgeDto.fromEntity(updated);
  }

  /**
   * Removes an edge from a flow.
   *
   * @param flowId the flow ID
   * @param edgeId the edge ID to remove
   */
  @Transactional
  public void removeEdge(String flowId, String edgeId) {
    log.info("Removing edge {} from flow {}", edgeId, flowId);

    FlowEdge edge = flowEdgeRepository.findByFlowIdAndEdgeId(flowId, edgeId)
        .orElseThrow(() -> new IllegalArgumentException("Edge not found: " + edgeId));

    flowEdgeRepository.delete(edge);
    log.info("Edge removed successfully: {}", edgeId);
  }

  /**
   * Lists all edges in a flow.
   *
   * @param flowId the flow ID
   * @return list of edge DTOs
   */
  @Transactional(readOnly = true)
  public List<FlowEdgeDto> listEdges(String flowId) {
    return flowEdgeRepository.findByFlowId(flowId)
        .stream()
        .map(FlowEdgeDto::fromEntity)
        .toList();
  }

  /**
   * Gets a specific edge by ID.
   *
   * @param flowId the flow ID
   * @param edgeId the edge ID
   * @return the edge DTO
   */
  @Transactional(readOnly = true)
  public FlowEdgeDto getEdge(String flowId, String edgeId) {
    FlowEdge edge = flowEdgeRepository.findByFlowIdAndEdgeId(flowId, edgeId)
        .orElseThrow(() -> new IllegalArgumentException("Edge not found: " + edgeId));
    return FlowEdgeDto.fromEntity(edge);
  }

  /**
   * Duplicates all edges from one flow to another.
   *
   * @param sourceFlowId the source flow ID
   * @param targetFlowId the target flow ID
   */
  @Transactional
  public void duplicateEdges(String sourceFlowId, String targetFlowId) {
    List<FlowEdge> sourceEdges = flowEdgeRepository.findByFlowId(sourceFlowId);
    for (FlowEdge sourceEdge : sourceEdges) {
      FlowEdge duplicate = FlowEdge.builder()
          .id(UUID.randomUUID().toString())
          .flowId(targetFlowId)
          .edgeId(sourceEdge.getEdgeId())
          .sourceNodeId(sourceEdge.getSourceNodeId())
          .targetNodeId(sourceEdge.getTargetNodeId())
          .sourceHandle(sourceEdge.getSourceHandle())
          .targetHandle(sourceEdge.getTargetHandle())
          .build();
      flowEdgeRepository.save(duplicate);
    }
    log.info("Edges duplicated from {} to {}", sourceFlowId, targetFlowId);
  }

  /**
   * Deletes all edges in a flow.
   *
   * @param flowId the flow ID
   */
  @Transactional
  public void deleteAllEdges(String flowId) {
    flowEdgeRepository.deleteByFlowId(flowId);
    log.info("All edges deleted for flow: {}", flowId);
  }

  /**
   * Counts edges in a flow.
   *
   * @param flowId the flow ID
   * @return the count of edges
   */
  public long countEdges(String flowId) {
    return flowEdgeRepository.countByFlowId(flowId);
  }

  /**
   * Detects if adding an edge would create a cycle.
   * Uses depth-first search to detect back edges.
   *
   * @param flowId the flow ID
   * @param sourceNodeId the source node ID
   * @param targetNodeId the target node ID
   * @return true if cycle would be created
   */
  private boolean wouldCreateCycle(String flowId, String sourceNodeId, String targetNodeId) {
    // A direct self-loop is a cycle
    if (sourceNodeId.equals(targetNodeId)) {
      return true;
    }

    // Check if there's already a path from target back to source
    // If yes, adding edge source->target would create a cycle
    return hasPath(flowId, targetNodeId, sourceNodeId);
  }

  /**
   * Checks if there's a path from source to target in the graph.
   * Uses DFS (depth-first search).
   *
   * @param flowId the flow ID
   * @param fromNodeId the starting node
   * @param toNodeId the target node
   * @return true if path exists
   */
  private boolean hasPath(String flowId, String fromNodeId, String toNodeId) {
    Set<String> visited = new HashSet<>();
    return dfsHasPath(flowId, fromNodeId, toNodeId, visited);
  }

  /**
   * DFS helper for path detection.
   *
   * @param flowId the flow ID
   * @param current the current node
   * @param target the target node
   * @param visited set of already visited nodes
   * @return true if path exists from current to target
   */
  private boolean dfsHasPath(String flowId, String current, String target, Set<String> visited) {
    if (current.equals(target)) {
      return true;
    }

    if (visited.contains(current)) {
      return false;
    }

    visited.add(current);

    List<FlowEdge> outgoing = flowEdgeRepository.findByFlowIdAndSourceNodeId(flowId, current);
    for (FlowEdge edge : outgoing) {
      if (dfsHasPath(flowId, edge.getTargetNodeId(), target, visited)) {
        return true;
      }
    }

    return false;
  }
}
