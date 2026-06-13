package dev.threadly.core.workspace;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.fixtures.BotFactory;
import dev.threadly.core.fixtures.UserFactory;
import dev.threadly.core.workspace.BotController.BotResponse;
import dev.threadly.core.workspace.BotController.CreateBotRequest;
import dev.threadly.core.workspace.BotController.UpdateBotRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@code /v1/bots}.
 *
 * <p>Each test case operates with its own org + user, created fresh in {@link #setUp()}.
 */
class BotIntegrationTest extends AbstractIntegrationTest {

    private String orgToken;
    private String orgEmail;

    @BeforeEach
    void setUp() {
        orgEmail = UserFactory.uniqueEmail();
        createTestOrg(orgEmail, UserFactory.DEFAULT_PASSWORD, "Bot Test Org " + orgEmail);
        orgToken = loginAndGetToken(orgEmail, UserFactory.DEFAULT_PASSWORD);
    }

    @Test
    void create_bot_returns_201_with_id() {
        CreateBotRequest req = BotFactory.createRequest("My First Bot");

        ResponseEntity<BotResponse> resp =
                authPost("/v1/bots", orgToken, req, BotResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BotResponse bot = resp.getBody();
        assertThat(bot).isNotNull();
        assertThat(bot.getId()).isNotBlank();
        assertThat(bot.getName()).isEqualTo("My First Bot");
        assertThat(bot.getOrgId()).isNotBlank();
        assertThat(bot.isActive()).isTrue();
    }

    @Test
    void list_bots_returns_only_org_bots() {
        // Create 2 bots for this org
        authPost("/v1/bots", orgToken, BotFactory.createRequest("Bot Alpha"), BotResponse.class);
        authPost("/v1/bots", orgToken, BotFactory.createRequest("Bot Beta"), BotResponse.class);

        // Create a bot in a completely separate org
        String otherEmail = UserFactory.uniqueEmail();
        createTestOrg(otherEmail, UserFactory.DEFAULT_PASSWORD, "Other Org");
        String otherToken = loginAndGetToken(otherEmail, UserFactory.DEFAULT_PASSWORD);
        authPost("/v1/bots", otherToken, BotFactory.createRequest("Bot Gamma"), BotResponse.class);

        // List bots for the original org — must not see Bot Gamma
        ResponseEntity<BotResponse[]> listResp =
                authGet("/v1/bots", orgToken, BotResponse[].class);

        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        BotResponse[] bots = listResp.getBody();
        assertThat(bots).isNotNull();
        assertThat(bots).hasSizeGreaterThanOrEqualTo(2);
        assertThat(bots).extracting(BotResponse::getName)
                .doesNotContain("Bot Gamma");
    }

    @Test
    void update_bot_changes_name_and_color() {
        ResponseEntity<BotResponse> created =
                authPost("/v1/bots", orgToken, BotFactory.createRequest("Original Name"), BotResponse.class);
        String botId = created.getBody().getId();

        UpdateBotRequest update = BotFactory.updateRequest("Renamed Bot");
        ResponseEntity<BotResponse> updated =
                authPatch("/v1/bots/" + botId, orgToken, update, BotResponse.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().getName()).isEqualTo("Renamed Bot");
        assertThat(updated.getBody().getTheme().toString()).contains("#10b981");
    }

    @Test
    void delete_bot_cascades_to_flows() {
        ResponseEntity<BotResponse> created =
                authPost("/v1/bots", orgToken, BotFactory.createRequest("Bot to Delete"), BotResponse.class);
        String botId = created.getBody().getId();

        // Create a draft flow for the bot (this persists a Flow row)
        authGet("/v1/bots/" + botId + "/flow", orgToken, JsonNode.class);

        // Delete the bot
        ResponseEntity<Void> del = authDelete("/v1/bots/" + botId, orgToken);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Bot should no longer be accessible
        ResponseEntity<JsonNode> getResp =
                authGet("/v1/bots/" + botId, orgToken, JsonNode.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void bot_from_other_org_returns_404() {
        // Create a bot in org A
        ResponseEntity<BotResponse> created =
                authPost("/v1/bots", orgToken, BotFactory.createRequest("Org A Bot"), BotResponse.class);
        String botId = created.getBody().getId();

        // Log in as org B
        String orgBEmail = UserFactory.uniqueEmail();
        createTestOrg(orgBEmail, UserFactory.DEFAULT_PASSWORD, "Org B");
        String orgBToken = loginAndGetToken(orgBEmail, UserFactory.DEFAULT_PASSWORD);

        // Org B cannot see org A's bot
        ResponseEntity<JsonNode> resp = authGet("/v1/bots/" + botId, orgBToken, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void get_embed_config_returns_snippet_with_bot_id() {
        ResponseEntity<BotResponse> created =
                authPost("/v1/bots", orgToken, BotFactory.createRequest("Embed Bot"), BotResponse.class);
        String botId = created.getBody().getId();

        ResponseEntity<BotController.EmbedResponse> embedResp =
                authGet("/v1/bots/" + botId + "/embed", orgToken, BotController.EmbedResponse.class);

        assertThat(embedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(embedResp.getBody().getSnippet()).contains(botId);
        assertThat(embedResp.getBody().getBotId()).isEqualTo(botId);
    }
}
