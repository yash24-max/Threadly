package dev.threadly.flow.controller;

import dev.threadly.flow.dto.CreateFlowEdgeRequest;
import dev.threadly.flow.dto.FlowEdgeDto;
import dev.threadly.flow.service.FlowEdgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Flow Edge operations.
 * Handles CRUD operations for edges within flows.
 */
@RestController
@RequestMapping("/api/v1/flows/{flowId}/edges")
@RequiredArgsConstructor
@Slf4j
public class FlowEdgeController {

  private final FlowEdgeService flowEdgeService;

  /**
   * Adds a new edge between two nodes.
   *
   * POST /api/v1/flows/{flowId}/edges
   * @param flowId the flow ID
   * @param request the edge creation request
   * @return the created edge
   */
  @PostMapping
  public ResponseEntity<FlowEdgeDto> addEdge(
      @PathVariable String flowId,
      @Valid @RequestBody CreateFlowEdgeRequest request) {
    log.info("Adding edge to flow {}: {} -> {}", flowId, request.getSourceNodeId(), request.getTargetNodeId());

    FlowEdgeDto created = flowEdgeService.addEdge(flowId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * Updates an existing edge.
   *
   * PATCH /api/v1/flows/{flowId}/edges/{edgeId}
   * @param flowId the flow ID
   * @param edgeId the edge ID
   * @param request the update request
   * @return the updated edge
   */
  @PatchMapping("/{edgeId}")
  public ResponseEntity<FlowEdgeDto> updateEdge(
      @PathVariable String flowId,
      @PathVariable String edgeId,
      @Valid @RequestBody CreateFlowEdgeRequest request) {
    log.info("Updating edge {} in flow {}", edgeId, flowId);

    FlowEdgeDto updated = flowEdgeService.updateEdge(flowId, edgeId, request);
    return ResponseEntity.ok(updated);
  }

  /**
   * Removes an edge from a flow.
   *
   * DELETE /api/v1/flows/{flowId}/edges/{edgeId}
   * @param flowId the flow ID
   * @param edgeId the edge ID
   * @return no content
   */
  @DeleteMapping("/{edgeId}")
  public ResponseEntity<Void> removeEdge(
      @PathVariable String flowId,
      @PathVariable String edgeId) {
    log.info("Removing edge {} from flow {}", edgeId, flowId);

    flowEdgeService.removeEdge(flowId, edgeId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Lists all edges in a flow.
   *
   * GET /api/v1/flows/{flowId}/edges
   * @param flowId the flow ID
   * @return list of edges
   */
  @GetMapping
  public ResponseEntity<List<FlowEdgeDto>> listEdges(
      @PathVariable String flowId) {
    log.debug("Listing edges for flow: {}", flowId);

    List<FlowEdgeDto> edges = flowEdgeService.listEdges(flowId);
    return ResponseEntity.ok(edges);
  }

  /**
   * Gets a specific edge.
   *
   * GET /api/v1/flows/{flowId}/edges/{edgeId}
   * @param flowId the flow ID
   * @param edgeId the edge ID
   * @return the edge details
   */
  @GetMapping("/{edgeId}")
  public ResponseEntity<FlowEdgeDto> getEdge(
      @PathVariable String flowId,
      @PathVariable String edgeId) {
    log.debug("Retrieving edge {} from flow {}", edgeId, flowId);

    FlowEdgeDto edge = flowEdgeService.getEdge(flowId, edgeId);
    return ResponseEntity.ok(edge);
  }
}
