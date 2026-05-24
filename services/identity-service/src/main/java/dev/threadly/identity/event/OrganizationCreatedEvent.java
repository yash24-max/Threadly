package dev.threadly.identity.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when an organization is created.
 * Published to Kafka topic: identity.organization.created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationCreatedEvent {

  /**
   * Event ID (UUID) for idempotency.
   */
  @JsonProperty("event_id")
  private String eventId;

  /**
   * Event timestamp in ISO 8601 format.
   */
  @JsonProperty("event_timestamp")
  private LocalDateTime eventTimestamp;

  /**
   * Organization ID (UUID).
   */
  @JsonProperty("org_id")
  private String orgId;

  /**
   * Organization name.
   */
  @JsonProperty("name")
  private String name;

  /**
   * Owner user ID (UUID).
   */
  @JsonProperty("owner_id")
  private String ownerId;

  /**
   * Billing plan type.
   */
  @JsonProperty("plan")
  private String plan;

  /**
   * Event source (identity-service).
   */
  @JsonProperty("source")
  private String source = "identity-service";
}
