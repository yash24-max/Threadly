"""AI completion endpoint — streams tokens via SSE with Langfuse tracing."""
from __future__ import annotations

import time
from typing import Annotated

import structlog
from fastapi import APIRouter, Depends, Header, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from uuid import UUID

from app.config import settings
from app.llm.provider import get_provider
from app.memory.context_builder import build_messages, build_system_prompt
from app.rag.query import format_context, query_kb

log = structlog.get_logger()
router = APIRouter()

# Lazy Langfuse client — only initialised when keys are configured
_langfuse = None


def _get_langfuse():
    global _langfuse
    if _langfuse is None and settings.langfuse_public_key and settings.langfuse_secret_key:
        try:
            from langfuse import Langfuse  # type: ignore
            _langfuse = Langfuse(
                public_key=settings.langfuse_public_key,
                secret_key=settings.langfuse_secret_key,
                host=settings.langfuse_host,
            )
        except ImportError:
            log.warning("langfuse.import.failed", msg="Install langfuse to enable tracing")
    return _langfuse


def _verify_secret(x_service_secret: Annotated[str, Header()] = "") -> None:
    if x_service_secret != settings.core_service_secret:
        raise HTTPException(status_code=403, detail="Invalid service secret")


class CompleteRequest(BaseModel):
    bot_id: UUID
    org_id: UUID
    conversation_id: UUID
    system_prompt: str = "You are a helpful assistant."
    user_message: str
    use_kb: bool = False
    max_tokens: int = 500
    provider: str = "auto"
    conversation_history: list[dict[str, str]] = []


@router.post("/complete")
async def complete(req: CompleteRequest, _: None = Depends(_verify_secret)) -> StreamingResponse:
    """Stream LLM tokens as text/plain with Langfuse tracing."""

    langfuse = _get_langfuse()
    trace = None
    if langfuse:
        trace = langfuse.trace(
            name="ai.complete",
            user_id=str(req.org_id),
            metadata={
                "bot_id": str(req.bot_id),
                "conversation_id": str(req.conversation_id),
                "provider": req.provider,
                "use_kb": req.use_kb,
            },
        )

    # 1. RAG retrieval
    rag_context = ""
    if req.use_kb:
        rag_start = time.monotonic()
        passages = await query_kb(str(req.bot_id), req.user_message)
        rag_context = format_context(passages)
        if trace:
            trace.span(
                name="rag.query",
                metadata={"passage_count": len(passages)},
                end_time=None,
                input=req.user_message,
                output=rag_context[:500],
            )

    # 2. Build messages
    system = build_system_prompt(req.system_prompt)
    messages = build_messages(req.conversation_history, req.user_message, rag_context)

    # 3. Stream from LLM
    provider = get_provider(req.provider)
    accumulated_tokens: list[str] = []
    start_ms = time.monotonic()

    async def token_stream():
        try:
            async for token in provider.stream_complete(
                system_prompt=system,
                messages=messages,
                max_tokens=req.max_tokens,
            ):
                accumulated_tokens.append(token)
                yield token

            # Post-stream: record generation to Langfuse
            if trace:
                full_response = "".join(accumulated_tokens)
                latency_ms = round((time.monotonic() - start_ms) * 1000)
                trace.generation(
                    name="llm.generate",
                    model=settings.anthropic_model if req.provider != "openai" else settings.openai_model,
                    input=[{"role": "system", "content": system}] + messages,
                    output=full_response,
                    usage={"total_tokens": sum(len(t.split()) for t in accumulated_tokens)},
                    metadata={"latency_ms": latency_ms},
                )
                langfuse.flush()
        except Exception as e:
            log.error("ai.complete.error", error=str(e))
            if trace:
                trace.update(metadata={"error": str(e)})
                langfuse.flush()
            yield "\n[error: AI service unavailable]"

    return StreamingResponse(token_stream(), media_type="text/plain")
