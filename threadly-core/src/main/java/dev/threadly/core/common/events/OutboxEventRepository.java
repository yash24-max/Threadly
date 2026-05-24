package dev.threadly.core.common.events;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for outbox events.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

  /**
   * Find all unpublished events, ordered by creation time.
   */
  @Query("SELECT e FROM OutboxEvent e WHERE e.published = FALSE ORDER BY e.createdAt ASC")
  List<OutboxEvent> findUnpublishedEvents();

  /**
   * Count unpublished events.
   */
  @Query("SELECT COUNT(e) FROM OutboxEvent e WHERE e.published = FALSE")
  long countUnpublished();

  /**
   * Find events by topic.
   */
  List<OutboxEvent> findByTopic(String topic);

  /**
   * Find published events (for cleanup).
   */
  @Query("SELECT e FROM OutboxEvent e WHERE e.published = TRUE")
  List<OutboxEvent> findPublishedEvents();
}
