package dev.threadly.core.common.events;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Event idempotency service.
 *
 * Ensures each event is processed only once, even if consumed multiple times
 * due to Kafka retries or network issues.
 *
 * Implementation uses Redis for fast deduplication:
 * - Key: "event-processed:{eventId}:{listenerName}"
 * - Value: timestamp
 * - TTL: 24 hours (configurable)
 *
 * Alternatively, use database for permanent deduplication audit trail.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventIdempotencyService {

  private final RedisTemplate<String, String> redisTemplate;

  private static final String KEY_PREFIX = "event-processed";
  private static final long DEFAULT_TTL_HOURS = 24;

  /**
   * Check if this is the first time processing this event.
   * Returns true if event has NOT been processed before, false if already processed.
   */
  public boolean isFirstTime(String eventId, String listenerName) {
    String key = buildKey(eventId, listenerName);
    Boolean exists = redisTemplate.hasKey(key);
    return !Boolean.TRUE.equals(exists);
  }

  /**
   * Mark event as processed.
   * Stores processing record in Redis with TTL.
   */
  public void markAsProcessed(String eventId, String listenerName) {
    try {
      String key = buildKey(eventId, listenerName);
      String timestamp = String.valueOf(System.currentTimeMillis());
      redisTemplate.opsForValue().set(
          key,
          timestamp,
          Duration.ofHours(DEFAULT_TTL_HOURS)
      );
      log.debug("Event marked as processed: key={}", key);
    } catch (Exception e) {
      log.error("Failed to mark event as processed: eventId={}", eventId, e);
      // Non-fatal: continue processing
    }
  }

  /**
   * Build Redis key for idempotency tracking.
   */
  private String buildKey(String eventId, String listenerName) {
    return KEY_PREFIX + ":" + eventId + ":" + listenerName;
  }

  /**
   * Clear idempotency record (for testing).
   */
  public void clearProcessed(String eventId, String listenerName) {
    String key = buildKey(eventId, listenerName);
    redisTemplate.delete(key);
  }

  /**
   * Get processing timestamp for event.
   * Returns null if not processed.
   */
  public Long getProcessingTimestamp(String eventId, String listenerName) {
    String key = buildKey(eventId, listenerName);
    String value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(value) : null;
  }
}
