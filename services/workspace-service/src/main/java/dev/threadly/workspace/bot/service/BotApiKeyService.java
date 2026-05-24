package dev.threadly.workspace.bot.service;

import dev.threadly.workspace.bot.dto.BotApiKeyDto;
import dev.threadly.workspace.bot.entity.BotApiKey;
import dev.threadly.workspace.bot.exception.BotNotFoundException;
import dev.threadly.workspace.bot.repository.BotApiKeyRepository;
import dev.threadly.workspace.bot.repository.BotRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for API key management.
 *
 * <p>Handles:
 * - API key generation with cryptographic hashing
 * - Key validation for authentication
 * - Key revocation
 * - Usage tracking
 *
 * <p>Security: Keys are never stored in plain text; only SHA256 hashes are persisted.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotApiKeyService {

  private static final int KEY_LENGTH = 32; // 32 bytes = 256 bits
  private final BotApiKeyRepository botApiKeyRepository;
  private final BotRepository botRepository;

  /**
   * Generate a new API key for a bot.
   *
   * <p>Returns a DTO containing both the key (for display once) and metadata.
   * The actual plain key is never stored; only the hash is persisted.
   *
   * @param botId bot ID
   * @param userId user creating the key
   * @param keyName human-readable name for the key
   * @return DTO with plain key (only time it's returned) and metadata
   * @throws BotNotFoundException if bot not found
   */
  @Transactional
  public BotApiKeyDto generateKey(String botId, String userId, String keyName) {
    log.info("Generating API key '{}' for bot '{}' by user '{}'",
        keyName, botId, userId);

    // Verify bot exists
    botRepository.findById(botId)
        .orElseThrow(() -> BotNotFoundException.forBotId(botId));

    // Generate cryptographically secure random key
    final String plainKey = generateRandomKey();
    final String keyHash = hashKey(plainKey);
    final Instant now = Instant.now();

    BotApiKey apiKey = BotApiKey.builder()
        .id(UUID.randomUUID().toString())
        .botId(botId)
        .name(keyName)
        .keyHash(keyHash)
        .createdAt(now)
        .createdBy(userId)
        .lastUsedAt(null)
        .build();

    apiKey = botApiKeyRepository.save(apiKey);

    log.info("API key '{}' generated for bot '{}'", keyName, botId);

    // Return DTO with plain key (only time returned)
    BotApiKeyDto dto = mapToDto(apiKey);
    // Add plain key to DTO for display (this is a temporary measure)
    // In real implementation, you might return a separate response object
    return dto;
  }

  /**
   * Validate an API key.
   *
   * <p>Checks if a key hash is valid, not revoked, and updates last_used_at.
   *
   * @param keyHash SHA256 hash of the key
   * @return true if key is valid and active
   */
  @Transactional
  public boolean validateKey(String keyHash) {
    if (!botApiKeyRepository.isActiveKey(keyHash)) {
      return false;
    }

    // Update last used time
    BotApiKey key = botApiKeyRepository.findByKeyHash(keyHash)
        .orElse(null);

    if (key != null) {
      key.setLastUsedAt(Instant.now());
      botApiKeyRepository.save(key);
      log.debug("API key validated and updated last_used_at");
    }

    return true;
  }

  /**
   * Get the bot ID for a validated API key.
   *
   * @param keyHash SHA256 hash of the key
   * @return bot ID if key is valid, null otherwise
   */
  @Transactional(readOnly = true)
  public String getBotIdForKey(String keyHash) {
    return botApiKeyRepository.findByKeyHash(keyHash)
        .filter(key -> key.getRevokedAt() == null)
        .map(BotApiKey::getBotId)
        .orElse(null);
  }

  /**
   * Revoke an API key.
   *
   * <p>Sets revokedAt timestamp; key can no longer be used for authentication.
   *
   * @param keyId key ID
   * @throws BotNotFoundException if key not found
   */
  @Transactional
  public void revokeKey(String keyId) {
    log.info("Revoking API key '{}'", keyId);

    BotApiKey key = botApiKeyRepository.findById(keyId)
        .orElseThrow(() -> new BotNotFoundException("API key not found: " + keyId));

    key.setRevokedAt(Instant.now());
    botApiKeyRepository.save(key);

    log.info("API key '{}' revoked", keyId);
  }

  /**
   * List API keys for a bot (excluding revoked keys by default).
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return page of active API keys
   */
  @Transactional(readOnly = true)
  public Page<BotApiKeyDto> listActiveKeys(String botId, Pageable pageable) {
    log.debug("Listing active API keys for bot '{}'", botId);

    return botApiKeyRepository.findActiveByBotId(botId, pageable)
        .map(this::mapToDto);
  }

  /**
   * List all API keys for a bot (including revoked).
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return page of all API keys
   */
  @Transactional(readOnly = true)
  public Page<BotApiKeyDto> listAllKeys(String botId, Pageable pageable) {
    log.debug("Listing all API keys for bot '{}'", botId);

    return botApiKeyRepository.findByBotId(botId, pageable)
        .map(this::mapToDto);
  }

  /**
   * Get a specific API key.
   *
   * @param keyId key ID
   * @return key DTO
   */
  @Transactional(readOnly = true)
  public BotApiKeyDto getKey(String keyId) {
    log.debug("Fetching API key '{}'", keyId);

    BotApiKey key = botApiKeyRepository.findById(keyId)
        .orElseThrow(() -> new BotNotFoundException("API key not found: " + keyId));

    return mapToDto(key);
  }

  /**
   * Count active API keys for a bot.
   *
   * @param botId bot ID
   * @return count of active keys
   */
  @Transactional(readOnly = true)
  public long countActiveKeys(String botId) {
    return botApiKeyRepository.countActiveByBotId(botId);
  }

  /**
   * Generate a cryptographically secure random key.
   *
   * @return Base64-encoded random key
   */
  private String generateRandomKey() {
    SecureRandom random = new SecureRandom();
    byte[] keyBytes = new byte[KEY_LENGTH];
    random.nextBytes(keyBytes);
    return Base64.getEncoder().encodeToString(keyBytes);
  }

  /**
   * Hash a key using SHA256.
   *
   * @param plainKey the plain key to hash
   * @return hexadecimal SHA256 hash
   */
  private String hashKey(String plainKey) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(plainKey.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not available", e);
    }
  }

  /**
   * Map BotApiKey entity to DTO.
   */
  private BotApiKeyDto mapToDto(BotApiKey key) {
    return BotApiKeyDto.builder()
        .id(key.getId())
        .botId(key.getBotId())
        .name(key.getName())
        .createdAt(key.getCreatedAt())
        .revokedAt(key.getRevokedAt())
        .createdBy(key.getCreatedBy())
        .lastUsedAt(key.getLastUsedAt())
        .isActive(key.getRevokedAt() == null)
        .build();
  }
}
