package dev.threadly.flow.controller;

import dev.threadly.flow.dto.CreateFlowNodeRequest;
import dev.threadly.flow.dto.FlowNodeDto;
import dev.threadly.flow.dto.UpdateFlowNodeRequest;
import dev.threadly.flow.service.FlowNodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Flow Node operations.
 * Handles CRUD operations for nodes within flows.
 */
@RestController
@RequestMapping("/api/v1/flows/{flowId}/nodes")
@RequiredArgsConstructor
@Slf4j
public class FlowNodeController {

  private final FlowNodeService flowNodeService;

  /**
   * Adds a new node to a flow.
   *
   * POST /api/v1/flows/{flowId}/nodes
   * @param flowId the flow ID
   * @param request the node creation request
   * @return the created node
   */
  @PostMapping
  public ResponseEntity<FlowNodeDto> addNode(
      @PathVariable String flowId,
      @Valid @RequestBody CreateFlowNodeRequest request) {
    log.info("Adding node to flow {}: {}", flowId, request.getNodeId());

    FlowNodeDto created = flowNodeService.addNode(flowId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * Updates an existing node.
   *
   * PATCH /api/v1/flows/{flowId}/nodes/{nodeId}
   * @param flowId the flow ID
   * @param nodeId the node ID
   * @param request the update request
   * @return the updated node
   */
  @PatchMapping("/{nodeId}")
  public ResponseEntity<FlowNodeDto> updateNode(
      @PathVariable String flowId,
      @PathVariable String nodeId,
      @Valid @RequestBody UpdateFlowNodeRequest request) {
    log.info("Updating node {} in flow {}", nodeId, flowId);

    FlowNodeDto updated = flowNodeService.updateNode(flowId, nodeId, request);
    return ResponseEntity.ok(updated);
  }

  /**
   * Removes a node from a flow.
   *
   * DELETE /api/v1/flows/{flowId}/nodes/{nodeId}
   * @param flowId the flow ID
   * @param nodeId the node ID
   * @return no content
   */
  @DeleteMapping("/{nodeId}")
  public ResponseEntity<Void> removeNode(
      @PathVariable String flowId,
      @PathVariable String nodeId) {
    log.info("Removing node {} from flow {}", nodeId, flowId);

    flowNodeService.removeNode(flowId, nodeId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Lists all nodes in a flow.
   *
   * GET /api/v1/flows/{flowId}/nodes
   * @param flowId the flow ID
   * @return list of nodes
   */
  @GetMapping
  public ResponseEntity<List<FlowNodeDto>> listNodes(
      @PathVariable String flowId) {
    log.debug("Listing nodes for flow: {}", flowId);

    List<FlowNodeDto> nodes = flowNodeService.listNodes(flowId);
    return ResponseEntity.ok(nodes);
  }

  /**
   * Gets a specific node.
   *
   * GET /api/v1/flows/{flowId}/nodes/{nodeId}
   * @param flowId the flow ID
   * @param nodeId the node ID
   * @return the node details
   */
  @GetMapping("/{nodeId}")
  public ResponseEntity<FlowNodeDto> getNode(
      @PathVariable String flowId,
      @PathVariable String nodeId) {
    log.debug("Retrieving node {} from flow {}", nodeId, flowId);

    FlowNodeDto node = flowNodeService.getNode(flowId, nodeId);
    return ResponseEntity.ok(node);
  }
}
