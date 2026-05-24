package dev.threadly.workspace.bot.controller;

import dev.threadly.workspace.bot.dto.BotApiKeyDto;
import dev.threadly.workspace.bot.dto.CreateApiKeyRequest;
import dev.threadly.workspace.bot.service.BotApiKeyService;
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
 * REST controller for API key management.
 *
 * <p>Base path: /api/v1/bots/{botId}/api-keys
 *
 * <p>Manages programmatic access tokens for bots.
 */
@RestController
@RequestMapping("/api/v1/bots/{botId}/api-keys")
@RequiredArgsConstructor
@Slf4j
public class BotApiKeyController {

  private final BotApiKeyService botApiKeyService;

  /**
   * Generate a new API key.
   *
   * <p>POST /api/v1/bots/{botId}/api-keys
   *
   * <p>SECURITY: The response includes the plain key, which is only visible once.
   *
   * <p>Request body:
   * {
   *   "name": "Production Key"
   * }
   *
   * @param botId bot ID
   * @param request API key creation request
   * @param principal authenticated user
   * @return API key DTO with plain key (visible only in response) with 201 status
   */
  @PostMapping
  public ResponseEntity<ApiKeyResponse> generateKey(
      @PathVariable String botId,
      @Valid @RequestBody CreateApiKeyRequest request,
      Principal principal) {
    log.info("Generating API key for bot: {}", botId);

    String userId = principal.getName();

    BotApiKeyDto keyDto = botApiKeyService.generateKey(botId, userId, request.getName());

    // In a real implementation, you'd generate the plain key here
    // and return it with the DTO
    String plainKey = "sk_bot_" + System.nanoTime(); // Placeholder

    ApiKeyResponse response = ApiKeyResponse.builder()
        .id(keyDto.getId())
        .botId(keyDto.getBotId())
        .name(keyDto.getName())
        .key(plainKey) // Only returned in creation response
        .createdAt(keyDto.getCreatedAt())
        .createdBy(keyDto.getCreatedBy())
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * List API keys for a bot (active keys only).
   *
   * <p>GET /api/v1/bots/{botId}/api-keys
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @param principal authenticated user
   * @return paginated list of active API keys (without plain keys)
   */
  @GetMapping
  public ResponseEntity<Page<BotApiKeyDto>> listKeys(
      @PathVariable String botId,
      Pageable pageable,
      Principal principal) {
    log.debug("Listing API keys for bot: {}", botId);

    Page<BotApiKeyDto> keys = botApiKeyService.listActiveKeys(botId, pageable);

    return ResponseEntity.ok(keys);
  }

  /**
   * Get a specific API key.
   *
   * <p>GET /api/v1/bots/{botId}/api-keys/{keyId}
   *
   * @param botId bot ID
   * @param keyId API key ID
   * @param principal authenticated user
   * @return API key DTO (without plain key)
   */
  @GetMapping("/{keyId}")
  public ResponseEntity<BotApiKeyDto> getKey(
      @PathVariable String botId,
      @PathVariable String keyId,
      Principal principal) {
    log.debug("Fetching API key '{}' for bot: {}", keyId, botId);

    BotApiKeyDto key = botApiKeyService.getKey(keyId);

    return ResponseEntity.ok(key);
  }

  /**
   * Revoke an API key.
   *
   * <p>DELETE /api/v1/bots/{botId}/api-keys/{keyId}
   *
   * <p>After revocation, the key cannot be used for authentication.
   *
   * @param botId bot ID
   * @param keyId API key ID
   * @param principal authenticated user
   * @return 204 No Content
   */
  @DeleteMapping("/{keyId}")
  public ResponseEntity<Void> revokeKey(
      @PathVariable String botId,
      @PathVariable String keyId,
      Principal principal) {
    log.info("Revoking API key '{}' for bot: {}", keyId, botId);

    botApiKeyService.revokeKey(keyId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Response DTO for key creation (includes plain key).
   */
  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  @lombok.Builder
  public static class ApiKeyResponse {
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private String id;

    @com.fasterxml.jackson.annotation.JsonProperty("bot_id")
    private String botId;

    @com.fasterxml.jackson.annotation.JsonProperty("name")
    private String name;

    @com.fasterxml.jackson.annotation.JsonProperty("key")
    private String key;

    @com.fasterxml.jackson.annotation.JsonProperty("created_at")
    private java.time.Instant createdAt;

    @com.fasterxml.jackson.annotation.JsonProperty("created_by")
    private String createdBy;

    @com.fasterxml.jackson.annotation.JsonProperty("warning")
    @lombok.Builder.Default
    private String warning = "This key will only be displayed once. Save it securely.";
  }
}
