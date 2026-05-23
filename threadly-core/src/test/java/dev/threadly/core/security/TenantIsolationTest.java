package dev.threadly.core.security;

import static org.assertj.core.api.Assertions.assertThat;

import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.conversation.ConversationRepository;
import dev.threadly.core.fixtures.TestBotFactory;
import dev.threadly.core.fixtures.TestFlowFactory;
import dev.threadly.core.fixtures.TestUserFactory;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Tenant-isolation security tests.
 *
 * <p>Two independent orgs (A and B) are provisioned. Each owns bots, flows,
 * and conversations. Every attempt by Org B to access Org A's resources must
 * return 404 — never 200, never 403, and never expose the resource ID in an
 * error message that leaks existence.
 */
class TenantIsolationTest extends AbstractIntegrationTest {

  @Autowired
  private ConversationRepository conversationRepository;

  private String tokenA;
  private String tokenB;
  private String orgAId;

  // Resources owned by Org A
  private String orgABotId;
  private String orgAConvId;

  @BeforeEach
  void setupOrgsAndResources() {
    // Create Org A
    String emailA = TestUserFactory.randomEmail();
    tokenA = signup_and_login(emailA, TestUserFactory.defaultPassword(),
        "Isolation Org A " + System.nanoTime());
    orgAId = fetchOrgId(tokenA);

    // Create Org B
    tokenB = signup_and_login(
        TestUserFactory.randomEmail(),
        TestUserFactory.defaultPassword(),
        "Isolation Org B " + System.nanoTime());

    // Org A creates a bot
    var botResp = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Org A Bot"), authHeaders(tokenA)),
        Map.class);
    orgABotId = (String) botResp.getBody().get("id");

    // Org A saves a flow for the bot
    rest.exchange(
        baseUrl("/v1/bots/" + orgABotId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.minimalValidFlow()),
            authHeaders(tokenA)),
        Map.class);

    // Org A seeds a conversation directly via the repository
    dev.threadly.core.workspace.Bot botRef = new dev.threadly.core.workspace.Bot();
    botRef.setId(UUID.fromString(orgABotId));
    Conversation conv = Conversation.builder()
        .bot(botRef)
        .orgId(UUID.fromString(orgAId))
        .visitorId("isolation-visitor")
        .status("open")
        .channel("website")
        .metadata("{}")
        .build();
    orgAConvId = conversationRepository.save(conv).getId().toString();
  }

  // ── Bot isolation ─────────────────────────────────────────────────────────

  @Test
  void orgB_cannot_read_orgA_bot() {
    var response = rest.exchange(
        baseUrl("/v1/bots/" + orgABotId),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenB)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void orgB_cannot_update_orgA_bot() {
    var response = rest.exchange(
        baseUrl("/v1/bots/" + orgABotId),
        HttpMethod.PATCH,
        new HttpEntity<>(TestBotFactory.updateBotPayload("Hijacked Name"), authHeaders(tokenB)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void orgB_cannot_delete_orgA_bot() {
    var response = rest.exchange(
        baseUrl("/v1/bots/" + orgABotId),
        HttpMethod.DELETE,
        new HttpEntity<>(authHeaders(tokenB)),
        Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    // The bot must still exist from Org A's perspective
    var getResp = rest.exchange(
        baseUrl("/v1/bots/" + orgABotId),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenA)),
        Map.class);
    assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  // ── Flow isolation ────────────────────────────────────────────────────────

  @Test
  void orgB_cannot_read_orgA_flow() {
    var response = rest.exchange(
        baseUrl("/v1/bots/" + orgABotId + "/flow"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenB)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void orgB_cannot_publish_orgA_flow() {
    var response = rest.exchange(
        baseUrl("/v1/bots/" + orgABotId + "/flow/publish"),
        HttpMethod.POST,
        new HttpEntity<>(authHeaders(tokenB)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  // ── Conversation isolation ────────────────────────────────────────────────

  @Test
  void orgB_cannot_read_orgA_conversations() {
    // List endpoint — Org B's list must be empty (not show Org A's data)
    var listResp = rest.exchange(
        baseUrl("/v1/conversations"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenB)),
        Map.class);
    assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<?> content = (List<?>) listResp.getBody().get("content");
    assertThat(content).isEmpty();

    // Direct fetch must return 404
    var getResp = rest.exchange(
        baseUrl("/v1/conversations/" + orgAConvId),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenB)),
        Map.class);
    assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void orgB_cannot_read_orgA_kb_documents() {
    // GET /v1/bots/{botId}/kb — must return 404 for cross-org bot ID
    var response = rest.exchange(
        baseUrl("/v1/bots/" + orgABotId + "/kb"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenB)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  // ── Parameterized cross-tenant GET sweep ─────────────────────────────────

  @ParameterizedTest(name = "cross-tenant GET {0} returns 404")
  @ValueSource(strings = {
      "/v1/bots/{botId}",
      "/v1/bots/{botId}/flow",
      "/v1/bots/{botId}/kb",
      "/v1/bots/{botId}/api-keys",
      "/v1/bots/{botId}/analytics/summary",
  })
  void cross_tenant_get_returns_404(String pathTemplate) {
    String path = pathTemplate.replace("{botId}", orgABotId);

    var response = rest.exchange(
        baseUrl(path),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenB)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  // ── Helper ────────────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private String fetchOrgId(String token) {
    Map<String, Object> me = rest.exchange(
        baseUrl("/v1/auth/me"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class).getBody();
    assert me != null;
    return (String) me.get("orgId");
  }
}
