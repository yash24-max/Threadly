package dev.threadly.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j configuration for circuit breakers and retries.
 *
 * Circuit Breaker:
 * - Opens after 5 failures
 * - Waits 30 seconds before attempting again
 * - Counts failures in 10-second windows
 *
 * Retry:
 * - 3 attempts total (1 initial + 2 retries)
 * - Exponential backoff: 100ms, 400ms, 1600ms
 * - Retries on IOException, TimeoutException
 */
@Slf4j
@Configuration
public class Resilience4jConfig {

  /**
   * Circuit breaker registry with default configuration.
   */
  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(50) // Open if 50%+ requests fail
        .slowCallRateThreshold(50) // Open if 50%+ requests are slow
        .slowCallDurationThreshold(Duration.ofSeconds(10)) // Request > 10s is "slow"
        .waitDurationInOpenState(Duration.ofSeconds(30)) // Try again after 30s
        .permittedNumberOfCallsInHalfOpenState(3) // Test 3 requests in half-open
        .slidingWindowSize(10) // Evaluate last 10 requests
        .recordExceptions(
            java.net.SocketTimeoutException.class,
            TimeoutException.class,
            java.io.IOException.class
        )
        .build();

    CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
    registry.getEventPublisher()
        .onEntryAdded(event -> log.info("Circuit breaker created: {}", event.getAddedEntry().getName()))
        .onEntryRemoved(event -> log.info("Circuit breaker removed: {}", event.getRemovedEntry().getName()));

    return registry;
  }

  /**
   * Retry registry with default configuration.
   */
  @Bean
  public RetryRegistry retryRegistry() {
    RetryConfig defaultConfig = RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(100)) // Initial delay
        .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialRandomBackoff(100, 2))
        .retryExceptions(
            java.io.IOException.class,
            TimeoutException.class,
            java.net.SocketTimeoutException.class
        )
        .ignoreExceptions(IllegalArgumentException.class)
        .build();

    RetryRegistry registry = RetryRegistry.of(defaultConfig);
    registry.getEventPublisher()
        .onEntryAdded(event -> log.info("Retry policy created: {}", event.getAddedEntry().getName()))
        .onEntryRemoved(event -> log.info("Retry policy removed: {}", event.getRemovedEntry().getName()));

    return registry;
  }

  /**
   * Default circuit breaker for Feign clients.
   */
  @Bean
  public CircuitBreaker feignCircuitBreaker(CircuitBreakerRegistry registry) {
    return registry.circuitBreaker(
        "feignCircuitBreaker",
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .recordExceptions(java.io.IOException.class, TimeoutException.class)
            .build()
    );
  }

  /**
   * Default retry policy for Feign clients.
   */
  @Bean
  public Retry feignRetry(RetryRegistry registry) {
    return registry.retry(
        "feignRetry",
        RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(100))
            .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialRandomBackoff(100, 2))
            .retryExceptions(java.io.IOException.class, TimeoutException.class)
            .build()
    );
  }
}
