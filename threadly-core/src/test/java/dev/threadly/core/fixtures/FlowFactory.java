package dev.threadly.core.fixtures;

import dev.threadly.core.flow.FlowController.SaveFlowRequest;

/**
 * Builds minimal valid flow JSON payloads for tests.
 */
public final class FlowFactory {

    private FlowFactory() {}

    /**
     * Returns a minimal valid flow JSON string containing a single {@code start} node.
     * This satisfies the server-side validation in {@code FlowService.validateFlowJson}.
     */
    public static String minimalValidFlowJson() {
        return """
                {
                  "version": 1,
                  "nodes": [
                    {
                      "id": "start-1",
                      "type": "start",
                      "position": { "x": 100, "y": 100 },
                      "data": {}
                    }
                  ],
                  "edges": []
                }
                """;
    }

    /**
     * Returns a two-node flow JSON: start → message.
     */
    public static String twoNodeFlowJson() {
        return """
                {
                  "version": 1,
                  "nodes": [
                    {
                      "id": "start-1",
                      "type": "start",
                      "position": { "x": 100, "y": 100 },
                      "data": {}
                    },
                    {
                      "id": "msg-1",
                      "type": "message",
                      "position": { "x": 300, "y": 100 },
                      "data": { "content": "Hello! How can I help you?" }
                    }
                  ],
                  "edges": [
                    { "id": "e1", "source": "start-1", "target": "msg-1" }
                  ]
                }
                """;
    }

    /**
     * Returns intentionally invalid JSON (malformed) to test validation rejection.
     */
    public static String malformedJson() {
        return "{ this is not valid JSON !! }";
    }

    /**
     * Returns well-formed JSON that is missing the required start node.
     */
    public static String noStartNodeFlowJson() {
        return """
                {
                  "version": 1,
                  "nodes": [
                    {
                      "id": "msg-1",
                      "type": "message",
                      "position": { "x": 100, "y": 100 },
                      "data": { "content": "Hello" }
                    }
                  ],
                  "edges": []
                }
                """;
    }

    /** Wraps a flow JSON string inside the {@link SaveFlowRequest} DTO. */
    public static SaveFlowRequest saveRequest(String flowJson) {
        SaveFlowRequest req = new SaveFlowRequest();
        req.setFlowJson(flowJson);
        return req;
    }
}
