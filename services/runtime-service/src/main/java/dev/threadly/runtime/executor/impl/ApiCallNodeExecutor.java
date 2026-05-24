package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import dev.threadly.runtime.service.SessionVariableManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * ApiCallNodeExecutor handles HTTP requests to external APIs.
 * Supports GET, POST, PUT, DELETE methods with headers and body.
 */
@Component
@Slf4j
public class ApiCallNodeExecutor extends NodeExecutor {

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private SessionVariableManager variableManager;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public String getName() {
    return "API Call Executor";
  }

  @Override
  public String getType() {
    return "API_CALL";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("url") && node.has("method");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "API Call node must have 'url' and 'method' fields";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing API_CALL node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();
      String url = node.get("url").asText();
      String method = node.get("method").asText().toUpperCase();

      // Resolve variables in URL
      url = variableManager.resolveVariables(url, context.getSessionVariables());
      log.info("Making {} request to: {}", method, url);

      // Build headers
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");
      if (node.has("headers") && node.get("headers").isObject()) {
        JsonNode headersNode = node.get("headers");
        headersNode.fieldNames().forEachRemaining(fieldName -> {
          String value = headersNode.get(fieldName).asText();
          String resolvedValue = variableManager.resolveVariables(
              value,
              context.getSessionVariables()
          );
          headers.set(fieldName, resolvedValue);
        });
      }

      // Build request body
      String body = null;
      if (node.has("body")) {
        JsonNode bodyNode = node.get("body");
        if (bodyNode.isObject()) {
          body = objectMapper.writeValueAsString(bodyNode);
        } else {
          body = bodyNode.asText();
        }
        body = variableManager.resolveVariables(body, context.getSessionVariables());
      }

      // Make request
      HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
      ResponseEntity<String> response = restTemplate.exchange(
          url,
          HttpMethod.valueOf(method),
          httpEntity,
          String.class
      );

      log.info("API response status: {}", response.getStatusCode());

      // Parse response
      JsonNode responseBody = objectMapper.readTree(response.getBody());

      // Store response in variable if specified
      if (node.has("response_variable")) {
        String responseVarName = node.get("response_variable").asText();
        context.setVariable(responseVarName, responseBody);
        log.debug("Stored API response in variable: {}", responseVarName);
      }

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("API call successful")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .nextNodeId(node.has("next") ? node.get("next").asText() : null)
          .build()
          .addVariable("_api_response_status", response.getStatusCode().value())
          .addVariable("_api_response_body", responseBody);

    } catch (Exception e) {
      log.error("Error executing API_CALL node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("API call failed")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }
}
