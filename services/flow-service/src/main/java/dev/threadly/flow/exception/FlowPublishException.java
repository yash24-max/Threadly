package dev.threadly.flow.exception;

/**
 * Exception thrown when flow publishing fails.
 */
public class FlowPublishException extends RuntimeException {

  private final String flowId;
  private final String reason;

  public FlowPublishException(String flowId, String reason) {
    super("Failed to publish flow " + flowId + ": " + reason);
    this.flowId = flowId;
    this.reason = reason;
  }

  public String getFlowId() {
    return flowId;
  }

  public String getReason() {
    return reason;
  }
}
