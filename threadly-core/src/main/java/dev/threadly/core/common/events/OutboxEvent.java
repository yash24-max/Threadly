package dev.threadly.core.common.events;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outbox event entity.
 *
 * Stored in database for guaranteed delivery.
 * When event is successfully published to Kafka, marked as published.
 * If Kafka publish fails, retry logic kicks in via OutboxService.publishOutboxEvents().
 *
 * Schema:
 *   CREATE TABLE outbox_events (
 *     id UUID PRIMARY KEY,
 *     event_id VARCHAR(255) NOT NULL,
 *     topic VARCHAR(255) NOT NULL,
 *     payload JSONB NOT NULL,
 *     published BOOLEAN DEFAULT FALSE,
 *     retry_count INT DEFAULT 0,
 *     created_at TIMESTAMP DEFAULT NOW(),
 *     published_at TIMESTAMP,
 *     INDEX(published, created_at)
 *   );
 */
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_unpublished", columnList = "published, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

  @Id
  private UUID id;

  @Column(nullable = false)
  private String eventId;

  @Column(nullable = false)
  private String topic;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(nullable = false)
  private Boolean published = false;

  @Column(nullable = false)
  private Integer retryCount = 0;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant publishedAt;
}
