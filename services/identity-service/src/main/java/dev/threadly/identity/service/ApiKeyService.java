package dev.threadly.identity.service;

import dev.threadly.identity.dto.ApiKeyDto;
import dev.threadly.identity.entity.ApiKey;
import dev.threadly.identity.exception.InvalidApiKeyException;
import dev.threadly.identity.exception.ResourceNotFoundException;
import dev.threadly.identity.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing API keys.
 * Handles API key generation, validation, revocation, and lifecycle management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ApiKeyService {

  private final ApiKeyRepository apiKeyRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Generates a new API key for an organization.
   *
   * @param orgId the organization ID
   * @param name human-readable name for the key
   * @param expiresIn expiry time in days (0 for non-expiring)
   * @param scopes comma-separated scopes/permissions
   * @return the plaintext API key (only returned once)
   */
  public String generateApiKey(String orgId, String name, int expiresIn, String scopes) {
    String keyValue = generateSecureKey();
    String keyHash = hashKey(keyValue);
    String keyPrefix = keyValue.substring(0, Math.min(8, keyValue.length()));

    LocalDateTime expiresAt = expiresIn > 0
        ? LocalDateTime.now(ZoneId.of("UTC")).plusDays(expiresIn)
        : null;

    ApiKey apiKey = ApiKey.builder()
        .id(UUID.randomUUID().toString())
        .orgId(orgId)
        .name(name)
        .keyHash(keyHash)
        .keyPrefix(keyPrefix)
        .expiresAt(expiresAt)
        .scopes(scopes != null ? scopes : "read,write")
        .revoked(false)
        .build();

    apiKeyRepository.save(apiKey);
    log.info("Generated API key: {} for org: {} (prefix: {})", name, orgId, keyPrefix);

    return keyValue;
  }

  /**
   * Validates an API key.
   * Checks if key exists, is not revoked, and not expired.
   *
   * @param keyValue the plaintext API key
   * @return ApiKey entity if valid
   * @throws InvalidApiKeyException if key is invalid
   */
  @Transactional(readOnly = true)
  public ApiKey validateApiKey(String keyValue) {
    String keyHash = hashKey(keyValue);

    return apiKeyRepository.findActiveByKeyHash(keyHash)
        .filter(key -> key.getExpiresAt() == null ||
            key.getExpiresAt().isAfter(LocalDateTime.now(ZoneId.of("UTC"))))
        .orElseThrow(() -> {
          log.warn("Invalid or expired API key attempted");
          return new InvalidApiKeyException();
        });
  }

  /**
   * Gets an API key by ID.
   *
   * @param keyId the API key ID
   * @return ApiKey entity
   * @throws ResourceNotFoundException if key not found
   */
  @Transactional(readOnly = true)
  public ApiKey getApiKeyById(String keyId) {
    return apiKeyRepository.findById(keyId)
        .orElseThrow(() -> new ResourceNotFoundException("ApiKey", keyId));
  }

  /**
   * Gets all active API keys for an organization.
   *
   * @param orgId the organization ID
   * @return list of active API keys
   */
  @Transactional(readOnly = true)
  public List<ApiKey> getOrgApiKeys(String orgId) {
    return apiKeyRepository.findActiveByOrgId(orgId);
  }

  /**
   * Revokes an API key.
   *
   * @param keyId the API key ID
   */
  public void revokeApiKey(String keyId) {
    ApiKey apiKey = getApiKeyById(keyId);
    apiKey.setRevoked(true);
    apiKey.setRevokedAt(LocalDateTime.now(ZoneId.of("UTC")));
    apiKeyRepository.save(apiKey);
    log.info("Revoked API key: {}", keyId);
  }

  /**
   * Updates the last used timestamp of an API key.
   * Called after successful key validation.
   *
   * @param keyId the API key ID
   */
  public void updateLastUsed(String keyId) {
    ApiKey apiKey = getApiKeyById(keyId);
    apiKey.setLastUsedAt(LocalDateTime.now(ZoneId.of("UTC")));
    apiKeyRepository.save(apiKey);
  }

  /**
   * Rotates an API key by revoking the old one and generating a new one.
   *
   * @param oldKeyId the old API key ID to revoke
   * @param orgId the organization ID
   * @param name name for the new key
   * @param expiresIn expiry time in days (0 for non-expiring)
   * @param scopes comma-separated scopes
   * @return the new plaintext API key
   */
  public String rotateApiKey(String oldKeyId, String orgId, String name,
      int expiresIn, String scopes) {
    revokeApiKey(oldKeyId);
    return generateApiKey(orgId, name, expiresIn, scopes);
  }

  /**
   * Checks if an API key has a specific scope/permission.
   *
   * @param apiKey the API key entity
   * @param scope the scope to check
   * @return true if key has the scope
   */
  public boolean hasScope(ApiKey apiKey, String scope) {
    return apiKey.getScopes() != null && apiKey.getScopes().contains(scope);
  }

  /**
   * Converts an ApiKey entity to ApiKeyDto for API responses.
   * Never includes the key hash in the response.
   *
   * @param apiKey the ApiKey entity
   * @return ApiKeyDto with non-sensitive information
   */
  public ApiKeyDto toDto(ApiKey apiKey) {
    return ApiKeyDto.builder()
        .id(apiKey.getId())
        .organizationId(apiKey.getOrgId())
        .name(apiKey.getName())
        .keyPrefix(apiKey.getKeyPrefix())
        .scopes(apiKey.getScopes())
        .lastUsedAt(apiKey.getLastUsedAt())
        .expiresAt(apiKey.getExpiresAt())
        .revoked(apiKey.getRevoked())
        .createdAt(apiKey.getCreatedAt())
        .build();
  }

  /**
   * Converts a list of ApiKey entities to ApiKeyDto list.
   *
   * @param apiKeys list of ApiKey entities
   * @return list of ApiKeyDto
   */
  public List<ApiKeyDto> toDto(List<ApiKey> apiKeys) {
    return apiKeys.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Generates a secure random API key.
   * Format: threadly_org_<random-uuid>
   *
   * @return random API key string
   */
  private String generateSecureKey() {
    return "threadly_" + UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * Hashes an API key using bcrypt for storage.
   *
   * @param keyValue the plaintext API key
   * @return hashed key
   */
  private String hashKey(String keyValue) {
    return passwordEncoder.encode(keyValue);
  }
}
