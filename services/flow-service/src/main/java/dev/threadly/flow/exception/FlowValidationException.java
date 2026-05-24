package dev.threadly.flow.exception;

import java.util.List;

/**
 * Exception thrown when flow validation fails.
 */
public class FlowValidationException extends RuntimeException {

  private final String flowId;
  private final List<String> errors;

  public FlowValidationException(String flowId, List<String> errors) {
    super("Flow validation failed for flow " + flowId + ": " + String.join("; ", errors));
    this.flowId = flowId;
    this.errors = errors;
  }

  public FlowValidationException(String flowId, String error) {
    super("Flow validation failed for flow " + flowId + ": " + error);
    this.flowId = flowId;
    this.errors = List.of(error);
  }

  public String getFlowId() {
    return flowId;
  }

  public List<String> getErrors() {
    return errors;
  }
}
