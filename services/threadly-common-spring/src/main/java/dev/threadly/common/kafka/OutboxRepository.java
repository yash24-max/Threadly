package dev.threadly.common.kafka;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for outbox events with custom queries for publishing workflow.
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

  /**
   * Find unpublished events (publishedAt IS NULL).
   * Used by polling job to publish events to Kafka.
   */
  @Query("SELECT oe FROM OutboxEvent oe WHERE oe.publishedAt IS NULL ORDER BY oe.createdAt ASC")
  List<OutboxEvent> findByPublishedAtIsNull();

  /**
   * Find unpublished events, limited to specified count.
   */
  @Query(value = "SELECT * FROM outbox_events WHERE published_at IS NULL ORDER BY created_at ASC LIMIT ?1",
      nativeQuery = true)
  List<OutboxEvent> findUnpublishedLimited(int limit);

  /**
   * Count unpublished events.
   */
  long countByPublishedAtIsNull();
}
