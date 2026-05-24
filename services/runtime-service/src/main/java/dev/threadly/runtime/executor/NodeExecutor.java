package dev.threadly.runtime.executor;

/**
 * NodeExecutor is the abstract base class for all node type executors.
 * Implements strategy pattern for different node type handling.
 * Each node type (message, question, condition, etc.) has its own executor implementation.
 */
public abstract class NodeExecutor {

  /**
   * Get the name of this executor
   */
  public abstract String getName();

  /**
   * Get the node type this executor handles
   */
  public abstract String getType();

  /**
   * Execute the node and return the execution result.
   * This is the main execution method that all subclasses must implement.
   *
   * @param context The execution context containing session, variables, and node information
   * @return ExecutionResult containing next node, variables, and messages
   */
  public abstract ExecutionResult execute(ExecutionContext context);

  /**
   * Validate node configuration before execution.
   * Optional validation method that can be overridden by subclasses.
   *
   * @param context The execution context
   * @return true if node is valid, false otherwise
   */
  public boolean validate(ExecutionContext context) {
    if (context.getCurrentNode() == null) {
      return false;
    }
    return true;
  }

  /**
   * Get error message for validation failure.
   * Override if validate() returns false.
   *
   * @param context The execution context
   * @return Error message describing validation failure
   */
  public String getValidationError(ExecutionContext context) {
    return "Node validation failed";
  }

  /**
   * Determine if this executor can handle the given node type
   */
  public boolean canHandle(String nodeType) {
    return this.getType().equalsIgnoreCase(nodeType);
  }

  /**
   * Default toString implementation for logging
   */
  @Override
  public String toString() {
    return String.format("%s[type=%s]", getName(), getType());
  }
}
