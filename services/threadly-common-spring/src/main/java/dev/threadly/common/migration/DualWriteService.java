package dev.threadly.common.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * DualWriteService - Shadow Write Execution
 *
 * Responsible for forwarding write operations to new microservices
 * during Phase 2 migration (dual-write mode).
 *
 * Configuration:
 *   migration.dual.write.enabled=true
 *   migration.dual.write.timeout.ms=5000
 *   migration.dual.write.skip.headers=X-Internal-*
 */

@Slf4j
@Service
@ConditionalOnProperty(
  name = "migration.dual.write.enabled",
  havingValue = "true",
  matchIfMissing = false
)
public class DualWriteService {

  private final HttpClient httpClient;

  @Value("${migration.dual.write.timeout.ms:5000}")
  private long timeoutMs;

  @Value("${migration.dual.write.service.base.url:http://localhost:8080}")
  private String serviceBaseUrl;

  public DualWriteService() {
    this.httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofMillis(5000))
      .build();
  }

  /**
   * Sync write operation from monolith to new microservice
   *
   * @param request Original HTTP request from monolith
   * @param response Original HTTP response from monolith
   * @throws Exception if shadow write fails
   */
  public void syncToNewService(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String query = request.getQueryString();

    // Build target URL (new service via API gateway)
    String targetUrl = serviceBaseUrl + uri;
    if (query != null) {
      targetUrl += "?" + query;
    }

    log.debug("[DualWrite] Forwarding {} {} to new service", method, uri);

    try {
      // Read request body
      String body = readRequestBody(request);

      // Build HTTP request
      HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(new java.net.URI(targetUrl))
        .timeout(Duration.ofMillis(timeoutMs));

      // Copy headers (exclude internal headers)
      copyHeaders(request, builder);

      // Set method and body
      if ("POST".equals(method)) {
        builder.POST(HttpRequest.BodyPublishers.ofString(body));
      } else if ("PATCH".equals(method)) {
        builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body));
      } else if ("DELETE".equals(method)) {
        builder.DELETE();
      } else {
        log.warn("Unsupported write method for dual-write: {}", method);
        return;
      }

      // Execute request
      HttpResponse<String> shadowResponse = httpClient.send(
        builder.build(),
        HttpResponse.BodyHandlers.ofString()
      );

      if (shadowResponse.statusCode() >= 400) {
        log.warn(
          "[DualWrite] Shadow write returned error status: {} for {} {}",
          shadowResponse.statusCode(),
          method,
          uri
        );
      } else {
        log.debug("[DualWrite] Shadow write succeeded: {} {}", method, uri);
      }
    } catch (Exception e) {
      log.error("[DualWrite] Error syncing to new service: {}", e.getMessage(), e);
      throw e;
    }
  }

  private String readRequestBody(HttpServletRequest request) throws IOException {
    StringBuilder body = new StringBuilder();
    try (BufferedReader reader = request.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        body.append(line);
      }
    }
    return body.toString();
  }

  private void copyHeaders(HttpServletRequest request, HttpRequest.Builder builder) {
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();

      // Skip internal headers and connection headers
      if (shouldSkipHeader(headerName)) {
        continue;
      }

      String headerValue = request.getHeader(headerName);
      builder.header(headerName, headerValue);
    }
  }

  private boolean shouldSkipHeader(String headerName) {
    String lower = headerName.toLowerCase();
    return lower.startsWith("x-internal-")
      || lower.equals("content-length")
      || lower.equals("transfer-encoding")
      || lower.equals("connection")
      || lower.equals("host")
      || lower.equals("upgrade")
      || lower.equals("proxy-connection");
  }
}
