package dev.threadly.knowledge.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Service configuration for Identity Service.
 *
 * Configures:
 * - Feign clients for inter-service communication
 * - Resilience4j circuit breakers
 * - OpenTelemetry tracing (auto-configured by Spring)
 */
@Configuration
public class ServiceConfig {

  private static final Logger logger = LoggerFactory.getLogger(ServiceConfig.class);

  /**
   * Default circuit breaker configuration.
   */
  @Bean
  public CircuitBreakerConfig defaultCircuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(50.0f)
        .waitDurationInOpenState(java.time.Duration.ofSeconds(30))
        .permittedNumberOfCallsInHalfOpenState(3)
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .recordExceptions(Exception.class)
        .build();
  }
}
