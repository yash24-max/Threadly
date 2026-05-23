package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.runtime.FlowGraph;
import dev.threadly.core.runtime.NodeExecutionResult;
import dev.threadly.core.runtime.NodeExecutor;
import dev.threadly.core.runtime.Session;
import dev.threadly.core.workspace.Bot;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Routes flow execution to a specific node based on the value of a session variable.
 *
 * <p>Node data:
 *
 * <pre>{@code
 * {
 *   "variable": "intent",
 *   "cases": [
 *     {"value": "billing", "next": "node_x"},
 *     {"value": "support", "next": "node_y"}
 *   ],
 *   "default": "node_z"
 * }
 * }</pre>
 */
@Slf4j
@Component
public class SwitchNodeExecutor implements NodeExecutor {

  @Override
  public String nodeType() {
    return "switch";
  }

  @Override
  public NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String variable = (String) data.get("variable");
    String defaultNode = (String) data.get("default");

    if (variable == null) {
      log.warn("SwitchNode {}: no variable configured — jumping to default", node.getId());
      return defaultNode != null ? NodeExecutionResult.jumpTo(defaultNode) : NodeExecutionResult.end();
    }

    Object actualValue = session.getVariables().get(variable);
    String actualStr = actualValue != null ? actualValue.toString() : "";

    Object casesRaw = data.get("cases");
    if (casesRaw instanceof List<?> caseList) {
      for (Object caseObj : caseList) {
        if (caseObj instanceof Map<?, ?> caseMap) {
          String caseValue = String.valueOf(caseMap.get("value"));
          String nextNode = String.valueOf(caseMap.get("next"));
          if (actualStr.equals(caseValue)) {
            log.debug(
                "SwitchNode {}: matched case '{}' → jumping to '{}'",
                node.getId(),
                caseValue,
                nextNode);
            return NodeExecutionResult.jumpTo(nextNode);
          }
        }
      }
    }

    log.debug(
        "SwitchNode {}: no case matched for value '{}' → jumping to default '{}'",
        node.getId(),
        actualStr,
        defaultNode);

    if (defaultNode != null) {
      return NodeExecutionResult.jumpTo(defaultNode);
    }
    return NodeExecutionResult.end();
  }
}
