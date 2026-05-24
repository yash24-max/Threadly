package dev.threadly.runtime.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * SessionEndedEvent is published when a session ends
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEndedEvent {

  private String sessionId;
  private String botId;
  private String visitorId;
  private Integer totalTokensUsed;
  private Long sessionDurationMs;
  private LocalDateTime endedAt;
  private String eventId;
  private LocalDateTime eventTimestamp;

  public SessionEndedEvent(String sessionId, String botId, String visitorId,
                          Integer totalTokensUsed, Long sessionDurationMs) {
    this.sessionId = sessionId;
    this.botId = botId;
    this.visitorId = visitorId;
    this.totalTokensUsed = totalTokensUsed;
    this.sessionDurationMs = sessionDurationMs;
    this.endedAt = LocalDateTime.now();
    this.eventId = java.util.UUID.randomUUID().toString();
    this.eventTimestamp = LocalDateTime.now();
  }
}
