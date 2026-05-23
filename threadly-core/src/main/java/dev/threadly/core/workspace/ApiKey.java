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

@Entity
@Table(name = "api_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.FilterDef(
    name = "orgFilterApiKey",
    parameters =
        @org.hibernate.annotations.ParamDef(name = "orgId", type = java.util.UUID.class))
@org.hibernate.annotations.Filter(name = "orgFilterApiKey", condition = "org_id = :orgId")
public class ApiKey {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "org_id", nullable = false)
  private UUID orgId;

  @Column(name = "bot_id", nullable = false)
  private UUID botId;

  @Column(nullable = false)
  private String name;

  /** BCrypt hash — used for verification only. */
  @Column(name = "key_hash", nullable = false)
  private String keyHash;

  /**
   * SHA-256 hex of the raw key — used for fast O(1) lookup by index. Never expose to clients.
   */
  @Column(name = "key_lookup_hash", nullable = false, unique = true)
  private String keyLookupHash;

  @Column(name = "key_prefix", nullable = false, length = 16)
  private String keyPrefix;

  @Column(name = "last_used_at")
  private Instant lastUsedAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}
