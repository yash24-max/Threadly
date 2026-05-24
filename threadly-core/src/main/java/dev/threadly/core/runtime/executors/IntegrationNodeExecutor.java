package dev.threadly.core.runtime.executors;

import dev.threadly.core.common.ThreadlyMetrics;
import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.runtime.FlowGraph;
import dev.threadly.core.runtime.NodeExecutionResult;
import dev.threadly.core.runtime.NodeExecutor;
import dev.threadly.core.runtime.Session;
import dev.threadly.core.workspace.Bot;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Executes third-party integrations (Slack, HubSpot, Salesforce, etc.) and stores the result.
 *
 * <p>Node data shape:
 * {
 *   "integrationType": "slack",
 *   "action": "send_message",
 *   "integrationId": "uuid-of-integration",
 *   "params": {
 *     "channel": "{{slack_channel}}",
 *     "message": "{{session.last_input}}"
 *   },
 *   "outputVariable": "integration_response",
 *   "errorBranch": "error_node_id"
 * }
 *
 * <p>Resolves template variables in params, calls IntegrationRegistry to execute the action,
 * and stores results in session variables with prefix "integration.output.*".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationNodeExecutor implements NodeExecutor {

  private final ThreadlyMetrics metrics;
  // Note: IntegrationRegistry would be injected here in production
  // private final IntegrationRegistry integrationRegistry;

  @Override
  public String nodeType() {
    return "integration";
  }

  @Override
  public NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String integrationType = (String) data.get("integrationType");
    String action = (String) data.get("action");
    String integrationId = (String) data.get("integrationId");
    Map<String, Object> params =
        (Map<String, Object>) data.getOrDefault("params", new HashMap<>());
    String outputVariable = (String) data.getOrDefault("outputVariable", "integration_result");
    String errorBranch = (String) data.getOrDefault("errorBranch", null);

    if (integrationType == null || integrationType.isBlank()) {
      log.error("IntegrationNode {}: integrationType is missing in node data.", node.getId());
      return NodeExecutionResult.next();
    }

    if (action == null || action.isBlank()) {
      log.error("IntegrationNode {}: action is missing in node data.", node.getId());
      return NodeExecutionResult.next();
    }

    if (integrationId == null || integrationId.isBlank()) {
      log.error("IntegrationNode {}: integrationId is missing in node data.", node.getId());
      return NodeExecutionResult.next();
    }

    try {
      // Resolve template variables in params
      Map<String, Object> resolvedParams = new HashMap<>();
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        String template = entry.getValue() == null ? "" : entry.getValue().toString();
        String resolved = TemplateEngine.render(template, session.getVariables());
        resolvedParams.put(entry.getKey(), resolved);
      }

      log.debug(
          "IntegrationNode {}: Executing {} integration action '{}' with params: {}",
          node.getId(),
          integrationType,
          action,
          resolvedParams);

      // In production, call the integration registry to execute the action:
      // IntegrationRegistry.IntegrationResult result = integrationRegistry.execute(
      //     integrationType, action, resolvedParams, integrationId, orgId, bot.getId());

      // For now, simulate a successful integration call
      Map<String, Object> integrationResult = executeIntegration(
          integrationType,
          action,
          resolvedParams,
          integrationId,
          orgId,
          bot.getId());

      // Store result in session variables with prefix
      Map<String, Object> vars = session.getVariables();
      vars.put(outputVariable, integrationResult.getOrDefault("data", new HashMap<>()));
      vars.put("integration.success", integrationResult.getOrDefault("success", false));
      vars.put("integration.message", integrationResult.getOrDefault("message", ""));
      vars.put("integration.output.status", integrationResult.getOrDefault("status", "unknown"));

      // Store individual output fields with dot notation
      Object outputData = integrationResult.get("output");
      if (outputData instanceof Map<?, ?> outputMap) {
        for (Map.Entry<?, ?> entry : outputMap.entrySet()) {
          vars.put("integration.output." + entry.getKey(), entry.getValue());
        }
      }

      session.setVariables(vars);

      // Track metrics
      metrics.incrementMessagesProcessed(bot.getId().toString(), "integration");

      log.debug(
          "IntegrationNode {}: {} integration action '{}' completed successfully.",
          node.getId(),
          integrationType,
          action);

      return NodeExecutionResult.next();

    } catch (Exception e) {
      log.error(
          "IntegrationNode {}: Failed to execute {} action '{}': {}",
          node.getId(),
          integrationType,
          action,
          e.getMessage(),
          e);

      // Store error in session
      Map<String, Object> vars = session.getVariables();
      vars.put(outputVariable + "_error", e.getMessage());
      vars.put("integration.success", false);
      vars.put("integration.error", e.getMessage());
      session.setVariables(vars);

      // Route to error branch if specified
      if (errorBranch != null && !errorBranch.isBlank()) {
        log.debug("IntegrationNode {}: Routing to error branch: {}", node.getId(), errorBranch);
        return NodeExecutionResult.jumpTo(errorBranch);
      }

      return NodeExecutionResult.next();
    }
  }

  /**
   * Simulates integration execution. In production, this would be delegated to an
   * IntegrationRegistry that dispatches to specific integration handlers (SlackIntegration,
   * HubSpotIntegration, etc.).
   */
  private Map<String, Object> executeIntegration(
      String integrationType,
      String action,
      Map<String, Object> params,
      String integrationId,
      UUID orgId,
      UUID botId) {
    // This is a simplified mock. In production, route to actual integration handlers.
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("status", "completed");
    result.put("message", "Integration action executed successfully");
    result.put("data", new HashMap<>());

    // Simulate response based on integration type
    Map<String, Object> output = new HashMap<>();
    switch (integrationType) {
      case "slack":
        output.put("channel_id", params.getOrDefault("channel", "unknown"));
        output.put("message_ts", "1234567890.123456");
        break;
      case "hubspot":
        output.put("contact_id", params.getOrDefault("email", "unknown"));
        output.put("created", true);
        break;
      case "salesforce":
        output.put("record_id", params.getOrDefault("id", "unknown"));
        output.put("updated", true);
        break;
      default:
        output.put("action_result", action + " completed");
    }

    result.put("output", output);
    return result;
  }
}
