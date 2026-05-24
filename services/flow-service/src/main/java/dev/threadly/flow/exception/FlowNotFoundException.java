package dev.threadly.flow.exception;

/**
 * Exception thrown when a flow is not found.
 */
public class FlowNotFoundException extends RuntimeException {

  private final String flowId;

  public FlowNotFoundException(String flowId) {
    super("Flow not found: " + flowId);
    this.flowId = flowId;
  }

  public FlowNotFoundException(String flowId, String message) {
    super(message);
    this.flowId = flowId;
  }

  public String getFlowId() {
    return flowId;
  }
}
