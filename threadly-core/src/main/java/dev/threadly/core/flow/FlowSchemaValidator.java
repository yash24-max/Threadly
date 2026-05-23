package dev.threadly.core.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.core.runtime.FlowGraph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates flow JSON against structural and type-specific rules.
 *
 * <p>Rules enforced:
 * <ol>
 *   <li>Exactly one node with type="start"</li>
 *   <li>At least one node with type="end" OR type="handoff"</li>
 *   <li>All edge source/target IDs must reference existing node IDs</li>
 *   <li>No orphaned nodes (every non-start node reachable from start via DFS)</li>
 *   <li>No cycles (DFS cycle detection)</li>
 *   <li>Each node's "data" satisfies type-specific required fields</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class FlowSchemaValidator {

  private final ObjectMapper objectMapper;

  public record ValidationResult(boolean valid, List<String> errors) {
    public static ValidationResult ok() {
      return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(List<String> errors) {
      return new ValidationResult(false, List.copyOf(errors));
    }
  }

  public ValidationResult validate(String flowJson) {
    List<String> errors = new ArrayList<>();

    if (flowJson == null || flowJson.isBlank()) {
      errors.add("Flow JSON must not be empty.");
      return ValidationResult.fail(errors);
    }

    FlowGraph graph;
    FlowGraph.RawFlow raw;
    try {
      raw = objectMapper.readValue(flowJson, FlowGraph.RawFlow.class);
      graph = FlowGraph.parse(flowJson, objectMapper);
    } catch (Exception e) {
      errors.add("Flow JSON is malformed: " + e.getMessage());
      return ValidationResult.fail(errors);
    }

    List<FlowGraph.Node> nodes = raw.getNodes();
    List<FlowGraph.Edge> edges = raw.getEdges();

    if (nodes == null || nodes.isEmpty()) {
      errors.add("Flow must contain at least one node.");
      return ValidationResult.fail(errors);
    }

    // Rule 1: Exactly one start node
    List<FlowGraph.Node> startNodes = nodes.stream()
        .filter(n -> "start".equals(n.getType()))
        .toList();
    if (startNodes.size() == 0) {
      errors.add("Flow must have exactly one node with type='start', but found none.");
    } else if (startNodes.size() > 1) {
      errors.add("Flow must have exactly one node with type='start', but found " + startNodes.size() + ".");
    }

    // Rule 2: At least one end or handoff node
    boolean hasTerminalNode = nodes.stream()
        .anyMatch(n -> "end".equals(n.getType()) || "handoff".equals(n.getType()));
    if (!hasTerminalNode) {
      errors.add("Flow must have at least one node with type='end' or type='handoff'.");
    }

    // Rule 3: All edge source/target IDs must reference existing node IDs
    Set<String> nodeIds = new HashSet<>();
    for (FlowGraph.Node n : nodes) {
      nodeIds.add(n.getId());
    }
    if (edges != null) {
      for (FlowGraph.Edge edge : edges) {
        if (edge.getSource() == null || !nodeIds.contains(edge.getSource())) {
          errors.add("Edge '" + edge.getId() + "' has invalid source node ID: " + edge.getSource());
        }
        if (edge.getTarget() == null || !nodeIds.contains(edge.getTarget())) {
          errors.add("Edge '" + edge.getId() + "' has invalid target node ID: " + edge.getTarget());
        }
      }
    }

    // Rules 4 & 5: Reachability (no orphaned nodes) + no cycles — only if we have a valid start
    if (startNodes.size() == 1) {
      String startId = startNodes.get(0).getId();

      // DFS-based reachability + cycle detection
      Set<String> visited = new HashSet<>();
      Set<String> inStack = new HashSet<>();
      boolean hasCycle = dfsCycleCheck(startId, graph, visited, inStack);

      if (hasCycle) {
        errors.add("Flow contains a cycle. Cycles are not permitted.");
      }

      // Orphaned nodes: nodes not reachable from start
      for (FlowGraph.Node n : nodes) {
        if (!n.getId().equals(startId) && !visited.contains(n.getId())) {
          errors.add("Node '" + n.getId() + "' (type='" + n.getType() + "') is orphaned and not reachable from the start node.");
        }
      }
    }

    // Rule 6: Type-specific field validation
    for (FlowGraph.Node node : nodes) {
      validateNodeData(node, errors);
    }

    return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
  }

  /**
   * DFS cycle detection. Returns true if a back-edge (cycle) is found.
   * Populates {@code visited} with all nodes reachable from {@code nodeId}.
   */
  private boolean dfsCycleCheck(String nodeId, FlowGraph graph,
      Set<String> visited, Set<String> inStack) {
    visited.add(nodeId);
    inStack.add(nodeId);

    List<FlowGraph.Edge> outEdges = graph.getEdgesFrom()
        .getOrDefault(nodeId, List.of());

    for (FlowGraph.Edge edge : outEdges) {
      String target = edge.getTarget();
      if (target == null) continue;
      if (!visited.contains(target)) {
        if (dfsCycleCheck(target, graph, visited, inStack)) {
          return true;
        }
      } else if (inStack.contains(target)) {
        return true;
      }
    }

    inStack.remove(nodeId);
    return false;
  }

  @SuppressWarnings("unchecked")
  private void validateNodeData(FlowGraph.Node node, List<String> errors) {
    String type = node.getType();
    Map<String, Object> data = node.getData();
    String prefix = "Node '" + node.getId() + "' (type='" + type + "')";

    if (type == null || data == null) return;

    switch (type) {
      case "message" -> {
        String content = stringField(data, "content");
        if (content == null || content.isBlank()) {
          errors.add(prefix + " requires non-blank 'content' in data.");
        }
      }
      case "question" -> {
        String variable = stringField(data, "variable");
        if (variable == null || variable.isBlank()) {
          errors.add(prefix + " requires non-blank 'variable' in data.");
        }
      }
      case "ai_reply" -> {
        String systemPrompt = stringField(data, "systemPrompt");
        if (systemPrompt == null || systemPrompt.isBlank()) {
          errors.add(prefix + " requires non-blank 'systemPrompt' in data.");
        }
      }
      case "condition" -> {
        Object conditions = data.get("conditions");
        if (!(conditions instanceof Collection<?> col) || col.isEmpty()) {
          errors.add(prefix + " requires non-empty 'conditions' array in data.");
        }
      }
      case "api_call" -> {
        String url = stringField(data, "url");
        String method = stringField(data, "method");
        if (url == null || url.isBlank()) {
          errors.add(prefix + " requires non-blank 'url' in data.");
        }
        if (method == null || method.isBlank()) {
          errors.add(prefix + " requires non-blank 'method' in data.");
        }
      }
      case "set_variable" -> {
        Object assignments = data.get("assignments");
        if (!(assignments instanceof Collection<?> col) || col.isEmpty()) {
          errors.add(prefix + " requires non-empty 'assignments' array in data.");
        }
      }
      case "collect_input" -> {
        String variable = stringField(data, "variable");
        if (variable == null || variable.isBlank()) {
          errors.add(prefix + " requires non-blank 'variable' in data.");
        }
      }
      case "switch" -> {
        String variable = stringField(data, "variable");
        if (variable == null || variable.isBlank()) {
          errors.add(prefix + " requires non-blank 'variable' in data.");
        }
        Object cases = data.get("cases");
        if (!(cases instanceof Collection<?> col) || col.isEmpty()) {
          errors.add(prefix + " requires non-empty 'cases' array in data.");
        }
      }
      case "delay" -> {
        Object seconds = data.get("seconds");
        boolean valid = false;
        if (seconds instanceof Number n) {
          valid = n.doubleValue() > 0;
        } else if (seconds instanceof String s) {
          try {
            valid = Double.parseDouble(s) > 0;
          } catch (NumberFormatException ignored) {
            // falls through to error
          }
        }
        if (!valid) {
          errors.add(prefix + " requires 'seconds' to be a positive number in data.");
        }
      }
      case "send_email" -> {
        String to = stringField(data, "to");
        String subject = stringField(data, "subject");
        if (to == null || to.isBlank()) {
          errors.add(prefix + " requires non-blank 'to' in data.");
        }
        if (subject == null || subject.isBlank()) {
          errors.add(prefix + " requires non-blank 'subject' in data.");
        }
      }
      // start, end, handoff have no required data fields
      default -> { /* unknown types pass through */ }
    }
  }

  private String stringField(Map<String, Object> data, String key) {
    Object v = data.get(key);
    return v instanceof String s ? s : (v != null ? v.toString() : null);
  }
}
