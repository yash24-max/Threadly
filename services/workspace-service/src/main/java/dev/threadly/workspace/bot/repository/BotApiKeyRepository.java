package dev.threadly.workspace.bot.repository;

import dev.threadly.workspace.bot.entity.BotApiKey;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for BotApiKey entity.
 */
public interface BotApiKeyRepository extends JpaRepository<BotApiKey, String> {

  /**
   * Find all API keys for a bot, excluding revoked keys.
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return paginated list of active API keys
   */
  @Query("SELECT bk FROM BotApiKey bk WHERE bk.botId = :botId AND bk.revokedAt IS NULL")
  Page<BotApiKey> findActiveByBotId(
      @Param("botId") String botId, Pageable pageable);

  /**
   * Find all API keys for a bot including revoked ones.
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return paginated list of all API keys
   */
  Page<BotApiKey> findByBotId(@Param("botId") String botId, Pageable pageable);

  /**
   * Find an API key by its hash.
   *
   * @param keyHash SHA256 hash of the API key
   * @return Optional containing the API key if found
   */
  Optional<BotApiKey> findByKeyHash(@Param("keyHash") String keyHash);

  /**
   * Check if a key hash exists and is not revoked.
   *
   * @param keyHash SHA256 hash of the API key
   * @return true if key is active, false otherwise
   */
  @Query("SELECT COUNT(bk) > 0 FROM BotApiKey bk WHERE bk.keyHash = :keyHash AND bk.revokedAt IS NULL")
  boolean isActiveKey(@Param("keyHash") String keyHash);

  /**
   * Count active API keys for a bot.
   *
   * @param botId bot ID
   * @return number of active keys
   */
  @Query("SELECT COUNT(bk) FROM BotApiKey bk WHERE bk.botId = :botId AND bk.revokedAt IS NULL")
  long countActiveByBotId(@Param("botId") String botId);

  /**
   * Delete all API keys for a bot.
   *
   * @param botId bot ID
   * @return number of records deleted
   */
  long deleteByBotId(@Param("botId") String botId);
}
