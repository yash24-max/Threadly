# Test Implementation Guide - Threadly Microservices

**Status:** Implementation Ready
**Last Updated:** 2026-05-24
**Owner:** QA & Testing Tech Lead

---

## Quick Start Checklist

### Immediate Actions (Week 1)

- [ ] Review and approve TEST_STRATEGY.md
- [ ] Create test directories in all 9 Java services
- [ ] Add test dependencies to all pom.xml files
- [ ] Set up base test classes (extend AbstractIntegrationTest)
- [ ] Configure Codecov integration in GitHub Actions
- [ ] Create Slack notifications for CI/CD test failures

### Infrastructure Setup (Week 1-2)

- [ ] Create `infrastructure/testing/` directory
- [ ] Create docker-compose.test.yml with all services
- [ ] Create test fixtures and factories
- [ ] Configure SonarQube quality gates
- [ ] Create testing runbook for developers

### Coverage Goals (Week 2-4)

- [ ] Identity Service: 85% coverage
- [ ] Workspace Service: 80% coverage
- [ ] Flow Service: 80% coverage
- [ ] Runtime Service: 80% coverage
- [ ] Conversation Service: 75% coverage
- [ ] Knowledge Service: 75% coverage
- [ ] Analytics Service: 70% coverage
- [ ] Billing Service: 75% coverage
- [ ] Integration Service: 70% coverage

---

## 1. Java Service Test Setup

### 1.1 Directory Structure for Each Service

```
services/[service-name]/
├── src/
│   ├── main/
│   │   ├── java/dev/threadly/[service]/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   └── config/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-test.yml
│   │
│   └── test/
│       ├── java/dev/threadly/[service]/
│       │   ├── controller/
│       │   │   └── [Service]ControllerTest.java        (NEW)
│       │   ├── service/
│       │   │   └── [Service]ServiceTest.java           (NEW)
│       │   ├── repository/
│       │   │   └── [Service]RepositoryTest.java        (NEW)
│       │   ├── integration/
│       │   │   └── [Service]IntegrationTest.java       (NEW)
│       │   └── AbstractTest.java                       (NEW)
│       │
│       └── resources/
│           ├── application-test.yml                    (NEW)
│           ├── test-data.sql                           (NEW)
│           └── logback-test.xml                        (NEW)

├── pom.xml                                             (MODIFY)
└── README.md                                           (UPDATE)
```

### 1.2 Add Testing Dependencies to pom.xml

**For all Java services, add to the `<dependencies>` section:**

```xml
<!-- Testing Framework -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
  <exclusions>
    <exclusion>
      <groupId>org.junit.vintage</groupId>
      <artifactId>junit-vintage-engine</artifactId>
    </exclusion>
  </exclusions>
</dependency>

<!-- TestContainers (PostgreSQL, Kafka, Generic) -->
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers</artifactId>
  <version>1.20.3</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
  <version>1.20.3</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>kafka</artifactId>
  <version>1.20.3</version>
  <scope>test</scope>
</dependency>

<!-- REST Assured API Testing -->
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>rest-assured</artifactId>
  <scope>test</scope>
</dependency>

<!-- Awaitility for Async Assertions -->
<dependency>
  <groupId>org.awaitility</groupId>
  <artifactId>awaitility</artifactId>
  <scope>test</scope>
</dependency>

<!-- H2 In-Memory Database (for fast unit tests) -->
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>test</scope>
</dependency>

<!-- Faker for Test Data Generation -->
<dependency>
  <groupId>net.datafaker</groupId>
  <artifactId>datafaker</artifactId>
  <version>2.0.2</version>
  <scope>test</scope>
</dependency>
```

### 1.3 Add Maven Plugins for Test Execution & Coverage

```xml
<build>
  <plugins>
    <!-- Surefire Plugin (Unit Tests) -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.1.2</version>
      <configuration>
        <includes>
          <include>**/*Test.java</include>
          <include>**/*Tests.java</include>
        </includes>
        <excludes>
          <exclude>**/*IntegrationTest.java</exclude>
        </excludes>
        <!-- Run tests in parallel for speed -->
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
      </configuration>
    </plugin>

    <!-- Failsafe Plugin (Integration Tests) -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-failsafe-plugin</artifactId>
      <version>3.1.2</version>
      <configuration>
        <includes>
          <include>**/*IntegrationTest.java</include>
        </includes>
        <parallel>methods</parallel>
        <threadCount>2</threadCount>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>integration-test</goal>
            <goal>verify</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

    <!-- JaCoCo Code Coverage -->
    <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <version>0.8.10</version>
      <executions>
        <execution>
          <goals>
            <goal>prepare-agent</goal>
          </goals>
        </execution>
        <execution>
          <id>report</id>
          <phase>test</phase>
          <goals>
            <goal>report</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <excludes>
          <exclude>**/config/**</exclude>
          <exclude>**/dto/**</exclude>
          <exclude>**/entity/**</exclude>
        </excludes>
      </configuration>
    </plugin>

    <!-- OWASP Dependency Check -->
    <plugin>
      <groupId>org.owasp</groupId>
      <artifactId>dependency-check-maven</artifactId>
      <version>9.0.0</version>
      <configuration>
        <failBuildOnCVSS>7.0</failBuildOnCVSS>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

    <!-- Spotless Code Formatter (Test Code) -->
    <plugin>
      <groupId>com.diffplug.spotless</groupId>
      <artifactId>spotless-maven-plugin</artifactId>
      <version>2.40.0</version>
      <configuration>
        <java>
          <includes>
            <include>src/test/java/**/*.java</include>
          </includes>
          <googleJavaFormat>
            <version>1.17.0</version>
          </googleJavaFormat>
        </java>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### 1.4 Create application-test.yml Configuration

**File:** `services/[service-name]/src/test/resources/application-test.yml`

```yaml
spring:
  application:
    name: threadly-[service]

  # H2 In-Memory Database (faster for unit tests)
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  # JPA Configuration
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop  # Recreate schema for each test
    show-sql: false
    properties:
      hibernate.format_sql: true

  # Kafka (will use Embedded Kafka via @EmbeddedKafka)
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: threadly-test
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: '*'

  # Redis (optional, use testcontainers if needed)
  redis:
    host: localhost
    port: 6379

# Disable unnecessary services for tests
logging:
  level:
    root: WARN
    dev.threadly: DEBUG

# Test-specific settings
app:
  security:
    jwt:
      secret: test-secret-key-that-is-at-least-32-characters-long-for-256-bit
      expiration: 86400000

  test:
    enabled: true  # Allows @ActiveProfiles("test") beans
```

### 1.5 Base Test Classes

**File:** `services/[service-name]/src/test/java/dev/threadly/[service]/AbstractTest.java`

```java
package dev.threadly.service.test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all unit tests in this service.
 * Provides setup for Spring context (minimal), MockMvc, etc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractTest {
    // Common test fixtures can go here
}
```

**File:** `services/[service-name]/src/test/java/dev/threadly/[service]/integration/AbstractIntegrationTest.java`

```java
package dev.threadly.service.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests that need:
 * - Real HTTP server (RANDOM_PORT)
 * - Kafka (via @EmbeddedKafka)
 * - Database (via TestContainers or H2)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:0",
        "log.retention.hours=1"
    }
)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setupRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }
}
```

---

## 2. Sample Test Implementation

### 2.1 Unit Test Example (Service Layer)

**File:** `services/workspace-service/src/test/java/dev/threadly/workspace/service/BotServiceTest.java`

```java
package dev.threadly.workspace.service;

import dev.threadly.common.exception.ResourceNotFoundException;
import dev.threadly.workspace.model.Bot;
import dev.threadly.workspace.repository.BotRepository;
import dev.threadly.workspace.test.AbstractTest;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("BotService Unit Tests")
class BotServiceTest extends AbstractTest {

    @Mock
    private BotRepository botRepository;

    @InjectMocks
    private BotService botService;

    private final Faker faker = new Faker();
    private String testOrgId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testOrgId = UUID.randomUUID().toString();
        testUserId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("Given valid bot data, when creating a bot, then bot is saved and returned")
    void testCreateBot() {
        // Given
        String botName = faker.name().name();
        Bot botRequest = Bot.builder()
            .name(botName)
            .orgId(testOrgId)
            .description("Test bot")
            .language("en")
            .build();

        Bot savedBot = Bot.builder()
            .id(UUID.randomUUID().toString())
            .name(botName)
            .orgId(testOrgId)
            .description("Test bot")
            .language("en")
            .build();

        when(botRepository.save(any(Bot.class))).thenReturn(savedBot);

        // When
        Bot result = botService.createBot(botRequest);

        // Then
        assertThat(result)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", botName)
            .hasFieldOrPropertyWithValue("orgId", testOrgId);

        verify(botRepository, times(1)).save(any(Bot.class));
    }

    @Test
    @DisplayName("Given bot ID, when getting bot, then bot is returned")
    void testGetBotById() {
        // Given
        String botId = UUID.randomUUID().toString();
        Bot expectedBot = Bot.builder()
            .id(botId)
            .name("Test Bot")
            .orgId(testOrgId)
            .build();

        when(botRepository.findById(botId)).thenReturn(Optional.of(expectedBot));

        // When
        Bot result = botService.getBotById(botId);

        // Then
        assertThat(result).isEqualTo(expectedBot);
        verify(botRepository, times(1)).findById(botId);
    }

    @Test
    @DisplayName("Given non-existent bot ID, when getting bot, then throws ResourceNotFoundException")
    void testGetBotByIdNotFound() {
        // Given
        String botId = UUID.randomUUID().toString();
        when(botRepository.findById(botId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> botService.getBotById(botId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Bot not found: " + botId);

        verify(botRepository, times(1)).findById(botId);
    }

    @Test
    @DisplayName("Given bot ID and update data, when updating bot, then bot is updated")
    void testUpdateBot() {
        // Given
        String botId = UUID.randomUUID().toString();
        String newDescription = "Updated description";

        Bot existingBot = Bot.builder()
            .id(botId)
            .name("Test Bot")
            .orgId(testOrgId)
            .description("Old description")
            .build();

        Bot updatedBot = Bot.builder()
            .id(botId)
            .name("Test Bot")
            .orgId(testOrgId)
            .description(newDescription)
            .build();

        when(botRepository.findById(botId)).thenReturn(Optional.of(existingBot));
        when(botRepository.save(any(Bot.class))).thenReturn(updatedBot);

        // When
        Bot result = botService.updateBot(botId, newDescription);

        // Then
        assertThat(result.getDescription()).isEqualTo(newDescription);
        verify(botRepository).save(argThat(bot ->
            bot.getDescription().equals(newDescription)
        ));
    }

    @Test
    @DisplayName("Given bot ID, when deleting bot, then bot is deleted")
    void testDeleteBot() {
        // Given
        String botId = UUID.randomUUID().toString();

        // When
        botService.deleteBot(botId);

        // Then
        verify(botRepository, times(1)).deleteById(botId);
    }

    @Test
    @DisplayName("Given invalid bot name (empty), when creating bot, then throws validation error")
    void testCreateBotWithInvalidName() {
        // Given
        Bot invalidBot = Bot.builder()
            .name("")
            .orgId(testOrgId)
            .build();

        // When & Then
        assertThatThrownBy(() -> botService.createBot(invalidBot))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Bot name cannot be empty");

        verify(botRepository, never()).save(any());
    }
}
```

### 2.2 Integration Test Example

**File:** `services/workspace-service/src/test/java/dev/threadly/workspace/integration/BotLifecycleIntegrationTest.java`

```java
package dev.threadly.workspace.integration;

import dev.threadly.workspace.model.Bot;
import dev.threadly.workspace.repository.BotRepository;
import dev.threadly.workspace.test.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Bot Lifecycle Integration Tests")
class BotLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BotRepository botRepository;

    private String testOrgId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testOrgId = UUID.randomUUID().toString();
        testUserId = UUID.randomUUID().toString();
        // Clear database
        botRepository.deleteAll();
    }

    @Test
    @DisplayName("Given valid bot data, when creating bot via API, then bot is created and returned")
    void testCreateBotViaAPI() {
        // Given
        Map<String, Object> botPayload = new HashMap<>();
        botPayload.put("name", "Integration Test Bot");
        botPayload.put("orgId", testOrgId);
        botPayload.put("description", "Test bot for integration testing");
        botPayload.put("language", "en");

        // When & Then
        String botId = given()
            .contentType(ContentType.JSON)
            .body(botPayload)
            .when()
            .post("/bots")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Integration Test Bot"))
            .body("orgId", equalTo(testOrgId))
            .extract()
            .path("id");

        // Verify in database
        assertThat(botRepository.findById(botId)).isPresent();
    }

    @Test
    @DisplayName("Given created bot, when retrieving bot, then bot data is correct")
    void testGetBotViaAPI() {
        // Given
        Bot bot = Bot.builder()
            .id(UUID.randomUUID().toString())
            .name("Test Bot")
            .orgId(testOrgId)
            .description("Test")
            .build();
        botRepository.save(bot);

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/bots/{botId}", bot.getId())
            .then()
            .statusCode(200)
            .body("id", equalTo(bot.getId()))
            .body("name", equalTo("Test Bot"));
    }

    @Test
    @DisplayName("Given multiple bots, when listing bots, then all bots are returned with pagination")
    void testListBotsViaAPI() {
        // Given: Create 3 bots
        for (int i = 0; i < 3; i++) {
            Bot bot = Bot.builder()
                .id(UUID.randomUUID().toString())
                .name("Bot " + i)
                .orgId(testOrgId)
                .build();
            botRepository.save(bot);
        }

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("pageSize", 10)
            .when()
            .get("/bots")
            .then()
            .statusCode(200)
            .body("items.size()", equalTo(3));
    }

    @Test
    @DisplayName("Given bot ID and update data, when updating bot, then bot is updated")
    void testUpdateBotViaAPI() {
        // Given
        Bot bot = Bot.builder()
            .id(UUID.randomUUID().toString())
            .name("Original Name")
            .orgId(testOrgId)
            .description("Original description")
            .build();
        botRepository.save(bot);

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("description", "Updated description");

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(updatePayload)
            .when()
            .patch("/bots/{botId}", bot.getId())
            .then()
            .statusCode(200)
            .body("description", equalTo("Updated description"));

        // Verify in database
        assertThat(botRepository.findById(bot.getId()))
            .get()
            .hasFieldOrPropertyWithValue("description", "Updated description");
    }

    @Test
    @DisplayName("Given bot ID, when deleting bot, then bot is deleted")
    void testDeleteBotViaAPI() {
        // Given
        Bot bot = Bot.builder()
            .id(UUID.randomUUID().toString())
            .name("Bot to Delete")
            .orgId(testOrgId)
            .build();
        botRepository.save(bot);

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .when()
            .delete("/bots/{botId}", bot.getId())
            .then()
            .statusCode(204);

        // Verify deleted from database
        assertThat(botRepository.findById(bot.getId())).isEmpty();
    }
}
```

---

## 3. TypeScript/React Testing Setup

### 3.1 Configure Vitest & Testing Library

**File:** `frontend/threadly-web/vitest.config.ts` (CREATE NEW)

```typescript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./tests/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'tests/',
        '**/*.d.ts',
        '**/index.ts',
        'tests/setup.ts',
      ],
      lines: 70,
      functions: 70,
      branches: 60,
      statements: 70,
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './'),
      '@/components': path.resolve(__dirname, './components'),
      '@/lib': path.resolve(__dirname, './lib'),
      '@/hooks': path.resolve(__dirname, './hooks'),
    },
  },
});
```

**File:** `frontend/threadly-web/tests/setup.ts` (CREATE NEW)

```typescript
import '@testing-library/jest-dom';
import { expect, afterEach, vi } from 'vitest';
import { cleanup } from '@testing-library/react';

// Cleanup after each test
afterEach(() => {
  cleanup();
});

// Mock localStorage
global.localStorage = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn(),
};

// Mock IntersectionObserver
global.IntersectionObserver = vi.fn(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
})) as any;

// Mock WebSocket
global.WebSocket = vi.fn(() => ({
  send: vi.fn(),
  close: vi.fn(),
  addEventListener: vi.fn(),
  removeEventListener: vi.fn(),
})) as any;
```

### 3.2 Setup MSW (Mock Service Worker)

**File:** `frontend/threadly-web/tests/mocks/handlers.ts` (CREATE NEW)

```typescript
import { http, HttpResponse } from 'msw';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export const handlers = [
  // Auth endpoints
  http.post(`${API_URL}/auth/signup`, async () => {
    return HttpResponse.json(
      {
        id: 'user-123',
        email: 'user@test.com',
        token: 'jwt-token-here',
      },
      { status: 201 }
    );
  }),

  http.post(`${API_URL}/auth/login`, async () => {
    return HttpResponse.json(
      {
        id: 'user-123',
        token: 'jwt-token-here',
      },
      { status: 200 }
    );
  }),

  // Bot endpoints
  http.post(`${API_URL}/bots`, async ({ request }) => {
    const body = await request.json() as any;
    return HttpResponse.json(
      {
        id: 'bot-123',
        ...body,
      },
      { status: 201 }
    );
  }),

  http.get(`${API_URL}/bots`, () => {
    return HttpResponse.json({
      items: [{ id: 'bot-1', name: 'Bot 1' }],
      page: 0,
      pageSize: 10,
    });
  }),

  http.get(`${API_URL}/bots/:botId`, ({ params }) => {
    return HttpResponse.json({
      id: params.botId,
      name: 'Test Bot',
    });
  }),

  // Flow endpoints
  http.post(`${API_URL}/flows`, async ({ request }) => {
    const body = await request.json() as any;
    return HttpResponse.json(
      {
        id: 'flow-123',
        ...body,
      },
      { status: 201 }
    );
  }),
];
```

**File:** `frontend/threadly-web/tests/mocks/server.ts` (CREATE NEW)

```typescript
import { setupServer } from 'msw/node';
import { handlers } from './handlers';

export const server = setupServer(...handlers);
```

**Update:** `frontend/threadly-web/tests/setup.ts`

```typescript
import { server } from './mocks/server';

// Start server before all tests
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));

// Reset handlers after each test
afterEach(() => server.resetHandlers());

// Clean up after all tests
afterAll(() => server.close());
```

### 3.3 Component Test Example

**File:** `frontend/threadly-web/tests/components/BotForm.test.tsx` (CREATE NEW)

```typescript
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BotForm } from '@/components/BotForm';

describe('BotForm', () => {
  it('renders form fields correctly', () => {
    render(<BotForm onSubmit={vi.fn()} />);

    expect(screen.getByLabelText(/bot name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create bot/i })).toBeInTheDocument();
  });

  it('submits form with valid data', async () => {
    const handleSubmit = vi.fn();
    render(<BotForm onSubmit={handleSubmit} />);

    const nameInput = screen.getByLabelText(/bot name/i);
    const submitButton = screen.getByRole('button', { name: /create bot/i });

    await userEvent.type(nameInput, 'My Test Bot');
    await userEvent.click(submitButton);

    await waitFor(() => {
      expect(handleSubmit).toHaveBeenCalledWith({
        name: 'My Test Bot',
        description: '',
      });
    });
  });

  it('displays validation error for empty name', async () => {
    render(<BotForm onSubmit={vi.fn()} />);

    const submitButton = screen.getByRole('button', { name: /create bot/i });
    await userEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/bot name is required/i)).toBeInTheDocument();
    });
  });

  it('disables submit button while submitting', async () => {
    const { rerender } = render(
      <BotForm onSubmit={vi.fn()} isLoading={false} />
    );

    const submitButton = screen.getByRole('button', { name: /create bot/i });
    expect(submitButton).not.toBeDisabled();

    rerender(<BotForm onSubmit={vi.fn()} isLoading={true} />);
    expect(submitButton).toBeDisabled();
  });
});
```

### 3.4 Hook Test Example

**File:** `frontend/threadly-web/tests/hooks/useBot.test.ts` (CREATE NEW)

```typescript
import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useBot } from '@/hooks/useBot';
import * as api from '@/lib/api-client';

vi.mock('@/lib/api-client');

describe('useBot', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches bot on mount', async () => {
    const mockBot = { id: 'bot-1', name: 'Test Bot' };
    vi.mocked(api.getBot).mockResolvedValue(mockBot);

    const { result } = renderHook(() => useBot('bot-1'));

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.bot).toEqual(mockBot);
    expect(api.getBot).toHaveBeenCalledWith('bot-1');
  });

  it('handles fetch error', async () => {
    const error = new Error('Failed to fetch');
    vi.mocked(api.getBot).mockRejectedValue(error);

    const { result } = renderHook(() => useBot('bot-1'));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.error).toEqual(error);
    expect(result.current.bot).toBeNull();
  });

  it('creates bot with data', async () => {
    const newBot = { name: 'New Bot', description: 'Test' };
    const savedBot = { id: 'bot-2', ...newBot };
    vi.mocked(api.createBot).mockResolvedValue(savedBot);

    const { result } = renderHook(() => useBot());

    await result.current.createBot(newBot);

    expect(api.createBot).toHaveBeenCalledWith(newBot);
    expect(result.current.bot).toEqual(savedBot);
  });
});
```

### 3.5 Update package.json Scripts

```json
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:watch": "vitest --watch",
    "test:coverage": "vitest --coverage",
    "test:unit": "vitest --run",
    "e2e": "playwright test",
    "e2e:ui": "playwright test --ui",
    "e2e:headed": "playwright test --headed"
  }
}
```

---

## 4. Widget (SDK) Testing

### 4.1 Update Widget Test Configuration

**File:** `frontend/threadly-widget/vitest.config.ts` (CREATE NEW)

```typescript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/__tests__/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      lines: 85,
      functions: 85,
      branches: 75,
      statements: 85,
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

### 4.2 Add Missing Widget Tests

Create tests for critical widget functionality:

```typescript
// src/__tests__/widget-lifecycle.test.ts
// src/__tests__/message-handler.test.ts
// src/__tests__/event-emitter.test.ts
// src/__tests__/ui-renderer.test.ts
```

---

## 5. CI/CD Pipeline Configuration

### 5.1 Update GitHub Actions Workflow

**File:** `.github/workflows/ci.yml` (UPDATE EXISTING)

Add test execution stages:

```yaml
jobs:
  # Java Services
  java-tests:
    name: Java Services Tests
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: threadly_test
          POSTGRES_USER: threadly
          POSTGRES_PASSWORD: threadly
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: temurin
          cache: maven

      - name: Build & Run Tests (All Services)
        run: |
          for service in services/*/; do
            if [ -f "$service/pom.xml" ]; then
              echo "Testing $(basename $service)..."
              (cd "$service" && mvn test -Dsurefire.useFile=false -B) || exit 1
            fi
          done

      - name: Run Integration Tests
        run: |
          for service in services/*/; do
            if [ -f "$service/pom.xml" ]; then
              echo "Integration testing $(basename $service)..."
              (cd "$service" && mvn verify -Dit.skip=false -B) || exit 1
            fi
          done

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./services/*/target/site/jacoco/jacoco.xml
          flags: java
          fail_ci_if_error: false

  # TypeScript Tests
  typescript-tests:
    name: TypeScript Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20.x'
          cache: 'pnpm'

      - run: pnpm install

      - name: Run Vitest (threadly-web)
        run: cd frontend/threadly-web && pnpm test:unit --coverage

      - name: Run Vitest (threadly-widget)
        run: cd frontend/threadly-widget && pnpm test:unit --coverage

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./frontend/*/coverage/coverage-final.json
          flags: typescript
          fail_ci_if_error: false

  # E2E Tests
  e2e-tests:
    name: End-to-End Tests
    runs-on: ubuntu-latest
    needs: [java-tests, typescript-tests]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20.x'
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: temurin

      - run: pnpm install

      - name: Start services
        run: docker-compose -f docker-compose.test.yml up -d

      - name: Run Playwright Tests
        run: cd frontend/threadly-web && pnpm exec playwright test

      - name: Upload Playwright Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: frontend/threadly-web/playwright-report/
```

---

## 6. Running Tests Locally

### 6.1 Java Services

```bash
# Single service - unit tests only
cd services/identity-service
mvn test

# Single service - unit + integration tests
mvn verify

# All services - unit tests
mvn test -f services/pom.xml

# All services - with coverage
mvn verify -f services/pom.xml -Djacoco.skip=false
```

### 6.2 TypeScript/React

```bash
# Unit tests (threadly-web)
cd frontend/threadly-web
pnpm test

# Watch mode
pnpm test:watch

# With coverage
pnpm test:coverage

# E2E tests
pnpm exec playwright test
```

### 6.3 Widget

```bash
# Unit tests
cd frontend/threadly-widget
pnpm test

# With coverage
pnpm test:coverage
```

---

## 7. Troubleshooting & Common Issues

### Issue: TestContainer fails to start

```bash
# Ensure Docker is running
docker ps

# Check Docker logs
docker logs

# Increase timeout
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
    .waitingFor(Wait.forLogMessage(".*ready to accept connections.*", 1))
    .withStartupTimeout(Duration.ofMinutes(2));
```

### Issue: Port conflict in tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Use RANDOM_PORT to avoid conflicts
```

### Issue: Flaky tests

```java
// Use Awaitility instead of Thread.sleep()
await()
  .atMost(5, SECONDS)
  .pollDelay(100, MILLISECONDS)
  .until(() -> resourceIsReady());
```

---

## Next Steps

1. **Week 1:** Set up test infrastructure, add base classes
2. **Week 2-3:** Implement unit tests for high-priority services
3. **Week 4:** Add integration tests
4. **Week 5+:** Expand coverage, add E2E tests, performance testing

