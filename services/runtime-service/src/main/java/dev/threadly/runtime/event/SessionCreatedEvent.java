package dev.threadly.runtime.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * SessionCreatedEvent is published when a new session is created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionCreatedEvent {

  private String sessionId;
  private String botId;
  private String flowId;
  private String visitorId;
  private LocalDateTime createdAt;
  private String eventId;
  private LocalDateTime eventTimestamp;

  public SessionCreatedEvent(String sessionId, String botId, String flowId, String visitorId) {
    this.sessionId = sessionId;
    this.botId = botId;
    this.flowId = flowId;
    this.visitorId = visitorId;
    this.createdAt = LocalDateTime.now();
    this.eventId = java.util.UUID.randomUUID().toString();
    this.eventTimestamp = LocalDateTime.now();
  }
}
