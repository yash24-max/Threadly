package dev.threadly.workspace.bot.controller;

import dev.threadly.workspace.bot.dto.BotWebhookDto;
import dev.threadly.workspace.bot.service.BotWebhookService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for webhook management.
 *
 * <p>Base path: /api/v1/bots/{botId}/webhooks
 *
 * <p>Manages event subscriptions and webhook configurations.
 */
@RestController
@RequestMapping("/api/v1/bots/{botId}/webhooks")
@RequiredArgsConstructor
@Slf4j
public class BotWebhookController {

  private final BotWebhookService botWebhookService;

  /**
   * Register a new webhook.
   *
   * <p>POST /api/v1/bots/{botId}/webhooks
   *
   * <p>Request body:
   * {
   *   "url": "https://example.com/webhook",
   *   "events": "bot.published,bot.deleted",
   *   "secret": "optional-secret-key"
   * }
   *
   * @param botId bot ID
   * @param request webhook registration request
   * @param principal authenticated user
   * @return registered webhook DTO with 201 status
   */
  @PostMapping
  public ResponseEntity<BotWebhookDto> registerWebhook(
      @PathVariable String botId,
      @Valid @RequestBody RegisterWebhookRequest request,
      Principal principal) {
    log.info("Registering webhook for bot '{}' at: {}", botId, request.getUrl());

    BotWebhookDto webhook = botWebhookService.registerWebhook(
        botId, request.getUrl(), request.getEvents(), request.getSecret());

    return ResponseEntity.status(HttpStatus.CREATED).body(webhook);
  }

  /**
   * List all webhooks for a bot.
   *
   * <p>GET /api/v1/bots/{botId}/webhooks
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @param principal authenticated user
   * @return paginated list of webhooks
   */
  @GetMapping
  public ResponseEntity<Page<BotWebhookDto>> listWebhooks(
      @PathVariable String botId,
      Pageable pageable,
      Principal principal) {
    log.debug("Listing webhooks for bot: {}", botId);

    Page<BotWebhookDto> webhooks = botWebhookService.listWebhooks(botId, pageable);

    return ResponseEntity.ok(webhooks);
  }

  /**
   * Get a specific webhook.
   *
   * <p>GET /api/v1/bots/{botId}/webhooks/{webhookId}
   *
   * @param botId bot ID
   * @param webhookId webhook ID
   * @param principal authenticated user
   * @return webhook DTO
   */
  @GetMapping("/{webhookId}")
  public ResponseEntity<BotWebhookDto> getWebhook(
      @PathVariable String botId,
      @PathVariable String webhookId,
      Principal principal) {
    log.debug("Fetching webhook '{}' for bot: {}", webhookId, botId);

    BotWebhookDto webhook = botWebhookService.getWebhook(webhookId);

    return ResponseEntity.ok(webhook);
  }

  /**
   * Update a webhook.
   *
   * <p>PATCH /api/v1/bots/{botId}/webhooks/{webhookId}
   *
   * <p>Request body (all fields optional):
   * {
   *   "url": "https://new-endpoint.com/webhook",
   *   "events": "bot.created,bot.updated",
   *   "secret": "new-secret"
   * }
   *
   * @param botId bot ID
   * @param webhookId webhook ID
   * @param request update request
   * @param principal authenticated user
   * @return updated webhook DTO
   */
  @PatchMapping("/{webhookId}")
  public ResponseEntity<BotWebhookDto> updateWebhook(
      @PathVariable String botId,
      @PathVariable String webhookId,
      @RequestBody UpdateWebhookRequest request,
      Principal principal) {
    log.info("Updating webhook '{}' for bot: {}", webhookId, botId);

    BotWebhookDto webhook = botWebhookService.updateWebhook(
        webhookId, request.getUrl(), request.getEvents(), request.getSecret());

    return ResponseEntity.ok(webhook);
  }

  /**
   * Toggle webhook active/inactive status.
   *
   * <p>POST /api/v1/bots/{botId}/webhooks/{webhookId}/active
   *
   * <p>Request body:
   * {
   *   "is_active": true
   * }
   *
   * @param botId bot ID
   * @param webhookId webhook ID
   * @param request activation request
   * @param principal authenticated user
   * @return updated webhook DTO
   */
  @PostMapping("/{webhookId}/active")
  public ResponseEntity<BotWebhookDto> setWebhookActive(
      @PathVariable String botId,
      @PathVariable String webhookId,
      @RequestBody SetActiveRequest request,
      Principal principal) {
    log.info("Setting webhook '{}' active={}", webhookId, request.isActive);

    BotWebhookDto webhook = botWebhookService.setWebhookActive(webhookId, request.isActive);

    return ResponseEntity.ok(webhook);
  }

  /**
   * Delete a webhook.
   *
   * <p>DELETE /api/v1/bots/{botId}/webhooks/{webhookId}
   *
   * @param botId bot ID
   * @param webhookId webhook ID
   * @param principal authenticated user
   * @return 204 No Content
   */
  @DeleteMapping("/{webhookId}")
  public ResponseEntity<Void> deleteWebhook(
      @PathVariable String botId,
      @PathVariable String webhookId,
      Principal principal) {
    log.info("Deleting webhook '{}' for bot: {}", webhookId, botId);

    botWebhookService.deleteWebhook(webhookId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Request DTO for registering a webhook.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class RegisterWebhookRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("url")
    private String url;

    @com.fasterxml.jackson.annotation.JsonProperty("events")
    private String events;

    @com.fasterxml.jackson.annotation.JsonProperty("secret")
    private String secret;
  }

  /**
   * Request DTO for updating a webhook.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class UpdateWebhookRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("url")
    private String url;

    @com.fasterxml.jackson.annotation.JsonProperty("events")
    private String events;

    @com.fasterxml.jackson.annotation.JsonProperty("secret")
    private String secret;
  }

  /**
   * Request DTO for setting webhook active status.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SetActiveRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("is_active")
    private boolean isActive;
  }
}
