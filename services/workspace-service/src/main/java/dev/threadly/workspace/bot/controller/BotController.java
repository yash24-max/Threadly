package dev.threadly.workspace.bot.controller;

import dev.threadly.workspace.bot.dto.BotDto;
import dev.threadly.workspace.bot.dto.CreateBotRequest;
import dev.threadly.workspace.bot.dto.UpdateBotRequest;
import dev.threadly.workspace.bot.service.BotService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for bot management endpoints.
 *
 * <p>Base path: /api/v1/bots
 *
 * <p>All endpoints enforce multi-tenancy (org_id from security context) and access control.
 */
@RestController
@RequestMapping("/api/v1/bots")
@RequiredArgsConstructor
@Slf4j
public class BotController {

  private final BotService botService;

  /**
   * Create a new bot.
   *
   * <p>POST /api/v1/bots
   *
   * @param request bot creation parameters
   * @param principal authenticated user
   * @return created bot DTO with 201 status
   */
  @PostMapping
  public ResponseEntity<BotDto> createBot(
      @Valid @RequestBody CreateBotRequest request,
      Principal principal) {
    log.info("Creating bot: {}", request.getName());

    String userId = principal.getName();
    String orgId = getOrgIdFromContext(); // From security context/headers

    BotDto bot = botService.createBot(orgId, userId, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(bot);
  }

  /**
   * List all bots for the organization.
   *
   * <p>GET /api/v1/bots
   *
   * @param pageable pagination parameters
   * @param principal authenticated user
   * @return paginated list of bots
   */
  @GetMapping
  public ResponseEntity<Page<BotDto>> listBots(
      Pageable pageable,
      Principal principal) {
    log.info("Listing bots for user: {}", principal.getName());

    String orgId = getOrgIdFromContext();

    Page<BotDto> bots = botService.listBots(orgId, pageable);

    return ResponseEntity.ok(bots);
  }

  /**
   * Search bots by name.
   *
   * <p>GET /api/v1/bots/search?query=searchTerm
   *
   * @param query search term
   * @param pageable pagination parameters
   * @param principal authenticated user
   * @return matching bots
   */
  @GetMapping("/search")
  public ResponseEntity<Page<BotDto>> searchBots(
      @RequestParam(value = "query", required = true) String query,
      Pageable pageable,
      Principal principal) {
    log.info("Searching bots with query: {}", query);

    String orgId = getOrgIdFromContext();

    Page<BotDto> results = botService.searchBots(orgId, query, pageable);

    return ResponseEntity.ok(results);
  }

  /**
   * Get a specific bot by ID.
   *
   * <p>GET /api/v1/bots/{botId}
   *
   * @param botId bot ID
   * @param principal authenticated user
   * @return bot DTO
   */
  @GetMapping("/{botId}")
  public ResponseEntity<BotDto> getBot(
      @PathVariable String botId,
      Principal principal) {
    log.info("Fetching bot: {}", botId);

    String userId = principal.getName();
    String orgId = getOrgIdFromContext();

    BotDto bot = botService.getBot(botId, orgId, userId);

    return ResponseEntity.ok(bot);
  }

  /**
   * Update a bot.
   *
   * <p>PATCH /api/v1/bots/{botId}
   *
   * <p>Requires EDITOR role or higher.
   *
   * @param botId bot ID
   * @param request update parameters
   * @param principal authenticated user
   * @return updated bot DTO
   */
  @PatchMapping("/{botId}")
  public ResponseEntity<BotDto> updateBot(
      @PathVariable String botId,
      @Valid @RequestBody UpdateBotRequest request,
      Principal principal) {
    log.info("Updating bot: {}", botId);

    String userId = principal.getName();
    String orgId = getOrgIdFromContext();

    BotDto bot = botService.updateBot(botId, orgId, userId, request);

    return ResponseEntity.ok(bot);
  }

  /**
   * Delete a bot (soft delete).
   *
   * <p>DELETE /api/v1/bots/{botId}
   *
   * <p>Requires OWNER role.
   *
   * @param botId bot ID
   * @param principal authenticated user
   * @return 204 No Content
   */
  @DeleteMapping("/{botId}")
  public ResponseEntity<Void> deleteBot(
      @PathVariable String botId,
      Principal principal) {
    log.info("Deleting bot: {}", botId);

    String userId = principal.getName();
    String orgId = getOrgIdFromContext();

    botService.deleteBot(botId, orgId, userId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Duplicate a bot.
   *
   * <p>POST /api/v1/bots/{botId}/duplicate
   *
   * <p>Request body: { "name": "New Bot Name" }
   *
   * @param botId source bot ID
   * @param request contains new bot name
   * @param principal authenticated user
   * @return duplicate bot DTO with 201 status
   */
  @PostMapping("/{botId}/duplicate")
  public ResponseEntity<BotDto> duplicateBot(
      @PathVariable String botId,
      @RequestBody CreateBotRequest request,
      Principal principal) {
    log.info("Duplicating bot: {} with name: {}", botId, request.getName());

    String userId = principal.getName();
    String orgId = getOrgIdFromContext();

    BotDto duplicate = botService.duplicateBot(botId, orgId, userId, request.getName());

    return ResponseEntity.status(HttpStatus.CREATED).body(duplicate);
  }

  /**
   * Get embed snippet for a bot.
   * GET /api/v1/bots/{botId}/embed
   */
  @GetMapping("/{botId}/embed")
  public ResponseEntity<java.util.Map<String, String>> getEmbedSnippet(
      @PathVariable String botId,
      Principal principal) {
    String orgId = getOrgIdFromContext();
    // Verify bot belongs to this org
    botService.getBot(botId, orgId, principal.getName());

    String snippet = String.format(
        "<script src=\"https://cdn.threadly.dev/widget.js\" data-bot-id=\"%s\" defer></script>",
        botId);
    return ResponseEntity.ok(java.util.Map.of("snippet", snippet));
  }

  /**
   * Extract organization ID from the Keycloak JWT claim "orgId".
   * Falls back to the X-Org-ID request header for internal service calls.
   *
   * @return organization ID
   */
  private String getOrgIdFromContext() {
    try {
      org.springframework.security.core.context.SecurityContext ctx =
          org.springframework.security.core.context.SecurityContextHolder.getContext();
      if (ctx.getAuthentication() != null
          && ctx.getAuthentication().getPrincipal() instanceof Jwt jwt) {
        String orgId = jwt.getClaimAsString("orgId");
        if (orgId != null && !orgId.isBlank()) return orgId;
      }
    } catch (Exception ignored) {
      // fall through to header check
    }
    // Header fallback (set by nginx or integration tests)
    jakarta.servlet.http.HttpServletRequest req =
        ((org.springframework.web.context.request.ServletRequestAttributes)
            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
            .getRequest();
    String headerOrgId = req.getHeader("X-Org-ID");
    if (headerOrgId != null && !headerOrgId.isBlank()) return headerOrgId;
    throw new dev.threadly.workspace.common.UnauthorizedException("Cannot determine orgId from security context");
  }
}
