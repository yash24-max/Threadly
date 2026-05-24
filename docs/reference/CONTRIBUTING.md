# Contributing Guide

**Last Updated**: 2025-05-24

---

## Development Workflow

### 1. Setup Local Environment

```bash
# Clone repository
git clone https://github.com/threadly/threadly.git
cd threadly

# Install dependencies
make install  # Runs: docker-compose up, npm install, mvn install

# Verify setup
make health  # Checks all services are running
```

### 2. Create Feature Branch

```bash
# Create branch from latest main
git checkout main
git pull origin main
git checkout -b feat/your-feature-name

# Or use conventional commits
git checkout -b fix/bug-id-description
git checkout -b docs/add-api-reference
git checkout -b refactor/code-cleanup
```

### 3. Make Changes

**Code Style**:
- **Java**: Follow Spring Boot conventions, 2-space indent
- **TypeScript**: Use ESLint config (`npm run lint`)
- **Python**: Follow PEP 8 (`black` formatter)

**Commit Messages** (Conventional Commits):
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Example**:
```
feat(identity-service): add user profile endpoint

- Add GET /users/{id}/profile endpoint
- Include user preferences and settings
- Add integration test

Closes #123
```

### 4. Test Changes

```bash
# Unit tests
mvn test -f services/identity-service

# Integration tests
mvn verify -DskipITs=false

# TypeScript tests
npm test --workspace=frontend/threadly-web

# Formatting
npm run lint --workspace=frontend/threadly-web
black --check services/threadly-ai
```

### 5. Push & Create PR

```bash
git push origin feat/your-feature-name

# Create PR on GitHub with:
# - Clear title and description
# - Link to related issues
# - Test results
```

### 6. Code Review

- Address reviewer feedback
- Ensure CI/CD passes
- Merge when approved

---

## Service Development Guide

### Adding a New Endpoint (Spring Boot)

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping("/{userId}")
  public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
    return ResponseEntity.ok(userService.findById(userId));
  }

  @PostMapping
  public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest req) {
    return ResponseEntity.status(CREATED).body(userService.create(req));
  }
}
```

### Publishing a Kafka Event

```java
@Service
public class UserEventPublisher {
  private final KafkaTemplate<String, UserEvent> kafkaTemplate;

  public void publishUserCreated(User user) {
    UserEvent event = UserEvent.builder()
      .eventId("evt_" + UUID.randomUUID())
      .eventType("user.created")
      .aggregateId(user.getId())
      .timestamp(Instant.now())
      .data(user)
      .build();

    kafkaTemplate.send("user-events", user.getOrgId(), event);
  }
}
```

### Consuming Kafka Events

```java
@Service
public class UserEventConsumer {
  @KafkaListener(topics = "user-events", groupId = "workspace-service-consumer")
  public void handleUserEvent(UserEvent event) {
    if ("user.created".equals(event.getEventType())) {
      // Create user in workspace service
    }
  }
}
```

### Frontend (React) Integration

```typescript
// Use generated API hooks
import { useListUsers, useCreateUser } from '@/generated/api/users';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export function UserList() {
  const { data, isLoading } = useListUsers(1, 20);
  const queryClient = useQueryClient();
  const { mutate: createUser } = useCreateUser();

  return (
    <div>
      {data?.data.map(user => <UserCard key={user.id} user={user} />)}
    </div>
  );
}
```

---

## Testing Requirements

### Unit Tests (Minimum 80% Coverage)

```bash
mvn test
mvn jacoco:report
# Check target/site/jacoco/index.html
```

### Integration Tests

```bash
# Must test cross-service communication
mvn verify -DskipITs=false

# Example: Bot creation triggers flow-service read
@SpringBootTest
class BotCreationIntegrationTest {
  @Test
  void creatingBotShouldBeVisibleInFlowService() {
    String botId = workspaceApi().post("/bots", newBot)
      .extract().path("id");
    
    flowApi().get("/bots/{botId}", botId)
      .then().statusCode(200);
  }
}
```

### E2E Tests

```bash
# Playwright browser tests
npm run test:e2e

# Example: Create bot flow
test('user can create and publish a bot', async ({ page }) => {
  await page.goto('http://localhost:3000/bots/new');
  await page.fill('[data-testid="bot-name"]', 'Test Bot');
  await page.click('[data-testid="btn-create"]');
  await expect(page).toHaveURL(/\/bots\/\d+\/edit/);
});
```

---

## Documentation

### Update API Docs When Adding Endpoints

1. Update OpenAPI spec: `services/*/openapi.yaml`
2. Regenerate client code: `npm run codegen`
3. Update REST endpoints doc: `docs/api/rest-endpoints.md`

### Update Architecture Docs

- Changes to system design → Update `docs/architecture/03-architecture.md`
- New microservice → Update `docs/architecture/18-microservices-architecture.md`
- Deployment changes → Update `docs/migration/DEPLOYMENT_PLAN.md`

---

## Deployment

### For Staging

```bash
# Merge to main triggers staging deploy
git merge feat/your-feature --no-ff
git push origin main

# Verify in GitHub Actions
# Check: https://github.com/threadly/threadly/actions

# Test in staging
curl https://api-staging.threadly.io/api/v1/health
```

### For Production

```bash
# Create release from main
git tag -a v1.2.3 -m "Release version 1.2.3"
git push origin v1.2.3

# Triggers production deploy with:
# - Database migrations
# - Service health checks
# - Gradual traffic rollout
# - Automated rollback on failure
```

---

## Troubleshooting

### Service Won't Start

```bash
# Check logs
docker logs threadly-identity-service

# Verify configuration
curl http://localhost:3001/actuator/env

# Check database connection
docker exec postgres psql -U threadly -c "SELECT 1"
```

### Test Failures

```bash
# Run single test
mvn test -Dtest=UserControllerTest#testGetUser

# Debug mode
mvn -Dorg.slf4j.simpleLogger.defaultLogLevel=debug test

# Run with coverage report
mvn jacoco:report
open target/site/jacoco/index.html
```

### Migration Issues

```bash
# Check current Flyway version
docker exec postgres psql -U threadly -c "SELECT * FROM flyway_schema_history"

# Rollback migration (manual)
docker exec postgres psql -U threadly -c "
  DELETE FROM flyway_schema_history 
  WHERE script = 'V10__add_new_column.sql'
"

# Rerun migrations
mvn flyway:migrate -Ddb.url=jdbc:postgresql://localhost:5432/threadly
```

---

## Pull Request Checklist

- [ ] Branch name follows `type/description` format
- [ ] Commit messages are clear and descriptive
- [ ] Code follows project style guidelines
- [ ] Added/updated tests (min 80% coverage)
- [ ] All tests pass locally (`make test`)
- [ ] Updated relevant documentation
- [ ] No merge conflicts
- [ ] PR description explains what and why
- [ ] Screenshots/GIFs if UI changes
- [ ] Linked related issues

---

## Release Process

### Version Numbering (Semantic Versioning)

```
MAJOR.MINOR.PATCH
1.2.3

- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes
```

### Release Steps

```bash
# 1. Create release branch
git checkout -b release/v1.2.3

# 2. Update version
# In pom.xml: <version>1.2.3</version>
# In package.json: "version": "1.2.3"

# 3. Update CHANGELOG.md

# 4. Create PR, get approval, merge

# 5. Tag release
git tag -a v1.2.3 -m "Release v1.2.3"
git push origin v1.2.3

# 6. Create GitHub Release
# https://github.com/threadly/threadly/releases/new
```

---

## Performance Best Practices

### Database Queries

```java
// ❌ Bad: N+1 query problem
List<User> users = userRepository.findAll();
for (User u : users) {
  u.getOrganization().getName();  // Extra query per user
}

// ✅ Good: Use JOIN FETCH
List<User> users = userRepository.findAllWithOrganization();
```

### Caching

```java
// ✅ Cache expensive operations
@Cacheable(value = "users", key = "#id")
public User findById(String id) {
  return userRepository.findById(id);
}

// Invalidate on update
@CacheEvict(value = "users", key = "#id")
public User update(String id, UserDTO dto) {
  return userRepository.save(...);
}
```

### Pagination

```java
// ✅ Always paginate list endpoints
Pageable pageable = PageRequest.of(0, 20);
Page<User> page = userRepository.findAll(pageable);
```

---

## Code Review Checklist

- [ ] Code is readable and self-documenting
- [ ] No hardcoded values (use config)
- [ ] Error handling is complete
- [ ] Security best practices followed
- [ ] Performance implications considered
- [ ] Tests are comprehensive
- [ ] Documentation is updated
- [ ] Follows project conventions

---

## Additional Resources

- [REST API Endpoints](../api/rest-endpoints.md)
- [Kafka Topics](../api/kafka-topics.md)
- [Development Setup](../architecture/10-dev-setup.md)
- [Security Guidelines](./SECURITY.md)
- [Troubleshooting](./TROUBLESHOOTING.md)

