package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.runtime.*;
import dev.threadly.core.workspace.Bot;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConditionNodeExecutor implements NodeExecutor {

  @Override
  public String nodeType() { return "condition"; }

  @Override
  public NodeExecutionResult execute(FlowGraph.Node node, Session session,
      Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String variable = (String) data.get("variable");
    String operator = (String) data.getOrDefault("operator", "exists");
    String value = (String) data.get("value");

    Object actual = variable != null ? session.getVariables().get(variable) : null;
    boolean result = evaluate(actual, operator, value);
    return NodeExecutionResult.next(result ? "true" : "false");
  }

  private boolean evaluate(Object actual, String operator, String expected) {
    return switch (operator) {
      case "exists" -> actual != null && !actual.toString().isBlank();
      case "equals" -> actual != null && actual.toString().equals(expected);
      case "contains" -> actual != null && actual.toString().contains(expected != null ? expected : "");
      case "gt" -> {
        try { yield Double.parseDouble(actual.toString()) > Double.parseDouble(expected); }
        catch (Exception e) { yield false; }
      }
      case "lt" -> {
        try { yield Double.parseDouble(actual.toString()) < Double.parseDouble(expected); }
        catch (Exception e) { yield false; }
      }
      default -> false;
    };
  }
}
