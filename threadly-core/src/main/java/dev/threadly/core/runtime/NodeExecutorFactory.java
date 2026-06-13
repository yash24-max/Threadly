package dev.threadly.core.runtime;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class NodeExecutorFactory {

  private final Map<String, NodeExecutor> executors;

  public NodeExecutorFactory(List<NodeExecutor> all) {
    this.executors = all.stream()
        .collect(Collectors.toMap(NodeExecutor::nodeType, Function.identity()));
  }

  public NodeExecutor getExecutor(String nodeType) {
    NodeExecutor executor = executors.get(nodeType);
    if (executor == null) {
      // Default: skip unknown node types
      return new NodeExecutor() {
        @Override public String nodeType() { return nodeType; }
        @Override public NodeExecutionResult execute(FlowGraph.Node node, Session session,
            dev.threadly.core.conversation.Conversation conversation,
            dev.threadly.core.workspace.Bot bot, java.util.UUID orgId) {
          return NodeExecutionResult.next();
        }
      };
    }
    return executor;
  }
}
