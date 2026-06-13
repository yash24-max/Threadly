package dev.threadly.core.flow;

import dev.threadly.core.workspace.Bot;
import dev.threadly.core.workspace.Org;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "flows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flow {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bot_id", nullable = false, unique = true)
  private Bot bot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "org_id", nullable = false)
  private Org org;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "draft_json", columnDefinition = "jsonb", nullable = false)
  @Builder.Default
  private String draftJson = "{\"version\":1,\"nodes\":[],\"edges\":[]}";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "published_json", columnDefinition = "jsonb")
  private String publishedJson;

  @Column(name = "published_at")
  private Instant publishedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
