package dev.threadly.core.runtime;

import dev.threadly.core.workspace.Bot;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bot_id", nullable = false)
  private Bot bot;

  @Column(name = "visitor_id", nullable = false)
  private String visitorId;

  @Column(name = "org_id", nullable = false)
  private UUID orgId;

  @Column(name = "conversation_id")
  private UUID conversationId;

  @Column(name = "current_node_id")
  private String currentNodeId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Builder.Default
  private Map<String, Object> variables = new HashMap<>();

  @Column(nullable = false)
  @Builder.Default
  private String status = "active"; // active | waiting | completed | handed_off

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
