package dev.threadly.core.flow;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.core.AbstractIntegrationTest;
import dev.threadly.core.fixtures.BotFactory;
import dev.threadly.core.fixtures.FlowFactory;
import dev.threadly.core.fixtures.UserFactory;
import dev.threadly.core.flow.FlowController.FlowResponse;
import dev.threadly.core.flow.FlowController.FlowVersionResponse;
import dev.threadly.core.flow.FlowController.SaveFlowRequest;
import dev.threadly.core.workspace.BotController.BotResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@code /v1/bots/{botId}/flow} endpoints.
 */
class FlowIntegrationTest extends AbstractIntegrationTest {

    private String orgToken;
    private String botId;

    @BeforeEach
    void setUp() {
        String email = UserFactory.uniqueEmail();
        createTestOrg(email, UserFactory.DEFAULT_PASSWORD, "Flow Test Org");
        orgToken = loginAndGetToken(email, UserFactory.DEFAULT_PASSWORD);

        ResponseEntity<BotResponse> botResp =
                authPost("/v1/bots", orgToken, BotFactory.createRequest("Flow Bot"), BotResponse.class);
        botId = botResp.getBody().getId();
    }

    @Test
    void create_flow_creates_draft_version() {
        ResponseEntity<FlowResponse> resp =
                authGet("/v1/bots/" + botId + "/flow", orgToken, FlowResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        FlowResponse flow = resp.getBody();
        assertThat(flow).isNotNull();
        assertThat(flow.getId()).isNotBlank();
        assertThat(flow.getBotId()).isEqualTo(botId);
        assertThat(flow.getDraftJson()).isNotBlank();
        assertThat(flow.getPublishedJson()).isNull(); // not yet published
    }

    @Test
    void update_flow_updates_draft_not_published() {
        String flowJson = FlowFactory.minimalValidFlowJson();
        SaveFlowRequest saveReq = FlowFactory.saveRequest(flowJson);

        ResponseEntity<FlowResponse> saveResp =
                authPost("/v1/bots/" + botId + "/flow", orgToken, saveReq, FlowResponse.class);

        // Note: PUT is mapped; use restTemplate exchange for PUT
        org.springframework.http.HttpEntity<SaveFlowRequest> entity =
                new org.springframework.http.HttpEntity<>(saveReq, authHeaders(orgToken));
        ResponseEntity<FlowResponse> putResp = restTemplate.exchange(
                "/v1/bots/" + botId + "/flow",
                org.springframework.http.HttpMethod.PUT,
                entity,
                FlowResponse.class);

        assertThat(putResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        FlowResponse flow = putResp.getBody();
        assertThat(flow.getDraftJson()).contains("start");
        assertThat(flow.getPublishedJson()).isNull(); // draft updated, still not published
    }

    @Test
    void publish_flow_creates_new_version_and_marks_published() {
        // Save a valid draft
        SaveFlowRequest saveReq = FlowFactory.saveRequest(FlowFactory.minimalValidFlowJson());
        org.springframework.http.HttpEntity<SaveFlowRequest> saveEntity =
                new org.springframework.http.HttpEntity<>(saveReq, authHeaders(orgToken));
        restTemplate.exchange(
                "/v1/bots/" + botId + "/flow",
                org.springframework.http.HttpMethod.PUT,
                saveEntity,
                FlowResponse.class);

        // Publish
        ResponseEntity<FlowResponse> publishResp =
                authPost("/v1/bots/" + botId + "/flow/publish", orgToken, null, FlowResponse.class);

        assertThat(publishResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        FlowResponse published = publishResp.getBody();
        assertThat(published.getPublishedJson()).isNotBlank();
        assertThat(published.getPublishedAt()).isNotBlank();

        // A version entry should exist
        ResponseEntity<FlowVersionResponse[]> versionsResp =
                authGet("/v1/bots/" + botId + "/flow/versions", orgToken, FlowVersionResponse[].class);
        assertThat(versionsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        FlowVersionResponse[] versions = versionsResp.getBody();
        assertThat(versions).isNotEmpty();
        assertThat(versions[0].getVersionNum()).isGreaterThanOrEqualTo(1);
        assertThat(versions[0].getSnapshotJson()).isNotBlank();
    }

    @Test
    void rollback_flow_to_previous_version() {
        // Publish version 1 (minimal flow)
        SaveFlowRequest v1Req = FlowFactory.saveRequest(FlowFactory.minimalValidFlowJson());
        org.springframework.http.HttpEntity<SaveFlowRequest> putV1 =
                new org.springframework.http.HttpEntity<>(v1Req, authHeaders(orgToken));
        restTemplate.exchange("/v1/bots/" + botId + "/flow",
                org.springframework.http.HttpMethod.PUT, putV1, FlowResponse.class);
        authPost("/v1/bots/" + botId + "/flow/publish", orgToken, null, FlowResponse.class);

        // Publish version 2 (two-node flow)
        SaveFlowRequest v2Req = FlowFactory.saveRequest(FlowFactory.twoNodeFlowJson());
        org.springframework.http.HttpEntity<SaveFlowRequest> putV2 =
                new org.springframework.http.HttpEntity<>(v2Req, authHeaders(orgToken));
        restTemplate.exchange("/v1/bots/" + botId + "/flow",
                org.springframework.http.HttpMethod.PUT, putV2, FlowResponse.class);
        authPost("/v1/bots/" + botId + "/flow/publish", orgToken, null, FlowResponse.class);

        // Rollback to version 1
        ResponseEntity<FlowResponse> rollbackResp =
                authPost("/v1/bots/" + botId + "/flow/versions/1/rollback", orgToken, null, FlowResponse.class);

        assertThat(rollbackResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Draft should be restored to the v1 JSON (contains only start node, not the message node)
        assertThat(rollbackResp.getBody().getDraftJson()).doesNotContain("msg-1");
    }

    @Test
    void flow_from_other_org_returns_404() {
        // Create a bot in a second org
        String otherEmail = UserFactory.uniqueEmail();
        createTestOrg(otherEmail, UserFactory.DEFAULT_PASSWORD, "Other Org");
        String otherToken = loginAndGetToken(otherEmail, UserFactory.DEFAULT_PASSWORD);
        ResponseEntity<BotResponse> otherBot =
                authPost("/v1/bots", otherToken, BotFactory.createRequest("Other Bot"), BotResponse.class);
        String otherBotId = otherBot.getBody().getId();

        // Org A tries to access org B's bot flow
        ResponseEntity<JsonNode> resp =
                authGet("/v1/bots/" + otherBotId + "/flow", orgToken, JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void publish_flow_with_no_start_node_returns_400() {
        // Save an invalid flow (no start node)
        SaveFlowRequest saveReq = FlowFactory.saveRequest(FlowFactory.noStartNodeFlowJson());
        org.springframework.http.HttpEntity<SaveFlowRequest> putEntity =
                new org.springframework.http.HttpEntity<>(saveReq, authHeaders(orgToken));
        restTemplate.exchange("/v1/bots/" + botId + "/flow",
                org.springframework.http.HttpMethod.PUT, putEntity, FlowResponse.class);

        // Attempt to publish must fail with 400
        ResponseEntity<JsonNode> publishResp =
                authPost("/v1/bots/" + botId + "/flow/publish", orgToken, null, JsonNode.class);
        assertThat(publishResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void invalid_flow_json_is_saved_as_draft_but_rejected_on_publish() {
        // Malformed JSON can be saved as a draft string (the server stores it)
        // but publish should reject it
        SaveFlowRequest malformed = FlowFactory.saveRequest(FlowFactory.malformedJson());
        org.springframework.http.HttpEntity<SaveFlowRequest> putEntity =
                new org.springframework.http.HttpEntity<>(malformed, authHeaders(orgToken));
        restTemplate.exchange("/v1/bots/" + botId + "/flow",
                org.springframework.http.HttpMethod.PUT, putEntity, JsonNode.class);

        ResponseEntity<JsonNode> publishResp =
                authPost("/v1/bots/" + botId + "/flow/publish", orgToken, null, JsonNode.class);
        assertThat(publishResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
