package dev.threadly.runtime.service;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.exception.InvalidFlowException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * FlowInterpreter parses and traverses flow definitions.
 * Determines next node based on current execution state and flow structure.
 */
@Service
@Slf4j
public class FlowInterpreter {

  /**
   * Get the entry node for a flow
   */
  public JsonNode getEntryNode(JsonNode flowDefinition) {
    if (flowDefinition == null) {
      throw new InvalidFlowException("Flow definition is null");
    }

    if (flowDefinition.has("start_node_id")) {
      String startNodeId = flowDefinition.get("start_node_id").asText();
      return getNodeById(flowDefinition, startNodeId);
    }

    if (flowDefinition.has("nodes") && flowDefinition.get("nodes").isArray()) {
      JsonNode firstNode = flowDefinition.get("nodes").get(0);
      if (firstNode != null) {
        return firstNode;
      }
    }

    throw new InvalidFlowException("No entry point found in flow definition");
  }

  /**
   * Get node by ID from flow definition
   */
  public JsonNode getNodeById(JsonNode flowDefinition, String nodeId) {
    if (flowDefinition == null || nodeId == null) {
      return null;
    }

    JsonNode nodesNode = flowDefinition.get("nodes");
    if (nodesNode == null || !nodesNode.isArray()) {
      log.warn("No nodes array found in flow definition");
      return null;
    }

    for (JsonNode node : nodesNode) {
      if (node.has("id") && node.get("id").asText().equals(nodeId)) {
        return node;
      }
    }

    log.warn("Node not found: {}", nodeId);
    return null;
  }

  /**
   * Get node type
   */
  public String getNodeType(JsonNode node) {
    if (node == null) {
      return null;
    }

    if (node.has("type")) {
      return node.get("type").asText();
    }

    // Try to infer type from node structure
    if (node.has("text") && node.has("variable")) {
      return "QUESTION";
    }
    if (node.has("text") || node.has("message")) {
      return "MESSAGE";
    }
    if (node.has("url") && node.has("method")) {
      return "API_CALL";
    }

    return "UNKNOWN";
  }

  /**
   * Get all edges from a node
   */
  public List<String> getOutgoingEdges(JsonNode node) {
    List<String> edges = new ArrayList<>();

    if (node == null) {
      return edges;
    }

    // Single next edge
    if (node.has("next")) {
      String next = node.get("next").asText();
      if (next != null && !next.isEmpty()) {
        edges.add(next);
      }
    }

    // Branch edges
    if (node.has("true_next")) {
      edges.add(node.get("true_next").asText());
    }
    if (node.has("false_next")) {
      edges.add(node.get("false_next").asText());
    }

    // Switch case edges
    if (node.has("cases") && node.get("cases").isArray()) {
      for (JsonNode caseNode : node.get("cases")) {
        if (caseNode.has("next")) {
          edges.add(caseNode.get("next").asText());
        }
      }
    }

    // Default edge
    if (node.has("default")) {
      edges.add(node.get("default").asText());
    }

    return edges;
  }

  /**
   * Validate flow structure
   */
  public void validateFlow(JsonNode flowDefinition) {
    if (flowDefinition == null) {
      throw new InvalidFlowException("Flow definition is null");
    }

    if (!flowDefinition.has("nodes") || !flowDefinition.get("nodes").isArray()) {
      throw new InvalidFlowException("Flow must have 'nodes' array");
    }

    JsonNode nodesNode = flowDefinition.get("nodes");
    if (nodesNode.size() == 0) {
      throw new InvalidFlowException("Flow must have at least one node");
    }

    // Validate entry point
    try {
      getEntryNode(flowDefinition);
    } catch (Exception e) {
      throw new InvalidFlowException("Invalid entry point: " + e.getMessage());
    }

    // Validate node references
    Set<String> nodeIds = new HashSet<>();
    for (JsonNode node : nodesNode) {
      if (node.has("id")) {
        nodeIds.add(node.get("id").asText());
      }
    }

    for (JsonNode node : nodesNode) {
      List<String> edges = getOutgoingEdges(node);
      for (String edgeNodeId : edges) {
        if (!nodeIds.contains(edgeNodeId) && !edgeNodeId.isEmpty()) {
          log.warn("Node references non-existent node: {} -> {}", node.get("id"), edgeNodeId);
        }
      }
    }

    log.info("Flow validation passed");
  }

  /**
   * Detect cycles in flow
   */
  public boolean hasCycle(JsonNode flowDefinition) {
    try {
      JsonNode startNode = getEntryNode(flowDefinition);
      Set<String> visited = new HashSet<>();
      Set<String> recursionStack = new HashSet<>();
      return hasCycleDFS(flowDefinition, startNode, visited, recursionStack);
    } catch (Exception e) {
      log.warn("Failed to detect cycles: {}", e.getMessage());
      return false;
    }
  }

  /**
   * DFS-based cycle detection
   */
  private boolean hasCycleDFS(JsonNode flowDefinition, JsonNode node,
                             Set<String> visited, Set<String> recursionStack) {
    if (node == null) {
      return false;
    }

    String nodeId = node.has("id") ? node.get("id").asText() : UUID.randomUUID().toString();

    if (recursionStack.contains(nodeId)) {
      return true; // Cycle detected
    }

    if (visited.contains(nodeId)) {
      return false;
    }

    visited.add(nodeId);
    recursionStack.add(nodeId);

    List<String> edges = getOutgoingEdges(node);
    for (String edgeNodeId : edges) {
      JsonNode nextNode = getNodeById(flowDefinition, edgeNodeId);
      if (hasCycleDFS(flowDefinition, nextNode, visited, recursionStack)) {
        return true;
      }
    }

    recursionStack.remove(nodeId);
    return false;
  }

  /**
   * Get all reachable nodes from a starting node
   */
  public Set<String> getReachableNodes(JsonNode flowDefinition, String startNodeId) {
    Set<String> reachable = new HashSet<>();
    Queue<String> queue = new LinkedList<>();
    queue.add(startNodeId);

    while (!queue.isEmpty()) {
      String nodeId = queue.poll();
      if (reachable.contains(nodeId)) {
        continue;
      }

      reachable.add(nodeId);
      JsonNode node = getNodeById(flowDefinition, nodeId);

      if (node != null) {
        List<String> edges = getOutgoingEdges(node);
        for (String edgeNodeId : edges) {
          if (!reachable.contains(edgeNodeId)) {
            queue.add(edgeNodeId);
          }
        }
      }
    }

    return reachable;
  }
}
