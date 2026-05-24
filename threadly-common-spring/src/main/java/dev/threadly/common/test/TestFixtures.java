package dev.threadly.common.test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Factory methods for creating test fixtures (mock entities).
 *
 * Usage:
 * @Test
 * void testCreateBot() {
 *   User user = TestFixtures.createUser();
 *   Organization org = TestFixtures.createOrganization(user.getId());
 *   Bot bot = TestFixtures.createBot(org.getId());
 *
 *   // Test code...
 * }
 */
public class TestFixtures {

  /** Create a test user. */
  public static UserFixture createUser() {
    return new UserFixture(
        UUID.randomUUID(),
        "test-user-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com",
        "password-hash",
        Instant.now(),
        Instant.now()
    );
  }

  /** Create a test organization. */
  public static OrganizationFixture createOrganization(UUID ownerId) {
    return new OrganizationFixture(
        UUID.randomUUID(),
        "Test Org " + UUID.randomUUID().toString().substring(0, 8),
        "test-org-" + UUID.randomUUID().toString().substring(0, 8),
        ownerId,
        "free",
        Instant.now(),
        Instant.now()
    );
  }

  /** Create a test bot. */
  public static BotFixture createBot(UUID orgId) {
    return new BotFixture(
        UUID.randomUUID(),
        orgId,
        "Test Bot",
        "A test bot for integration testing",
        "en",
        "#4F46E5",
        "active",
        Instant.now(),
        Instant.now()
    );
  }

  /** Create a test conversation. */
  public static ConversationFixture createConversation(UUID botId, UUID orgId) {
    return new ConversationFixture(
        UUID.randomUUID(),
        botId,
        orgId,
        "visitor-" + UUID.randomUUID().toString().substring(0, 8),
        "open",
        Instant.now(),
        null,
        0
    );
  }

  /** Create a test message. */
  public static MessageFixture createMessage(UUID conversationId, String senderType) {
    return new MessageFixture(
        UUID.randomUUID(),
        conversationId,
        senderType,
        UUID.randomUUID().toString(),
        "Test " + senderType,
        "This is a test message.",
        "text",
        Instant.now()
    );
  }

  /** Create a test lead. */
  public static LeadFixture createLead(UUID botId, UUID orgId) {
    return new LeadFixture(
        UUID.randomUUID(),
        botId,
        orgId,
        "test-lead-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com",
        "+1234567890",
        "Test Lead",
        "new",
        Instant.now(),
        Instant.now()
    );
  }

  /** Create a test flow. */
  public static FlowFixture createFlow(UUID botId, UUID orgId) {
    return new FlowFixture(
        UUID.randomUUID(),
        botId,
        orgId,
        "Test Flow",
        "A test flow",
        new HashMap<>(),
        1,
        false,
        Instant.now(),
        Instant.now()
    );
  }

  /** Create a test session. */
  public static SessionFixture createSession(UUID botId, UUID orgId, UUID flowId) {
    return new SessionFixture(
        UUID.randomUUID(),
        botId,
        orgId,
        "visitor-" + UUID.randomUUID().toString().substring(0, 8),
        flowId,
        null,
        new HashMap<>(),
        "active",
        Instant.now(),
        null
    );
  }

  // Fixture classes

  public record UserFixture(
      UUID userId,
      String email,
      String passwordHash,
      Instant createdAt,
      Instant updatedAt
  ) {}

  public record OrganizationFixture(
      UUID orgId,
      String name,
      String slug,
      UUID ownerId,
      String subscriptionPlan,
      Instant createdAt,
      Instant updatedAt
  ) {}

  public record BotFixture(
      UUID botId,
      UUID orgId,
      String name,
      String description,
      String language,
      String accentColor,
      String status,
      Instant createdAt,
      Instant updatedAt
  ) {}

  public record ConversationFixture(
      UUID conversationId,
      UUID botId,
      UUID orgId,
      String visitorId,
      String status,
      Instant startedAt,
      Instant endedAt,
      int messageCount
  ) {}

  public record MessageFixture(
      UUID messageId,
      UUID conversationId,
      String senderType,
      String senderId,
      String senderName,
      String content,
      String messageType,
      Instant createdAt
  ) {}

  public record LeadFixture(
      UUID leadId,
      UUID botId,
      UUID orgId,
      String email,
      String phone,
      String name,
      String status,
      Instant createdAt,
      Instant updatedAt
  ) {}

  public record FlowFixture(
      UUID flowId,
      UUID botId,
      UUID orgId,
      String name,
      String description,
      Map<String, Object> flowJson,
      int currentVersion,
      boolean isPublished,
      Instant createdAt,
      Instant updatedAt
  ) {}

  public record SessionFixture(
      UUID sessionId,
      UUID botId,
      UUID orgId,
      String visitorId,
      UUID flowId,
      String currentNodeId,
      Map<String, Object> variables,
      String status,
      Instant startedAt,
      Instant endedAt
  ) {}
}
