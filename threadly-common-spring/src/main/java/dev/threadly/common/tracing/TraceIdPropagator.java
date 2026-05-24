package dev.threadly.common.tracing;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propagate distributed trace IDs across service boundaries.
 *
 * Trace flow:
 * 1. Client sends HTTP request with "traceparent" header (W3C Trace Context)
 * 2. This component extracts it in HTTP handler
 * 3. Stores in ThreadLocal (Spring RequestContext)
 * 4. When publishing Kafka events, injects into message headers
 * 5. Consumer receives headers, sets RequestContext for downstream calls
 *
 * Header format (W3C Trace Context):
 * "00-traceId-spanId-sampled"
 * - Version: 00 (W3C 1.0)
 * - traceId: 32-char hex (e.g., "4bf92f3577b34da6a3ce929d0e0e4736")
 * - spanId: 16-char hex (e.g., "00f067aa0ba902b7")
 * - sampled: 00 (not sampled) or 01 (sampled)
 *
 * Example:
 * <pre>
 * {@code
 * // In HTTP handler (Spring interceptor or servlet filter):
 * @Component
 * public class TraceIdInterceptor implements HandlerInterceptor {
 *   @Autowired TraceIdPropagator propagator;
 *
 *   @Override
 *   public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
 *     String traceparent = req.getHeader("traceparent");
 *     propagator.setTraceIdFromHeader(traceparent);
 *     return true;
 *   }
 * }
 *
 * // In Kafka producer:
 * KafkaTemplate.send(
 *   MessageBuilder.withPayload(event)
 *     .setHeader("traceparent", propagator.getTraceparent())
 *     .build()
 * );
 *
 * // In Kafka consumer:
 * @KafkaListener(...)
 * public void onEvent(@Header("traceparent") String traceparent) {
 *   propagator.setTraceIdFromHeader(traceparent);
 *   // Now any downstream HTTP calls will include the same traceparent
 * }
 * }
 * </pre>
 *
 * Integration with OpenTelemetry:
 * - Auto-instrumentation will capture HTTP and Kafka spans
 * - Trace ID propagation allows correlating all spans into one trace
 * - Metrics and logs can be tagged with traceId for correlation
 */
@Slf4j
@Component
public class TraceIdPropagator {

  private static final String TRACEPARENT_HEADER = "traceparent";
  private static final String TRACE_ID_ATTR = "traceId";
  private static final String TRACEPARENT_ATTR = "traceparent";

  /**
   * Extract and set trace ID from HTTP request header.
   *
   * Typically called in a servlet filter or HTTP interceptor.
   *
   * @param traceparent W3C Trace Context header value (e.g., "00-traceId-spanId-sampled")
   */
  public void setTraceIdFromHeader(String traceparent) {
    if (traceparent == null || traceparent.isEmpty()) {
      // Generate new trace ID
      traceparent = generateNewTraceparent();
      log.debug("Generated new trace ID: {}", extractTraceId(traceparent));
    } else {
      log.debug("Received trace ID from header: {}", extractTraceId(traceparent));
    }

    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs != null) {
      attrs.setAttribute(TRACEPARENT_ATTR, traceparent, ServletRequestAttributes.SCOPE_REQUEST);
      attrs.setAttribute(TRACE_ID_ATTR, extractTraceId(traceparent), ServletRequestAttributes.SCOPE_REQUEST);
    }
  }

  /**
   * Get the current trace ID (extracted from request header or generated).
   *
   * Returns null if no request context (e.g., called from scheduled task).
   *
   * @return Trace ID (32-char hex) or null
   */
  public String getTraceId() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return null;
    }
    return (String) attrs.getAttribute(TRACE_ID_ATTR, ServletRequestAttributes.SCOPE_REQUEST);
  }

  /**
   * Get the full traceparent header value for propagation.
   *
   * Returns null if no request context.
   *
   * @return Traceparent header value or null
   */
  public String getTraceparent() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return generateNewTraceparent();
    }
    Object attr = attrs.getAttribute(TRACEPARENT_ATTR, ServletRequestAttributes.SCOPE_REQUEST);
    return attr != null ? (String) attr : generateNewTraceparent();
  }

  /**
   * Inject trace ID into Kafka message headers.
   *
   * Use this when building a Kafka message:
   * <pre>
   * {@code
   * Message<SomeEvent> msg = MessageBuilder.withPayload(event)
   *     .setHeader("traceparent", propagator.getTraceparent())
   *     .build();
   * kafkaTemplate.send("my-topic", msg);
   * }
   * </pre>
   *
   * @param kafkaTemplate KafkaTemplate to send message
   * @param topic Kafka topic
   * @param key Message key (partition key)
   * @param payload Message payload
   */
  public void sendWithTraceId(
      KafkaTemplate<String, Object> kafkaTemplate,
      String topic,
      String key,
      Object payload) {

    String traceparent = getTraceparent();

    Message<Object> message = MessageBuilder
        .withPayload(payload)
        .setHeader(TRACEPARENT_HEADER, traceparent)
        .setHeader(KafkaHeaders.TOPIC, topic)
        .build();

    log.debug("Sending Kafka message with trace ID: topic={}, traceId={}",
        topic, extractTraceId(traceparent));

    kafkaTemplate.send(message);
  }

  /**
   * Extract trace ID from traceparent header.
   *
   * Format: "00-traceId-spanId-sampled"
   *
   * @param traceparent Traceparent header value
   * @return Trace ID (32-char hex) or null
   */
  private String extractTraceId(String traceparent) {
    if (traceparent == null || traceparent.isEmpty()) {
      return null;
    }
    String[] parts = traceparent.split("-");
    return parts.length > 1 ? parts[1] : null;
  }

  /**
   * Generate a new W3C Trace Context header.
   *
   * Format: "00-traceId-spanId-01" (sampled)
   * - version: 00 (W3C 1.0)
   * - traceId: random 32-char hex
   * - spanId: random 16-char hex
   * - sampled: 01 (always sample)
   *
   * @return New traceparent value
   */
  private String generateNewTraceparent() {
    String traceId = UUID.randomUUID().toString().replace("-", "");
    String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    return "00-" + traceId + "-" + spanId + "-01";
  }
}
