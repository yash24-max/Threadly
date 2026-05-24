package dev.threadly.common.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Outbox pattern entity for reliable event publishing.
 *
 * Events are written to this table in the same transaction as the main business logic.
 * A separate job (Spring @Scheduled or Debezium) polls this table and publishes to Kafka.
 *
 * Benefits:
 * - Guarantees that event is published if and only if main entity is created
 * - Avoids distributed transaction complexity
 * - Allows async publishing without blocking the request
 */
@Entity
@Table(
    name = "outbox_events",
    indexes = {
        @Index(name = "idx_published_at", columnList = "published_at"),
        @Index(name = "idx_aggregate_id", columnList = "aggregate_id"),
        @Index(name = "idx_event_type", columnList = "event_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /** Event type (e.g., "session.completed", "conversation.created") */
  @Column(nullable = false, length = 100)
  private String eventType;

  /** ID of the aggregate that triggered this event (e.g., sessionId, conversationId) */
  @Column(nullable = false)
  private UUID aggregateId;

  /** Event payload as JSON */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private JsonNode payload;

  /** When the event was created */
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  /** When the event was published to Kafka (null = not yet published) */
  @Column
  private Instant publishedAt;

  /**
   * Mark event as published.
   */
  public void markAsPublished() {
    this.publishedAt = Instant.now();
  }

  /**
   * Check if event has been published.
   */
  public boolean isPublished() {
    return publishedAt != null;
  }

  @jakarta.persistence.PrePersist
  void onCreated() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
