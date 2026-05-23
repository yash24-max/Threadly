package dev.threadly.core.common;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate-limiting filter using Bucket4j backed by Redis via Lettuce.
 *
 * <ul>
 *   <li>Auth endpoints ({@code /v1/auth/**}): 10 req/min per IP
 *   <li>API endpoints ({@code /v1/**}): 1000 req/min per org (or per IP when org not available)
 * </ul>
 */
@Slf4j
@Component
@Order(Integer.MIN_VALUE + 1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private static final int AUTH_LIMIT_PER_MIN = 10;
  private static final int API_LIMIT_PER_MIN = 1000;

  private final LettuceConnectionFactory lettuceConnectionFactory;

  @Value("${threadly.rate-limit.auth-login-per-min:10}")
  private int authLimitPerMin;

  @Value("${threadly.rate-limit.default-per-min-per-org:300}")
  private int apiLimitPerMin;

  private ProxyManager<byte[]> proxyManager;

  @PostConstruct
  public void init() {
    RedisClient redisClient = (RedisClient) lettuceConnectionFactory.getNativeClient();
    StatefulRedisConnection<byte[], byte[]> connection =
        redisClient.connect(ByteArrayCodec.INSTANCE);
    proxyManager = LettuceBasedProxyManager.builderFor(connection).build();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    if (path.startsWith("/v1/auth/")) {
      String ip = getClientIp(request);
      String bucketKey = "rl:auth:" + ip;
      if (!tryConsume(bucketKey, authLimitPerMin)) {
        sendRateLimitResponse(response, "Too many authentication attempts. Try again in a minute.");
        return;
      }
    } else if (path.startsWith("/v1/")) {
      UUID orgId = TenantContext.getOrgIdOrNull();
      String bucketKey = orgId != null
          ? "rl:api:org:" + orgId
          : "rl:api:ip:" + getClientIp(request);
      if (!tryConsume(bucketKey, apiLimitPerMin)) {
        sendRateLimitResponse(response, "API rate limit exceeded. Try again later.");
        return;
      }
    }

    chain.doFilter(request, response);
  }

  private boolean tryConsume(String key, int limitPerMin) {
    try {
      Supplier<BucketConfiguration> configSupplier =
          () ->
              BucketConfiguration.builder()
                  .addLimit(
                      Bandwidth.builder()
                          .capacity(limitPerMin)
                          .refillGreedy(limitPerMin, Duration.ofMinutes(1))
                          .build())
                  .build();
      Bucket bucket = proxyManager.builder().build(key.getBytes(StandardCharsets.UTF_8), configSupplier);
      return bucket.tryConsume(1);
    } catch (Exception e) {
      // Redis unavailable — fail open to avoid blocking all requests
      log.warn("Rate limit Redis unavailable for key {}: {}", key, e.getMessage());
      return true;
    }
  }

  private void sendRateLimitResponse(HttpServletResponse response, String message)
      throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"error\":\"" + message + "\",\"status\":429}");
  }

  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp;
    }
    return request.getRemoteAddr();
  }
}
