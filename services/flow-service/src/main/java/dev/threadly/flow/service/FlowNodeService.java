package dev.threadly.flow.service;

import dev.threadly.flow.dto.CreateFlowNodeRequest;
import dev.threadly.flow.dto.FlowNodeDto;
import dev.threadly.flow.dto.UpdateFlowNodeRequest;
import dev.threadly.flow.entity.FlowNode;
import dev.threadly.flow.exception.InvalidNodeTypeException;
import dev.threadly.flow.repository.FlowNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for Flow Node operations.
 * Handles CRUD operations for nodes within flows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowNodeService {

  private final FlowNodeRepository flowNodeRepository;
  private final dev.threadly.flow.catalog.NodeCatalogService nodeCatalogService;

  /**
   * Adds a new node to a flow.
   *
   * @param flowId the flow ID
   * @param request the node creation request
   * @return the created node DTO
   * @throws InvalidNodeTypeException if node type is not valid
   */
  @Transactional
  public FlowNodeDto addNode(String flowId, CreateFlowNodeRequest request) {
    log.info("Adding node {} to flow {}", request.getNodeId(), flowId);

    // Validate node type
    var catalog = nodeCatalogService.getNodeCatalog();
    boolean isValidType = catalog.stream()
        .anyMatch(entry -> entry.getType().equals(request.getType()));

    if (!isValidType) {
      throw new InvalidNodeTypeException(flowId, request.getType());
    }

    FlowNode node = FlowNode.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .nodeId(request.getNodeId())
        .type(request.getType())
        .positionX(request.getPositionX())
        .positionY(request.getPositionY())
        .dataJson(request.getDataJson())
        .build();

    FlowNode saved = flowNodeRepository.save(node);
    log.info("Node added successfully: {}", saved.getId());
    return FlowNodeDto.fromEntity(saved);
  }

  /**
   * Updates an existing node.
   *
   * @param flowId the flow ID
   * @param nodeId the node ID
   * @param request the update request
   * @return the updated node DTO
   */
  @Transactional
  public FlowNodeDto updateNode(String flowId, String nodeId, UpdateFlowNodeRequest request) {
    log.info("Updating node {} in flow {}", nodeId, flowId);

    FlowNode node = flowNodeRepository.findByFlowIdAndNodeId(flowId, nodeId)
        .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

    if (request.getPositionX() != null && request.getPositionY() != null) {
      node.updatePosition(request.getPositionX(), request.getPositionY());
    }
    if (request.getDataJson() != null) {
      node.updateData(request.getDataJson());
    }

    FlowNode updated = flowNodeRepository.save(node);
    log.info("Node updated successfully: {}", nodeId);
    return FlowNodeDto.fromEntity(updated);
  }

  /**
   * Removes a node from a flow.
   *
   * @param flowId the flow ID
   * @param nodeId the node ID to remove
   */
  @Transactional
  public void removeNode(String flowId, String nodeId) {
    log.info("Removing node {} from flow {}", nodeId, flowId);

    FlowNode node = flowNodeRepository.findByFlowIdAndNodeId(flowId, nodeId)
        .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

    flowNodeRepository.delete(node);
    log.info("Node removed successfully: {}", nodeId);
  }

  /**
   * Lists all nodes in a flow.
   *
   * @param flowId the flow ID
   * @return list of node DTOs
   */
  @Transactional(readOnly = true)
  public List<FlowNodeDto> listNodes(String flowId) {
    return flowNodeRepository.findByFlowId(flowId)
        .stream()
        .map(FlowNodeDto::fromEntity)
        .toList();
  }

  /**
   * Gets a specific node by ID.
   *
   * @param flowId the flow ID
   * @param nodeId the node ID
   * @return the node DTO
   */
  @Transactional(readOnly = true)
  public FlowNodeDto getNode(String flowId, String nodeId) {
    FlowNode node = flowNodeRepository.findByFlowIdAndNodeId(flowId, nodeId)
        .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));
    return FlowNodeDto.fromEntity(node);
  }

  /**
   * Duplicates all nodes from one flow to another.
   *
   * @param sourceFlowId the source flow ID
   * @param targetFlowId the target flow ID
   */
  @Transactional
  public void duplicateNodes(String sourceFlowId, String targetFlowId) {
    List<FlowNode> sourceNodes = flowNodeRepository.findByFlowId(sourceFlowId);
    for (FlowNode sourceNode : sourceNodes) {
      FlowNode duplicate = FlowNode.builder()
          .id(UUID.randomUUID().toString())
          .flowId(targetFlowId)
          .nodeId(sourceNode.getNodeId())
          .type(sourceNode.getType())
          .positionX(sourceNode.getPositionX())
          .positionY(sourceNode.getPositionY())
          .dataJson(sourceNode.getDataJson())
          .build();
      flowNodeRepository.save(duplicate);
    }
    log.info("Nodes duplicated from {} to {}", sourceFlowId, targetFlowId);
  }

  /**
   * Deletes all nodes in a flow.
   *
   * @param flowId the flow ID
   */
  @Transactional
  public void deleteAllNodes(String flowId) {
    flowNodeRepository.deleteByFlowId(flowId);
    log.info("All nodes deleted for flow: {}", flowId);
  }

  /**
   * Counts nodes in a flow.
   *
   * @param flowId the flow ID
   * @return the count of nodes
   */
  public long countNodes(String flowId) {
    return flowNodeRepository.countByFlowId(flowId);
  }
}
