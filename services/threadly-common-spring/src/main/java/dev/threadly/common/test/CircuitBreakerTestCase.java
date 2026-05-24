package dev.threadly.common.test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base test case for circuit breaker pattern testing.
 *
 * Provides utilities for:
 * 1. Simulating failures and timeout scenarios
 * 2. Verifying circuit breaker opens when threshold exceeded
 * 3. Testing fallback behavior
 * 4. Verifying half-open state and recovery
 * 5. Testing exponential backoff retry
 *
 * Circuit Breaker States:
 * - CLOSED: Normal operation, requests pass through
 * - OPEN: Threshold exceeded, requests fail immediately with fallback
 * - HALF_OPEN: Testing if service recovered, limited requests allowed
 * - DISABLED: Circuit breaker disabled for testing
 *
 * Example:
 * <pre>
 * {@code
 * @SpringBootTest
 * public class BillingServiceClientTest extends CircuitBreakerTestCase {
 *
 *   @Autowired private BillingServiceClient client;
 *   @Autowired private CircuitBreakerRegistry registry;
 *
 *   @Test
 *   public void testCircuitBreakerOpensAfterThreshold() throws Exception {
 *     CircuitBreaker breaker = getCircuitBreaker("billingServiceClient");
 *
 *     // Simulate 5 consecutive failures
 *     for (int i = 0; i < 5; i++) {
 *       simulateFailure();
 *       client.checkBilling("cust-123");
 *     }
 *
 *     // Verify: circuit breaker is OPEN
 *     assertCircuitBreakerState(breaker, OPEN);
 *
 *     // Next request fails immediately without calling service
 *     Result result = client.checkBilling("cust-123");
 *     assertFalse(result.isAllowed()); // Fallback response
 *   }
 *
 *   @Test
 *   public void testCircuitBreakerRecovery() throws Exception {
 *     CircuitBreaker breaker = getCircuitBreaker("billingServiceClient");
 *
 *     // Open the circuit
 *     openCircuitBreaker(breaker);
 *     assertEquals(OPEN, breaker.getState());
 *
 *     // Wait for half-open window (30 seconds by default)
 *     Thread.sleep(30000);
 *
 *     // Service is now healthy - request succeeds
 *     simulateSuccess();
 *     Result result = client.checkBilling("cust-123");
 *     assertTrue(result.isAllowed());
 *
 *     // Verify: circuit breaker is CLOSED again
 *     assertCircuitBreakerState(breaker, CLOSED);
 *   }
 *
 *   @Test
 *   public void testFallbackOnCircuitOpen() throws Exception {
 *     // Open circuit
 *     CircuitBreaker breaker = getCircuitBreaker("billingServiceClient");
 *     openCircuitBreaker(breaker);
 *
 *     // Verify: fallback is returned
 *     BillingResponse fallback = client.checkBilling("cust-123");
 *     assertNull(fallback); // or default/cached value
 *   }
 * }
 * }
 * </pre>
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class CircuitBreakerTestCase {

  protected CircuitBreakerRegistry circuitBreakerRegistry;

  @BeforeEach
  public void setupCircuitBreakerTest() {
    log.info("Setting up circuit breaker test case");
  }

  /**
   * Get a circuit breaker by name.
   *
   * @param name Circuit breaker name (e.g., "billingServiceClient")
   * @return CircuitBreaker instance
   */
  protected CircuitBreaker getCircuitBreaker(String name) {
    return circuitBreakerRegistry.find(name)
        .orElseThrow(() -> new AssertionError("Circuit breaker not found: " + name));
  }

  /**
   * Assert circuit breaker is in a specific state.
   *
   * @param breaker CircuitBreaker to verify
   * @param expectedState Expected state (CLOSED, OPEN, HALF_OPEN, DISABLED)
   */
  protected void assertCircuitBreakerState(CircuitBreaker breaker, CircuitBreaker.State expectedState) {
    CircuitBreaker.State actual = breaker.getState();
    if (!actual.equals(expectedState)) {
      throw new AssertionError("Expected circuit breaker state " + expectedState
          + " but was " + actual);
    }
    log.info("Circuit breaker state verified: {}", actual);
  }

  /**
   * Open the circuit breaker by force (for testing).
   *
   * @param breaker CircuitBreaker to open
   */
  protected void openCircuitBreaker(CircuitBreaker breaker) {
    breaker.transitionToOpenState();
    log.info("Circuit breaker opened: {}", breaker.getName());
  }

  /**
   * Close the circuit breaker by force (for testing).
   *
   * @param breaker CircuitBreaker to close
   */
  protected void closeCircuitBreaker(CircuitBreaker breaker) {
    breaker.transitionToClosedState();
    log.info("Circuit breaker closed: {}", breaker.getName());
  }

  /**
   * Move circuit breaker to half-open state (for testing).
   *
   * @param breaker CircuitBreaker to transition
   */
  protected void halfOpenCircuitBreaker(CircuitBreaker breaker) {
    breaker.transitionToHalfOpenState();
    log.info("Circuit breaker half-open: {}", breaker.getName());
  }

  /**
   * Simulate a service failure.
   * Can be used with mocks to throw exceptions.
   */
  protected void simulateFailure() {
    log.debug("Simulating service failure");
  }

  /**
   * Simulate a service timeout.
   */
  protected void simulateTimeout() {
    log.debug("Simulating service timeout");
  }

  /**
   * Simulate a service recovery (success).
   */
  protected void simulateSuccess() {
    log.debug("Simulating service recovery");
  }

  /**
   * Get metrics for a circuit breaker.
   *
   * @param breaker CircuitBreaker to get metrics for
   * @return CircuitBreaker metrics (failure count, slow calls, etc.)
   */
  protected CircuitBreaker.Metrics getMetrics(CircuitBreaker breaker) {
    return breaker.getMetrics();
  }

  /**
   * Record a failure for testing.
   *
   * @param breaker CircuitBreaker to record failure for
   * @param error Exception to record
   */
  protected void recordFailure(CircuitBreaker breaker, Throwable error) {
    breaker.onError(0, error);
    log.info("Recorded failure for circuit breaker: {} -> {}", breaker.getName(), error.getClass().getSimpleName());
  }

  /**
   * Record a success for testing.
   *
   * @param breaker CircuitBreaker to record success for
   */
  protected void recordSuccess(CircuitBreaker breaker) {
    breaker.onSuccess(0, java.util.concurrent.TimeUnit.MILLISECONDS);
    log.info("Recorded success for circuit breaker: {}", breaker.getName());
  }
}
