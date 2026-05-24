package dev.threadly.runtime.executor;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.*;

/**
 * ExecutionResult encapsulates the output of a node execution.
 * Contains next node reference, updated variables, messages, and status information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionResult {

  // Navigation
  private String nextNodeId;
  private List<String> branchNodeIds;  // For multi-branch scenarios

  // State changes
  private Map<String, Object> variableUpdates;

  // Output messages
  private List<MessageOutput> messages;

  // Metadata
  private ExecutionStatus status;
  private String statusMessage;
  private Long executionTimeMs;

  // Error information
  private String errorMessage;
  private Exception exception;

  // For paused execution (waiting for user input)
  private Boolean shouldPause;
  private String pauseReason;

  /**
   * Message output for user-facing content
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class MessageOutput {
    private String type; // TEXT, QUESTION, IMAGE, RICH_TEXT, etc.
    private String content;
    private JsonNode metadata; // For rich content, options, etc.
  }

  /**
   * Execution status enumeration
   */
  public enum ExecutionStatus {
    SUCCESS,
    FAILURE,
    PAUSED,
    TIMEOUT,
    SKIPPED
  }

  /**
   * Builder helper method to add a variable update
   */
  public ExecutionResult addVariable(String name, Object value) {
    if (this.variableUpdates == null) {
      this.variableUpdates = new HashMap<>();
    }
    this.variableUpdates.put(name, value);
    return this;
  }

  /**
   * Builder helper method to add a message
   */
  public ExecutionResult addMessage(String type, String content) {
    if (this.messages == null) {
      this.messages = new ArrayList<>();
    }
    this.messages.add(MessageOutput.builder()
        .type(type)
        .content(content)
        .build());
    return this;
  }

  /**
   * Check if execution was successful
   */
  public boolean isSuccess() {
    return status == ExecutionStatus.SUCCESS;
  }

  /**
   * Check if execution was paused (waiting for user)
   */
  public boolean isPaused() {
    return status == ExecutionStatus.PAUSED;
  }
}
