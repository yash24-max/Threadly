package dev.threadly.flow.exception;

/**
 * Exception thrown when a flow definition is invalid.
 */
public class InvalidFlowDefinitionException extends RuntimeException {

  private final String flowId;
  private final String reason;

  public InvalidFlowDefinitionException(String reason) {
    super("Invalid flow definition: " + reason);
    this.flowId = null;
    this.reason = reason;
  }

  public InvalidFlowDefinitionException(String flowId, String reason) {
    super("Invalid flow definition for flow " + flowId + ": " + reason);
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
