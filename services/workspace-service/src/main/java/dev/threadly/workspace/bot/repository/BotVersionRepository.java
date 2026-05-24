package dev.threadly.workspace.bot.repository;

import dev.threadly.workspace.bot.entity.BotVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for BotVersion entity.
 */
public interface BotVersionRepository extends JpaRepository<BotVersion, String> {

  /**
   * Find all versions of a bot ordered by version number descending.
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return paginated list of versions
   */
  Page<BotVersion> findByBotIdOrderByVersionNumberDesc(
      @Param("botId") String botId, Pageable pageable);

  /**
   * Find a specific version by bot ID and version number.
   *
   * @param botId bot ID
   * @param versionNumber version number
   * @return Optional containing the version if found
   */
  Optional<BotVersion> findByBotIdAndVersionNumber(
      @Param("botId") String botId, @Param("versionNumber") Integer versionNumber);

  /**
   * Get the latest version number for a bot.
   *
   * @param botId bot ID
   * @return latest version number, or 0 if no versions exist
   */
  @Query(
      "SELECT COALESCE(MAX(bv.versionNumber), 0) FROM BotVersion bv WHERE bv.botId = :botId")
  Integer getLatestVersionNumber(@Param("botId") String botId);

  /**
   * Get the most recently published version of a bot.
   *
   * @param botId bot ID
   * @return Optional containing the latest published version
   */
  @Query(
      "SELECT bv FROM BotVersion bv WHERE bv.botId = :botId "
          + "ORDER BY bv.publishedAt DESC LIMIT 1")
  Optional<BotVersion> findLatestByBotId(@Param("botId") String botId);

  /**
   * Count versions for a bot.
   *
   * @param botId bot ID
   * @return number of versions
   */
  long countByBotId(@Param("botId") String botId);

  /**
   * Delete all versions of a bot.
   *
   * @param botId bot ID
   * @return number of records deleted
   */
  long deleteByBotId(@Param("botId") String botId);
}
