package dev.threadly.runtime.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Session entity represents a conversational session between a visitor and a bot.
 * Maintains session state, variables, and lifecycle information.
 */
@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_session_bot_id", columnList = "bot_id"),
    @Index(name = "idx_session_flow_id", columnList = "flow_id"),
    @Index(name = "idx_session_visitor_id", columnList = "visitor_id"),
    @Index(name = "idx_session_state", columnList = "state")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 36)
  private String botId;

  @Column(nullable = false, length = 36)
  private String flowId;

  @Column(nullable = false, length = 36)
  private String visitorId;

  @Column(columnDefinition = "TEXT")
  private String sessionVariablesJson;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SessionState state;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime lastMessageAt;

  private LocalDateTime endedAt;

  @Column(nullable = false)
  private Integer tokenUsageCount;

  @Version
  private Long version;

  /**
   * Session state enumeration for session lifecycle management
   */
  public enum SessionState {
    ACTIVE,      // Session is running
    PAUSED,      // Waiting for user input
    ENDED,       // Session terminated
    ERROR        // Error state
  }
}
