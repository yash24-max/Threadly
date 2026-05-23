package dev.threadly.core.fixtures;

/**
 * Factory helpers that produce flow JSON strings for use in integration tests.
 *
 * <p>All JSON is structured to exercise FlowSchemaValidator rules:
 * <ul>
 *   <li>{@link #minimalValidFlow()} — passes validation (Start → Message → End)</li>
 *   <li>{@link #flowWithCycle()} — fails validation (Start → A → B → A)</li>
 *   <li>{@link #flowMissingStart()} — fails validation (only Message → End)</li>
 * </ul>
 */
public final class TestFlowFactory {

  private TestFlowFactory() {}

  /**
   * Returns the smallest valid flow JSON that passes FlowSchemaValidator:
   * a start node, a message node, and an end node connected in sequence.
   */
  public static String minimalValidFlow() {
    return """
        {
          "version": 1,
          "nodes": [
            {
              "id": "start-1",
              "type": "start",
              "position": {"x": 100, "y": 100},
              "data": {}
            },
            {
              "id": "msg-1",
              "type": "message",
              "position": {"x": 300, "y": 100},
              "data": {"content": "Hello! How can I help you today?"}
            },
            {
              "id": "end-1",
              "type": "end",
              "position": {"x": 500, "y": 100},
              "data": {}
            }
          ],
          "edges": [
            {"id": "e1", "source": "start-1", "target": "msg-1", "sourceHandle": "default"},
            {"id": "e2", "source": "msg-1",   "target": "end-1",  "sourceHandle": "default"}
          ]
        }
        """;
  }

  /**
   * Returns a flow JSON that contains a cycle: Start → nodeA → nodeB → nodeA.
   * FlowSchemaValidator must reject this with a cycle error.
   */
  public static String flowWithCycle() {
    return """
        {
          "version": 1,
          "nodes": [
            {
              "id": "start-1",
              "type": "start",
              "position": {"x": 100, "y": 100},
              "data": {}
            },
            {
              "id": "node-a",
              "type": "message",
              "position": {"x": 300, "y": 100},
              "data": {"content": "Node A"}
            },
            {
              "id": "node-b",
              "type": "message",
              "position": {"x": 500, "y": 100},
              "data": {"content": "Node B"}
            }
          ],
          "edges": [
            {"id": "e1", "source": "start-1", "target": "node-a",  "sourceHandle": "default"},
            {"id": "e2", "source": "node-a",  "target": "node-b",  "sourceHandle": "default"},
            {"id": "e3", "source": "node-b",  "target": "node-a",  "sourceHandle": "default"}
          ]
        }
        """;
  }

  /**
   * Returns a flow JSON that is missing a start node.
   * FlowSchemaValidator must reject this with a "no start node" error.
   */
  public static String flowMissingStart() {
    return """
        {
          "version": 1,
          "nodes": [
            {
              "id": "msg-1",
              "type": "message",
              "position": {"x": 100, "y": 100},
              "data": {"content": "Orphaned message node"}
            },
            {
              "id": "end-1",
              "type": "end",
              "position": {"x": 300, "y": 100},
              "data": {}
            }
          ],
          "edges": [
            {"id": "e1", "source": "msg-1", "target": "end-1", "sourceHandle": "default"}
          ]
        }
        """;
  }

  /**
   * Returns a save-flow request body wrapping the given flow JSON string.
   * Suitable for PUT /v1/bots/{botId}/flow.
   */
  public static java.util.Map<String, Object> saveDraftPayload(String flowJson) {
    return java.util.Map.of("flowJson", flowJson);
  }
}
