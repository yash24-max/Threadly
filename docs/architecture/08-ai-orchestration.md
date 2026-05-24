# AI Orchestration

## Components
- **threadly-ai** (Python/FastAPI) — LLM calls, embeddings, RAG
- **Qdrant** — vector store, one collection per bot
- **Langfuse** — LLM tracing, cost tracking, prompt management

## Prompt structure
```
[System]
You are a helpful assistant for {bot_name}.
{custom_system_prompt}

[Context — from RAG]
Relevant information from the knowledge base:
{rag_passages}
Source: {citation_1_title}, {citation_2_title}

[Conversation history]
{last_N_turns}

[Summary of earlier conversation]
{memory_summary}

[User message]
{current_message}
```

## RAG pipeline

### Ingestion
1. Document uploaded → stored in MinIO/R2
2. Outbox event fires → threadly-ai `/kb/ingest`
3. Parse: `unstructured` for HTML/DOCX, `pypdf` for PDFs
4. Chunk: `RecursiveCharacterTextSplitter` (chunk=512, overlap=64, token-aware)
5. Embed: `voyage-3-lite` (primary) or `text-embedding-3-small` (fallback)
6. Write to Qdrant collection `bot_{botId}`, payload includes `doc_id`, `chunk_index`, `text`, `title`

### Query (per AI reply)
1. Embed the visitor's latest message
2. Qdrant nearest-neighbor top 8, filter by `bot_id`
3. Cohere rerank (if enabled) → top 3
4. Return passages + citation metadata

## Memory management
- Store last 20 messages in Redis session
- On session > 20 messages → summarize older turns via a cheap LLM call (`claude-haiku`) and store summary
- Final prompt = summary + last 10 turns + RAG + current message
- Target: always under 4000 tokens for the context window

## Provider abstraction
```python
class LLMProvider(Protocol):
    async def complete(self, messages, stream, max_tokens, temperature) -> AsyncIterator[str]: ...
    async def embed(self, texts) -> list[list[float]]: ...
```
Concrete: `AnthropicProvider`, `OpenAIProvider`. Fallback order configurable per bot.

## Streaming
- threadly-ai streams tokens via SSE to threadly-core
- Core immediately publishes each token to Centrifugo channel `chat:{botId}:{visitorId}`
- Widget renders tokens as they arrive

## Cost tracking
Every LLM call records: `input_tokens`, `output_tokens`, `model`, `latency_ms`, `cost_usd` via Langfuse and stores in `messages` table. Daily rollup aggregates cost per org/bot for the dashboard.

## Eval strategy
- Golden test set: 20 Q&A pairs per bot template
- `promptfoo` runs nightly against the golden set
- LLM-as-judge scores: accuracy, relevance, hallucination rate
- Alert if accuracy drops > 5% from baseline
