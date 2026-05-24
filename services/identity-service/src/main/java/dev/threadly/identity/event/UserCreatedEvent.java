package dev.threadly.identity.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when a user is created.
 * Published to Kafka topic: identity.user.created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent {

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
   * User ID (UUID).
   */
  @JsonProperty("user_id")
  private String userId;

  /**
   * Organization ID (UUID).
   */
  @JsonProperty("org_id")
  private String orgId;

  /**
   * User's email address.
   */
  @JsonProperty("email")
  private String email;

  /**
   * User's full name.
   */
  @JsonProperty("full_name")
  private String fullName;

  /**
   * Event source (identity-service).
   */
  @JsonProperty("source")
  private String source = "identity-service";
}
