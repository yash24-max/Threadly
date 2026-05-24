package dev.threadly.core.integration;

import dev.threadly.core.common.entity.Auditable;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

/**
 * JPA entity representing a configured third-party integration.
 *
 * <p>
 * Each integration binds a connector type (e.g., Slack) to an organization and
 * stores non-sensitive configuration (e.g., workspace ID). Sensitive data
 * (tokens, API keys) are stored separately in the credential store.
 * </p>
 *
 * <p>
 * Status tracks whether the integration is active, disconnected, or failed.
 * Timestamps are managed by the {@link Auditable} superclass.
 * </p>
 */
@Entity
@Table(
    name = "integrations",
    indexes = {
      @Index(name = "idx_integration_org_id", columnList = "org_id"),
      @Index(name = "idx_integration_type", columnList = "type"),
      @Index(name = "idx_integration_org_type", columnList = "org_id,type"),
      @Index(name = "idx_integration_status", columnList = "status"),
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"organization", "config"})
public class Integration extends Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @NotBlank(message = "Organization ID is required")
  @Column(name = "org_id", nullable = false, length = 36)
  private String orgId;

  @ManyToOne(optional = true)
  @JoinColumn(
      name = "org_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_integration_organization"))
  private Object organization; // TODO: replace with actual Organization entity when available

  @NotBlank(message = "Integration name is required")
  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @NotBlank(message = "Integration type is required")
  @Column(name = "type", nullable = false, length = 50)
  private String type; // e.g., "SLACK", "GMAIL", "STRIPE"

  @NotNull(message = "Configuration is required")
  @Type(JsonBinaryType.class)
  @Column(name = "config", nullable = false, columnDefinition = "jsonb")
  @Builder.Default
  private Map<String, Object> config = new HashMap<>();

  @Column(name = "credentials_id", length = 36)
  private String credentialsId; // UUID reference to vault/secret manager

  @NotNull(message = "Integration status is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private Status status = Status.PENDING;

  /**
   * Optional: timestamp when the integration was last tested.
   *
   * <p>
   * Can be used to track stale connections.
   * </p>
   */
  @Column(name = "last_tested_at")
  private java.time.Instant lastTestedAt;

  /**
   * Status enum for integrations.
   *
   * <p>
   * Transitions:
   * </p>
   *
   * <ul>
   *   <li>PENDING → CONNECTED (after successful auth)
   *   <li>CONNECTED → DISCONNECTED (user revokes)
   *   <li>CONNECTED → FAILED (health check fails)
   *   <li>FAILED → CONNECTED (reconnect succeeds)
   * </ul>
   */
  public enum Status {
    /** Integration is configured but not yet authenticated. */
    PENDING,

    /** Integration is authenticated and working. */
    CONNECTED,

    /** Integration was connected but user revoked access or manually disabled. */
    DISCONNECTED,

    /** Integration connection failed health checks (e.g., token expired). */
    FAILED,
  }

  /**
   * Checks if this integration is in an active, working state.
   *
   * @return true if status is CONNECTED
   */
  public boolean isActive() {
    return status == Status.CONNECTED;
  }

  /**
   * Checks if this integration can be executed.
   *
   * @return true if status is CONNECTED and has valid credentials
   */
  public boolean isExecutable() {
    return isActive() && credentialsId != null && !credentialsId.isBlank();
  }

  /**
   * Sets a configuration value.
   *
   * @param key the key
   * @param value the value
   */
  public void setConfigValue(String key, Object value) {
    if (config == null) {
      config = new HashMap<>();
    }
    config.put(key, value);
  }

  /**
   * Gets a configuration value.
   *
   * @param key the key
   * @return the value, or null if not found
   */
  public Object getConfigValue(String key) {
    return config != null ? config.get(key) : null;
  }

  /**
   * Gets a configuration value as a String.
   *
   * @param key the key
   * @return the value as string, or null if not found
   */
  public String getConfigValueAsString(String key) {
    Object val = getConfigValue(key);
    return val != null ? val.toString() : null;
  }

  /**
   * Gets a configuration value as a Map (for nested objects).
   *
   * @param key the key
   * @return the value as map, or null if not found or not a map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getConfigValueAsMap(String key) {
    Object val = getConfigValue(key);
    return val instanceof Map ? (Map<String, Object>) val : null;
  }

  /**
   * Marks this integration as successfully connected.
   *
   * @param credentialsId the credential store ID
   */
  public void markConnected(String credentialsId) {
    this.status = Status.CONNECTED;
    this.credentialsId = credentialsId;
    this.lastTestedAt = java.time.Instant.now();
  }

  /**
   * Marks this integration as failed.
   */
  public void markFailed() {
    this.status = Status.FAILED;
  }

  /**
   * Marks this integration as disconnected.
   */
  public void markDisconnected() {
    this.status = Status.DISCONNECTED;
  }

  @Override
  public String toString() {
    return String.format(
        "Integration{id=%s, name='%s', type='%s', status=%s, org=%s}",
        id, name, type, status, orgId);
  }
}
