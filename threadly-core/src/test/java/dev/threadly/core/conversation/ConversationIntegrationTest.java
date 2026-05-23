package dev.threadly.core.conversation;

import static org.assertj.core.api.Assertions.assertThat;

import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.fixtures.TestBotFactory;
import dev.threadly.core.fixtures.TestUserFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

class ConversationIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private ConversationRepository conversationRepository;

  @Autowired
  private MessageRepository messageRepository;

  private String token;
  private String orgBToken;
  private String botId;

  @BeforeEach
  void setup() {
    token = signup_and_login(
        TestUserFactory.randomEmail(),
        TestUserFactory.defaultPassword(),
        "Conv Org A " + System.nanoTime());
    orgBToken = signup_and_login(
        TestUserFactory.randomEmail(),
        TestUserFactory.defaultPassword(),
        "Conv Org B " + System.nanoTime());

    // Create a bot owned by Org A
    var botResp = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Conv Bot"), authHeaders(token)),
        Map.class);
    botId = (String) botResp.getBody().get("id");
  }

  /**
   * Seeds a conversation directly via JPA to avoid dependency on the realtime layer.
   * Returns the conversation ID string.
   */
  private String seedConversation(java.util.UUID orgId, java.util.UUID botEntityId,
      String visitorId, String status) {
    // Use a raw JPQL insert via the repository to stay within the test layer
    dev.threadly.core.workspace.Org orgRef = new dev.threadly.core.workspace.Org();
    // We will use the ConversationRepository via Spring + JPA after fetching refs
    // For simplicity, create via proxy endpoint logic — use a direct entity build:
    dev.threadly.core.workspace.Bot botRef = new dev.threadly.core.workspace.Bot();
    botRef.setId(botEntityId);

    Conversation conv = Conversation.builder()
        .bot(botRef)
        .orgId(orgId)
        .visitorId(visitorId)
        .status(status)
        .channel("website")
        .metadata("{}")
        .build();
    return conversationRepository.save(conv).getId().toString();
  }

  @Test
  void list_conversations_returns_paginated_results() {
    // Seed 3 conversations for this org via repository
    @SuppressWarnings("unchecked")
    Map<String, Object> meBody = rest.exchange(
        baseUrl("/v1/auth/me"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class).getBody();
    java.util.UUID orgId = java.util.UUID.fromString((String) meBody.get("orgId"));
    java.util.UUID botUuid = java.util.UUID.fromString(botId);

    for (int i = 0; i < 3; i++) {
      seedConversation(orgId, botUuid, "visitor-" + i, "open");
    }

    var response = rest.exchange(
        baseUrl("/v1/conversations?page=0&size=10"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<?, ?> page = response.getBody();
    assertThat(page).containsKey("content");
    assertThat((List<?>) page.get("content")).hasSizeGreaterThanOrEqualTo(3);
    assertThat(page.get("totalElements")).isNotNull();
  }

  @Test
  void get_conversation_returns_transcript_with_messages() {
    @SuppressWarnings("unchecked")
    Map<String, Object> meBody = rest.exchange(
        baseUrl("/v1/auth/me"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class).getBody();
    java.util.UUID orgId = java.util.UUID.fromString((String) meBody.get("orgId"));
    java.util.UUID botUuid = java.util.UUID.fromString(botId);
    String convId = seedConversation(orgId, botUuid, "visitor-transcript", "open");

    var response = rest.exchange(
        baseUrl("/v1/conversations/" + convId),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<?, ?> body = response.getBody();
    assertThat(body).containsKey("conversation");
    assertThat(body).containsKey("messages");
    Map<?, ?> convSummary = (Map<?, ?>) body.get("conversation");
    assertThat(convSummary.get("id")).isEqualTo(convId);
  }

  @Test
  void close_conversation_changes_status_to_closed() {
    @SuppressWarnings("unchecked")
    Map<String, Object> meBody = rest.exchange(
        baseUrl("/v1/auth/me"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class).getBody();
    java.util.UUID orgId = java.util.UUID.fromString((String) meBody.get("orgId"));
    java.util.UUID botUuid = java.util.UUID.fromString(botId);
    String convId = seedConversation(orgId, botUuid, "visitor-close", "open");

    var response = rest.exchange(
        baseUrl("/v1/conversations/" + convId + "/close"),
        HttpMethod.POST,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().get("status")).isEqualTo("CLOSED");
  }

  @Test
  void conversation_from_other_org_returns_404() {
    // Org A seeds a conversation
    @SuppressWarnings("unchecked")
    Map<String, Object> meBodyA = rest.exchange(
        baseUrl("/v1/auth/me"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class).getBody();
    java.util.UUID orgAId = java.util.UUID.fromString((String) meBodyA.get("orgId"));
    java.util.UUID botUuid = java.util.UUID.fromString(botId);
    String orgAConvId = seedConversation(orgAId, botUuid, "visitor-isolation", "open");

    // Org B attempts to read Org A's conversation
    var response = rest.exchange(
        baseUrl("/v1/conversations/" + orgAConvId),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(orgBToken)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void bulk_close_conversations_updates_all_statuses() {
    @SuppressWarnings("unchecked")
    Map<String, Object> meBody = rest.exchange(
        baseUrl("/v1/auth/me"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class).getBody();
    java.util.UUID orgId = java.util.UUID.fromString((String) meBody.get("orgId"));
    java.util.UUID botUuid = java.util.UUID.fromString(botId);

    String convId1 = seedConversation(orgId, botUuid, "bulk-visitor-1", "open");
    String convId2 = seedConversation(orgId, botUuid, "bulk-visitor-2", "open");
    String convId3 = seedConversation(orgId, botUuid, "bulk-visitor-3", "open");

    var payload = Map.of("ids", List.of(convId1, convId2, convId3));
    var response = rest.exchange(
        baseUrl("/v1/conversations/bulk-close"),
        HttpMethod.POST,
        new HttpEntity<>(payload, authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().get("affected")).isEqualTo(3);

    // Verify each conversation now has status CLOSED
    for (String id : List.of(convId1, convId2, convId3)) {
      var convResp = rest.exchange(
          baseUrl("/v1/conversations/" + id),
          HttpMethod.GET,
          new HttpEntity<>(authHeaders(token)),
          Map.class);
      @SuppressWarnings("unchecked")
      Map<?, ?> conv = (Map<?, ?>) convResp.getBody().get("conversation");
      assertThat(conv.get("status")).isEqualTo("CLOSED");
    }
  }
}
