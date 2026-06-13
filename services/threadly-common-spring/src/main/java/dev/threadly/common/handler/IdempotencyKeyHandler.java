package dev.threadly.common.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect that intercepts {@link Idempotent} methods and caches responses by
 * the {@code Idempotency-Key} request header.
 */
@Aspect
@Component
@Slf4j
public class IdempotencyKeyHandler {

  // In-memory cache as fallback if Redis is not available
  private final ConcurrentHashMap<String, CachedResponse> memoryCache = new ConcurrentHashMap<>();

  @Autowired(required = false)
  private RedisTemplate<String, Object> redisTemplate;

  @Around("@annotation(idempotent)")
  public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return joinPoint.proceed(); // Not a web request, skip idempotency
    }

    jakarta.servlet.http.HttpServletRequest request = attrs.getRequest();
    String idempotencyKey = request.getHeader("Idempotency-Key");

    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      log.debug("No Idempotency-Key header, processing request normally");
      return joinPoint.proceed();
    }

    String cacheKey = idempotencyKey + ":" + request.getMethod() + ":" + request.getRequestURI();

    // Check Redis cache first
    if (redisTemplate != null) {
      Object cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached != null) {
        log.info("Idempotent request cache hit: key={}", idempotencyKey);
        return cached;
      }
    }

    // Check in-memory cache
    CachedResponse cached = memoryCache.get(cacheKey);
    if (cached != null && cached.isValid()) {
      log.info("Idempotent request in-memory cache hit: key={}", idempotencyKey);
      return cached.response;
    }

    // Execute method
    Object result = joinPoint.proceed();

    // Cache result
    int ttl = idempotent.ttlSeconds();
    cacheResult(cacheKey, result, ttl);
    log.info("Cached idempotent response: key={}, ttl={}s", idempotencyKey, ttl);

    return result;
  }

  private void cacheResult(String key, Object result, int ttlSeconds) {
    // Redis
    if (redisTemplate != null) {
      try {
        redisTemplate.opsForValue().set(key, result, ttlSeconds, TimeUnit.SECONDS);
      } catch (Exception e) {
        log.warn("Failed to cache in Redis, falling back to memory", e);
        memoryCache.put(key, new CachedResponse(result, System.currentTimeMillis() + (ttlSeconds * 1000L)));
      }
    } else {
      // Memory
      memoryCache.put(key, new CachedResponse(result, System.currentTimeMillis() + (ttlSeconds * 1000L)));
    }
  }

  private static class CachedResponse {
    Object response;
    long expiresAt;

    CachedResponse(Object response, long expiresAt) {
      this.response = response;
      this.expiresAt = expiresAt;
    }

    boolean isValid() {
      return System.currentTimeMillis() < expiresAt;
    }
  }
}
