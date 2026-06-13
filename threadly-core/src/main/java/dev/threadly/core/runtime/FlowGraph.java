package dev.threadly.core.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.Getter;

/** Parses and traverses the flow JSON graph. */
@Getter
public class FlowGraph {

  private final Map<String, Node> nodes;
  private final Map<String, List<Edge>> edgesFrom;

  private FlowGraph(Map<String, Node> nodes, Map<String, List<Edge>> edgesFrom) {
    this.nodes = nodes;
    this.edgesFrom = edgesFrom;
  }

  public static FlowGraph parse(String json, ObjectMapper mapper) throws Exception {
    RawFlow raw = mapper.readValue(json, RawFlow.class);
    Map<String, Node> nodeMap = raw.getNodes().stream()
        .collect(Collectors.toMap(Node::getId, n -> n));
    Map<String, List<Edge>> edgeMap = new HashMap<>();
    for (Edge e : raw.getEdges()) {
      edgeMap.computeIfAbsent(e.getSource(), k -> new ArrayList<>()).add(e);
    }
    return new FlowGraph(nodeMap, edgeMap);
  }

  public Node getNode(String id) {
    return nodes.get(id);
  }

  /** Return next node ID following the edge from nodeId with the given handle. */
  public String nextNodeId(String nodeId, String handle) {
    List<Edge> edges = edgesFrom.getOrDefault(nodeId, Collections.emptyList());
    if (edges.isEmpty()) return null;
    if (handle == null || "default".equals(handle)) {
      return edges.get(0).getTarget();
    }
    return edges.stream()
        .filter(e -> handle.equals(e.getSourceHandle()))
        .findFirst()
        .map(Edge::getTarget)
        .orElse(edges.get(0).getTarget());
  }

  @Data
  public static class RawFlow {
    private int version;
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
  }

  @Data
  public static class Node {
    private String id;
    private String type;
    private Map<String, Object> data = new HashMap<>();
    private Map<String, Double> position;
  }

  @Data
  public static class Edge {
    private String id;
    private String source;
    private String sourceHandle;
    private String target;
  }
}
