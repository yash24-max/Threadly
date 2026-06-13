package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.runtime.*;
import dev.threadly.core.workspace.Bot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Executes an external HTTP API call and stores the response
 * in the session variable specified by {@code responseVariable}.
 */
@Slf4j
@Component
public class ApiCallNodeExecutor implements NodeExecutor {

  private final WebClient webClient = WebClient.builder()
      .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
      .build();

  @Override
  public String nodeType() { return "api_call"; }

  @Override
  public NodeExecutionResult execute(FlowGraph.Node node, Session session,
      Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String method = TemplateEngine.render(
        (String) data.getOrDefault("method", "GET"), session.getVariables()).toUpperCase();
    String url = TemplateEngine.render(
        (String) data.getOrDefault("url", ""), session.getVariables());
    String body = TemplateEngine.render(
        (String) data.getOrDefault("body", ""), session.getVariables());
    String responseVar = (String) data.getOrDefault("responseVariable", "api_response");

    if (url.isBlank()) {
      log.warn("ApiCallNode {}: url is blank — skipping", node.getId());
      return NodeExecutionResult.next();
    }

    try {
      WebClient.RequestBodySpec spec = webClient.method(HttpMethod.valueOf(method)).uri(url);

      // Add custom headers
      Object headers = data.get("headers");
      if (headers instanceof Map<?, ?> hMap) {
        hMap.forEach((k, v) -> spec.header(String.valueOf(k),
            TemplateEngine.render(String.valueOf(v), session.getVariables())));
      }

      String responseBody;
      if (!body.isBlank() && !method.equals("GET")) {
        spec.contentType(org.springframework.http.MediaType.APPLICATION_JSON);
        responseBody = spec.bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(10))
            .block();
      } else {
        responseBody = spec.retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(10))
            .block();
      }

      Map<String, Object> vars = session.getVariables();
      vars.put(responseVar, responseBody != null ? responseBody : "");
      session.setVariables(vars);

      log.debug("ApiCallNode {} completed → stored in session var '{}'", node.getId(), responseVar);
    } catch (Exception e) {
      log.error("ApiCallNode {} failed: {}", node.getId(), e.getMessage());
      Map<String, Object> vars = session.getVariables();
      vars.put(responseVar + "_error", e.getMessage());
      session.setVariables(vars);
    }

    return NodeExecutionResult.next();
  }
}
