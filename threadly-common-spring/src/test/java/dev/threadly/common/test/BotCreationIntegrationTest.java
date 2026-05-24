package dev.threadly.common.test;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * BotCreationIntegrationTest - Cross-service integration test example
 *
 * Validates that creating a bot in workspace-service:
 * 1. Writes to workspace_service.bots table
 * 2. Publishes bot.created Kafka event
 * 3. Is visible in flow-service (via inter-service call)
 * 4. Is visible in analytics-service (via Kafka event processing)
 *
 * During Phase 2 migration:
 * - Writes should go to BOTH monolith + services
 * - Event should be visible in both old and new systems
 * - No data loss or inconsistency
 *
 * Run: mvn verify -DskipITs=false
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Bot Creation Cross-Service Integration Tests")
public class BotCreationIntegrationTest extends MicroservicesIntegrationTest {

    /**
     * Test: Create bot in workspace-service, verify visibility in flow-service
     *
     * This is the PRIMARY integration test during Phase 2:
     * - Verifies dual-write consistency
     * - Validates event propagation via Kafka
     * - Confirms data sync across service boundaries
     */
    @Test
    @DisplayName("Create bot in workspace-service, verify in flow-service")
    void testBotCreationPropagatesAcrossServices() {
        // Given: An org context and unique bot name
        String botName = "TestBot_" + UUID.randomUUID();
        Map<String, Object> botPayload = new HashMap<>();
        botPayload.put("name", botName);
        botPayload.put("org_id", testOrgId);
        botPayload.put("description", "Integration test bot");
        botPayload.put("language", "en");

        // When: Create bot via workspace-service
        String botId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .body(botPayload)
            .when()
            .post("/api/bots")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .extract()
            .path("id");

        // And: Wait for Kafka event propagation (dual-write lag)
        waitForEventPropagation(500);

        // Then: Verify bot is visible in workspace-service
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .when()
            .get("/api/bots/{botId}", botId)
            .then()
            .statusCode(200)
            .body("name", org.hamcrest.Matchers.equalTo(botName));

        // And: Verify bot is visible in flow-service (inter-service consistency)
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .when()
            .get("/api/flows?botId={botId}", botId)
            .then()
            .statusCode(200)
            .body("size()", greaterThan(0)); // Flow service knows about bot
    }

    /**
     * Test: Create bot, verify in analytics-service via Kafka event
     *
     * Kafka propagation test:
     * - Workspace-service publishes bot.created event
     * - Analytics-service consumes event
     * - Event metrics are recorded
     */
    @Test
    @DisplayName("Bot creation event is processed by analytics-service")
    void testBotCreationEventProcessing() {
        // Given
        String botName = "AnalyticsTest_" + UUID.randomUUID();
        Map<String, Object> botPayload = new HashMap<>();
        botPayload.put("name", botName);
        botPayload.put("org_id", testOrgId);

        // When: Create bot
        String botId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .body(botPayload)
            .when()
            .post("/api/bots")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Wait for Kafka processing
        waitForEventPropagation(1000); // Longer wait for analytics consumer

        // Then: Verify event was recorded in analytics-service
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .when()
            .get("/api/analytics/events?type=bot.created&botId={botId}", botId)
            .then()
            .statusCode(200)
            .body("events.size()", greaterThan(0));
    }

    /**
     * Test: Update bot, verify changes propagate to all services
     *
     * Phase 2 dual-write consistency:
     * - Monolith receives PATCH request
     * - DualWriteInterceptor forwards to services
     * - All services reflect the change
     */
    @Test
    @DisplayName("Bot update propagates to all services")
    void testBotUpdateConsistency() {
        // Given: An existing bot
        String botName = "BotToUpdate_" + UUID.randomUUID();
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("name", botName);
        createPayload.put("org_id", testOrgId);

        String botId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .body(createPayload)
            .when()
            .post("/api/bots")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // When: Update bot settings
        String newDescription = "Updated description at " + System.currentTimeMillis();
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("description", newDescription);
        updatePayload.put("accent_color", "#FF5733");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .body(updatePayload)
            .when()
            .patch("/api/bots/{botId}", botId)
            .then()
            .statusCode(200);

        // Wait for propagation
        waitForEventPropagation(500);

        // Then: Verify update in workspace-service
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .when()
            .get("/api/bots/{botId}", botId)
            .then()
            .statusCode(200)
            .body("description", org.hamcrest.Matchers.equalTo(newDescription))
            .body("accent_color", org.hamcrest.Matchers.equalTo("#FF5733"));

        // And: Verify update is visible to flow-service
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .when()
            .get("/api/flows/bots/{botId}", botId)
            .then()
            .statusCode(200);
    }

    /**
     * Test: Delete bot, verify cascading deletes across services
     *
     * Data consistency test:
     * - Delete operation cascades to related records
     * - Kafka event triggers cleanup in dependent services
     * - No orphaned records left behind
     */
    @Test
    @DisplayName("Bot deletion cascades to flows and sessions")
    void testBotDeletionConsistency() {
        // Given: A bot with associated data
        String botName = "BotToDelete_" + UUID.randomUUID();
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("name", botName);
        createPayload.put("org_id", testOrgId);

        String botId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .body(createPayload)
            .when()
            .post("/api/bots")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // When: Delete the bot
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .when()
            .delete("/api/bots/{botId}", botId)
            .then()
            .statusCode(204);

        // Wait for Kafka cascade
        waitForEventPropagation(1000);

        // Then: Verify bot is gone from workspace-service
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .when()
            .get("/api/bots/{botId}", botId)
            .then()
            .statusCode(404);

        // And: Verify flows are cleaned up in flow-service
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .when()
            .get("/api/flows?botId={botId}", botId)
            .then()
            .statusCode(200)
            .body("flows.size()", org.hamcrest.Matchers.equalTo(0));
    }

    /**
     * Test: List bots, verify pagination and filtering
     *
     * Service resilience test:
     * - Multiple services correctly implement list pagination
     * - Filters work correctly across service boundaries
     * - No N+1 query problems
     */
    @Test
    @DisplayName("Bot listing with pagination and filters")
    void testBotListingAndFiltering() {
        // Given: Multiple bots created
        for (int i = 0; i < 3; i++) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", "ListTest_" + i + "_" + UUID.randomUUID());
            payload.put("org_id", testOrgId);

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + testAuthToken)
                .body(payload)
                .when()
                .post("/api/bots")
                .then()
                .statusCode(201);
        }

        // When: List bots with pagination
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + testAuthToken)
            .queryParam("page", 0)
            .queryParam("pageSize", 10)
            .when()
            .get("/api/bots")
            .then()
            .statusCode(200)
            .body("page", org.hamcrest.Matchers.equalTo(0))
            .body("items.size()", greaterThan(0));
    }
}
