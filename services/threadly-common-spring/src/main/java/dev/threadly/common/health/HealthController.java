package dev.threadly.common.health;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check endpoints for Kubernetes probes.
 *
 * Endpoints:
 * - GET /health → Liveness probe (is service running?)
 * - GET /ready → Readiness probe (is service ready for traffic?)
 * - GET /metrics → Prometheus metrics on :9090/actuator/prometheus
 *
 * Kubernetes integration:
 * <pre>
 * {@code
 * apiVersion: apps/v1
 * kind: Deployment
 * metadata:
 *   name: conversation-service
 * spec:
 *   template:
 *     spec:
 *       containers:
 *       - name: conversation-service
 *         livenessProbe:
 *           httpGet:
 *             path: /health
 *             port: 8080
 *           initialDelaySeconds: 10
 *           periodSeconds: 10
 *
 *         readinessProbe:
 *           httpGet:
 *             path: /ready
 *             port: 8080
 *           initialDelaySeconds: 5
 *           periodSeconds: 5
 * }
 * </pre>
 *
 * Prometheus metrics configuration:
 * <pre>
 * spring.boot.admin:
 *   routes:
 *     - id: prometheus
 *       uri: http://localhost:9090
 *       predicates:
 *       - Path=/metrics
 *
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health,metrics,prometheus
 *   metrics:
 *     export:
 *       prometheus:
 *         enabled: true
 * }
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class HealthController {

  private final HealthEndpoint healthEndpoint;

  /**
   * Liveness probe: Is the service process running?
   *
   * Returns 200 OK if the service is alive.
   * Kubernetes restarts the pod if this returns non-200 after initialDelaySeconds.
   *
   * @return 200 OK
   */
  @GetMapping("health")
  public ResponseEntity<Map<String, Object>> liveness() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "alive");
    log.debug("Liveness probe: service is alive");
    return ResponseEntity.ok(response);
  }

  /**
   * Readiness probe: Is the service ready to accept traffic?
   *
   * Checks:
   * 1. Database connectivity
   * 2. Kafka connectivity
   * 3. External service dependencies (Feign clients)
   *
   * Returns 200 OK if all dependencies are healthy.
   * Kubernetes removes the pod from load balancer if this returns non-200.
   *
   * @return 200 OK if ready, 503 Service Unavailable if not
   */
  @GetMapping("ready")
  public ResponseEntity<Map<String, Object>> readiness() {
    try {
      HealthComponent health = healthEndpoint.health();

      // Check overall health status
      String status = health.getStatus().toString();

      if ("UP".equals(status)) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("service", "ready");
        log.debug("Readiness probe: service is ready");
        return ResponseEntity.ok(response);
      } else {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("service", "not-ready");
        response.put("details", health.getDetails());
        log.warn("Readiness probe: service is not ready. Status: {}", status);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
      }
    } catch (Exception e) {
      log.error("Error checking readiness: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("status", "DOWN");
      response.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
  }
}
