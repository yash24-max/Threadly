package dev.threadly.runtime.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * NodeExecutorFactory uses factory pattern to provide appropriate executor
 * for any node type in the flow.
 * Manages and registers all node executor implementations.
 */
@Component
@Slf4j
public class NodeExecutorFactory {

  private final Map<String, NodeExecutor> executorRegistry = new HashMap<>();

  /**
   * Constructor that auto-discovers and registers all NodeExecutor beans
   */
  @Autowired
  public NodeExecutorFactory(List<NodeExecutor> executors) {
    log.info("Initializing NodeExecutorFactory with {} executors", executors.size());
    for (NodeExecutor executor : executors) {
      String type = executor.getType().toUpperCase();
      executorRegistry.put(type, executor);
      log.debug("Registered executor: {} for type: {}", executor.getName(), type);
    }
  }

  /**
   * Get executor for a specific node type
   *
   * @param nodeType The type of the node
   * @return NodeExecutor for the node type
   * @throws NodeExecutorNotFoundException if no executor is registered for the type
   */
  public NodeExecutor getExecutor(String nodeType) {
    String normalizedType = nodeType.toUpperCase();
    NodeExecutor executor = executorRegistry.get(normalizedType);
    if (executor == null) {
      log.error("No executor found for node type: {}", nodeType);
      throw new NodeExecutorNotFoundException(
          "No executor registered for node type: " + nodeType);
    }
    return executor;
  }

  /**
   * Check if executor exists for node type
   */
  public boolean hasExecutor(String nodeType) {
    return executorRegistry.containsKey(nodeType.toUpperCase());
  }

  /**
   * Get all registered executor types
   */
  public Set<String> getRegisteredTypes() {
    return new HashSet<>(executorRegistry.keySet());
  }

  /**
   * Register a custom executor
   */
  public void registerExecutor(String nodeType, NodeExecutor executor) {
    String normalizedType = nodeType.toUpperCase();
    executorRegistry.put(normalizedType, executor);
    log.info("Registered custom executor: {} for type: {}", executor.getName(), normalizedType);
  }

  /**
   * Exception thrown when executor is not found
   */
  public static class NodeExecutorNotFoundException extends RuntimeException {
    public NodeExecutorNotFoundException(String message) {
      super(message);
    }

    public NodeExecutorNotFoundException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
