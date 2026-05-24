package dev.threadly.identity.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Component for publishing domain events to Kafka.
 * Handles event serialization and topic routing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  /**
   * Publishes a UserCreatedEvent to the identity.user.created topic.
   *
   * @param userId the user ID
   * @param orgId the organization ID
   * @param email the user's email
   * @param fullName the user's full name
   */
  public void publishUserCreated(String userId, String orgId, String email, String fullName) {
    UserCreatedEvent event = UserCreatedEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventTimestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .userId(userId)
        .orgId(orgId)
        .email(email)
        .fullName(fullName)
        .build();

    kafkaTemplate.send("identity.user.created", userId, event);
    log.debug("Published UserCreatedEvent for user: {}", userId);
  }

  /**
   * Publishes an OrganizationCreatedEvent to the identity.organization.created topic.
   *
   * @param orgId the organization ID
   * @param name the organization name
   * @param ownerId the owner user ID
   * @param plan the billing plan
   */
  public void publishOrganizationCreated(String orgId, String name, String ownerId, String plan) {
    OrganizationCreatedEvent event = OrganizationCreatedEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventTimestamp(LocalDateTime.now(ZoneId.of("UTC")))
        .orgId(orgId)
        .name(name)
        .ownerId(ownerId)
        .plan(plan)
        .build();

    kafkaTemplate.send("identity.organization.created", orgId, event);
    log.debug("Published OrganizationCreatedEvent for org: {}", orgId);
  }
}
