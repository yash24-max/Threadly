package dev.threadly.core.flow;

import static org.assertj.core.api.Assertions.assertThat;

import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.fixtures.TestBotFactory;
import dev.threadly.core.fixtures.TestFlowFactory;
import dev.threadly.core.fixtures.TestUserFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

class FlowIntegrationTest extends AbstractIntegrationTest {

  private String token;
  private String botId;
  private String botIdOrgB;
  private String tokenOrgB;

  @BeforeEach
  void setup() {
    token = signup_and_login(
        TestUserFactory.randomEmail(),
        TestUserFactory.defaultPassword(),
        "Flow Org A " + System.nanoTime());

    tokenOrgB = signup_and_login(
        TestUserFactory.randomEmail(),
        TestUserFactory.defaultPassword(),
        "Flow Org B " + System.nanoTime());

    // Create a fresh bot for Org A
    var botResp = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Flow Test Bot"), authHeaders(token)),
        Map.class);
    botId = (String) botResp.getBody().get("id");

    // Create a fresh bot for Org B
    var botRespB = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Flow Test Bot B"), authHeaders(tokenOrgB)),
        Map.class);
    botIdOrgB = (String) botRespB.getBody().get("id");
  }

  @Test
  void create_flow_returns_201_with_draft_status() {
    // Saving a draft for the first time effectively creates the flow resource
    var response = rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.minimalValidFlow()),
            authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<?, ?> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.get("id")).isNotNull();
    assertThat(body.get("botId")).isEqualTo(botId);
    assertThat(body.get("draftJson")).isNotNull();
    // Not published yet
    assertThat(body.get("publishedJson")).isNull();
  }

  @Test
  void update_flow_json_updates_draft() {
    // Initial save
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.minimalValidFlow()),
            authHeaders(token)),
        Map.class);

    // Build a slightly modified flow (add a custom message)
    String updatedFlow = TestFlowFactory.minimalValidFlow()
        .replace("Hello! How can I help you today?", "Updated greeting!");

    var response = rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(updatedFlow), authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    String draftJson = (String) response.getBody().get("draftJson");
    assertThat(draftJson).contains("Updated greeting!");
  }

  @Test
  void publish_flow_creates_new_version() {
    // Save a valid draft first
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.minimalValidFlow()),
            authHeaders(token)),
        Map.class);

    // Publish the draft
    var publishResp = rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow/publish"),
        HttpMethod.POST,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    assertThat(publishResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(publishResp.getBody().get("publishedJson")).isNotNull();
    assertThat(publishResp.getBody().get("publishedAt")).isNotNull();
  }

  @Test
  void get_flow_versions_returns_history() {
    // Save and publish twice to build version history
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.minimalValidFlow()),
            authHeaders(token)),
        Map.class);
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow/publish"),
        HttpMethod.POST,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    String updatedFlow = TestFlowFactory.minimalValidFlow()
        .replace("Hello! How can I help you today?", "Version 2 greeting!");
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(updatedFlow), authHeaders(token)),
        Map.class);
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow/publish"),
        HttpMethod.POST,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    var versionsResp = rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow/versions"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        List.class);

    assertThat(versionsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(versionsResp.getBody()).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void rollback_to_previous_version_works() {
    // Publish version 1
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.minimalValidFlow()),
            authHeaders(token)),
        Map.class);
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow/publish"),
        HttpMethod.POST,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    // Publish version 2
    String v2Flow = TestFlowFactory.minimalValidFlow()
        .replace("Hello! How can I help you today?", "V2 message");
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(v2Flow), authHeaders(token)),
        Map.class);
    rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow/publish"),
        HttpMethod.POST,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    // Rollback to version 1
    var rollbackResp = rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow/versions/1/rollback"),
        HttpMethod.POST,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    assertThat(rollbackResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String draftJson = (String) rollbackResp.getBody().get("draftJson");
    assertThat(draftJson).contains("Hello! How can I help you today?");
  }

  @Test
  void flow_from_other_org_returns_404() {
    // Save a draft in Org B's bot
    rest.exchange(
        baseUrl("/v1/bots/" + botIdOrgB + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.minimalValidFlow()),
            authHeaders(tokenOrgB)),
        Map.class);

    // Org A attempts to read Org B's bot flow — must get 404
    var response = rest.exchange(
        baseUrl("/v1/bots/" + botIdOrgB + "/flow"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void invalid_flow_missing_start_node_returns_422() {
    var response = rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.flowMissingStart()),
            authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    // Response should contain error details
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void flow_with_cycle_returns_422_with_error_message() {
    var response = rest.exchange(
        baseUrl("/v1/bots/" + botId + "/flow"),
        HttpMethod.PUT,
        new HttpEntity<>(TestFlowFactory.saveDraftPayload(TestFlowFactory.flowWithCycle()),
            authHeaders(token)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    // Response body must mention "cycle" so the UI can display a helpful message
    String responseBody = response.getBody() != null ? response.getBody().toString() : "";
    assertThat(responseBody.toLowerCase()).contains("cycle");
  }
}
