package dev.threadly.core.runtime;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NodeExecutionResult {

  /** Edge handle to follow (null = first edge, "true"/"false" for conditions). */
  private final String edgeHandle;

  /** Pause execution (waiting for user input). */
  private final boolean pause;

  /** End of flow reached. */
  private final boolean end;

  /** Handoff to human agent triggered. */
  private final boolean handoff;

  /**
   * Wait until this timestamp before resuming. Used by the delay node. Non-null signals a
   * wait-until state.
   */
  private final Instant waitUntil;

  /**
   * Jump directly to a specific node ID, bypassing edge traversal. Used by the switch node.
   */
  private final String jumpToNodeId;

  public static NodeExecutionResult next() {
    return NodeExecutionResult.builder().edgeHandle("default").build();
  }

  public static NodeExecutionResult next(String handle) {
    return NodeExecutionResult.builder().edgeHandle(handle).build();
  }

  public static NodeExecutionResult pause() {
    return NodeExecutionResult.builder().pause(true).build();
  }

  public static NodeExecutionResult end() {
    return NodeExecutionResult.builder().end(true).build();
  }

  public static NodeExecutionResult handoff() {
    return NodeExecutionResult.builder().handoff(true).build();
  }

  public static NodeExecutionResult waitUntil(Instant resumeAt) {
    return NodeExecutionResult.builder().waitUntil(resumeAt).pause(true).build();
  }

  public static NodeExecutionResult jumpTo(String nodeId) {
    return NodeExecutionResult.builder().jumpToNodeId(nodeId).build();
  }
}
