# Flow Specification

## Overview
A flow is a directed graph stored as JSON in the `flows` table. The runtime engine traverses nodes from the `start` node, executing each node's logic.

## Flow JSON schema

```json
{
  "version": 1,
  "nodes": [NodeObject],
  "edges": [EdgeObject]
}
```

## Node object

```json
{
  "id": "string (unique within flow)",
  "type": "NodeType",
  "position": { "x": 100, "y": 200 },
  "data": { /* node-type-specific config */ }
}
```

## Edge object

```json
{
  "id": "string",
  "source": "nodeId",
  "sourceHandle": "default | true | false | custom",
  "target": "nodeId"
}
```

## Node types

### `start`
Entry point. Every flow has exactly one.
```json
{ "type": "start", "data": {} }
```

### `message`
Send a text message to the visitor.
```json
{
  "type": "message",
  "data": {
    "text": "Hello! How can I help you today? {{visitor.name}}",
    "delay_ms": 500
  }
}
```

### `question`
Send a message and wait for the visitor's response. Stores reply in `{{session.last_input}}`.
```json
{
  "type": "question",
  "data": {
    "text": "What is your email address?",
    "variable": "visitor.email",
    "input_type": "text | email | phone | number"
  }
}
```

### `condition`
Branch based on a variable. Produces `true` and `false` edge handles.
```json
{
  "type": "condition",
  "data": {
    "variable": "visitor.email",
    "operator": "exists | equals | contains | gt | lt",
    "value": "optional comparison value"
  }
}
```

### `ai_reply`
Call the AI with a prompt. Streams tokens back to the visitor.
```json
{
  "type": "ai_reply",
  "data": {
    "system_prompt": "You are a helpful support agent for {{bot.name}}.",
    "use_kb": true,
    "max_tokens": 500,
    "temperature": 0.7,
    "provider": "anthropic | openai | auto"
  }
}
```

### `api_call`
Call an external HTTP endpoint.
```json
{
  "type": "api_call",
  "data": {
    "method": "GET | POST | PUT",
    "url": "https://api.example.com/endpoint",
    "headers": { "X-Key": "{{bot.api_key}}" },
    "body": { "email": "{{visitor.email}}" },
    "response_variable": "session.api_response",
    "timeout_ms": 5000
  }
}
```

### `set_variable`
Set a session variable.
```json
{
  "type": "set_variable",
  "data": {
    "variable": "session.intent",
    "value": "billing_query"
  }
}
```

### `handoff`
Trigger human handoff.
```json
{
  "type": "handoff",
  "data": {
    "message": "Connecting you to our team. Please wait a moment.",
    "department": "support | sales | billing"
  }
}
```

### `end`
Terminate the flow session.
```json
{
  "type": "end",
  "data": {
    "message": "Thanks for chatting! Have a great day."
  }
}
```

## Variable system
- `{{visitor.name}}`, `{{visitor.email}}`, `{{visitor.phone}}` — collected via question nodes
- `{{session.last_input}}` — last message sent by visitor
- `{{bot.name}}`, `{{bot.id}}` — current bot info
- `{{session.<key>}}` — arbitrary session variables set via `set_variable`

## Runtime semantics
1. Start at `start` node.
2. Execute current node (send message, evaluate condition, call AI, etc.).
3. Follow outgoing edge(s). For `condition`, follow `true` or `false` edge.
4. If `question` node — pause execution, store state in Redis, wait for next message.
5. On next message — resume from paused state.
6. On `handoff` — pause AI, notify agents via `agent:{agentId}` channel.
7. On `end` — mark session as complete, conversation as closed.
