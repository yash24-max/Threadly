package dev.threadly.common.test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests with Testcontainers.
 *
 * Automatically spins up:
 * - PostgreSQL database
 * - Kafka broker
 *
 * Usage:
 * @SpringBootTest
 * class MyServiceIntegrationTest extends AbstractIntegrationTest {
 *
 *   @Autowired
 *   private MockMvc mockMvc;
 *
 *   @Autowired
 *   private MyRepository repository;
 *
 *   @Test
 *   void testCreateEntity() {
 *     // Test code using mockMvc or repository
 *   }
 * }
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      DockerImageName.parse("postgres:16-alpine")
  )
      .withDatabaseName("threadly_test")
      .withUsername("threadly")
      .withPassword("test");

  @Container
  static KafkaContainer kafka = new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
  );

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  protected MockMvc mockMvc;

  /**
   * Helper: parse JWT token for testing.
   */
  protected String extractTokenFromResponse(String responseBody, String tokenPath) {
    // Example: $.accessToken from JSON response
    return responseBody;
  }
}
