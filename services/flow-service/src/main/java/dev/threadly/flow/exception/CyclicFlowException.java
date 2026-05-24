package dev.threadly.flow.exception;

import java.util.List;

/**
 * Exception thrown when a cyclic dependency is detected in the flow.
 */
public class CyclicFlowException extends RuntimeException {

  private final String flowId;
  private final List<String> cycle;

  public CyclicFlowException(String flowId, List<String> cycle) {
    super("Cyclic dependency detected in flow " + flowId + ": " + String.join(" -> ", cycle));
    this.flowId = flowId;
    this.cycle = cycle;
  }

  public CyclicFlowException(String flowId, String message) {
    super("Cyclic dependency detected in flow " + flowId + ": " + message);
    this.flowId = flowId;
    this.cycle = List.of();
  }

  public String getFlowId() {
    return flowId;
  }

  public List<String> getCycle() {
    return cycle;
  }
}
