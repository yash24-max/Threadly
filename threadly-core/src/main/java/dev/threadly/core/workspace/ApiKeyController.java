package dev.threadly.core.workspace;

import dev.threadly.core.common.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bots/{botId}/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "Bot API key management")
public class ApiKeyController {

  private final ApiKeyRepository apiKeyRepository;
  private final BotRepository botRepository;
  private final PasswordEncoder passwordEncoder;

  @GetMapping
  @Operation(summary = "List API keys for a bot (prefix only, never full key)")
  public List<ApiKeyResponse> list(@PathVariable UUID botId) {
    UUID orgId = TenantContext.getOrgId();
    requireBotAccess(botId, orgId);
    return apiKeyRepository.findAllByBotIdAndOrgIdAndRevokedAtIsNull(botId, orgId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @PostMapping
  @Operation(summary = "Create a new API key")
  @Transactional
  public ResponseEntity<CreateApiKeyResponse> create(
      @PathVariable UUID botId, @Valid @RequestBody CreateApiKeyRequest req) {
    UUID orgId = TenantContext.getOrgId();
    requireBotAccess(botId, orgId);

    String rawKey = "tly_live_" + UUID.randomUUID().toString().replace("-", "");
    String keyPrefix = rawKey.substring(0, 8);
    String keyHash = passwordEncoder.encode(rawKey);
    String keyLookupHash = sha256Hex(rawKey);

    ApiKey apiKey =
        ApiKey.builder()
            .orgId(orgId)
            .botId(botId)
            .name(req.getName())
            .keyHash(keyHash)
            .keyLookupHash(keyLookupHash)
            .keyPrefix(keyPrefix)
            .build();
    apiKeyRepository.save(apiKey);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new CreateApiKeyResponse(
                apiKey.getId().toString(),
                apiKey.getName(),
                rawKey,
                keyPrefix,
                apiKey.getCreatedAt() != null ? apiKey.getCreatedAt().toString() : null));
  }

  @DeleteMapping("/{keyId}")
  @Operation(summary = "Revoke an API key")
  @Transactional
  public ResponseEntity<Void> revoke(@PathVariable UUID botId, @PathVariable UUID keyId) {
    UUID orgId = TenantContext.getOrgId();
    ApiKey apiKey =
        apiKeyRepository
            .findByIdAndBotIdAndOrgId(keyId, botId, orgId)
            .orElseThrow(() -> new EntityNotFoundException("API key not found: " + keyId));
    apiKey.setRevokedAt(Instant.now());
    apiKeyRepository.save(apiKey);
    return ResponseEntity.noContent().build();
  }

  private void requireBotAccess(UUID botId, UUID orgId) {
    botRepository
        .findByIdAndOrgId(botId, orgId)
        .orElseThrow(() -> new EntityNotFoundException("Bot not found: " + botId));
  }

  private ApiKeyResponse toResponse(ApiKey k) {
    ApiKeyResponse r = new ApiKeyResponse();
    r.setId(k.getId().toString());
    r.setName(k.getName());
    r.setKeyPrefix(k.getKeyPrefix());
    r.setLastUsedAt(k.getLastUsedAt() != null ? k.getLastUsedAt().toString() : null);
    r.setCreatedAt(k.getCreatedAt() != null ? k.getCreatedAt().toString() : null);
    return r;
  }

  private static String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  // ── DTOs ──────────────────────────────────────────────────────────────

  @Data
  public static class ApiKeyResponse {
    private String id;
    private String name;
    private String keyPrefix;
    private String lastUsedAt;
    private String createdAt;
  }

  @Data
  public static class CreateApiKeyRequest {
    @NotBlank private String name;
  }

  @Data
  public static class CreateApiKeyResponse {
    private final String id;
    private final String name;
    private final String key;
    private final String keyPrefix;
    private final String createdAt;
  }
}
