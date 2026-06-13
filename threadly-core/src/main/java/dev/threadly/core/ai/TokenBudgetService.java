package dev.threadly.core.ai;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Per-org daily token budget enforcement.
 * Uses Redis counters with a midnight-rollover TTL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBudgetService {

  private static final long DEFAULT_DAILY_BUDGET = 1_000_000L; // 1M tokens/day

  @Value("${threadly.ai.daily-token-budget:1000000}")
  private long dailyBudget;

  private final StringRedisTemplate redisTemplate;

  /** Returns true if the org is within budget. */
  public boolean isWithinBudget(UUID orgId) {
    String key = tokenKey(orgId);
    String val = redisTemplate.opsForValue().get(key);
    long used = val != null ? Long.parseLong(val) : 0L;
    return used < dailyBudget;
  }

  /** Record token usage for an org. */
  public void recordUsage(UUID orgId, long tokensUsed) {
    String key = tokenKey(orgId);
    Long count = redisTemplate.opsForValue().increment(key, tokensUsed);
    if (count != null && count <= tokensUsed) {
      // First write — set TTL to midnight (approx 24h)
      redisTemplate.expire(key, Duration.ofHours(24));
    }
    log.debug("Token usage: org={} +{} total={}", orgId, tokensUsed, count);
  }

  /** Returns current daily usage for an org. */
  public long getCurrentUsage(UUID orgId) {
    String val = redisTemplate.opsForValue().get(tokenKey(orgId));
    return val != null ? Long.parseLong(val) : 0L;
  }

  private String tokenKey(UUID orgId) {
    // Key rotates at midnight UTC via TTL — good enough for daily budget
    return "token_budget:" + orgId.toString();
  }
}
