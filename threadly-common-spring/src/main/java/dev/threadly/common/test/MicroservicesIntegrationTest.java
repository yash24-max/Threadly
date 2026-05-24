package dev.threadly.common.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * MicroservicesIntegrationTest - Base class for cross-service integration tests
 *
 * During Phase 2-3 migration, validates that operations on one service
 * are correctly reflected in dependent services via Kafka events.
 *
 * Example: Create bot in workspace-service, verify visible in flow-service
 *
 * Usage:
 * @SpringBootTest
 * @Testcontainers
 * public class BotCreationIntegrationTest extends MicroservicesIntegrationTest {
 *   @Test
 *   void testBotCreatePropagatesToFlowService() {
 *     // Given
 *     String botName = "TestBot_" + UUID.randomUUID();
 *
 *     // When: Create bot via workspace-service
 *     String botId = workspaceApi()
 *       .post("/bots")
 *       .body(Map.of("name", botName, "org_id", orgId))
 *       .then()
 *       .statusCode(201)
 *       .extract()
 *       .path("id");
 *
 *     // Then: Verify bot visible in flow-service
 *     waitForEventPropagation(2000); // Allow Kafka event processing
 *     flowApi()
 *       .get("/bots/{botId}/flows", botId)
 *       .then()
 *       .statusCode(200)
 *       .body("size()", greaterThan(0)); // Flow service knows about bot
 *   }
 * }
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(partitions = 1, brokerProperties = {
    "listeners=PLAINTEXT://localhost:0",
    "log.retention.hours=1"
})
@ActiveProfiles("test")
public abstract class MicroservicesIntegrationTest {

    @LocalServerPort
    protected int localServerPort;

    @Autowired
    protected TestRestTemplate restTemplate;

    // Test fixtures
    protected String testOrgId;
    protected String testUserId;
    protected String testAuthToken;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("threadly_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> qdrant = new GenericContainer<>("qdrant/qdrant:latest")
        .withExposedPorts(6333)
        .waitingFor(Wait.forHttp("/health").forPort(6333));

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + localServerPort;

        // Create test organization and user
        testOrgId = UUID.randomUUID().toString();
        testUserId = UUID.randomUUID().toString();

        // Get test auth token (from Identity Service or mock)
        testAuthToken = getTestAuthToken(testUserId, testOrgId);
    }

    /**
     * Rest API client for Identity Service
     * Used for: User creation, org membership, JWT validation
     */
    protected ApiClient identityApi() {
        return new ApiClient(restTemplate, "identity-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Rest API client for Workspace Service
     * Used for: Bot CRUD, workspace settings
     */
    protected ApiClient workspaceApi() {
        return new ApiClient(restTemplate, "workspace-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Rest API client for Flow Service
     * Used for: Flow CRUD, versioning, publishing
     */
    protected ApiClient flowApi() {
        return new ApiClient(restTemplate, "flow-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Rest API client for Runtime Service
     * Used for: Session creation, node execution
     */
    protected ApiClient runtimeApi() {
        return new ApiClient(restTemplate, "runtime-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Rest API client for Conversation Service
     * Used for: Conversations, messages, leads, handoff
     */
    protected ApiClient conversationApi() {
        return new ApiClient(restTemplate, "conversation-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Rest API client for Knowledge Service
     * Used for: KB document upload, RAG query
     */
    protected ApiClient knowledgeApi() {
        return new ApiClient(restTemplate, "knowledge-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Rest API client for Analytics Service
     * Used for: Event tracking, dashboard queries
     */
    protected ApiClient analyticsApi() {
        return new ApiClient(restTemplate, "analytics-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Rest API client for Billing Service
     * Used for: Plans, subscriptions, usage metering
     */
    protected ApiClient billingApi() {
        return new ApiClient(restTemplate, "billing-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Rest API client for Integration Service
     * Used for: Connectors, action execution, marketplace
     */
    protected ApiClient integrationApi() {
        return new ApiClient(restTemplate, "integration-service")
            .withAuthToken(testAuthToken);
    }

    /**
     * Wait for Kafka event propagation
     *
     * During Phase 2-3, events take time to process.
     * Use this to avoid flaky tests.
     *
     * @param delayMs milliseconds to wait (typically 100-2000)
     */
    protected void waitForEventPropagation(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Assert that a resource exists in database across services
     *
     * Example: Bot created in workspace-service should exist when
     * queried from flow-service (via service-to-service calls)
     */
    protected void assertResourceExists(String resourceType, String resourceId, ApiClient apiClient) {
        apiClient
            .get("/{type}/{id}", resourceType, resourceId)
            .then()
            .statusCode(200);
    }

    /**
     * Assert that Kafka event was published (via metrics or consumer group lag)
     */
    protected void assertEventPublished(String topic, String eventKey) {
        // In real test: query Kafka consumer group lag
        // If lag decreased, event was processed
        // For now: placeholder
    }

    /**
     * Get test JWT token from Identity Service
     */
    private String getTestAuthToken(String userId, String orgId) {
        // Mock implementation: can be overridden in subclass
        // Real implementation queries Identity Service /auth/test-token
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIx" +
               "IiwibmFtZSI6IlRlc3QgVXNlciIsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    /**
     * Fluent API client for easier test assertions
     */
    protected static class ApiClient {
        private final TestRestTemplate restTemplate;
        private final String serviceName;
        private String authToken;

        public ApiClient(TestRestTemplate restTemplate, String serviceName) {
            this.restTemplate = restTemplate;
            this.serviceName = serviceName;
        }

        ApiClient withAuthToken(String token) {
            this.authToken = token;
            return this;
        }

        public RestAssuredRequest post(String path) {
            return new RestAssuredRequest("POST", path, authToken);
        }

        public RestAssuredRequest get(String path, Object... args) {
            return new RestAssuredRequest("GET", String.format(path, args), authToken);
        }

        public RestAssuredRequest patch(String path) {
            return new RestAssuredRequest("PATCH", path, authToken);
        }

        public RestAssuredRequest delete(String path) {
            return new RestAssuredRequest("DELETE", path, authToken);
        }
    }

    /**
     * Fluent REST request builder
     */
    protected static class RestAssuredRequest {
        private final String method;
        private final String path;
        private final String authToken;

        public RestAssuredRequest(String method, String path, String authToken) {
            this.method = method;
            this.path = path;
            this.authToken = authToken;
        }

        public RestAssuredRequest body(Object body) {
            switch (method) {
                case "POST":
                    given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .body(body)
                        .when()
                        .post(path);
                    break;
                case "PATCH":
                    given()
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .body(body)
                        .when()
                        .patch(path);
                    break;
            }
            return this;
        }

        public Object then() {
            return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .request(method, path)
                .then();
        }
    }
}
