package dev.threadly.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized circuit breaker configuration for all Feign clients.
 *
 * Defaults:
 * - failureRateThreshold: 50% → circuit opens if 50% of requests fail
 * - slowCallRateThreshold: 50% → circuit opens if 50% of requests are slow
 * - slowCallDurationThreshold: 10s → a request > 10s is "slow"
 * - waitDurationInOpenState: 30s → retry after 30 seconds
 * - permittedNumberOfCallsInHalfOpenState: 5 → test 5 requests in half-open
 * - slidingWindowSize: 10 → evaluate over last 10 requests
 *
 * Retry:
 * - maxAttempts: 3 (initial + 2 retries)
 * - exponentialBackoff: 100ms initial, 2x multiplier
 * - retries on: SocketTimeoutException, TimeoutException, IOException
 *
 * Fallback: Returns error DTO, does NOT throw exception.
 */
@Slf4j
@Configuration
public class CircuitBreakerConfig {

  /**
   * Standard circuit breaker config for inter-service calls.
   */
  public static CircuitBreakerConfig getDefaultCircuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(50) // Open if 50%+ requests fail
        .slowCallRateThreshold(50) // Open if 50%+ requests are slow
        .slowCallDurationThreshold(Duration.ofSeconds(10)) // Request > 10s is "slow"
        .waitDurationInOpenState(Duration.ofSeconds(30)) // Try again after 30s
        .permittedNumberOfCallsInHalfOpenState(5) // Test 5 requests in half-open
        .slidingWindowSize(10) // Evaluate last 10 requests
        .recordExceptions(
            java.net.SocketTimeoutException.class,
            TimeoutException.class,
            java.io.IOException.class
        )
        .ignoreExceptions(IllegalArgumentException.class) // Don't count validation errors
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .build();
  }

  /**
   * Standard retry config for transient failures.
   */
  public static RetryConfig getDefaultRetryConfig() {
    return RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(100))
        .intervalFunction(
            io.github.resilience4j.core.IntervalFunction.ofExponentialRandomBackoff(100, 2)
        )
        .retryExceptions(
            java.io.IOException.class,
            TimeoutException.class,
            java.net.SocketTimeoutException.class
        )
        .ignoreExceptions(IllegalArgumentException.class)
        .build();
  }

  /**
   * Build a Resilience4j-decorated Feign client with circuit breaker and retry.
   * Example usage:
   * <pre>
   * {@code
   * @Bean
   * public UserServiceClient userServiceClient(Decoder decoder, Encoder encoder) {
   *   FeignDecorators decorators = FeignDecorators.builder()
   *       .withCircuitBreaker(circuitBreakerRegistry.circuitBreaker("userServiceClient"))
   *       .withRetry(retryRegistry.retry("userServiceClient"))
   *       .build();
   *   return Resilience4jFeign.builder(decorators)
   *       .decoder(decoder)
   *       .encoder(encoder)
   *       .target(UserServiceClient.class, "http://user-service:8080");
   * }
   * }
   * </pre>
   */
  public static FeignDecorators buildFeignDecorators(
      io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker,
      io.github.resilience4j.retry.Retry retry) {
    return FeignDecorators.builder()
        .withCircuitBreaker(circuitBreaker)
        .withRetry(retry)
        .build();
  }
}
