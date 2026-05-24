package dev.threadly.workspace.bot.service;

import dev.threadly.workspace.bot.dto.BotVersionDto;
import dev.threadly.workspace.bot.entity.BotVersion;
import dev.threadly.workspace.bot.event.BotPublishedEvent;
import dev.threadly.workspace.bot.exception.BotNotFoundException;
import dev.threadly.workspace.bot.repository.BotRepository;
import dev.threadly.workspace.bot.repository.BotVersionRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for bot versioning and publication.
 *
 * <p>Handles:
 * - Creating bot versions on publish
 * - Maintaining version history
 * - Rollback to previous versions
 * - Configuration snapshots
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotVersionService {

  private final BotVersionRepository botVersionRepository;
  private final BotRepository botRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  /**
   * Publish current bot configuration as a new version.
   *
   * <p>Increments version number and creates snapshot.
   *
   * @param botId bot ID
   * @param orgId organization ID
   * @param userId user publishing the version
   * @param configSnapshot JSON snapshot of bot configuration
   * @param releaseNotes optional release notes
   * @return published version DTO
   * @throws BotNotFoundException if bot not found
   */
  @Transactional
  public BotVersionDto publishVersion(
      String botId,
      String orgId,
      String userId,
      String configSnapshot,
      String releaseNotes) {
    log.info("Publishing version for bot '{}' by user '{}'", botId, userId);

    // Verify bot exists
    botRepository.findByIdAndOrgId(botId, orgId)
        .orElseThrow(() -> BotNotFoundException.forBotInOrg(botId, orgId));

    // Get next version number
    Integer nextVersion = botVersionRepository.getLatestVersionNumber(botId) + 1;

    final Instant now = Instant.now();
    BotVersion version = BotVersion.builder()
        .id(UUID.randomUUID().toString())
        .botId(botId)
        .versionNumber(nextVersion)
        .configSnapshot(configSnapshot)
        .publishedAt(now)
        .publishedBy(userId)
        .releaseNotes(releaseNotes)
        .build();

    version = botVersionRepository.save(version);

    // Publish event
    BotPublishedEvent event = BotPublishedEvent.builder()
        .botId(botId)
        .orgId(orgId)
        .versionNumber(nextVersion)
        .publishedBy(userId)
        .releaseNotes(releaseNotes)
        .timestamp(now)
        .build();
    kafkaTemplate.send("threadly.bot.published", event);

    log.info("Bot '{}' published as version {}", botId, nextVersion);

    return mapToDto(version);
  }

  /**
   * Get a specific bot version.
   *
   * @param botId bot ID
   * @param versionNumber version number
   * @return version DTO
   * @throws BotNotFoundException if version not found
   */
  @Transactional(readOnly = true)
  public BotVersionDto getVersion(String botId, Integer versionNumber) {
    log.debug("Fetching version {} for bot '{}'", versionNumber, botId);

    BotVersion version = botVersionRepository.findByBotIdAndVersionNumber(botId, versionNumber)
        .orElseThrow(() -> new BotNotFoundException(
            String.format("Version %d not found for bot %s", versionNumber, botId)));

    return mapToDto(version);
  }

  /**
   * List all versions of a bot.
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return page of versions (newest first)
   */
  @Transactional(readOnly = true)
  public Page<BotVersionDto> listVersions(String botId, Pageable pageable) {
    log.debug("Listing versions for bot '{}'", botId);

    return botVersionRepository.findByBotIdOrderByVersionNumberDesc(botId, pageable)
        .map(this::mapToDto);
  }

  /**
   * Get the latest published version of a bot.
   *
   * @param botId bot ID
   * @return latest version DTO
   * @throws BotNotFoundException if no versions exist
   */
  @Transactional(readOnly = true)
  public BotVersionDto getLatestVersion(String botId) {
    log.debug("Fetching latest version for bot '{}'", botId);

    BotVersion version = botVersionRepository.findLatestByBotId(botId)
        .orElseThrow(() -> new BotNotFoundException(
            String.format("No versions found for bot %s", botId)));

    return mapToDto(version);
  }

  /**
   * Rollback to a previous version.
   *
   * <p>Creates a new version identical to the rolled-back version.
   *
   * @param botId bot ID
   * @param rollbackToVersion version to rollback to
   * @param userId user requesting rollback
   * @return new version created from rollback
   */
  @Transactional
  public BotVersionDto rollbackVersion(
      String botId, Integer rollbackToVersion, String userId) {
    log.info("Rolling back bot '{}' to version {} by user '{}'",
        botId, rollbackToVersion, userId);

    BotVersion sourceVersion = botVersionRepository.findByBotIdAndVersionNumber(
        botId, rollbackToVersion)
        .orElseThrow(() -> new BotNotFoundException(
            String.format("Version %d not found for bot %s", rollbackToVersion, botId)));

    // Create new version with same config
    Integer nextVersion = botVersionRepository.getLatestVersionNumber(botId) + 1;
    final Instant now = Instant.now();

    BotVersion newVersion = BotVersion.builder()
        .id(UUID.randomUUID().toString())
        .botId(botId)
        .versionNumber(nextVersion)
        .configSnapshot(sourceVersion.getConfigSnapshot())
        .publishedAt(now)
        .publishedBy(userId)
        .releaseNotes(String.format("Rollback to version %d", rollbackToVersion))
        .build();

    newVersion = botVersionRepository.save(newVersion);

    log.info("Bot '{}' rolled back to version {}; created new version {}",
        botId, rollbackToVersion, nextVersion);

    return mapToDto(newVersion);
  }

  /**
   * Count versions for a bot.
   *
   * @param botId bot ID
   * @return count of versions
   */
  @Transactional(readOnly = true)
  public long countVersions(String botId) {
    return botVersionRepository.countByBotId(botId);
  }

  /**
   * Map BotVersion entity to DTO.
   */
  private BotVersionDto mapToDto(BotVersion version) {
    return BotVersionDto.builder()
        .id(version.getId())
        .botId(version.getBotId())
        .versionNumber(version.getVersionNumber())
        .configSnapshot(version.getConfigSnapshot())
        .publishedAt(version.getPublishedAt())
        .publishedBy(version.getPublishedBy())
        .releaseNotes(version.getReleaseNotes())
        .build();
  }
}
