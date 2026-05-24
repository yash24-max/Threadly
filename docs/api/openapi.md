# OpenAPI Specification & Code Generation

**Last Updated**: 2025-05-24  
**OpenAPI Version**: 3.0.0  
**Specification Format**: YAML

---

## Table of Contents

1. [Overview](#overview)
2. [Accessing the OpenAPI Spec](#accessing-the-openapi-spec)
3. [Spec Structure](#spec-structure)
4. [Code Generation with Orval](#code-generation-with-orval)
5. [Client Library Setup](#client-library-setup)
6. [Example Usage](#example-usage)

---

## Overview

Threadly provides a complete OpenAPI 3.0.0 specification for all 9 microservices. This allows:

- **Automatic SDK generation** — Generate TypeScript, Python, Go, Java clients
- **Interactive API docs** — Swagger UI, ReDoc
- **API contract validation** — CI/CD integration
- **Documentation sync** — Auto-update docs from code

---

## Accessing the OpenAPI Spec

### Interactive Swagger UI

```
http://localhost:8080/swagger-ui.html
http://localhost:3001/swagger-ui.html  (Identity Service)
http://localhost:3002/swagger-ui.html  (Workspace Service)
... (port 3001-3009)
```

### OpenAPI YAML Files

Located in each service:

```
services/
├── identity-service/src/main/resources/openapi.yaml
├── workspace-service/src/main/resources/openapi.yaml
├── flow-service/src/main/resources/openapi.yaml
├── runtime-service/src/main/resources/openapi.yaml
├── conversation-service/src/main/resources/openapi.yaml
├── knowledge-service/src/main/resources/openapi.yaml
├── analytics-service/src/main/resources/openapi.yaml
├── billing-service/src/main/resources/openapi.yaml
└── integration-service/src/main/resources/openapi.yaml
```

### Download Spec

```bash
# Download from running service
curl http://localhost:3002/v3/api-docs > workspace-service-openapi.json

# Or via Swagger UI
# Click "Download OpenAPI" button in Swagger UI
```

### Aggregate Spec (All Services)

```bash
# Combine all service specs (coming soon)
bash scripts/aggregate-openapi.sh > /tmp/threadly-combined-openapi.yaml
```

---

## Spec Structure

### Example Service Spec (Workspace Service)

```yaml
openapi: 3.0.0

info:
  title: Threadly Workspace Service API
  version: 1.0.0
  description: Manages bots, teams, and workspace settings
  contact:
    name: Threadly Team
    url: https://threadly.io
  license:
    name: MIT

servers:
  - url: http://localhost:3002
    description: Local development
  - url: https://api.threadly.io
    description: Production

paths:
  /api/v1/bots:
    get:
      summary: List bots in workspace
      operationId: listBots
      tags:
        - Bots
      security:
        - bearerAuth: []
      parameters:
        - name: page
          in: query
          schema:
            type: integer
          description: Page number (1-indexed)
        - name: limit
          in: query
          schema:
            type: integer
          description: Results per page (max 100)
        - name: status
          in: query
          schema:
            type: string
            enum: [active, archived]
      responses:
        '200':
          description: Bots retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BotListResponse'
        '401':
          $ref: '#/components/responses/UnauthorizedError'
        '429':
          $ref: '#/components/responses/RateLimitError'

    post:
      summary: Create new bot
      operationId: createBot
      tags:
        - Bots
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateBotRequest'
      responses:
        '201':
          description: Bot created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BotResponse'
        '400':
          $ref: '#/components/responses/BadRequestError'
        '401':
          $ref: '#/components/responses/UnauthorizedError'

  /api/v1/bots/{botId}:
    get:
      summary: Get bot details
      operationId: getBot
      tags:
        - Bots
      parameters:
        - name: botId
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Bot details retrieved
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BotResponse'
        '404':
          $ref: '#/components/responses/NotFoundError'

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT Bearer Token

  schemas:
    Bot:
      type: object
      properties:
        id:
          type: string
          description: Unique bot ID
        name:
          type: string
          description: Bot name
        description:
          type: string
        status:
          type: string
          enum: [active, archived, draft]
        org_id:
          type: string
        created_at:
          type: string
          format: date-time
        updated_at:
          type: string
          format: date-time
      required:
        - id
        - name
        - org_id

    BotResponse:
      type: object
      properties:
        data:
          $ref: '#/components/schemas/Bot'
        meta:
          type: object
          properties:
            timestamp:
              type: string
              format: date-time
            version:
              type: string

    BotListResponse:
      type: object
      properties:
        data:
          type: array
          items:
            $ref: '#/components/schemas/Bot'
        pagination:
          type: object
          properties:
            page:
              type: integer
            limit:
              type: integer
            total:
              type: integer
            pages:
              type: integer

    CreateBotRequest:
      type: object
      properties:
        name:
          type: string
          minLength: 1
          maxLength: 100
        description:
          type: string
          maxLength: 500
        initial_greeting:
          type: string
        knowledge_base_id:
          type: string
      required:
        - name

  responses:
    UnauthorizedError:
      description: Authentication required
      content:
        application/json:
          schema:
            type: object
            properties:
              error:
                type: object
                properties:
                  code:
                    type: string
                    example: UNAUTHORIZED
                  message:
                    type: string
                    example: Invalid or missing token

    BadRequestError:
      description: Request validation failed
      content:
        application/json:
          schema:
            type: object
            properties:
              error:
                type: object
                properties:
                  code:
                    type: string
                    example: INVALID_REQUEST
                  message:
                    type: string
                  details:
                    type: object

    NotFoundError:
      description: Resource not found
      content:
        application/json:
          schema:
            type: object
            properties:
              error:
                type: object
                properties:
                  code:
                    type: string
                    example: NOT_FOUND
                  message:
                    type: string

    RateLimitError:
      description: Rate limit exceeded
      content:
        application/json:
          schema:
            type: object
            properties:
              error:
                type: object
                properties:
                  code:
                    type: string
                    example: RATE_LIMITED
                  message:
                    type: string
              retryAfter:
                type: integer
                description: Seconds to wait before retrying
```

---

## Code Generation with Orval

Orval automatically generates typed API clients from OpenAPI specs.

### Setup (Frontend Project)

```bash
cd frontend/threadly-web

# 1. Install Orval
npm install -D orval

# 2. Create orval.config.ts
cat > orval.config.ts << 'EOF'
import { defineConfig } from 'orval';

export default defineConfig({
  api: {
    input: 'http://localhost:8080/v3/api-docs.yaml',
    output: {
      target: 'src/generated/api',
      client: 'react-query',
      httpClient: 'axios',
      mode: 'tags-split',
      mock: false,
    },
    hooks: {
      afterAllFilesWrite: 'prettier --write',
    },
  },
});
EOF

# 3. Generate code
npx orval --config orval.config.ts

# Output:
# Generated:
#   src/generated/api/bots.ts
#   src/generated/api/flows.ts
#   src/generated/api/conversations.ts
#   ... (one file per tag)
```

### Configuration Options

```typescript
// orval.config.ts
{
  // Use TanStack Query (React Query)
  client: 'react-query',
  
  // Axios HTTP client
  httpClient: 'axios',
  
  // Split by OpenAPI tags
  mode: 'tags-split',
  
  // Generate hooks with custom naming
  operationIdTemplate: 'use{{title}}',
  
  // Include models in separate file
  models: {
    target: 'src/generated/models',
  },
  
  // Override types (e.g., Date objects)
  override: {
    transformer: '@app/utils/custom-transformers.ts',
  },
}
```

### Generated Code Example

After running Orval:

```typescript
// src/generated/api/bots.ts (auto-generated)

import { useMutation, useQuery } from '@tanstack/react-query';
import { axios } from '@/lib/axios';

export const useListBots = (
  page?: number,
  limit?: number,
  status?: 'active' | 'archived'
) => {
  return useQuery({
    queryKey: ['listBots', { page, limit, status }],
    queryFn: async () => {
      const { data } = await axios.get('/api/v1/bots', {
        params: { page, limit, status },
      });
      return data;
    },
  });
};

export const useCreateBot = () => {
  return useMutation({
    mutationFn: async (payload: CreateBotRequest) => {
      const { data } = await axios.post('/api/v1/bots', payload);
      return data;
    },
  });
};

export const useGetBot = (botId: string) => {
  return useQuery({
    queryKey: ['getBot', botId],
    queryFn: async () => {
      const { data } = await axios.get(`/api/v1/bots/${botId}`);
      return data;
    },
  });
};
```

---

## Client Library Setup

### TypeScript/JavaScript

```bash
npm install @threadly/sdk-js @tanstack/react-query axios
```

**Usage**:
```typescript
import { useListBots, useCreateBot } from '@/generated/api/bots';

function BotList() {
  const { data, isLoading } = useListBots();
  const { mutate: createBot } = useCreateBot();

  return (
    <>
      {data?.data.map(bot => (
        <div key={bot.id}>{bot.name}</div>
      ))}
    </>
  );
}
```

### Python

```bash
pip install threadly-sdk
```

**Usage**:
```python
from threadly_sdk import ThreadlyAPI

client = ThreadlyAPI(api_key='sk_live_xyz789')

# List bots
bots = client.bots.list(page=1, limit=20)
for bot in bots.data:
    print(bot.name)

# Create bot
new_bot = client.bots.create(
    name="Support Bot",
    description="Handles support inquiries"
)
```

### Go

```bash
go get github.com/threadly/sdk-go
```

**Usage**:
```go
package main

import (
    "github.com/threadly/sdk-go"
)

func main() {
    client := threadly.New("sk_live_xyz789")
    
    // List bots
    bots, err := client.Bots.List(ctx, nil)
    
    // Create bot
    bot, err := client.Bots.Create(ctx, &threadly.CreateBotRequest{
        Name: "Support Bot",
    })
}
```

### Java

```xml
<!-- pom.xml -->
<dependency>
  <groupId>dev.threadly</groupId>
  <artifactId>sdk-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Usage**:
```java
ThreadlyClient client = new ThreadlyClient("sk_live_xyz789");

// List bots
BotListResponse bots = client.getBots()
    .page(1)
    .limit(20)
    .execute();

// Create bot
Bot bot = client.createBot(
    CreateBotRequest.builder()
        .name("Support Bot")
        .description("Handles support inquiries")
        .build()
).execute();
```

---

## Example Usage

### TypeScript (Frontend)

```typescript
// pages/bots/index.tsx
import { useListBots, useCreateBot } from '@/generated/api/bots';
import { useState } from 'react';

export default function BotsPage() {
  const { data, isLoading, refetch } = useListBots(1, 20, 'active');
  const { mutate: createBot, isPending } = useCreateBot();
  const [newBotName, setNewBotName] = useState('');

  const handleCreateBot = async () => {
    createBot({ name: newBotName }, {
      onSuccess: () => {
        setNewBotName('');
        refetch();
      },
    });
  };

  if (isLoading) return <div>Loading...</div>;

  return (
    <div>
      <h1>My Bots</h1>
      <ul>
        {data?.data.map(bot => (
          <li key={bot.id}>{bot.name}</li>
        ))}
      </ul>
      
      <input
        value={newBotName}
        onChange={(e) => setNewBotName(e.target.value)}
        placeholder="Bot name"
      />
      <button onClick={handleCreateBot} disabled={isPending}>
        Create Bot
      </button>
    </div>
  );
}
```

### Python (Backend Integration)

```python
# scripts/migrate_bots.py
from threadly_sdk import ThreadlyAPI

client = ThreadlyAPI(api_key='sk_live_xyz789')

# Fetch all bots across organizations
for page in range(1, 100):
    bots = client.bots.list(page=page, limit=100)
    
    for bot in bots.data:
        print(f"Bot: {bot.name}")
        print(f"  Status: {bot.status}")
        print(f"  Created: {bot.created_at}")
    
    if not bots.pagination.next:
        break
```

### cURL Examples

```bash
# List bots
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/bots?page=1&limit=20

# Create bot
curl -X POST http://localhost:8080/api/v1/bots \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Support Bot",
    "description": "Customer support assistant"
  }'

# Get bot details
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/bots/bot_123

# Publish bot
curl -X POST http://localhost:8080/api/v1/bots/bot_123/publish \
  -H "Authorization: Bearer $TOKEN"
```

---

## Testing Generated Code

```typescript
// bots.test.ts
import { renderHook, waitFor } from '@testing-library/react';
import { useListBots, useCreateBot } from '@/generated/api/bots';

describe('Bots API', () => {
  it('should list bots', async () => {
    const { result } = renderHook(() => useListBots(1, 20));
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.data?.data).toBeDefined();
  });

  it('should create bot', async () => {
    const { result } = renderHook(() => useCreateBot());
    
    result.current.mutate({ name: 'New Bot' });
    
    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
  });
});
```

---

## Validating Specs

### Static Analysis

```bash
# Install spectral (OpenAPI linter)
npm install -g @stoplight/spectral-cli

# Validate spec
spectral lint workspace-service-openapi.yaml

# Example output:
# 27:5  error  paths.items['/api/v1/bots/{botId}'].parameters.items[0] does not have examples  examples
# 41:7  warning  responses.items['200'].schema is missing additionalProperties: false  additionalProperties
```

### Contract Testing

```bash
# Install pact (contract testing)
npm install -D @pact-foundation/pact

# Run consumer contract tests
npm run test:contract

# Run provider contract tests
npm run test:provider
```

---

## CI/CD Integration

### Generate Code on Push

```yaml
# .github/workflows/api-codegen.yml
name: API Code Generation

on:
  push:
    paths:
      - 'services/**/src/main/resources/openapi.yaml'
      - '.github/workflows/api-codegen.yml'

jobs:
  generate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Install Orval
        run: npm install -g orval
      
      - name: Generate code
        run: npx orval --config orval.config.ts
      
      - name: Validate generated code
        run: npm run lint:generated
      
      - name: Commit changes
        run: |
          git config user.name "OpenAPI Bot"
          git commit -am "chore: regenerate API clients"
          git push
```

### Validate Spec Quality

```yaml
# .github/workflows/openapi-validate.yml
name: OpenAPI Validation

on:
  pull_request:
    paths:
      - 'services/**/openapi.yaml'

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Validate with Spectral
        run: |
          npm install -g @stoplight/spectral-cli
          spectral lint services/*/src/main/resources/openapi.yaml
      
      - name: Generate SDK (dry-run)
        run: npx orval --config orval.config.ts --dry-run
```

---

## Related Documentation

- [REST Endpoints](./rest-endpoints.md) — Full endpoint reference
- [Kafka Topics](./kafka-topics.md) — Event streaming topics
- [Contributing](../reference/CONTRIBUTING.md) — SDK development guide

