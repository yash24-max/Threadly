package dev.threadly.flow.controller;

import dev.threadly.flow.dto.FlowPublishHistoryDto;
import dev.threadly.flow.dto.FlowVersionDto;
import dev.threadly.flow.repository.FlowPublishLogRepository;
import dev.threadly.flow.service.FlowVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Flow Version and Publishing operations.
 * Handles versioning, publishing, and rollback operations.
 */
@RestController
@RequestMapping("/api/v1/flows/{flowId}")
@RequiredArgsConstructor
@Slf4j
public class FlowVersionController {

  private final FlowVersionService versionService;
  private final FlowPublishLogRepository publishLogRepository;

  /**
   * Publishes the current draft as a new version.
   *
   * POST /api/v1/flows/{flowId}/publish
   * @param flowId the flow ID
   * @param auth the security authentication object
   * @return the published version
   */
  @PostMapping("/publish")
  public ResponseEntity<FlowVersionDto> publishFlow(
      @PathVariable String flowId,
      Authentication auth) {
    log.info("Publishing flow: {}", flowId);

    String userId = (String) auth.getCredentials();
    FlowVersionDto version = versionService.publishFlow(flowId, userId);

    return ResponseEntity.status(HttpStatus.CREATED).body(version);
  }

  /**
   * Gets a specific version of a flow.
   *
   * GET /api/v1/flows/{flowId}/versions/{versionNumber}
   * @param flowId the flow ID
   * @param versionNumber the version number
   * @return the version details
   */
  @GetMapping("/versions/{versionNumber}")
  public ResponseEntity<FlowVersionDto> getVersion(
      @PathVariable String flowId,
      @PathVariable Integer versionNumber) {
    log.debug("Retrieving flow {} version {}", flowId, versionNumber);

    FlowVersionDto version = versionService.getVersion(flowId, versionNumber);
    return ResponseEntity.ok(version);
  }

  /**
   * Gets the active published version of a flow.
   *
   * GET /api/v1/flows/{flowId}/version/active
   * @param flowId the flow ID
   * @return the active version
   */
  @GetMapping("/version/active")
  public ResponseEntity<FlowVersionDto> getActiveVersion(
      @PathVariable String flowId) {
    log.debug("Retrieving active version for flow: {}", flowId);

    FlowVersionDto version = versionService.getActiveVersion(flowId);
    return ResponseEntity.ok(version);
  }

  /**
   * Lists all versions of a flow.
   *
   * GET /api/v1/flows/{flowId}/versions?page=0&size=20
   * @param flowId the flow ID
   * @param pageable pagination parameters
   * @return page of versions
   */
  @GetMapping("/versions")
  public ResponseEntity<Page<FlowVersionDto>> listVersions(
      @PathVariable String flowId,
      Pageable pageable) {
    log.debug("Listing versions for flow: {}, page: {}", flowId, pageable.getPageNumber());

    Page<FlowVersionDto> versions = versionService.listVersions(flowId, pageable);
    return ResponseEntity.ok(versions);
  }

  /**
   * Rolls back to a previous version.
   *
   * POST /api/v1/flows/{flowId}/versions/{versionNumber}/rollback
   * @param flowId the flow ID
   * @param versionNumber the version number to rollback to
   * @param reason the rollback reason
   * @param auth the security authentication object
   * @return the rollback version
   */
  @PostMapping("/versions/{versionNumber}/rollback")
  public ResponseEntity<FlowVersionDto> rollbackVersion(
      @PathVariable String flowId,
      @PathVariable Integer versionNumber,
      @RequestParam(required = false, defaultValue = "Manual rollback") String reason,
      Authentication auth) {
    log.info("Rolling back flow {} to version {}", flowId, versionNumber);

    String userId = (String) auth.getCredentials();
    FlowVersionDto version = versionService.rollbackVersion(flowId, versionNumber, userId, reason);

    return ResponseEntity.ok(version);
  }

  /**
   * Gets publish history for a flow.
   *
   * GET /api/v1/flows/{flowId}/publish-history?page=0&size=20
   * @param flowId the flow ID
   * @param pageable pagination parameters
   * @return page of publish history records
   */
  @GetMapping("/publish-history")
  public ResponseEntity<Page<FlowPublishHistoryDto>> getPublishHistory(
      @PathVariable String flowId,
      Pageable pageable) {
    log.debug("Retrieving publish history for flow: {}", flowId);

    Page<FlowPublishHistoryDto> history = publishLogRepository.findByFlowIdOrderByCreatedAtDesc(flowId, pageable)
        .map(FlowPublishHistoryDto::fromEntity);

    return ResponseEntity.ok(history);
  }

  /**
   * Gets the most recent publish event for a flow.
   *
   * GET /api/v1/flows/{flowId}/publish-history/latest
   * @param flowId the flow ID
   * @return the most recent publish event
   */
  @GetMapping("/publish-history/latest")
  public ResponseEntity<FlowPublishHistoryDto> getLatestPublishEvent(
      @PathVariable String flowId) {
    log.debug("Retrieving latest publish event for flow: {}", flowId);

    FlowPublishHistoryDto latest = publishLogRepository.getMostRecentPublish(flowId)
        .map(FlowPublishHistoryDto::fromEntity)
        .orElseThrow(() -> new IllegalArgumentException("No publish history found for flow: " + flowId));

    return ResponseEntity.ok(latest);
  }
}
