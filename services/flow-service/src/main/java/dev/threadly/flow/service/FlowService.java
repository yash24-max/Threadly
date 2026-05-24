package dev.threadly.flow.service;

import dev.threadly.flow.dto.CreateFlowRequest;
import dev.threadly.flow.dto.FlowDto;
import dev.threadly.flow.dto.UpdateFlowRequest;
import dev.threadly.flow.entity.Flow;
import dev.threadly.flow.exception.FlowNotFoundException;
import dev.threadly.flow.repository.FlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for Flow CRUD operations.
 * Handles creation, reading, updating, and deletion of flows.
 * Enforces multi-tenancy and access control.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowService {

  private final FlowRepository flowRepository;
  private final FlowVersionService flowVersionService;
  private final FlowNodeService flowNodeService;
  private final FlowEdgeService flowEdgeService;
  private final FlowValidationService flowValidationService;

  /**
   * Creates a new flow in draft status.
   *
   * @param request the creation request containing bot ID, name, and description
   * @param orgId the organization ID (from security context)
   * @param userId the user ID (from security context)
   * @return the created flow DTO
   */
  @Transactional
  public FlowDto createFlow(CreateFlowRequest request, String orgId, String userId) {
    log.info("Creating new flow for bot {} in org {}", request.getBotId(), orgId);

    Flow flow = Flow.builder()
        .id(UUID.randomUUID().toString())
        .botId(request.getBotId())
        .orgId(orgId)
        .name(request.getName())
        .description(request.getDescription())
        .status(Flow.FlowStatus.DRAFT)
        .createdBy(userId)
        .build();

    Flow savedFlow = flowRepository.save(flow);

    // Initialize validation record
    flowValidationService.createValidationRecord(savedFlow.getId());

    log.info("Flow created successfully: {}", savedFlow.getId());
    return FlowDto.fromEntity(savedFlow);
  }

  /**
   * Retrieves a flow by ID with organization isolation.
   *
   * @param flowId the flow ID
   * @param orgId the organization ID
   * @return the flow DTO
   * @throws FlowNotFoundException if flow not found
   */
  @Transactional(readOnly = true)
  public FlowDto getFlow(String flowId, String orgId) {
    Flow flow = flowRepository.findByIdAndOrgId(flowId, orgId)
        .orElseThrow(() -> new FlowNotFoundException(flowId));
    return FlowDto.fromEntity(flow);
  }

  /**
   * Lists flows for a bot with pagination.
   *
   * @param botId the bot ID
   * @param orgId the organization ID
   * @param pageable pagination parameters
   * @return page of flow DTOs
   */
  @Transactional(readOnly = true)
  public Page<FlowDto> listFlowsByBot(String botId, String orgId, Pageable pageable) {
    return flowRepository.findByBotIdAndOrgId(botId, orgId, pageable)
        .map(FlowDto::fromEntity);
  }

  /**
   * Lists all flows for an organization.
   *
   * @param orgId the organization ID
   * @param pageable pagination parameters
   * @return page of flow DTOs
   */
  @Transactional(readOnly = true)
  public Page<FlowDto> listFlows(String orgId, Pageable pageable) {
    return flowRepository.findByOrgId(orgId, pageable)
        .map(FlowDto::fromEntity);
  }

  /**
   * Updates a flow (only draft flows can be updated).
   *
   * @param flowId the flow ID
   * @param orgId the organization ID
   * @param request the update request
   * @return the updated flow DTO
   * @throws FlowNotFoundException if flow not found
   */
  @Transactional
  public FlowDto updateFlow(String flowId, String orgId, UpdateFlowRequest request) {
    log.info("Updating flow: {}", flowId);

    Flow flow = flowRepository.findByIdAndOrgId(flowId, orgId)
        .orElseThrow(() -> new FlowNotFoundException(flowId));

    // Only draft flows can be edited
    if (!flow.getStatus().equals(Flow.FlowStatus.DRAFT)) {
      throw new IllegalStateException("Cannot update published or archived flows. Create a new version instead.");
    }

    if (request.getName() != null) {
      flow.setName(request.getName());
    }
    if (request.getDescription() != null) {
      flow.setDescription(request.getDescription());
    }
    if (request.getStatus() != null) {
      try {
        flow.setStatus(Flow.FlowStatus.valueOf(request.getStatus().toUpperCase()));
      } catch (IllegalArgumentException e) {
        log.warn("Invalid status: {}", request.getStatus());
      }
    }

    Flow updated = flowRepository.save(flow);
    log.info("Flow updated successfully: {}", flowId);
    return FlowDto.fromEntity(updated);
  }

  /**
   * Deletes a flow (only draft flows can be deleted).
   *
   * @param flowId the flow ID
   * @param orgId the organization ID
   * @throws FlowNotFoundException if flow not found
   */
  @Transactional
  public void deleteFlow(String flowId, String orgId) {
    log.info("Deleting flow: {}", flowId);

    Flow flow = flowRepository.findByIdAndOrgId(flowId, orgId)
        .orElseThrow(() -> new FlowNotFoundException(flowId));

    if (!flow.getStatus().equals(Flow.FlowStatus.DRAFT)) {
      throw new IllegalStateException("Cannot delete published or archived flows");
    }

    // Delete associated data
    flowNodeService.deleteAllNodes(flowId);
    flowEdgeService.deleteAllEdges(flowId);
    flowValidationService.deleteValidationRecord(flowId);
    flowVersionService.deleteAllVersions(flowId);

    flowRepository.deleteById(flowId);
    log.info("Flow deleted successfully: {}", flowId);
  }

  /**
   * Duplicates a flow, creating a new draft copy.
   *
   * @param flowId the flow ID to duplicate
   * @param orgId the organization ID
   * @param userId the user ID
   * @return the duplicated flow DTO
   * @throws FlowNotFoundException if flow not found
   */
  @Transactional
  public FlowDto duplicateFlow(String flowId, String orgId, String userId) {
    log.info("Duplicating flow: {}", flowId);

    Flow original = flowRepository.findByIdAndOrgId(flowId, orgId)
        .orElseThrow(() -> new FlowNotFoundException(flowId));

    Flow duplicate = Flow.builder()
        .id(UUID.randomUUID().toString())
        .botId(original.getBotId())
        .orgId(original.getOrgId())
        .name(original.getName() + " (Copy)")
        .description(original.getDescription())
        .status(Flow.FlowStatus.DRAFT)
        .createdBy(userId)
        .build();

    Flow saved = flowRepository.save(duplicate);

    // Copy nodes and edges if they exist
    flowNodeService.duplicateNodes(flowId, saved.getId());
    flowEdgeService.duplicateEdges(flowId, saved.getId());

    // Initialize validation
    flowValidationService.createValidationRecord(saved.getId());

    log.info("Flow duplicated successfully: {} -> {}", flowId, saved.getId());
    return FlowDto.fromEntity(saved);
  }

  /**
   * Checks if a flow exists and belongs to the organization.
   *
   * @param flowId the flow ID
   * @param orgId the organization ID
   * @return true if flow exists and belongs to org
   */
  public boolean flowExists(String flowId, String orgId) {
    return flowRepository.findByIdAndOrgId(flowId, orgId).isPresent();
  }
}
