package dev.threadly.core.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Set;

/**
 * Idempotency-Key interceptor for state-mutating POST endpoints.
 * If a key has been seen within the TTL window, returns 409 Conflict.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

  private static final String HEADER = "Idempotency-Key";
  private static final Duration TTL = Duration.ofHours(24);
  private static final int MAX_KEY_LENGTH = 128;

  // Only these paths require idempotency keys
  private static final Set<String> IDEMPOTENT_PATHS = Set.of(
      "/v1/bots",
      "/v1/auth/signup",
      "/v1/auth/refresh"
  );

  private final StringRedisTemplate redisTemplate;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) throws Exception {

    if (!HttpMethod.POST.matches(request.getMethod())) return true;

    String path = request.getRequestURI();
    boolean requiresKey = IDEMPOTENT_PATHS.stream().anyMatch(path::startsWith);
    if (!requiresKey) return true;

    String key = request.getHeader(HEADER);
    if (key == null || key.isBlank()) {
      // Allow through without idempotency — caller chose not to use it
      return true;
    }

    if (key.length() > MAX_KEY_LENGTH) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/problem+json");
      response.getWriter().write("""
          {"status":400,"title":"Bad Request","detail":"Idempotency-Key must be \u2264 128 characters"}
          """);
      return false;
    }

    String redisKey = "idempotency:" + key;
    Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", TTL);
    if (Boolean.FALSE.equals(isNew)) {
      log.debug("Duplicate request rejected for Idempotency-Key: {}", key);
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      response.setContentType("application/problem+json");
      response.getWriter().write("""
          {"status":409,"title":"Conflict","detail":"Duplicate request: this Idempotency-Key was already processed."}
          """);
      return false;
    }

    return true;
  }
}
