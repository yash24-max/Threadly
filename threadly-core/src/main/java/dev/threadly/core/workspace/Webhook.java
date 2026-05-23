package dev.threadly.core.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "webhooks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.FilterDef(
    name = "orgFilterWebhook",
    parameters =
        @org.hibernate.annotations.ParamDef(name = "orgId", type = java.util.UUID.class))
@org.hibernate.annotations.Filter(name = "orgFilterWebhook", condition = "org_id = :orgId")
public class Webhook {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "org_id", nullable = false)
  private UUID orgId;

  @Column(name = "bot_id", nullable = false)
  private UUID botId;

  @Column(nullable = false)
  private String url;

  /**
   * JSON array of event names e.g. ["conversation.ended","handoff.created"]
   */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Builder.Default
  private String events = "[]";

  /** HMAC secret for X-Threadly-Signature header. */
  @Column(name = "secret", nullable = false)
  private String secret;

  @Column(name = "active", nullable = false)
  @Builder.Default
  private boolean active = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}
