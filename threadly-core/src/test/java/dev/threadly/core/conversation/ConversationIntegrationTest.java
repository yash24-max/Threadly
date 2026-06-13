package dev.threadly.core.conversation;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.fixtures.BotFactory;
import dev.threadly.core.fixtures.ConversationFactory;
import dev.threadly.core.fixtures.UserFactory;
import dev.threadly.core.workspace.Bot;
import dev.threadly.core.workspace.BotController.BotResponse;
import dev.threadly.core.workspace.BotRepository;
import dev.threadly.core.workspace.Org;
import dev.threadly.core.workspace.OrgRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@code /v1/conversations} endpoints.
 *
 * <p>Conversations are created directly via the repository (not via the widget HTTP path)
 * so we can test the dashboard/inbox API independently.
 */
class ConversationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private OrgRepository orgRepository;

    private String orgToken;
    private UUID orgId;
    private Bot testBot;

    @BeforeEach
    void setUp() {
        String email = UserFactory.uniqueEmail();
        orgId = createTestOrg(email, UserFactory.DEFAULT_PASSWORD, "Conversation Test Org");
        orgToken = loginAndGetToken(email, UserFactory.DEFAULT_PASSWORD);

        // Create a bot directly via the API to get a persisted Bot with the correct org
        ResponseEntity<BotResponse> botResp =
                authPost("/v1/bots", orgToken, BotFactory.createRequest("Conv Bot"), BotResponse.class);
        UUID botUUID = UUID.fromString(botResp.getBody().getId());
        testBot = botRepository.findById(botUUID).orElseThrow();
    }

    @Test
    void list_conversations_returns_paginated_results() {
        // Seed 3 conversations
        for (int i = 0; i < 3; i++) {
            ConversationFactory.createConversation(conversationRepository, testBot, orgId);
        }

        ResponseEntity<JsonNode> resp = authGet("/v1/conversations?page=0&size=10", orgToken, JsonNode.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.has("content")).isTrue();
        assertThat(body.get("content").isArray()).isTrue();
        assertThat(body.get("content").size()).isGreaterThanOrEqualTo(3);
        assertThat(body.get("totalElements").asLong()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void get_conversation_with_messages_transcript() {
        Conversation conv = ConversationFactory.createConversation(conversationRepository, testBot, orgId);
        ConversationFactory.addMessages(messageRepository, conv, 4);

        ResponseEntity<JsonNode> resp =
                authGet("/v1/conversations/" + conv.getId(), orgToken, JsonNode.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = resp.getBody();
        assertThat(body.has("conversation")).isTrue();
        assertThat(body.has("messages")).isTrue();
        assertThat(body.get("messages").isArray()).isTrue();
        assertThat(body.get("messages").size()).isEqualTo(4);
        // First message should be from user
        assertThat(body.get("messages").get(0).get("role").asText()).isEqualTo("user");
    }

    @Test
    void close_conversation_changes_status() {
        Conversation conv = ConversationFactory.createConversation(conversationRepository, testBot, orgId);
        assertThat(conv.getStatus()).isEqualTo("open");

        ResponseEntity<JsonNode> closeResp =
                authPost("/v1/conversations/" + conv.getId() + "/close", orgToken, null, JsonNode.class);

        assertThat(closeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(closeResp.getBody().get("status").asText()).isEqualTo("CLOSED");
    }

    @Test
    void handoff_conversation_sets_handed_off_status() {
        Conversation conv = ConversationFactory.createConversation(conversationRepository, testBot, orgId);

        ResponseEntity<JsonNode> handoffResp =
                authPost("/v1/conversations/" + conv.getId() + "/handoff", orgToken, null, JsonNode.class);

        assertThat(handoffResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(handoffResp.getBody().get("status").asText()).isEqualTo("HANDED_OFF");
    }

    @Test
    void conversation_from_other_org_returns_404() {
        // Conversation owned by org A
        Conversation convA = ConversationFactory.createConversation(conversationRepository, testBot, orgId);

        // Log in as org B
        String orgBEmail = UserFactory.uniqueEmail();
        createTestOrg(orgBEmail, UserFactory.DEFAULT_PASSWORD, "Org B");
        String orgBToken = loginAndGetToken(orgBEmail, UserFactory.DEFAULT_PASSWORD);

        ResponseEntity<JsonNode> resp =
                authGet("/v1/conversations/" + convA.getId(), orgBToken, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_conversation_status_via_patch() {
        Conversation conv = ConversationFactory.createConversation(conversationRepository, testBot, orgId);

        java.util.Map<String, String> patchBody = java.util.Map.of("status", "CLOSED");
        ResponseEntity<JsonNode> patchResp =
                authPatch("/v1/conversations/" + conv.getId(), orgToken, patchBody, JsonNode.class);

        assertThat(patchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchResp.getBody().get("status").asText()).isEqualTo("CLOSED");
    }

    @Test
    void agent_can_send_a_message_to_a_conversation() {
        Conversation conv = ConversationFactory.createConversation(conversationRepository, testBot, orgId);

        java.util.Map<String, String> msgBody = java.util.Map.of("content", "Hi, I'm taking over!");
        ResponseEntity<Void> sendResp =
                authPost("/v1/conversations/" + conv.getId() + "/messages", orgToken, msgBody, Void.class);

        assertThat(sendResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify message persisted
        ResponseEntity<JsonNode> msgsResp =
                authGet("/v1/conversations/" + conv.getId() + "/messages", orgToken, JsonNode.class);
        assertThat(msgsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(msgsResp.getBody().isArray()).isTrue();
        assertThat(msgsResp.getBody().size()).isEqualTo(1);
        assertThat(msgsResp.getBody().get(0).get("role").asText()).isEqualTo("agent");
        assertThat(msgsResp.getBody().get(0).get("content").asText()).isEqualTo("Hi, I'm taking over!");
    }
}
