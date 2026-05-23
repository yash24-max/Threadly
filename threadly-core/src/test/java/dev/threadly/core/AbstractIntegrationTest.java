package dev.threadly.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("threadly_test")
          .withUsername("threadly")
          .withPassword("test");

  @Container
  @SuppressWarnings("resource")
  static GenericContainer<?> redis =
      new GenericContainer<>("redis:7-alpine")
          .withExposedPorts(6379)
          .withCommand("redis-server", "--requirepass", "test");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
    r.add("spring.data.redis.host", redis::getHost);
    r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    r.add("spring.data.redis.password", () -> "test");
    // Disable external calls — point to unreachable ports
    r.add("threadly.ai.url", () -> "http://localhost:9999");
    r.add("threadly.centrifugo.url", () -> "http://localhost:9998");
    r.add("threadly.centrifugo.api-key", () -> "test-api-key");
  }

  @Autowired protected TestRestTemplate rest;
  @Autowired protected ObjectMapper mapper;
  @LocalServerPort protected int port;

  protected String baseUrl(String path) {
    return "http://localhost:" + port + path;
  }

  /**
   * Creates a new org + admin user via signup, then logs in and returns
   * "Bearer {accessToken}" ready to use as an Authorization header value.
   */
  protected String signup_and_login(String email, String password, String orgName) {
    Map<String, Object> signupBody =
        Map.of(
            "email", email,
            "password", password,
            "orgName", orgName,
            "name", "Test User");
    rest.postForEntity(baseUrl("/v1/auth/signup"), signupBody, Map.class);

    Map<String, Object> loginBody = Map.of("email", email, "password", password);
    @SuppressWarnings("unchecked")
    var loginResp = rest.postForEntity(baseUrl("/v1/auth/login"), loginBody, Map.class);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = loginResp.getBody();
    assert body != null : "Login response body must not be null";
    return "Bearer " + body.get("accessToken");
  }

  protected HttpHeaders authHeaders(String token) {
    var h = new HttpHeaders();
    h.set("Authorization", token);
    h.setContentType(MediaType.APPLICATION_JSON);
    return h;
  }
}
