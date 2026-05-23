package dev.threadly.core.runtime;

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
}
