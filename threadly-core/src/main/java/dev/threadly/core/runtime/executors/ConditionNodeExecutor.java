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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Evaluates one or more conditions against session variables and routes to trueBranch or
 * falseBranch.
 *
 * <p>Node data shape:
 *
 * <pre>{@code
 * {
 *   "conditions": [
 *     {"variable": "intent", "operator": "eq", "value": "billing"},
 *     {"variable": "score",  "operator": "gte", "value": "80"}
 *   ],
 *   "logicalOperator": "AND",   // "AND" (default) or "OR"
 *   "trueBranch":  "node_yes",
 *   "falseBranch": "node_no"
 * }
 * }</pre>
 *
 * <p>Supported operators: eq, neq, gt, gte, lt, lte, contains, not_contains,
 * starts_with, ends_with, empty, not_empty, regex.
 */
@Slf4j
@Component
public class ConditionNodeExecutor implements NodeExecutor {

  @Override
  public String nodeType() {
    return "condition";
  }

  @Override
  @SuppressWarnings("unchecked")
  public NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String trueBranch = (String) data.get("trueBranch");
    String falseBranch = (String) data.get("falseBranch");
    String logicalOp = data.containsKey("logicalOperator")
        ? String.valueOf(data.get("logicalOperator")).toUpperCase()
        : "AND";

    Object conditionsRaw = data.get("conditions");
    if (!(conditionsRaw instanceof List<?> conditionList) || conditionList.isEmpty()) {
      log.warn("ConditionNode {}: no 'conditions' list configured — routing to false branch", node.getId());
      return jumpTo(falseBranch);
    }

    boolean result = evaluateAll((List<Map<String, Object>>) conditionList, logicalOp, session, node.getId());

    log.debug("ConditionNode {}: evaluation result={} logicalOp={}", node.getId(), result, logicalOp);
    return result ? jumpTo(trueBranch) : jumpTo(falseBranch);
  }

  private boolean evaluateAll(List<Map<String, Object>> conditions, String logicalOp,
      Session session, String nodeId) {
    if ("OR".equals(logicalOp)) {
      for (Map<String, Object> c : conditions) {
        if (evaluateCondition(c, session, nodeId)) return true;
      }
      return false;
    }
    // AND (default)
    for (Map<String, Object> c : conditions) {
      if (!evaluateCondition(c, session, nodeId)) return false;
    }
    return true;
  }

  private boolean evaluateCondition(Map<String, Object> condition, Session session, String nodeId) {
    String variable = condition.containsKey("variable")
        ? String.valueOf(condition.get("variable")) : null;
    String operator = condition.containsKey("operator")
        ? String.valueOf(condition.get("operator")) : "eq";
    String expectedValue = condition.containsKey("value")
        ? String.valueOf(condition.get("value")) : null;

    Object actual = variable != null ? session.getVariables().get(variable) : null;
    String actualStr = actual != null ? actual.toString() : null;

    return evaluate(actualStr, operator, expectedValue, nodeId);
  }

  private boolean evaluate(String actual, String operator, String expected, String nodeId) {
    return switch (operator) {
      case "eq" -> {
        if (actual == null) yield expected == null || "null".equals(expected);
        yield actual.equals(expected);
      }
      case "neq" -> {
        if (actual == null) yield !(expected == null || "null".equals(expected));
        yield !actual.equals(expected);
      }
      case "gt" -> compareNumeric(actual, expected, nodeId) > 0;
      case "gte" -> compareNumeric(actual, expected, nodeId) >= 0;
      case "lt" -> compareNumeric(actual, expected, nodeId) < 0;
      case "lte" -> compareNumeric(actual, expected, nodeId) <= 0;
      case "contains" -> {
        if (actual == null || expected == null) yield false;
        yield actual.toLowerCase().contains(expected.toLowerCase());
      }
      case "not_contains" -> {
        if (actual == null || expected == null) yield true;
        yield !actual.toLowerCase().contains(expected.toLowerCase());
      }
      case "starts_with" -> {
        if (actual == null || expected == null) yield false;
        yield actual.startsWith(expected);
      }
      case "ends_with" -> {
        if (actual == null || expected == null) yield false;
        yield actual.endsWith(expected);
      }
      case "empty" -> actual == null || actual.isBlank();
      case "not_empty" -> actual != null && !actual.isBlank();
      case "regex" -> {
        if (actual == null || expected == null) yield false;
        try {
          yield Pattern.compile(expected).matcher(actual).find();
        } catch (PatternSyntaxException e) {
          log.warn("ConditionNode {}: invalid regex pattern '{}': {}", nodeId, expected, e.getMessage());
          yield false;
        }
      }
      default -> {
        log.warn("ConditionNode {}: unknown operator '{}' — treating as false", nodeId, operator);
        yield false;
      }
    };
  }

  /**
   * Compares actual vs expected as doubles if possible; falls back to lexicographic comparison.
   * Returns negative/zero/positive as per Comparable contract.
   */
  private int compareNumeric(String actual, String expected, String nodeId) {
    if (actual == null || expected == null) return 0;
    try {
      double a = Double.parseDouble(actual);
      double e = Double.parseDouble(expected);
      return Double.compare(a, e);
    } catch (NumberFormatException ex) {
      log.debug("ConditionNode {}: numeric parse failed, falling back to string compare", nodeId);
      return actual.compareTo(expected);
    }
  }

  private NodeExecutionResult jumpTo(String nodeId) {
    if (nodeId == null || nodeId.isBlank()) {
      return NodeExecutionResult.end();
    }
    return NodeExecutionResult.jumpTo(nodeId);
  }
}
