package dev.threadly.workspace.bot.repository;

import dev.threadly.workspace.bot.entity.BotWebhook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for BotWebhook entity.
 */
public interface BotWebhookRepository extends JpaRepository<BotWebhook, String> {

  /**
   * Find all webhooks for a bot.
   *
   * @param botId bot ID
   * @param pageable pagination parameters
   * @return paginated list of webhooks
   */
  Page<BotWebhook> findByBotId(@Param("botId") String botId, Pageable pageable);

  /**
   * Find all active webhooks for a bot.
   *
   * @param botId bot ID
   * @return list of active webhooks
   */
  @Query("SELECT bw FROM BotWebhook bw WHERE bw.botId = :botId AND bw.isActive = true")
  List<BotWebhook> findActivByBotId(@Param("botId") String botId);

  /**
   * Find a webhook by ID and bot ID.
   *
   * @param id webhook ID
   * @param botId bot ID
   * @return Optional containing the webhook if found
   */
  Optional<BotWebhook> findByIdAndBotId(
      @Param("id") String id, @Param("botId") String botId);

  /**
   * Count webhooks for a bot.
   *
   * @param botId bot ID
   * @return number of webhooks
   */
  long countByBotId(@Param("botId") String botId);

  /**
   * Count active webhooks for a bot.
   *
   * @param botId bot ID
   * @return number of active webhooks
   */
  @Query("SELECT COUNT(bw) FROM BotWebhook bw WHERE bw.botId = :botId AND bw.isActive = true")
  long countActiveByBotId(@Param("botId") String botId);

  /**
   * Delete all webhooks for a bot.
   *
   * @param botId bot ID
   * @return number of records deleted
   */
  long deleteByBotId(@Param("botId") String botId);

  /**
   * Find webhooks subscribed to a specific event.
   *
   * @param botId bot ID
   * @param event event type
   * @return list of webhooks interested in this event
   */
  @Query(
      "SELECT bw FROM BotWebhook bw WHERE bw.botId = :botId "
          + "AND bw.isActive = true "
          + "AND (bw.events LIKE CONCAT('%', :event, '%') OR bw.events = '*')")
  List<BotWebhook> findByBotIdAndEvent(
      @Param("botId") String botId, @Param("event") String event);
}
