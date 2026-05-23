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
@Table(name = "bot_credentials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.FilterDef(
    name = "orgFilterCredential",
    parameters =
        @org.hibernate.annotations.ParamDef(name = "orgId", type = java.util.UUID.class))
@org.hibernate.annotations.Filter(name = "orgFilterCredential", condition = "org_id = :orgId")
public class BotCredential {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "org_id", nullable = false)
  private UUID orgId;

  @Column(name = "bot_id", nullable = false)
  private UUID botId;

  @Column(nullable = false)
  private String name;

  @Column(name = "encrypted_value", nullable = false, columnDefinition = "TEXT")
  private String encryptedValue;

  @Column(nullable = false)
  @Builder.Default
  private String type = "generic";

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}
