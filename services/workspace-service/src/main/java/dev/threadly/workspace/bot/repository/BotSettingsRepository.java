package dev.threadly.workspace.bot.repository;

import dev.threadly.workspace.bot.entity.BotSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for BotSettings entity.
 */
public interface BotSettingsRepository extends JpaRepository<BotSettings, String> {

  /**
   * Find settings by bot ID.
   *
   * @param botId bot ID
   * @return Optional containing settings if found
   */
  Optional<BotSettings> findByBotId(@Param("botId") String botId);

  /**
   * Check if settings exist for a bot.
   *
   * @param botId bot ID
   * @return true if settings exist, false otherwise
   */
  @Query("SELECT COUNT(bs) > 0 FROM BotSettings bs WHERE bs.botId = :botId")
  boolean existsByBotId(@Param("botId") String botId);

  /**
   * Delete settings by bot ID.
   *
   * @param botId bot ID
   * @return number of records deleted
   */
  long deleteByBotId(@Param("botId") String botId);
}
