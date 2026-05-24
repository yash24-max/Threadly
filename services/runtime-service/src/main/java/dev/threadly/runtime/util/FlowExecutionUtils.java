package dev.threadly.runtime.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

/**
 * FlowExecutionUtils provides utility methods for flow processing and execution
 */
@Slf4j
public class FlowExecutionUtils {

  /**
   * Extract all unique node IDs from a flow definition
   */
  public static Set<String> extractNodeIds(JsonNode flowDefinition) {
    Set<String> nodeIds = new HashSet<>();
    if (flowDefinition == null || !flowDefinition.has("nodes")) {
      return nodeIds;
    }

    JsonNode nodesNode = flowDefinition.get("nodes");
    if (nodesNode.isArray()) {
      for (JsonNode node : nodesNode) {
        if (node.has("id")) {
          nodeIds.add(node.get("id").asText());
        }
      }
    }

    return nodeIds;
  }

  /**
   * Find all nodes of a specific type
   */
  public static List<JsonNode> findNodesByType(JsonNode flowDefinition, String nodeType) {
    List<JsonNode> results = new ArrayList<>();
    if (flowDefinition == null || !flowDefinition.has("nodes")) {
      return results;
    }

    JsonNode nodesNode = flowDefinition.get("nodes");
    if (nodesNode.isArray()) {
      for (JsonNode node : nodesNode) {
        String type = node.has("type") ? node.get("type").asText() : null;
        if (nodeType.equalsIgnoreCase(type)) {
          results.add(node);
        }
      }
    }

    return results;
  }

  /**
   * Count total nodes in flow
   */
  public static int countNodes(JsonNode flowDefinition) {
    if (flowDefinition == null || !flowDefinition.has("nodes")) {
      return 0;
    }
    return flowDefinition.get("nodes").size();
  }

  /**
   * Get flow metadata
   */
  public static Map<String, Object> getFlowMetadata(JsonNode flowDefinition) {
    Map<String, Object> metadata = new HashMap<>();

    if (flowDefinition == null) {
      return metadata;
    }

    if (flowDefinition.has("id")) {
      metadata.put("id", flowDefinition.get("id").asText());
    }
    if (flowDefinition.has("name")) {
      metadata.put("name", flowDefinition.get("name").asText());
    }
    if (flowDefinition.has("version")) {
      metadata.put("version", flowDefinition.get("version").asText());
    }
    if (flowDefinition.has("description")) {
      metadata.put("description", flowDefinition.get("description").asText());
    }

    metadata.put("totalNodes", countNodes(flowDefinition));
    metadata.put("nodeIds", extractNodeIds(flowDefinition));

    return metadata;
  }

  /**
   * Deep copy a JsonNode
   */
  public static JsonNode deepCopyJsonNode(JsonNode node, ObjectMapper mapper) {
    try {
      String json = mapper.writeValueAsString(node);
      return mapper.readTree(json);
    } catch (IOException e) {
      log.error("Failed to deep copy JsonNode", e);
      return node;
    }
  }

  /**
   * Merge two objects (for variable updates)
   */
  public static Map<String, Object> mergeObjects(Map<String, Object> base, Map<String, Object> updates) {
    Map<String, Object> result = new HashMap<>(base);
    if (updates != null) {
      result.putAll(updates);
    }
    return result;
  }

  /**
   * Generate unique execution ID
   */
  public static String generateExecutionId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Format execution time for display
   */
  public static String formatExecutionTime(long milliseconds) {
    if (milliseconds < 1000) {
      return milliseconds + "ms";
    }
    double seconds = milliseconds / 1000.0;
    if (seconds < 60) {
      return String.format("%.2fs", seconds);
    }
    double minutes = seconds / 60.0;
    return String.format("%.2fm", minutes);
  }

  /**
   * Validate node structure
   */
  public static boolean isValidNode(JsonNode node) {
    if (node == null) {
      return false;
    }
    if (!node.has("id")) {
      return false;
    }
    if (!node.has("type")) {
      return false;
    }
    return true;
  }

  /**
   * Extract variable references from text
   */
  public static Set<String> extractVariableReferences(String text) {
    Set<String> variables = new HashSet<>();
    if (text == null) {
      return variables;
    }

    String pattern = "\\{\\{(.*?)\\}\\}";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
    java.util.regex.Matcher m = p.matcher(text);

    while (m.find()) {
      String varName = m.group(1).trim();
      variables.add(varName);
    }

    return variables;
  }

  /**
   * Sanitize variable name
   */
  public static String sanitizeVariableName(String name) {
    if (name == null) {
      return null;
    }
    return name.trim().replaceAll("[^a-zA-Z0-9_]", "_");
  }

  /**
   * Check if string is a variable reference
   */
  public static boolean isVariableReference(String value) {
    return value != null && value.matches("^\\{\\{.*\\}\\}$");
  }

  /**
   * Extract variable name from reference
   */
  public static String extractVariableName(String reference) {
    if (!isVariableReference(reference)) {
      return null;
    }
    return reference.substring(2, reference.length() - 2).trim();
  }
}
