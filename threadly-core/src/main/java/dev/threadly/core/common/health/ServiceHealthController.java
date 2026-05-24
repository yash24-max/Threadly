package dev.threadly.core.common.health;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service health endpoints for Kubernetes/service mesh integration.
 *
 * Endpoints:
 * - GET /health/live (liveness probe) — is service process alive?
 * - GET /health/ready (readiness probe) — is service ready to accept traffic?
 * - GET /metrics (Prometheus metrics) — service metrics scraping
 *
 * Kubernetes probe configuration example:
 *   livenessProbe:
 *     httpGet:
 *       path: /health/live
 *       port: 8080
 *     initialDelaySeconds: 30
 *     periodSeconds: 10
 *   readinessProbe:
 *     httpGet:
 *       path: /health/ready
 *       port: 8080
 *     initialDelaySeconds: 5
 *     periodSeconds: 5
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class ServiceHealthController {

  private final HealthEndpoint healthEndpoint;

  /**
   * Liveness probe endpoint.
   * Returns 200 if service process is alive, 503 if crashed.
   *
   * Use for: Kubernetes liveness probe (restart on failure)
   */
  @GetMapping("/live")
  public ResponseEntity<Map<String, String>> liveness() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("timestamp", String.valueOf(System.currentTimeMillis()));
    return ResponseEntity.ok(response);
  }

  /**
   * Readiness probe endpoint.
   * Returns 200 if service is ready to handle traffic.
   * Returns 503 if service dependencies (DB, cache, etc.) are unavailable.
   *
   * Use for: Kubernetes readiness probe (remove from load balancer on failure)
   */
  @GetMapping("/ready")
  public ResponseEntity<Map<String, Object>> readiness() {
    try {
      HealthComponent health = healthEndpoint.health();

      // Check overall health status
      String status = health.getStatus().toString();
      boolean isReady = status.equals("UP");

      Map<String, Object> response = new HashMap<>();
      response.put("status", status);
      response.put("ready", isReady);
      response.put("timestamp", System.currentTimeMillis());

      if (isReady) {
        return ResponseEntity.ok(response);
      } else {
        // Service is unhealthy: not ready for traffic
        log.warn("Service readiness check failed: {}", health);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
      }
    } catch (Exception e) {
      log.error("Error checking readiness", e);
      Map<String, String> response = new HashMap<>();
      response.put("status", "DOWN");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
  }

  /**
   * Detailed health status endpoint (for debugging).
   * Returns detailed health info for all components: DB, Redis, etc.
   */
  @GetMapping("/detailed")
  public ResponseEntity<HealthComponent> detailed() {
    HealthComponent health = healthEndpoint.health();
    return ResponseEntity.ok(health);
  }

  /**
   * Service info endpoint.
   * Returns service name, version, and other metadata.
   */
  @GetMapping("/info")
  public ResponseEntity<Map<String, String>> info() {
    Map<String, String> info = new HashMap<>();
    info.put("service", "threadly-core");
    info.put("version", "0.1.0-SNAPSHOT");
    info.put("port", "8080");
    info.put("metrics-port", "9090");
    return ResponseEntity.ok(info);
  }
}
