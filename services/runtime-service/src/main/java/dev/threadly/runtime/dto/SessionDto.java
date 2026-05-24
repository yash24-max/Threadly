package dev.threadly.runtime.dto;

import dev.threadly.runtime.model.Session;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Session DTO for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {

  private String id;
  private String botId;
  private String flowId;
  private String visitorId;
  private Session.SessionState state;
  private Map<String, Object> variables;
  private Integer tokenUsageCount;
  private LocalDateTime createdAt;
  private LocalDateTime lastMessageAt;
  private LocalDateTime endedAt;
}
