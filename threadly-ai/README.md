# threadly-ai

Python FastAPI AI sidecar for Threadly.

## Responsibilities
- LLM completions (Anthropic primary, OpenAI fallback) with streaming
- Document ingestion → chunking → embedding → Qdrant
- RAG query: retrieve + format context for LLM
- Conversation memory management

## Run locally

```bash
uv run uvicorn app.main:app --reload --port 8081

# Or via Make:
make ai-run
```

API docs: http://localhost:8081/docs
