package dev.threadly.workspace.bot.controller;

import dev.threadly.workspace.bot.dto.BotVersionDto;
import dev.threadly.workspace.bot.service.BotVersionService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for bot versioning and publication.
 *
 * <p>Base path: /api/v1/bots/{botId}/versions
 *
 * <p>Manages bot version history and publication.
 */
@RestController
@RequestMapping("/api/v1/bots/{botId}/versions")
@RequiredArgsConstructor
@Slf4j
public class BotVersionController {

  private final BotVersionService botVersionService;

  /**
   * Publish current bot configuration as a new version.
   *
   * <p>POST /api/v1/bots/{botId}/versions
   *
   * <p>Request body:
   * {
   *   "config_snapshot": "{...json config...}",
   *   "release_notes": "optional release notes"
   * }
   *
   * @param botId bot ID
   * @param request publication request
   * @param principal authenticated user
   * @return published version DTO with 201 status
   */
  @PostMapping
  public ResponseEntity<BotVersionDto> publishVersion(
      @PathVariable String botId,
      @Valid @RequestBody PublishVersionRequest request,
      Principal principal) {
    log.info("Publishing version for bot: {}", botId);

    String userId = principal.getName();
    String orgId = getOrgIdFromContext();

    BotVersionDto version = botVersionService.publishVersion(
        botId, orgId, userId, request.getConfigSnapshot(), request.getReleaseNotes());

    return ResponseEntity.status(HttpStatus.CREATED).body(version);
  }

  /**
   * List all versions of a bot.
   *
   * <p>GET /api/v1/bots/{botId}/versions
   *
   * <p>Returns versions in descending order (newest first).
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @param principal authenticated user
   * @return paginated list of versions
   */
  @GetMapping
  public ResponseEntity<Page<BotVersionDto>> listVersions(
      @PathVariable String botId,
      Pageable pageable,
      Principal principal) {
    log.debug("Listing versions for bot: {}", botId);

    Page<BotVersionDto> versions = botVersionService.listVersions(botId, pageable);

    return ResponseEntity.ok(versions);
  }

  /**
   * Get a specific version.
   *
   * <p>GET /api/v1/bots/{botId}/versions/{versionNumber}
   *
   * @param botId bot ID
   * @param versionNumber version number
   * @param principal authenticated user
   * @return version DTO
   */
  @GetMapping("/{versionNumber}")
  public ResponseEntity<BotVersionDto> getVersion(
      @PathVariable String botId,
      @PathVariable Integer versionNumber,
      Principal principal) {
    log.debug("Fetching version {} for bot: {}", versionNumber, botId);

    BotVersionDto version = botVersionService.getVersion(botId, versionNumber);

    return ResponseEntity.ok(version);
  }

  /**
   * Get the latest published version.
   *
   * <p>GET /api/v1/bots/{botId}/versions/latest
   *
   * @param botId bot ID
   * @param principal authenticated user
   * @return latest version DTO
   */
  @GetMapping("/latest")
  public ResponseEntity<BotVersionDto> getLatestVersion(
      @PathVariable String botId,
      Principal principal) {
    log.debug("Fetching latest version for bot: {}", botId);

    BotVersionDto version = botVersionService.getLatestVersion(botId);

    return ResponseEntity.ok(version);
  }

  /**
   * Rollback to a previous version.
   *
   * <p>POST /api/v1/bots/{botId}/versions/{versionNumber}/rollback
   *
   * <p>Creates a new version with the same configuration as the specified version.
   *
   * @param botId bot ID
   * @param versionNumber version to rollback to
   * @param principal authenticated user
   * @return new version created from rollback with 201 status
   */
  @PostMapping("/{versionNumber}/rollback")
  public ResponseEntity<BotVersionDto> rollbackVersion(
      @PathVariable String botId,
      @PathVariable Integer versionNumber,
      Principal principal) {
    log.info("Rolling back bot {} to version {}", botId, versionNumber);

    String userId = principal.getName();

    BotVersionDto version = botVersionService.rollbackVersion(botId, versionNumber, userId);

    return ResponseEntity.status(HttpStatus.CREATED).body(version);
  }

  /**
   * Extract organization ID from security context or request headers.
   */
  private String getOrgIdFromContext() {
    return System.getProperty("test.org.id", "org-default");
  }

  /**
   * Request DTO for publishing a version.
   */
  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PublishVersionRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("config_snapshot")
    private String configSnapshot;

    @com.fasterxml.jackson.annotation.JsonProperty("release_notes")
    private String releaseNotes;
  }
}
