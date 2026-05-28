package dev.threadly.flow.controller;

import dev.threadly.flow.dto.CreateFlowRequest;
import dev.threadly.flow.dto.FlowDto;
import dev.threadly.flow.dto.UpdateFlowRequest;
import dev.threadly.flow.service.FlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Flow operations.
 * Handles CRUD operations for flows.
 * Enforces multi-tenancy and access control via security context.
 */
@RestController
@RequestMapping("/api/v1/flows")
@RequiredArgsConstructor
@Slf4j
public class FlowController {

  private final FlowService flowService;

  /**
   * Creates a new flow.
   *
   * POST /api/v1/flows
   * @param request the creation request
   * @param auth the security authentication object
   * @return the created flow
   */
  @PostMapping
  public ResponseEntity<FlowDto> createFlow(
      @Valid @RequestBody CreateFlowRequest request,
      Authentication auth) {
    log.info("Creating new flow: {}", request.getName());

    String orgId = auth.getName(); // Org ID from JWT
    String userId = (String) auth.getCredentials();
    FlowDto created = flowService.createFlow(request, orgId, userId);

    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * Retrieves a specific flow.
   *
   * GET /api/v1/flows/{flowId}
   * @param flowId the flow ID
   * @param auth the security authentication object
   * @return the flow details
   */
  @GetMapping("/{flowId}")
  public ResponseEntity<FlowDto> getFlow(
      @PathVariable String flowId,
      Authentication auth) {
    log.debug("Retrieving flow: {}", flowId);

    String orgId = auth.getName();
    FlowDto flow = flowService.getFlow(flowId, orgId);

    return ResponseEntity.ok(flow);
  }

  /**
   * Lists flows for a specific bot.
   *
   * GET /api/v1/flows?botId={botId}&page=0&size=20
   * @param botId the bot ID
   * @param pageable pagination parameters
   * @param auth the security authentication object
   * @return page of flows
   */
  @GetMapping
  public ResponseEntity<Page<FlowDto>> listFlows(
      @RequestParam(required = false) String botId,
      Pageable pageable,
      Authentication auth) {
    log.debug("Listing flows for bot: {}, page: {}", botId, pageable.getPageNumber());

    String orgId = auth.getName();
    String userId = (String) auth.getCredentials();
    Page<FlowDto> flows;

    if (botId != null && !botId.isEmpty()) {
      flows = flowService.listFlowsByBot(botId, orgId, pageable);
      // BE-007: Auto-create an empty default flow for new bots that have no flows yet.
      // This prevents the builder from spinning forever on first open.
      if (flows.isEmpty()) {
        log.info("No flows found for bot: {}. Auto-creating default empty flow.", botId);
        CreateFlowRequest defaultFlow = CreateFlowRequest.builder()
            .botId(botId)
            .name("Main Flow")
            .description("Default flow created automatically")
            .build();
        flowService.createFlow(defaultFlow, orgId, userId);
        flows = flowService.listFlowsByBot(botId, orgId, pageable);
      }
    } else {
      flows = flowService.listFlows(orgId, pageable);
    }

    return ResponseEntity.ok(flows);
  }

  /**
   * Updates a flow.
   *
   * PATCH /api/v1/flows/{flowId}
   * @param flowId the flow ID
   * @param request the update request
   * @param auth the security authentication object
   * @return the updated flow
   */
  @PatchMapping("/{flowId}")
  public ResponseEntity<FlowDto> updateFlow(
      @PathVariable String flowId,
      @Valid @RequestBody UpdateFlowRequest request,
      Authentication auth) {
    log.info("Updating flow: {}", flowId);

    String orgId = auth.getName();
    FlowDto updated = flowService.updateFlow(flowId, orgId, request);

    return ResponseEntity.ok(updated);
  }

  /**
   * Deletes a flow.
   *
   * DELETE /api/v1/flows/{flowId}
   * @param flowId the flow ID
   * @param auth the security authentication object
   * @return no content
   */
  @DeleteMapping("/{flowId}")
  public ResponseEntity<Void> deleteFlow(
      @PathVariable String flowId,
      Authentication auth) {
    log.info("Deleting flow: {}", flowId);

    String orgId = auth.getName();
    flowService.deleteFlow(flowId, orgId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Duplicates a flow.
   *
   * POST /api/v1/flows/{flowId}/duplicate
   * @param flowId the flow ID to duplicate
   * @param auth the security authentication object
   * @return the duplicated flow
   */
  @PostMapping("/{flowId}/duplicate")
  public ResponseEntity<FlowDto> duplicateFlow(
      @PathVariable String flowId,
      Authentication auth) {
    log.info("Duplicating flow: {}", flowId);

    String orgId = auth.getName();
    String userId = (String) auth.getCredentials();
    FlowDto duplicate = flowService.duplicateFlow(flowId, orgId, userId);

    return ResponseEntity.status(HttpStatus.CREATED).body(duplicate);
  }
}
