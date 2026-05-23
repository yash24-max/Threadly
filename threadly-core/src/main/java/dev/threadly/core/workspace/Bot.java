package dev.threadly.core.workspace;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.FilterDef(name = "orgFilterBot",
    parameters = @org.hibernate.annotations.ParamDef(name = "orgId", type = java.util.UUID.class))
@org.hibernate.annotations.Filter(name = "orgFilterBot", condition = "org_id = :orgId")
public class Bot {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "org_id", nullable = false)
  private Org org;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false)
  @Builder.Default
  private String language = "en";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Builder.Default
  private String theme = "{\"color\":\"#4F46E5\",\"position\":\"bottom-right\"}";

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
