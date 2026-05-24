package dev.threadly.runtime.executor;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.model.Session;
import lombok.*;
import org.slf4j.MDC;

import java.util.*;

/**
 * ExecutionContext provides all necessary information for node execution.
 * Acts as a transient container for execution state during flow processing.
 * Uses MDC for distributed tracing of execution flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionContext {

  // Session and flow information
  private Session session;
  private JsonNode flowDefinition; // Entire flow JSON
  private JsonNode currentNode;     // Current node being executed

  // Execution state
  private Map<String, Object> sessionVariables;
  private List<String> executedNodeIds;  // For loop detection
  private Integer executionDepth;
  private Long startTimeMs;

  // Conversation context
  private String conversationSummary;
  private List<ConversationTurn> recentTurns;

  // Visitor context
  private Map<String, Object> visitorData;

  // External service clients (injected)
  private Map<String, Object> serviceClients; // Rest template, feign clients, etc.

  // Configuration
  private Integer maxExecutionDepth;
  private Long nodeExecutionTimeoutMs;
  private Boolean enableLogging;

  /**
   * Conversation turn representation for context building
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ConversationTurn {
    private String role; // USER, ASSISTANT
    private String content;
    private Long timestamp;
    private String nodeId;
  }

  /**
   * Set MDC context for distributed tracing
   */
  public void setMDCContext() {
    if (session != null) {
      MDC.put("sessionId", session.getId());
      MDC.put("botId", session.getBotId());
      MDC.put("visitorId", session.getVisitorId());
      MDC.put("flowId", session.getFlowId());
    }
    if (currentNode != null && currentNode.has("id")) {
      MDC.put("nodeId", currentNode.get("id").asText());
    }
    if (executionDepth != null) {
      MDC.put("executionDepth", String.valueOf(executionDepth));
    }
  }

  /**
   * Clear MDC context
   */
  public void clearMDCContext() {
    MDC.remove("sessionId");
    MDC.remove("botId");
    MDC.remove("visitorId");
    MDC.remove("flowId");
    MDC.remove("nodeId");
    MDC.remove("executionDepth");
  }

  /**
   * Get a variable with type safety
   */
  @SuppressWarnings("unchecked")
  public <T> T getVariable(String name, Class<T> clazz) {
    Object value = sessionVariables.get(name);
    if (value == null) {
      return null;
    }
    if (clazz.isInstance(value)) {
      return (T) value;
    }
    throw new RuntimeException("Variable " + name + " is not of type " + clazz.getName());
  }

  /**
   * Set a variable
   */
  public void setVariable(String name, Object value) {
    sessionVariables.put(name, value);
  }

  /**
   * Check if variable exists
   */
  public boolean hasVariable(String name) {
    return sessionVariables.containsKey(name);
  }

  /**
   * Get node property by path (supports nested access with dot notation)
   */
  public JsonNode getNodeProperty(String path) {
    if (currentNode == null) {
      return null;
    }
    String[] parts = path.split("\\.");
    JsonNode current = currentNode;
    for (String part : parts) {
      if (current.has(part)) {
        current = current.get(part);
      } else {
        return null;
      }
    }
    return current;
  }

  /**
   * Check for infinite loops
   */
  public boolean isExecutionDepthExceeded() {
    return executionDepth != null && maxExecutionDepth != null &&
           executionDepth >= maxExecutionDepth;
  }

  /**
   * Calculate elapsed execution time
   */
  public long getElapsedTimeMs() {
    if (startTimeMs == null) {
      return 0;
    }
    return System.currentTimeMillis() - startTimeMs;
  }

  /**
   * Get recent conversation for LLM context
   */
  public List<ConversationTurn> getRecentConversation(int limit) {
    if (recentTurns == null || recentTurns.isEmpty()) {
      return new ArrayList<>();
    }
    int start = Math.max(0, recentTurns.size() - limit);
    return recentTurns.subList(start, recentTurns.size());
  }

  /**
   * Add conversation turn
   */
  public void addConversationTurn(String role, String content, String nodeId) {
    if (recentTurns == null) {
      recentTurns = new ArrayList<>();
    }
    recentTurns.add(ConversationTurn.builder()
        .role(role)
        .content(content)
        .timestamp(System.currentTimeMillis())
        .nodeId(nodeId)
        .build());
  }
}
