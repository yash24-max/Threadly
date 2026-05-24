package dev.threadly.runtime.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import dev.threadly.runtime.executor.NodeExecutorFactory;
import dev.threadly.runtime.exception.FlowExecutionException;
import dev.threadly.runtime.exception.InvalidFlowException;
import dev.threadly.runtime.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * RuntimeExecutor is the main orchestrator for flow execution.
 * Coordinates node execution, flow traversal, and session state management.
 */
@Service
@Slf4j
public class RuntimeExecutor {

  @Autowired
  private NodeExecutorFactory executorFactory;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private SessionVariableManager variableManager;

  @Autowired
  private ExecutionTracker executionTracker;

  @Autowired
  private FlowInterpreter flowInterpreter;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${runtime.max-execution-depth:100}")
  private Integer maxExecutionDepth;

  @Value("${runtime.node-execution-timeout-ms:30000}")
  private Long nodeExecutionTimeoutMs;

  /**
   * Execute a flow for a session starting from the entry node
   */
  @Transactional
  public ExecutionResult executeFlow(String sessionId, JsonNode flowDefinition) {
    Session session = sessionService.getSession(sessionId);
    log.info("Starting flow execution for session: {}", sessionId);

    try {
      // Validate flow
      flowInterpreter.validateFlow(flowDefinition);

      // Get entry node
      JsonNode entryNode = flowInterpreter.getEntryNode(flowDefinition);

      // Initialize context
      Map<String, Object> variables = variableManager.getAllVariables(sessionId);
      ExecutionContext context = ExecutionContext.builder()
          .session(session)
          .flowDefinition(flowDefinition)
          .currentNode(entryNode)
          .sessionVariables(variables)
          .executedNodeIds(new ArrayList<>())
          .executionDepth(0)
          .startTimeMs(System.currentTimeMillis())
          .maxExecutionDepth(maxExecutionDepth)
          .nodeExecutionTimeoutMs(nodeExecutionTimeoutMs)
          .enableLogging(true)
          .build();

      return executeNode(context);

    } catch (Exception e) {
      log.error("Error executing flow", e);
      sessionService.updateSessionState(sessionId, Session.SessionState.ERROR);
      throw new FlowExecutionException("Flow execution failed: " + e.getMessage(), e);
    }
  }

  /**
   * Resume execution from where it was paused
   */
  @Transactional
  public ExecutionResult resumeExecution(String sessionId, String userResponse, JsonNode flowDefinition) {
    Session session = sessionService.getSession(sessionId);
    log.info("Resuming flow execution for session: {}", sessionId);

    try {
      // Store user response in appropriate variable
      // This would be determined from the paused state

      sessionService.resumeSession(sessionId);

      // Continue execution from the node after the question
      Map<String, Object> variables = variableManager.getAllVariables(sessionId);
      JsonNode entryNode = flowInterpreter.getEntryNode(flowDefinition);

      ExecutionContext context = ExecutionContext.builder()
          .session(session)
          .flowDefinition(flowDefinition)
          .currentNode(entryNode)
          .sessionVariables(variables)
          .executedNodeIds(new ArrayList<>())
          .executionDepth(0)
          .startTimeMs(System.currentTimeMillis())
          .maxExecutionDepth(maxExecutionDepth)
          .nodeExecutionTimeoutMs(nodeExecutionTimeoutMs)
          .enableLogging(true)
          .build();

      return executeNode(context);

    } catch (Exception e) {
      log.error("Error resuming execution", e);
      throw new FlowExecutionException("Resume execution failed: " + e.getMessage(), e);
    }
  }

  /**
   * Execute a single node and continue flow
   */
  private ExecutionResult executeNode(ExecutionContext context) {
    // Check execution depth
    if (context.isExecutionDepthExceeded()) {
      log.error("Execution depth exceeded: {}", context.getExecutionDepth());
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Maximum execution depth exceeded")
          .errorMessage("Possible infinite loop detected")
          .executionTimeMs(System.currentTimeMillis() - context.getStartTimeMs())
          .build();
    }

    // Check elapsed time
    if (context.getElapsedTimeMs() > nodeExecutionTimeoutMs) {
      log.error("Execution timeout exceeded");
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.TIMEOUT)
          .statusMessage("Execution timeout")
          .errorMessage("Flow execution exceeded timeout")
          .executionTimeMs(System.currentTimeMillis() - context.getStartTimeMs())
          .build();
    }

    JsonNode currentNode = context.getCurrentNode();
    if (currentNode == null) {
      log.info("Reached end of flow (no next node)");
      sessionService.endSession(context.getSession().getId());
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Flow completed")
          .executionTimeMs(System.currentTimeMillis() - context.getStartTimeMs())
          .build();
    }

    String nodeId = currentNode.has("id") ? currentNode.get("id").asText() : "unknown";
    String nodeType = flowInterpreter.getNodeType(currentNode);

    log.debug("Executing node: {} (type: {})", nodeId, nodeType);

    // Check for loops
    if (context.getExecutedNodeIds().contains(nodeId)) {
      log.warn("Node has been executed before (potential loop): {}", nodeId);
    }
    context.getExecutedNodeIds().add(nodeId);

    // Get executor
    NodeExecutor executor;
    try {
      executor = executorFactory.getExecutor(nodeType);
    } catch (NodeExecutorFactory.NodeExecutorNotFoundException e) {
      log.error("No executor found for node type: {}", nodeType);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Unknown node type: " + nodeType)
          .errorMessage(e.getMessage())
          .executionTimeMs(System.currentTimeMillis() - context.getStartTimeMs())
          .build();
    }

    // Validate node
    if (!executor.validate(context)) {
      log.error("Node validation failed: {}", executor.getValidationError(context));
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Node validation failed")
          .errorMessage(executor.getValidationError(context))
          .executionTimeMs(System.currentTimeMillis() - context.getStartTimeMs())
          .build();
    }

    // Execute node
    long nodeStartTime = System.currentTimeMillis();
    context.setExecutionDepth(context.getExecutionDepth() + 1);
    ExecutionResult result = executor.execute(context);

    // Track execution
    executionTracker.logExecution(context.getSession().getId(), nodeId, nodeType, currentNode, result);

    // Update variables in database
    if (result.getVariableUpdates() != null && !result.getVariableUpdates().isEmpty()) {
      variableManager.setVariables(context.getSession().getId(), result.getVariableUpdates());
    }

    // Update context variables
    if (result.getVariableUpdates() != null) {
      context.getSessionVariables().putAll(result.getVariableUpdates());
    }

    // Handle paused execution
    if (result.isPaused()) {
      log.info("Flow paused at node: {}", nodeId);
      sessionService.pauseSession(context.getSession().getId());
      return result;
    }

    // Handle failure
    if (!result.isSuccess()) {
      log.error("Node execution failed: {}", result.getStatusMessage());
      sessionService.updateSessionState(context.getSession().getId(), Session.SessionState.ERROR);
      return result;
    }

    // Continue to next node
    String nextNodeId = result.getNextNodeId();
    if (nextNodeId == null || nextNodeId.isEmpty()) {
      log.info("No next node specified, ending flow");
      sessionService.endSession(context.getSession().getId());
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Flow completed")
          .executionTimeMs(System.currentTimeMillis() - context.getStartTimeMs())
          .messages(result.getMessages())
          .build();
    }

    // Get next node
    JsonNode nextNode = flowInterpreter.getNodeById(context.getFlowDefinition(), nextNodeId);
    if (nextNode == null) {
      log.warn("Next node not found: {}", nextNodeId);
      sessionService.endSession(context.getSession().getId());
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Next node not found")
          .errorMessage("Node " + nextNodeId + " not found in flow")
          .executionTimeMs(System.currentTimeMillis() - context.getStartTimeMs())
          .build();
    }

    context.setCurrentNode(nextNode);
    return executeNode(context);
  }
}
