package dev.threadly.common.migration;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DualWriteInterceptor - Phase 2 Migration Support
 *
 * During microservices migration Phase 2, this interceptor:
 * 1. Intercepts POST/PATCH/DELETE requests
 * 2. Writes to BOTH monolith (primary) + new service (shadow)
 * 3. If new service write fails: logs error but doesn't block response (eventual consistency)
 * 4. Publishes metrics: write-lag, write-failures per service
 *
 * Configuration:
 *   migration.dual.write.enabled=true
 *   migration.dual.write.fail.strategy=log_only (don't fail fast)
 *   migration.dual.write.timeout.ms=5000 (max wait for shadow write)
 *
 * Metrics (Micrometer):
 *   - dual_write.lag_ms{service} - latency of shadow write (ms)
 *   - dual_write.failures{service} - count of failed shadow writes
 *   - dual_write.success{service} - count of successful shadow writes
 */

@Slf4j
@Component
@ConditionalOnProperty(
  name = "migration.dual.write.enabled",
  havingValue = "true",
  matchIfMissing = false
)
public class DualWriteInterceptor implements HandlerInterceptor {

  private final DualWriteService dualWriteService;
  private final MeterRegistry meterRegistry;
  private final ExecutorService executor = Executors.newFixedThreadPool(10);

  // Track write operations for metrics
  private final Map<String, WriteMetrics> writeMetrics = new ConcurrentHashMap<>();

  public DualWriteInterceptor(DualWriteService dualWriteService, MeterRegistry meterRegistry) {
    this.dualWriteService = dualWriteService;
    this.meterRegistry = meterRegistry;
  }

  @Override
  public void postHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler,
    org.springframework.web.servlet.ModelAndView modelAndView
  ) {
    // Only intercept write operations
    String method = request.getMethod();
    if (!isWriteOperation(method)) {
      return;
    }

    // Skip if response is error
    if (response.getStatus() >= 400) {
      return;
    }

    String serviceName = detectServiceName(request);
    String operation = String.format("%s %s", method, request.getRequestURI());

    log.debug("[DualWrite] Intercepted {} on service: {}", operation, serviceName);

    // Shadow write to new service (async, non-blocking)
    executor.submit(() -> {
      long startTime = System.currentTimeMillis();
      try {
        dualWriteService.syncToNewService(request, response);
        long lag = System.currentTimeMillis() - startTime;

        recordSuccess(serviceName, lag);
        log.debug("[DualWrite] Shadow write succeeded for {} in {}ms", serviceName, lag);
      } catch (Exception e) {
        recordFailure(serviceName);
        log.warn(
          "[DualWrite] Shadow write failed for service: {}. Error: {}. (This is non-blocking)",
          serviceName,
          e.getMessage()
        );
      }
    });
  }

  private boolean isWriteOperation(String method) {
    return method.equals("POST") || method.equals("PATCH") || method.equals("DELETE");
  }

  private String detectServiceName(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (uri.contains("/bots/")) return "workspace-service";
    if (uri.contains("/flows/")) return "flow-service";
    if (uri.contains("/sessions/")) return "runtime-service";
    if (uri.contains("/conversations/")) return "conversation-service";
    if (uri.contains("/kb/")) return "knowledge-service";
    if (uri.contains("/dashboard/")) return "analytics-service";
    if (uri.contains("/billing/")) return "billing-service";
    if (uri.contains("/integrations/")) return "integration-service";
    if (uri.contains("/auth/") || uri.contains("/orgs/")) return "identity-service";
    return "unknown";
  }

  private void recordSuccess(String serviceName, long lagMs) {
    WriteMetrics metrics = writeMetrics.computeIfAbsent(
      serviceName,
      k -> new WriteMetrics()
    );
    metrics.successCount++;
    metrics.totalLag += lagMs;

    meterRegistry.timer("dual_write.lag_ms", "service", serviceName).record(lagMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    meterRegistry.counter("dual_write.success", "service", serviceName).increment();
  }

  private void recordFailure(String serviceName) {
    WriteMetrics metrics = writeMetrics.computeIfAbsent(
      serviceName,
      k -> new WriteMetrics()
    );
    metrics.failureCount++;

    meterRegistry.counter("dual_write.failures", "service", serviceName).increment();
  }

  /**
   * Get write metrics for monitoring dashboard
   */
  public Map<String, WriteMetrics> getMetrics() {
    return Map.copyOf(writeMetrics);
  }

  /**
   * Metrics POJO for write operations
   */
  public static class WriteMetrics {
    public long successCount = 0;
    public long failureCount = 0;
    public long totalLag = 0;

    public double avgLag() {
      if (successCount == 0) return 0;
      return (double) totalLag / successCount;
    }

    public double failureRate() {
      if (successCount + failureCount == 0) return 0;
      return (double) failureCount / (successCount + failureCount);
    }
  }
}
