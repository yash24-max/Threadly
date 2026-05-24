package dev.threadly.runtime.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * NodeExecutedEvent is published when a node is executed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeExecutedEvent {

  private String sessionId;
  private String nodeId;
  private String nodeType;
  private String status; // SUCCESS, FAILURE, PAUSED
  private Long executionTimeMs;
  private String errorMessage;
  private LocalDateTime executedAt;
  private String eventId;
  private LocalDateTime eventTimestamp;

  public NodeExecutedEvent(String sessionId, String nodeId, String nodeType,
                          String status, Long executionTimeMs) {
    this.sessionId = sessionId;
    this.nodeId = nodeId;
    this.nodeType = nodeType;
    this.status = status;
    this.executionTimeMs = executionTimeMs;
    this.executedAt = LocalDateTime.now();
    this.eventId = java.util.UUID.randomUUID().toString();
    this.eventTimestamp = LocalDateTime.now();
  }
}
