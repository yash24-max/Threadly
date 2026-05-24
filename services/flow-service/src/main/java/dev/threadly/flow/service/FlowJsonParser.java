package dev.threadly.flow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.threadly.flow.entity.FlowEdge;
import dev.threadly.flow.entity.FlowNode;
import dev.threadly.flow.repository.FlowEdgeRepository;
import dev.threadly.flow.repository.FlowNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Flow JSON parsing and building.
 * Converts flow entities to JSON definitions and vice versa.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowJsonParser {

  private final FlowNodeRepository flowNodeRepository;
  private final FlowEdgeRepository flowEdgeRepository;
  private final ObjectMapper objectMapper;

  /**
   * Builds a complete flow definition JSON from flow entities.
   * The JSON includes all nodes and edges in a format suitable for the UI.
   *
   * @param flowId the flow ID
   * @return JSON string representing the complete flow definition
   */
  @Transactional(readOnly = true)
  public String buildFlowDefinitionJson(String flowId) {
    log.debug("Building flow definition JSON for flow: {}", flowId);

    ObjectNode rootNode = objectMapper.createObjectNode();

    // Add metadata
    rootNode.put("flowId", flowId);
    rootNode.put("version", "1.0");

    // Build nodes array
    List<FlowNode> nodes = flowNodeRepository.findByFlowId(flowId);
    ArrayNode nodesArray = objectMapper.createArrayNode();

    for (FlowNode node : nodes) {
      ObjectNode nodeObj = objectMapper.createObjectNode();
      nodeObj.put("id", node.getNodeId());
      nodeObj.put("type", node.getType());
      nodeObj.put("positionX", node.getPositionX());
      nodeObj.put("positionY", node.getPositionY());

      // Parse data if it's valid JSON, otherwise include as string
      try {
        if (node.getDataJson() != null) {
          nodeObj.set("data", objectMapper.readTree(node.getDataJson()));
        }
      } catch (Exception e) {
        log.debug("Failed to parse node data as JSON, treating as string: {}", node.getDataJson());
        if (node.getDataJson() != null) {
          nodeObj.put("data", node.getDataJson());
        }
      }

      nodesArray.add(nodeObj);
    }
    rootNode.set("nodes", nodesArray);

    // Build edges array
    List<FlowEdge> edges = flowEdgeRepository.findByFlowId(flowId);
    ArrayNode edgesArray = objectMapper.createArrayNode();

    for (FlowEdge edge : edges) {
      ObjectNode edgeObj = objectMapper.createObjectNode();
      edgeObj.put("id", edge.getEdgeId());
      edgeObj.put("source", edge.getSourceNodeId());
      edgeObj.put("target", edge.getTargetNodeId());

      if (edge.getSourceHandle() != null) {
        edgeObj.put("sourceHandle", edge.getSourceHandle());
      }
      if (edge.getTargetHandle() != null) {
        edgeObj.put("targetHandle", edge.getTargetHandle());
      }

      edgesArray.add(edgeObj);
    }
    rootNode.set("edges", edgesArray);

    try {
      return objectMapper.writeValueAsString(rootNode);
    } catch (Exception e) {
      log.error("Failed to serialize flow definition JSON", e);
      throw new RuntimeException("Failed to build flow definition JSON", e);
    }
  }

  /**
   * Parses a flow definition JSON and extracts metadata.
   * Validates that the JSON structure is correct.
   *
   * @param jsonDefinition the flow definition JSON
   * @return parsed JSON node for further processing
   * @throws IllegalArgumentException if JSON is invalid
   */
  public JsonNode parseFlowDefinition(String jsonDefinition) {
    try {
      JsonNode node = objectMapper.readTree(jsonDefinition);

      // Validate structure
      if (!node.has("nodes") || !node.has("edges")) {
        throw new IllegalArgumentException("Flow definition must contain 'nodes' and 'edges' arrays");
      }

      if (!node.get("nodes").isArray() || !node.get("edges").isArray()) {
        throw new IllegalArgumentException("'nodes' and 'edges' must be arrays");
      }

      return node;
    } catch (Exception e) {
      log.error("Failed to parse flow definition JSON", e);
      throw new IllegalArgumentException("Invalid flow definition JSON: " + e.getMessage(), e);
    }
  }

  /**
   * Extracts node count from a flow definition JSON.
   *
   * @param jsonDefinition the flow definition JSON
   * @return the number of nodes
   */
  public int getNodeCount(String jsonDefinition) {
    try {
      JsonNode node = objectMapper.readTree(jsonDefinition);
      if (node.has("nodes") && node.get("nodes").isArray()) {
        return node.get("nodes").size();
      }
    } catch (Exception e) {
      log.warn("Failed to extract node count from JSON", e);
    }
    return 0;
  }

  /**
   * Extracts edge count from a flow definition JSON.
   *
   * @param jsonDefinition the flow definition JSON
   * @return the number of edges
   */
  public int getEdgeCount(String jsonDefinition) {
    try {
      JsonNode node = objectMapper.readTree(jsonDefinition);
      if (node.has("edges") && node.get("edges").isArray()) {
        return node.get("edges").size();
      }
    } catch (Exception e) {
      log.warn("Failed to extract edge count from JSON", e);
    }
    return 0;
  }

  /**
   * Validates JSON schema of a flow definition.
   * Checks that required fields are present and types are correct.
   *
   * @param jsonDefinition the flow definition JSON
   * @return true if valid, false otherwise
   */
  public boolean validateJsonSchema(String jsonDefinition) {
    try {
      JsonNode root = parseFlowDefinition(jsonDefinition);

      // Validate nodes
      JsonNode nodesArray = root.get("nodes");
      for (JsonNode nodeJson : nodesArray) {
        if (!nodeJson.has("id") || !nodeJson.has("type")) {
          log.warn("Node missing required fields: id or type");
          return false;
        }
      }

      // Validate edges
      JsonNode edgesArray = root.get("edges");
      for (JsonNode edgeJson : edgesArray) {
        if (!edgeJson.has("id") || !edgeJson.has("source") || !edgeJson.has("target")) {
          log.warn("Edge missing required fields: id, source, or target");
          return false;
        }
      }

      return true;
    } catch (Exception e) {
      log.warn("JSON schema validation failed", e);
      return false;
    }
  }
}
