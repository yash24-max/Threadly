package dev.threadly.core.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Central registry for all circuit breakers used by Feign/REST clients across microservices.
 *
 * Defaults:
 * - Failure threshold: 5 consecutive failures
 * - Wait duration (half-open): 30 seconds
 * - Failure rate threshold: 50%
 * - Slow call rate threshold: 100%
 * - Slow call duration: 2 seconds
 *
 * All inter-service calls should be wrapped with @CircuitBreaker annotation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreakerRegistry {

  public static final int DEFAULT_FAILURE_THRESHOLD = 5;
  public static final int DEFAULT_WAIT_DURATION_SECONDS = 30;
  public static final float DEFAULT_FAILURE_RATE_THRESHOLD = 50.0f;
  public static final float DEFAULT_SLOW_CALL_RATE_THRESHOLD = 100.0f;
  public static final int DEFAULT_SLOW_CALL_DURATION_SECONDS = 2;

  @Getter private final io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry registry;

  public CircuitBreakerRegistry() {
    // Create registry with global defaults
    this.registry = io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.of(
        getDefaultConfig()
    );

    // Add event logging
    this.registry.getEventPublisher()
        .onEntryAdded(event -> logCircuitBreakerCreated(event))
        .onEntryRemoved(event -> logCircuitBreakerRemoved(event));
  }

  /**
   * Get or create a circuit breaker with default configuration.
   * Service names should follow convention: {service-name}-feign
   * e.g., "identity-service-feign", "workspace-service-feign"
   */
  public CircuitBreaker getCircuitBreaker(String name) {
    return registry.circuitBreaker(name);
  }

  /**
   * Get or create a circuit breaker with custom configuration.
   */
  public CircuitBreaker getCircuitBreaker(String name, CircuitBreakerConfig customConfig) {
    return registry.circuitBreaker(name, customConfig);
  }

  /**
   * Default circuit breaker configuration.
   */
  private static CircuitBreakerConfig getDefaultConfig() {
    return CircuitBreakerConfig.custom()
        .failureThreshold(DEFAULT_FAILURE_THRESHOLD)
        .waitDurationInOpenState(Duration.ofSeconds(DEFAULT_WAIT_DURATION_SECONDS))
        .failureRateThreshold(DEFAULT_FAILURE_RATE_THRESHOLD)
        .slowCallRateThreshold(DEFAULT_SLOW_CALL_RATE_THRESHOLD)
        .slowCallDurationThreshold(Duration.ofSeconds(DEFAULT_SLOW_CALL_DURATION_SECONDS))
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .recordExceptions(Exception.class)
        .ignoreExceptions()
        .build();
  }

  /**
   * Pre-configured circuit breaker for identity service.
   * May have stricter thresholds for critical auth service.
   */
  public CircuitBreaker identityServiceBreaker() {
    String name = "identity-service-feign";
    if (!registry.find(name).isPresent()) {
      CircuitBreakerConfig config = CircuitBreakerConfig.custom()
          .failureThreshold(3) // Stricter for auth
          .waitDurationInOpenState(Duration.ofSeconds(20))
          .failureRateThreshold(40.0f)
          .slowCallDurationThreshold(Duration.ofSeconds(1))
          .automaticTransitionFromOpenToHalfOpenEnabled(true)
          .recordExceptions(Exception.class)
          .ignoreExceptions()
          .build();
      return registry.circuitBreaker(name, config);
    }
    return registry.circuitBreaker(name);
  }

  /**
   * Pre-configured circuit breaker for workspace service.
   */
  public CircuitBreaker workspaceServiceBreaker() {
    return getCircuitBreaker("workspace-service-feign");
  }

  /**
   * Pre-configured circuit breaker for conversation service.
   */
  public CircuitBreaker conversationServiceBreaker() {
    return getCircuitBreaker("conversation-service-feign");
  }

  /**
   * Pre-configured circuit breaker for knowledge service.
   */
  public CircuitBreaker knowledgeServiceBreaker() {
    return getCircuitBreaker("knowledge-service-feign");
  }

  /**
   * Pre-configured circuit breaker for billing service.
   */
  public CircuitBreaker billingServiceBreaker() {
    return getCircuitBreaker("billing-service-feign");
  }

  /**
   * Pre-configured circuit breaker for flow service.
   */
  public CircuitBreaker flowServiceBreaker() {
    return getCircuitBreaker("flow-service-feign");
  }

  /**
   * Pre-configured circuit breaker for runtime service.
   */
  public CircuitBreaker runtimeServiceBreaker() {
    return getCircuitBreaker("runtime-service-feign");
  }

  /**
   * Get all active circuit breakers and their states.
   */
  public Map<String, String> getHealthStatus() {
    Map<String, String> status = new HashMap<>();
    registry.getAllCircuitBreakers().forEach(cb ->
        status.put(cb.getName(), cb.getState().toString())
    );
    return status;
  }

  private void logCircuitBreakerCreated(EntryAddedEvent event) {
    log.info("Circuit breaker created: {}", event.getAddedEntry().getName());
  }

  private void logCircuitBreakerRemoved(EntryRemovedEvent event) {
    log.info("Circuit breaker removed: {}", event.getRemovedEntry().getName());
  }

  /**
   * Spring Bean for CircuitBreakerRegistry as dependency.
   */
  @Bean
  public io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry() {
    return this.registry;
  }
}
