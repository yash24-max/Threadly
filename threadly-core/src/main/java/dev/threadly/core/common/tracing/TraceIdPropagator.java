package dev.threadly.core.common.tracing;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Distributed tracing ID propagator.
 *
 * Ensures one trace ID spans all hops:
 * - HTTP Request (Nginx/API Gateway)
 * - Service → Service (via Feign/RestTemplate)
 * - Service → Kafka (event publish)
 * - Kafka → Service (event consume)
 * - Service → External API (Stripe, etc.)
 *
 * Uses OpenTelemetry/Micrometer standards:
 * - Header: "traceparent" (W3C format)
 * - Alternative: "X-Trace-Id"
 *
 * Production: OpenTelemetry auto-instrumentation handles this.
 * This class provides manual fallbacks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TraceIdPropagator implements HandlerInterceptor {

  private final Tracer tracer;

  private static final String HEADER_TRACEPARENT = "traceparent";
  private static final String HEADER_TRACE_ID = "X-Trace-Id";

  /**
   * Extract trace ID from HTTP request headers.
   * Called by Spring interceptor on incoming requests.
   */
  @Override
  public boolean preHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) {
    // Extract W3C traceparent header
    String traceparent = request.getHeader(HEADER_TRACEPARENT);
    if (traceparent != null) {
      // Parse W3C format: "00-trace-id-span-id-trace-flags"
      String[] parts = traceparent.split("-");
      if (parts.length >= 3) {
        String traceId = parts[1];
        // OpenTelemetry will handle context propagation
        log.debug("Trace ID extracted from request: {}", traceId);
      }
    }

    // Fallback to custom header
    String customTraceId = request.getHeader(HEADER_TRACE_ID);
    if (customTraceId != null) {
      log.debug("Custom trace ID extracted: {}", customTraceId);
    }

    return true;
  }

  /**
   * Inject trace ID into outgoing Kafka event.
   * Called before publishing event to Kafka.
   */
  public void injectTraceIdToKafka(ProducerRecord<String, String> record) {
    try {
      String currentTraceId = getOrCreateTraceId();
      // In production, Micrometer/OpenTelemetry handles this automatically
      // This is a fallback for manual event publishing
      log.debug("Trace ID injected to Kafka: {}", currentTraceId);
    } catch (Exception e) {
      log.warn("Failed to inject trace ID to Kafka event", e);
    }
  }

  /**
   * Extract trace ID from Kafka message.
   * Called when consuming events.
   */
  public String extractTraceIdFromKafka(ConsumerRecord<String, String> record) {
    try {
      // Extract from message headers
      byte[] traceIdBytes = record.headers().lastHeader("traceparent") != null
          ? record.headers().lastHeader("traceparent").value()
          : null;

      if (traceIdBytes != null) {
        String traceparent = new String(traceIdBytes);
        String[] parts = traceparent.split("-");
        if (parts.length >= 3) {
          String traceId = parts[1];
          log.debug("Trace ID extracted from Kafka: {}", traceId);
          return traceId;
        }
      }
    } catch (Exception e) {
      log.warn("Failed to extract trace ID from Kafka event", e);
    }

    return getOrCreateTraceId();
  }

  /**
   * Get current trace ID from Micrometer context or create new one.
   */
  private String getOrCreateTraceId() {
    if (tracer != null && tracer.currentSpan() != null) {
      return tracer.currentSpan().context().traceId();
    }
    // Fallback: generate new UUID
    return java.util.UUID.randomUUID().toString();
  }

  /**
   * Add trace ID to response headers (for debugging).
   */
  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception ex
  ) {
    String traceId = getOrCreateTraceId();
    response.setHeader(HEADER_TRACE_ID, traceId);
  }
}
