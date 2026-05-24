package dev.threadly.common.feign;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Workspace Service (:3002).
 *
 * Endpoints:
 * - Bot CRUD
 * - Workspace settings
 * - Team management
 */
@FeignClient(
    name = "workspace-service",
    url = "${threadly.services.workspace-service.url:http://workspace-service:3002}"
)
public interface WorkspaceServiceClient {

  /**
   * GET /bots — List bots in org.
   */
  @GetMapping("/bots")
  BotsListResponse listBots(@RequestHeader("Authorization") String token);

  /**
   * POST /bots — Create new bot.
   */
  @PostMapping("/bots")
  BotDTO createBot(
      @RequestBody CreateBotRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /bots/{botId} — Get bot details.
   */
  @GetMapping("/bots/{botId}")
  BotDTO getBot(
      @PathVariable UUID botId,
      @RequestHeader("Authorization") String token
  );

  /**
   * PATCH /bots/{botId} — Update bot settings.
   */
  @PatchMapping("/bots/{botId}")
  BotDTO updateBot(
      @PathVariable UUID botId,
      @RequestBody UpdateBotRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * DELETE /bots/{botId} — Archive/delete bot.
   */
  @DeleteMapping("/bots/{botId}")
  void deleteBot(
      @PathVariable UUID botId,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /workspace/settings — Get workspace settings.
   */
  @GetMapping("/workspace/settings")
  WorkspaceSettingsDTO getWorkspaceSettings(@RequestHeader("Authorization") String token);

  /**
   * PATCH /workspace/settings — Update workspace settings.
   */
  @PatchMapping("/workspace/settings")
  WorkspaceSettingsDTO updateWorkspaceSettings(
      @RequestBody UpdateWorkspaceSettingsRequest request,
      @RequestHeader("Authorization") String token
  );

  // DTOs

  record BotsListResponse(List<BotDTO> bots, int total) {}

  record BotDTO(
      UUID botId,
      UUID orgId,
      String name,
      String description,
      String language,
      String accentColor,
      String avatarUrl,
      String status,
      java.time.Instant createdAt,
      java.time.Instant updatedAt
  ) {}

  record CreateBotRequest(
      String name,
      String description,
      String language,
      String accentColor
  ) {}

  record UpdateBotRequest(
      String name,
      String description,
      String language,
      String accentColor,
      String status
  ) {}

  record WorkspaceSettingsDTO(
      UUID orgId,
      String customDomain,
      java.util.Map<String, Object> customBranding,
      java.util.Map<String, Object> rateLimits,
      java.time.Instant updatedAt
  ) {}

  record UpdateWorkspaceSettingsRequest(
      String customDomain,
      java.util.Map<String, Object> customBranding,
      java.util.Map<String, Object> rateLimits
  ) {}
}
