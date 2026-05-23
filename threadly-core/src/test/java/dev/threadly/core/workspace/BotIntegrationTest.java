package dev.threadly.core.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.fixtures.TestBotFactory;
import dev.threadly.core.fixtures.TestUserFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

class BotIntegrationTest extends AbstractIntegrationTest {

  private String tokenOrgA;
  private String tokenOrgB;

  @BeforeEach
  void setup() {
    tokenOrgA = signup_and_login(
        TestUserFactory.randomEmail(),
        TestUserFactory.defaultPassword(),
        "Bot Test Org A " + System.nanoTime());
    tokenOrgB = signup_and_login(
        TestUserFactory.randomEmail(),
        TestUserFactory.defaultPassword(),
        "Bot Test Org B " + System.nanoTime());
  }

  @Test
  void create_bot_returns_201_with_id() {
    var payload = TestBotFactory.createBotPayload("Customer Support Bot");

    var response = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(payload, authHeaders(tokenOrgA)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Map<?, ?> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.get("id")).isNotNull();
    assertThat(body.get("name")).isEqualTo("Customer Support Bot");
    assertThat(body.get("active")).isEqualTo(true);
  }

  @Test
  void list_bots_returns_only_current_org_bots() {
    // Org A creates 2 bots
    rest.postForEntity(baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Org A Bot 1"), authHeaders(tokenOrgA)),
        Map.class);
    rest.postForEntity(baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Org A Bot 2"), authHeaders(tokenOrgA)),
        Map.class);

    // Org B creates 1 bot
    rest.postForEntity(baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Org B Bot 1"), authHeaders(tokenOrgB)),
        Map.class);

    // Org A list should contain exactly its 2 bots
    var responseA = rest.exchange(
        baseUrl("/v1/bots"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenOrgA)),
        List.class);
    assertThat(responseA.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<?> botsA = responseA.getBody();
    assertThat(botsA).isNotNull().hasSize(2);

    // Org B list should contain exactly its 1 bot
    var responseB = rest.exchange(
        baseUrl("/v1/bots"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenOrgB)),
        List.class);
    assertThat(responseB.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<?> botsB = responseB.getBody();
    assertThat(botsB).isNotNull().hasSize(1);
  }

  @Test
  void get_bot_by_id_returns_bot() {
    var createResp = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Get Bot Test"), authHeaders(tokenOrgA)),
        Map.class);
    String botId = (String) createResp.getBody().get("id");

    var response = rest.exchange(
        baseUrl("/v1/bots/" + botId),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenOrgA)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().get("id")).isEqualTo(botId);
    assertThat(response.getBody().get("name")).isEqualTo("Get Bot Test");
  }

  @Test
  void update_bot_name_returns_updated_bot() {
    var createResp = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Original Name"), authHeaders(tokenOrgA)),
        Map.class);
    String botId = (String) createResp.getBody().get("id");

    var updateResp = rest.exchange(
        baseUrl("/v1/bots/" + botId),
        HttpMethod.PATCH,
        new HttpEntity<>(TestBotFactory.updateBotPayload("Renamed Bot"), authHeaders(tokenOrgA)),
        Map.class);

    assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateResp.getBody().get("name")).isEqualTo("Renamed Bot");
  }

  @Test
  void delete_bot_returns_204() {
    var createResp = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Bot To Delete"), authHeaders(tokenOrgA)),
        Map.class);
    String botId = (String) createResp.getBody().get("id");

    var deleteResp = rest.exchange(
        baseUrl("/v1/bots/" + botId),
        HttpMethod.DELETE,
        new HttpEntity<>(authHeaders(tokenOrgA)),
        Void.class);

    assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    // Subsequent GET must return 404
    var getResp = rest.exchange(
        baseUrl("/v1/bots/" + botId),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenOrgA)),
        Map.class);
    assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void get_bot_from_other_org_returns_404() {
    // Org A creates a bot
    var createResp = rest.postForEntity(
        baseUrl("/v1/bots"),
        new HttpEntity<>(TestBotFactory.createBotPayload("Org A Private Bot"), authHeaders(tokenOrgA)),
        Map.class);
    String orgABotId = (String) createResp.getBody().get("id");

    // Org B tries to fetch Org A's bot — must be invisible
    var response = rest.exchange(
        baseUrl("/v1/bots/" + orgABotId),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(tokenOrgB)),
        Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
