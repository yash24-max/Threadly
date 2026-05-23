package dev.threadly.core.common;

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

@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "org_id", nullable = false)
  private UUID orgId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(nullable = false, length = 100)
  private String action;

  @Column(name = "resource_type", nullable = false, length = 50)
  private String resourceType;

  @Column(name = "resource_id")
  private UUID resourceId;

  /** Old state as JSON text (JSONB in Postgres). */
  @Column(name = "old_value", columnDefinition = "TEXT")
  private String oldValue;

  /** New state as JSON text (JSONB in Postgres). */
  @Column(name = "new_value", columnDefinition = "TEXT")
  private String newValue;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
