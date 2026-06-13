package dev.threadly.core.security;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.conversation.ConversationRepository;
import dev.threadly.core.fixtures.BotFactory;
import dev.threadly.core.fixtures.ConversationFactory;
import dev.threadly.core.fixtures.FlowFactory;
import dev.threadly.core.fixtures.UserFactory;
import dev.threadly.core.workspace.Bot;
import dev.threadly.core.workspace.BotController.BotResponse;
import dev.threadly.core.workspace.BotRepository;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CRITICAL security tests verifying that no data belonging to Org A is visible or modifiable
 * by Org B.  All cross-tenant access attempts must return 404 (not 403 — revealing that the
 * resource exists would itself be a data leak).
 *
 * <p>Setup creates two orgs, each with a bot, a flow, and a conversation.
 * Every test authenticates as Org B and attempts to access Org A's resources.
 */
class TenantIsolationTest extends AbstractIntegrationTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private BotRepository botRepository;

    // Org A state
    private String tokenA;
    private UUID orgAId;
    private String botAId;
    private UUID conversationAId;

    // Org B state
    private String tokenB;

    @BeforeEach
    void setUp() {
        // ── Org A ───────────────────────────────────────────────────────────
        String emailA = UserFactory.uniqueEmail();
        orgAId = createTestOrg(emailA, UserFactory.DEFAULT_PASSWORD, "Org A " + emailA);
        tokenA = loginAndGetToken(emailA, UserFactory.DEFAULT_PASSWORD);

        ResponseEntity<BotResponse> botAResp =
                authPost("/v1/bots", tokenA, BotFactory.createRequest("Org A Bot"), BotResponse.class);
        botAId = botAResp.getBody().getId();

        // Seed a flow for org A's bot
        org.springframework.http.HttpEntity<dev.threadly.core.flow.FlowController.SaveFlowRequest> flowEntity =
                new org.springframework.http.HttpEntity<>(
                        FlowFactory.saveRequest(FlowFactory.minimalValidFlowJson()), authHeaders(tokenA));
        restTemplate.exchange("/v1/bots/" + botAId + "/flow",
                org.springframework.http.HttpMethod.PUT, flowEntity, JsonNode.class);

        // Seed a conversation for org A
        Bot botA = botRepository.findById(UUID.fromString(botAId)).orElseThrow();
        Conversation convA = ConversationFactory.createConversation(conversationRepository, botA, orgAId);
        conversationAId = convA.getId();

        // ── Org B ───────────────────────────────────────────────────────────
        String emailB = UserFactory.uniqueEmail();
        createTestOrg(emailB, UserFactory.DEFAULT_PASSWORD, "Org B " + emailB);
        tokenB = loginAndGetToken(emailB, UserFactory.DEFAULT_PASSWORD);
    }

    // ── Bot isolation ────────────────────────────────────────────────────────

    @Test
    void org_b_cannot_read_org_a_bot() {
        ResponseEntity<JsonNode> resp = authGet("/v1/bots/" + botAId, tokenB, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void org_b_cannot_modify_org_a_bot() {
        ResponseEntity<JsonNode> resp = authPatch(
                "/v1/bots/" + botAId,
                tokenB,
                java.util.Map.of("name", "Hijacked"),
                JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void org_b_cannot_delete_org_a_bot() {
        ResponseEntity<Void> resp = authDelete("/v1/bots/" + botAId, tokenB);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── Conversation isolation ───────────────────────────────────────────────

    @Test
    void org_b_cannot_read_org_a_conversations() {
        ResponseEntity<JsonNode> resp =
                authGet("/v1/conversations/" + conversationAId, tokenB, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void org_b_conversation_list_does_not_expose_org_a_data() {
        ResponseEntity<JsonNode> resp = authGet("/v1/conversations", tokenB, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode content = resp.getBody().get("content");
        // None of org B's conversations should have org A's conversation id
        for (JsonNode item : content) {
            assertThat(item.get("id").asText()).isNotEqualTo(conversationAId.toString());
        }
    }

    @Test
    void org_b_cannot_close_org_a_conversation() {
        ResponseEntity<JsonNode> resp =
                authPost("/v1/conversations/" + conversationAId + "/close", tokenB, null, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void org_b_cannot_handoff_org_a_conversation() {
        ResponseEntity<JsonNode> resp =
                authPost("/v1/conversations/" + conversationAId + "/handoff", tokenB, null, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── Flow isolation ───────────────────────────────────────────────────────

    @Test
    void org_b_cannot_read_org_a_flow() {
        ResponseEntity<JsonNode> resp =
                authGet("/v1/bots/" + botAId + "/flow", tokenB, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void org_b_cannot_publish_org_a_flow() {
        ResponseEntity<JsonNode> resp =
                authPost("/v1/bots/" + botAId + "/flow/publish", tokenB, null, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── Org B's own bot list must not include Org A's bots ──────────────────

    @Test
    void org_b_bot_list_excludes_org_a_bots() {
        ResponseEntity<BotResponse[]> resp = authGet("/v1/bots", tokenB, BotResponse[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        BotResponse[] bots = resp.getBody();
        assertThat(bots).isNotNull();
        for (BotResponse bot : bots) {
            assertThat(bot.getId()).isNotEqualTo(botAId);
        }
    }

    // ── Parameterized sweep of all tenant-scoped endpoints ──────────────────

    static Stream<Arguments> allTenantScopedEndpoints() {
        // These will be resolved with real IDs in the test body using placeholder {botId}
        return Stream.of(
                Arguments.of("GET", "/v1/bots/{botId}"),
                Arguments.of("GET", "/v1/bots/{botId}/embed"),
                Arguments.of("GET", "/v1/bots/{botId}/flow"),
                Arguments.of("GET", "/v1/bots/{botId}/flow/versions"),
                Arguments.of("GET", "/v1/conversations/{convId}"),
                Arguments.of("GET", "/v1/conversations/{convId}/messages")
        );
    }

    @ParameterizedTest(name = "{0} {1} from cross-tenant token → 404")
    @MethodSource("allTenantScopedEndpoints")
    void all_tenant_scoped_endpoints_reject_cross_tenant_access(String method, String pathTemplate) {
        String path = pathTemplate
                .replace("{botId}", botAId)
                .replace("{convId}", conversationAId.toString());

        ResponseEntity<JsonNode> resp = authGet(path, tokenB, JsonNode.class);
        assertThat(resp.getStatusCode())
                .as("Expected 404 for %s %s accessed by Org B", method, path)
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
