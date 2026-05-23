package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.runtime.*;
import dev.threadly.core.workspace.Bot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Sets one or more session variables from literal values or template expressions.
 * Node data shape:
 * {
 *   "assignments": [
 *     { "variable": "customer_name", "value": "{{session.last_input}}" },
 *     { "variable": "greeting", "value": "Hello, {{customer_name}}!" }
 *   ]
 * }
 */
@Slf4j
@Component
public class SetVariableNodeExecutor implements NodeExecutor {

  @Override
  public String nodeType() { return "set_variable"; }

  @Override
  public NodeExecutionResult execute(FlowGraph.Node node, Session session,
      Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    Object assignments = data.get("assignments");

    if (assignments instanceof Iterable<?> list) {
      Map<String, Object> vars = session.getVariables();
      for (Object item : list) {
        if (item instanceof Map<?, ?> rawAssignment) {
          @SuppressWarnings("unchecked")
          Map<String, Object> assignment = (Map<String, Object>) rawAssignment;
          String variable = (String) assignment.get("variable");
          Object rawValue = assignment.getOrDefault("value", "");
          String valueTemplate = rawValue == null ? "" : rawValue.toString();
          if (variable != null && !variable.isBlank()) {
            String resolved = TemplateEngine.render(valueTemplate, vars);
            vars.put(variable, resolved);
            log.debug("SetVariable: {}={}", variable, resolved);
          }
        }
      }
      session.setVariables(vars);
    }

    return NodeExecutionResult.next();
  }
}
