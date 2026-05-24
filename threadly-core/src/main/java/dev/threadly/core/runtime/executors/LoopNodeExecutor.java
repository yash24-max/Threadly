package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.runtime.FlowGraph;
import dev.threadly.core.runtime.NodeExecutionResult;
import dev.threadly.core.runtime.NodeExecutor;
import dev.threadly.core.runtime.Session;
import dev.threadly.core.workspace.Bot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Loop node executor that iterates over an array variable and executes the loop body for each item.
 *
 * <p>Node data shape:
 * {
 *   "arrayVariable": "items",
 *   "loopVariableName": "item",
 *   "maxIterations": 100
 * }
 *
 * <p>Sets loop context variables on each iteration:
 * - {{loop.item}}: Current item value
 * - {{loop.index}}: 0-based index
 * - {{loop.total}}: Total number of items
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoopNodeExecutor implements NodeExecutor {

  private static final int DEFAULT_MAX_ITERATIONS = 100;

  @Override
  public String nodeType() {
    return "loop";
  }

  @Override
  public NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String arrayVariable = (String) data.getOrDefault("arrayVariable", "items");
    String loopVariableName = (String) data.getOrDefault("loopVariableName", "item");
    int maxIterations = ((Number) data.getOrDefault("maxIterations", DEFAULT_MAX_ITERATIONS))
        .intValue();

    Map<String, Object> vars = session.getVariables();

    // Load array from session variables
    Object arrayObj = vars.getOrDefault(arrayVariable, List.of());
    List<?> items = new ArrayList<>();

    if (arrayObj instanceof List<?> list) {
      items = list;
    } else if (arrayObj instanceof Iterable<?> iterable) {
      items = new ArrayList<>();
      for (Object item : iterable) {
        ((List<Object>) items).add(item);
      }
    }

    // Enforce max iterations safety limit
    if (items.size() > maxIterations) {
      log.warn(
          "LoopNode {}: Array '{}' has {} items, exceeds maxIterations limit of {}. Clamping to {}.",
          node.getId(),
          arrayVariable,
          items.size(),
          maxIterations,
          maxIterations);
      items = items.subList(0, maxIterations);
    }

    // Set loop metadata in session
    Map<String, Object> loopMeta = new HashMap<>();
    loopMeta.put("arrayVariable", arrayVariable);
    loopMeta.put("loopVariableName", loopVariableName);
    loopMeta.put("total", items.size());
    vars.put("_loop_meta", loopMeta);

    if (items.isEmpty()) {
      log.debug("LoopNode {}: Array '{}' is empty, skipping loop.", node.getId(), arrayVariable);
      session.setVariables(vars);
      // Skip to the loop exit point (next node after loop)
      return NodeExecutionResult.next();
    }

    // Initialize loop context: set first item, index 0, total
    Object firstItem = items.get(0);
    vars.put(loopVariableName, firstItem);
    vars.put("loop.item", firstItem);
    vars.put("loop.index", 0);
    vars.put("loop.total", items.size());
    vars.put("_loop_items", items);
    vars.put("_loop_current_index", 0);

    session.setVariables(vars);

    log.debug(
        "LoopNode {}: Starting loop over array '{}' with {} items.",
        node.getId(),
        arrayVariable,
        items.size());

    // Return next to execute loop body (child nodes)
    return NodeExecutionResult.next();
  }
}
