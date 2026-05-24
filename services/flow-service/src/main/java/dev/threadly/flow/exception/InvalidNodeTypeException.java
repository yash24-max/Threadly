package dev.threadly.flow.exception;

/**
 * Exception thrown when an invalid node type is used in a flow.
 */
public class InvalidNodeTypeException extends RuntimeException {

  private final String nodeType;
  private final String flowId;

  public InvalidNodeTypeException(String nodeType) {
    super("Invalid node type: " + nodeType);
    this.nodeType = nodeType;
    this.flowId = null;
  }

  public InvalidNodeTypeException(String flowId, String nodeType) {
    super("Invalid node type '" + nodeType + "' in flow " + flowId);
    this.nodeType = nodeType;
    this.flowId = flowId;
  }

  public String getNodeType() {
    return nodeType;
  }

  public String getFlowId() {
    return flowId;
  }
}
