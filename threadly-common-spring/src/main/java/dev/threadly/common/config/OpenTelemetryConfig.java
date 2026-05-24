package dev.threadly.common.config;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry auto-configuration for distributed tracing.
 *
 * Exports traces to OTLP collector (Honeycomb, Tempo, Jaeger).
 * Trace ID automatically added to all logs (via MDC).
 *
 * Configuration:
 * management.tracing.enabled=true
 * management.otlp.tracing.endpoint=http://otel-collector:4317
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
public class OpenTelemetryConfig {

  @Value("${management.otlp.tracing.endpoint:http://otel-collector:4317}")
  private String otlpEndpoint;

  /**
   * Configure OTLP exporter for sending traces to collector.
   */
  @Bean
  public OtlpGrpcSpanExporter otlpGrpcSpanExporter() {
    log.info("Initializing OTLP Exporter: endpoint={}", otlpEndpoint);
    return OtlpGrpcSpanExporter.builder()
        .setEndpoint(otlpEndpoint)
        .build();
  }

  /**
   * Configure tracer provider with OTLP span processor.
   */
  @Bean
  public SdkTracerProvider sdkTracerProvider(OtlpGrpcSpanExporter otlpExporter) {
    return SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.create(otlpExporter))
        .build();
  }

  /**
   * Tracer bean for manual instrumentation (optional).
   */
  @Bean
  public Tracer tracer(SdkTracerProvider sdkTracerProvider) {
    return sdkTracerProvider.get("threadly-service");
  }
}
