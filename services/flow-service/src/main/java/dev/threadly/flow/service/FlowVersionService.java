package dev.threadly.flow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.flow.dto.FlowVersionDto;
import dev.threadly.flow.entity.Flow;
import dev.threadly.flow.entity.FlowPublishLog;
import dev.threadly.flow.entity.FlowVersion;
import dev.threadly.flow.exception.FlowNotFoundException;
import dev.threadly.flow.exception.FlowPublishException;
import dev.threadly.flow.repository.FlowPublishLogRepository;
import dev.threadly.flow.repository.FlowRepository;
import dev.threadly.flow.repository.FlowVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for Flow Version management.
 * Handles publishing, versioning, and rollback operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowVersionService {

  private final FlowVersionRepository versionRepository;
  private final FlowRepository flowRepository;
  private final FlowPublishLogRepository publishLogRepository;
  private final FlowValidationService validationService;
  private final FlowJsonParser jsonParser;
  private final ObjectMapper objectMapper;

  /**
   * Publishes the current draft as a new version.
   *
   * @param flowId the flow ID
   * @param userId the user ID (from security context)
   * @return the created version DTO
   * @throws FlowNotFoundException if flow not found
   * @throws FlowPublishException if validation fails
   */
  @Transactional
  public FlowVersionDto publishFlow(String flowId, String userId) {
    log.info("Publishing flow: {}", flowId);

    Flow flow = flowRepository.findById(flowId)
        .orElseThrow(() -> new FlowNotFoundException(flowId));

    // Validate before publishing
    var validation = validationService.validateFlow(flowId);
    if (!validation.getIsValid()) {
      throw new FlowPublishException(flowId, "Flow has validation errors");
    }

    // Get next version number
    Integer nextVersion = versionRepository.getLatestVersionNumber(flowId) + 1;

    // Build flow definition JSON
    String definitionJson = jsonParser.buildFlowDefinitionJson(flowId);

    // Create new version
    FlowVersion version = FlowVersion.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .versionNumber(nextVersion)
        .definitionJson(definitionJson)
        .publishedAt(LocalDateTime.now())
        .publishedBy(userId)
        .isActive(true)
        .build();

    // Deactivate previous active version if exists
    versionRepository.findByFlowIdAndIsActiveTrue(flowId)
        .ifPresent(prev -> {
          prev.setIsActive(false);
          versionRepository.save(prev);
        });

    FlowVersion saved = versionRepository.save(version);

    // Update flow status and current version
    flow.setStatus(Flow.FlowStatus.PUBLISHED);
    flow.setCurrentVersionId(saved.getId());
    flowRepository.save(flow);

    // Log publish event
    FlowPublishLog publishLog = FlowPublishLog.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .publishedBy(userId)
        .eventType(FlowPublishLog.EventType.PUBLISHED)
        .previousVersionId(versionRepository.findByFlowIdAndVersionNumber(flowId, nextVersion - 1)
            .map(FlowVersion::getId).orElse(null))
        .currentVersionId(saved.getId())
        .build();
    publishLogRepository.save(publishLog);

    log.info("Flow published successfully: {} (version {})", flowId, nextVersion);
    return FlowVersionDto.fromEntity(saved);
  }

  /**
   * Gets a specific version of a flow.
   *
   * @param flowId the flow ID
   * @param versionNumber the version number
   * @return the version DTO
   * @throws FlowNotFoundException if version not found
   */
  @Transactional(readOnly = true)
  public FlowVersionDto getVersion(String flowId, Integer versionNumber) {
    FlowVersion version = versionRepository.findByFlowIdAndVersionNumber(flowId, versionNumber)
        .orElseThrow(() -> new FlowNotFoundException(flowId,
            "Version " + versionNumber + " not found"));
    return FlowVersionDto.fromEntity(version);
  }

  /**
   * Gets the active published version of a flow.
   *
   * @param flowId the flow ID
   * @return the version DTO
   * @throws FlowNotFoundException if no active version found
   */
  @Transactional(readOnly = true)
  public FlowVersionDto getActiveVersion(String flowId) {
    FlowVersion version = versionRepository.findByFlowIdAndIsActiveTrue(flowId)
        .orElseThrow(() -> new FlowNotFoundException(flowId, "No published version found"));
    return FlowVersionDto.fromEntity(version);
  }

  /**
   * Lists all versions of a flow.
   *
   * @param flowId the flow ID
   * @param pageable pagination parameters
   * @return page of versions
   */
  @Transactional(readOnly = true)
  public Page<FlowVersionDto> listVersions(String flowId, Pageable pageable) {
    return versionRepository.findByFlowIdOrderByVersionNumberDesc(flowId, pageable)
        .map(FlowVersionDto::fromEntity);
  }

  /**
   * Rolls back to a previous version.
   * Creates a new active version with the content of the previous version.
   *
   * @param flowId the flow ID
   * @param versionNumber the version number to rollback to
   * @param userId the user ID (from security context)
   * @param reason the rollback reason
   * @return the rollback version DTO
   * @throws FlowNotFoundException if version not found
   */
  @Transactional
  public FlowVersionDto rollbackVersion(String flowId, Integer versionNumber, String userId, String reason) {
    log.info("Rolling back flow {} to version {}", flowId, versionNumber);

    Flow flow = flowRepository.findById(flowId)
        .orElseThrow(() -> new FlowNotFoundException(flowId));

    FlowVersion sourceVersion = versionRepository.findByFlowIdAndVersionNumber(flowId, versionNumber)
        .orElseThrow(() -> new FlowNotFoundException(flowId,
            "Version " + versionNumber + " not found"));

    // Get next version number
    Integer nextVersion = versionRepository.getLatestVersionNumber(flowId) + 1;

    // Create rollback version with same definition
    FlowVersion rollbackVersion = FlowVersion.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .versionNumber(nextVersion)
        .definitionJson(sourceVersion.getDefinitionJson())
        .publishedAt(LocalDateTime.now())
        .publishedBy(userId)
        .isActive(true)
        .build();

    // Deactivate current active version
    versionRepository.findByFlowIdAndIsActiveTrue(flowId)
        .ifPresent(prev -> {
          prev.setIsActive(false);
          versionRepository.save(prev);
        });

    FlowVersion saved = versionRepository.save(rollbackVersion);

    // Update flow
    flow.setCurrentVersionId(saved.getId());
    flowRepository.save(flow);

    // Log rollback event
    FlowPublishLog rollbackLog = FlowPublishLog.builder()
        .id(UUID.randomUUID().toString())
        .flowId(flowId)
        .publishedBy(userId)
        .eventType(FlowPublishLog.EventType.ROLLBACK)
        .previousVersionId(versionRepository.findByFlowIdAndVersionNumber(flowId, nextVersion - 1)
            .map(FlowVersion::getId).orElse(null))
        .currentVersionId(saved.getId())
        .rollbackReason(reason)
        .build();
    publishLogRepository.save(rollbackLog);

    log.info("Flow rolled back successfully: {} (from version {} to {})", flowId, versionNumber, nextVersion);
    return FlowVersionDto.fromEntity(saved);
  }

  /**
   * Deletes all versions of a flow.
   *
   * @param flowId the flow ID
   */
  @Transactional
  public void deleteAllVersions(String flowId) {
    var versions = versionRepository.findAllByFlowId(flowId);
    versionRepository.deleteAll(versions);
    log.info("All versions deleted for flow: {}", flowId);
  }

  /**
   * Gets the version count for a flow.
   *
   * @param flowId the flow ID
   * @return the count
   */
  public Integer getVersionCount(String flowId) {
    Integer latest = versionRepository.getLatestVersionNumber(flowId);
    return latest != null ? latest : 0;
  }
}
