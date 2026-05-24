package dev.threadly.flow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.flow.dto.FlowValidationDto;
import dev.threadly.flow.dto.ValidationErrorDto;
import dev.threadly.flow.entity.*;
import dev.threadly.flow.exception.FlowValidationException;
import dev.threadly.flow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for Flow Validation.
 * Validates flow definitions against business rules.
 * Checks nodes, edges, and overall flow structure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowValidationService {

  private final FlowValidationRepository validationRepository;
  private final FlowRepository flowRepository;
  private final FlowNodeRepository flowNodeRepository;
  private final FlowEdgeRepository flowEdgeRepository;
  private final FlowVersionRepository flowVersionRepository;
  private final dev.threadly.flow.catalog.NodeCatalogService nodeCatalogService;
  private final ObjectMapper objectMapper;

  /**
   * Validates an entire flow definition.
   * Checks nodes, edges, and overall structure.
   *
   * @param flowId the flow ID
   * @return the validation DTO
   * @throws FlowValidationException if validation fails
   */
  @Transactional
  public FlowValidationDto validateFlow(String flowId) {
    log.info("Validating flow: {}", flowId);

    Flow flow = flowRepository.findById(flowId)
        .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

    List<String> errors = new ArrayList<>();

    // Check if flow has nodes
    List<FlowNode> nodes = flowNodeRepository.findByFlowId(flowId);
    if (nodes.isEmpty()) {
      errors.add("Flow must contain at least one node");
    }

    // Validate each node
    for (FlowNode node : nodes) {
      errors.addAll(validateNode(node));
    }

    // Validate edges
    List<FlowEdge> edges = flowEdgeRepository.findByFlowId(flowId);
    for (FlowEdge edge : edges) {
      errors.addAll(validateEdge(flowId, edge));
    }

    // Check for unreachable nodes (optional - warn but don't fail)
    List<String> unreachable = findUnreachableNodes(flowId, nodes, edges);
    if (!unreachable.isEmpty()) {
      errors.add("Unreachable nodes: " + String.join(", ", unreachable));
    }

    // Update validation record
    FlowValidation validation = validationRepository.findByFlowId(flowId)
        .orElseGet(() -> FlowValidation.builder().id(UUID.randomUUID().toString()).flowId(flowId).build());

    if (errors.isEmpty()) {
      validation.markAsValid();
      log.info("Flow validation passed: {}", flowId);
    } else {
      String errorsJson = convertErrorsToJson(errors);
      validation.markAsInvalid(errorsJson);
      log.warn("Flow validation failed for {}: {}", flowId, String.join("; ", errors));
    }

    validationRepository.save(validation);
    return FlowValidationDto.fromEntity(validation);
  }

  /**
   * Validates a single node.
   *
   * @param node the node to validate
   * @return list of validation errors (empty if valid)
   */
  private List<String> validateNode(FlowNode node) {
    List<String> errors = new ArrayList<>();

    // Validate node type
    var catalog = nodeCatalogService.getNodeCatalog();
    boolean isValidType = catalog.stream()
        .anyMatch(entry -> entry.getType().equals(node.getType()));

    if (!isValidType) {
      errors.add("Invalid node type: " + node.getType());
    }

    // Validate position
    if (node.getPositionX() == null || node.getPositionY() == null) {
      errors.add("Node " + node.getNodeId() + " has missing position coordinates");
    }

    // Node-specific validation can be extended here
    // For example, checking required fields in dataJson based on node type

    return errors;
  }

  /**
   * Validates a single edge.
   *
   * @param flowId the flow ID
   * @param edge the edge to validate
   * @return list of validation errors (empty if valid)
   */
  private List<String> validateEdge(String flowId, FlowEdge edge) {
    List<String> errors = new ArrayList<>();

    // Check if nodes exist
    if (!flowNodeRepository.existsByFlowIdAndNodeId(flowId, edge.getSourceNodeId())) {
      errors.add("Edge references non-existent source node: " + edge.getSourceNodeId());
    }
    if (!flowNodeRepository.existsByFlowIdAndNodeId(flowId, edge.getTargetNodeId())) {
      errors.add("Edge references non-existent target node: " + edge.getTargetNodeId());
    }

    // Check for self-loops (generally invalid in most flow systems)
    if (edge.isSelfLoop()) {
      errors.add("Self-loops are not allowed: " + edge.getEdgeId());
    }

    return errors;
  }

  /**
   * Finds nodes that are unreachable from the start node.
   *
   * @param flowId the flow ID
   * @param nodes list of all nodes
   * @param edges list of all edges
   * @return list of unreachable node IDs
   */
  private List<String> findUnreachableNodes(String flowId, List<FlowNode> nodes, List<FlowEdge> edges) {
    if (nodes.isEmpty()) {
      return Collections.emptyList();
    }

    // Build adjacency list
    Map<String, List<String>> graph = new HashMap<>();
    for (FlowNode node : nodes) {
      graph.put(node.getNodeId(), new ArrayList<>());
    }
    for (FlowEdge edge : edges) {
      graph.get(edge.getSourceNodeId()).add(edge.getTargetNodeId());
    }

    // Find reachable nodes from first node
    Set<String> reachable = new HashSet<>();
    dfsReachable(nodes.get(0).getNodeId(), graph, reachable);

    // Find unreachable
    List<String> unreachable = new ArrayList<>();
    for (FlowNode node : nodes) {
      if (!reachable.contains(node.getNodeId())) {
        unreachable.add(node.getNodeId());
      }
    }

    return unreachable;
  }

  /**
   * DFS helper to find reachable nodes.
   *
   * @param nodeId current node
   * @param graph adjacency list
   * @param reachable set to accumulate reachable nodes
   */
  private void dfsReachable(String nodeId, Map<String, List<String>> graph, Set<String> reachable) {
    if (reachable.contains(nodeId)) {
      return;
    }
    reachable.add(nodeId);
    for (String neighbor : graph.getOrDefault(nodeId, Collections.emptyList())) {
      dfsReachable(neighbor, graph, reachable);
    }
  }

  /**
   * Gets validation status for a flow.
   *
   * @param flowId the flow ID
   * @return the validation DTO
   */
  @Transactional(readOnly = true)
  public FlowValidationDto getValidationStatus(String flowId) {
    FlowValidation validation = validationRepository.findByFlowId(flowId)
        .orElseThrow(() -> new IllegalArgumentException("Validation record not found for flow: " + flowId));
    return FlowValidationDto.fromEntity(validation);
  }

  /**
   * Creates a new validation record for a flow.
   *
   * @param flowId the flow ID
   */
  @Transactional
  public void createValidationRecord(String flowId) {
    FlowValidation validation = FlowValidation.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .isValid(false)
        .validationErrorsJson("[]")
        .build();
    validationRepository.save(validation);
  }

  /**
   * Deletes validation record for a flow.
   *
   * @param flowId the flow ID
   */
  @Transactional
  public void deleteValidationRecord(String flowId) {
    validationRepository.findByFlowId(flowId).ifPresent(validationRepository::delete);
  }

  /**
   * Converts error list to JSON array string.
   *
   * @param errors list of error messages
   * @return JSON array string
   */
  private String convertErrorsToJson(List<String> errors) {
    try {
      List<ValidationErrorDto> errorDtos = errors.stream()
          .map(msg -> ValidationErrorDto.builder()
              .message(msg)
              .code("VALIDATION_ERROR")
              .build())
          .toList();
      return objectMapper.writeValueAsString(errorDtos);
    } catch (Exception e) {
      log.error("Failed to serialize validation errors", e);
      return "[]";
    }
  }
}
