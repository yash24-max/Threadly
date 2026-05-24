"""AI routes — streaming completion, summarize, suggest-replies, extract-entities, classify-intent."""
from __future__ import annotations

import time
from typing import Annotated, Any
from uuid import UUID

import structlog
from fastapi import APIRouter, Depends, Header, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from app.config import settings
from app.llm.provider import LLMMessage, get_chain
from app.memory.context_builder import build_messages, build_system_prompt
from app.rag.query import (
    format_context_with_citations,
    hybrid_query,
)

log = structlog.get_logger()
router = APIRouter()

# ---------------------------------------------------------------------------
# Lazy Langfuse client
# ---------------------------------------------------------------------------
_langfuse: Any = None


def _get_langfuse() -> Any:
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


# ---------------------------------------------------------------------------
# Pydantic request / response models
# ---------------------------------------------------------------------------


class CompleteRequest(BaseModel):
    bot_id: UUID
    org_id: UUID
    conversation_id: UUID
    system_prompt: str = "You are a helpful assistant."
    user_message: str
    use_kb: bool = False
    use_rerank: bool = False
    max_tokens: int = 500
    provider: str = "auto"
    conversation_history: list[dict[str, str]] = []


class Citation(BaseModel):
    index: int
    document_name: str
    page_number: int | None
    passage_text: str


class SummarizeRequest(BaseModel):
    conversation_id: str
    messages: list[dict[str, str]]


class SummarizeResponse(BaseModel):
    summary: str


class SuggestRepliesRequest(BaseModel):
    conversation_id: str
    messages: list[dict[str, str]]
    num_suggestions: int = 3


class SuggestRepliesResponse(BaseModel):
    suggestions: list[str]


class ExtractEntitiesRequest(BaseModel):
    text: str
    entity_types: list[str]  # e.g. ["email", "phone", "name", "date"]


class ExtractEntitiesResponse(BaseModel):
    entities: dict[str, str | list[str]]


class ClassifyIntentRequest(BaseModel):
    text: str
    intents: list[str]


class ClassifyIntentResponse(BaseModel):
    intent: str
    confidence: float


# ---------------------------------------------------------------------------
# POST /complete  (streaming)
# ---------------------------------------------------------------------------


@router.post("/complete")
async def complete(
    req: CompleteRequest, _: None = Depends(_verify_secret)
) -> StreamingResponse:
    """Stream LLM tokens as text/plain with Langfuse tracing and citations."""

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
    citations: list[Citation] = []
    rag_passages_count = 0

    if req.use_kb:
        passages = await hybrid_query(
            str(req.bot_id),
            req.user_message,
            top_k=settings.rag_top_k,
            use_rerank=req.use_rerank,
        )
        rag_passages_count = len(passages)
        if passages:
            rag_context, citation_dicts = format_context_with_citations(passages)
            citations = [Citation(**c) for c in citation_dicts]
        if trace:
            trace.span(
                name="rag.query",
                metadata={"passage_count": rag_passages_count},
                input=req.user_message,
                output=rag_context[:500],
            )

    # 2. Build messages
    system = build_system_prompt(req.system_prompt)
    messages = build_messages(req.conversation_history, req.user_message, rag_context)

    # 3. Stream from LLM with fallback
    chain = get_chain(req.provider)
    accumulated_tokens: list[str] = []
    start_ms = time.monotonic()

    llm_msgs = [LLMMessage(role="system", content=system)] + [
        LLMMessage(role=m["role"], content=m["content"]) for m in messages
    ]
    model_name = chain.primary.model_name

    async def token_stream() -> Any:
        nonlocal model_name
        try:
            async for token in chain.stream_with_fallback(
                llm_msgs,
                max_tokens=req.max_tokens,
            ):
                accumulated_tokens.append(token)
                yield token

            # Post-stream: record generation to Langfuse
            if trace:
                full_response = "".join(accumulated_tokens)
                latency_ms = round((time.monotonic() - start_ms) * 1000)
                # Approximate token count from word split
                approx_input = sum(len(m.content.split()) for m in llm_msgs)
                approx_output = len(full_response.split())
                trace.generation(
                    name="llm.generate",
                    model=model_name,
                    input=[{"role": m.role, "content": m.content} for m in llm_msgs],
                    output=full_response,
                    usage={
                        "input": approx_input,
                        "output": approx_output,
                    },
                    metadata={
                        "latency_ms": latency_ms,
                        "rag_passages_count": rag_passages_count,
                        "bot_id": str(req.bot_id),
                        "conversation_id": str(req.conversation_id),
                    },
                )
                langfuse.flush()
        except Exception as e:
            log.error("ai.complete.error", error=str(e))
            if trace:
                trace.update(metadata={"error": str(e)})
                langfuse.flush()
            yield "\n[error: AI service unavailable]"

    return StreamingResponse(token_stream(), media_type="text/plain")


# ---------------------------------------------------------------------------
# POST /summarize
# ---------------------------------------------------------------------------


@router.post("/summarize", response_model=SummarizeResponse)
async def summarize(
    req: SummarizeRequest, _: None = Depends(_verify_secret)
) -> SummarizeResponse:
    """Produce a 2-3 sentence summary of a conversation."""
    langfuse = _get_langfuse()

    convo_text = "\n".join(
        f"{m['role'].upper()}: {m['content']}" for m in req.messages
    )
    prompt = (
        "Please summarize the following conversation in 2-3 concise sentences. "
        "Focus on the main topic and outcome.\n\n"
        f"{convo_text}"
    )

    llm_msgs = [
        LLMMessage(role="system", content="You are a helpful assistant that summarizes conversations."),
        LLMMessage(role="user", content=prompt),
    ]

    chain = get_chain()
    start = time.monotonic()
    result = await chain.complete_with_fallback(llm_msgs, max_tokens=200, temperature=0.3)

    if langfuse:
        trace = langfuse.trace(
            name="ai.summarize",
            metadata={"conversation_id": req.conversation_id},
        )
        trace.generation(
            name="llm.summarize",
            model=result.model,
            input=[m.model_dump() for m in llm_msgs],
            output=result.content,
            usage={"input": result.input_tokens, "output": result.output_tokens},
            metadata={
                "cost_usd": result.cost_usd,
                "latency_ms": round((time.monotonic() - start) * 1000),
                "conversation_id": req.conversation_id,
            },
        )
        langfuse.flush()

    return SummarizeResponse(summary=result.content.strip())


# ---------------------------------------------------------------------------
# POST /suggest-replies
# ---------------------------------------------------------------------------


@router.post("/suggest-replies", response_model=SuggestRepliesResponse)
async def suggest_replies(
    req: SuggestRepliesRequest, _: None = Depends(_verify_secret)
) -> SuggestRepliesResponse:
    """Generate quick reply suggestions for a human agent."""
    langfuse = _get_langfuse()

    convo_text = "\n".join(
        f"{m['role'].upper()}: {m['content']}" for m in req.messages[-10:]
    )
    prompt = (
        f"You are assisting a customer support agent. "
        f"Based on the following conversation, suggest {req.num_suggestions} short, "
        f"natural reply options the agent could send next. "
        f"Return ONLY a JSON array of strings, nothing else.\n\n"
        f"Conversation:\n{convo_text}"
    )

    llm_msgs = [
        LLMMessage(role="system", content="You are a customer support assistant that generates reply suggestions."),
        LLMMessage(role="user", content=prompt),
    ]

    chain = get_chain()
    start = time.monotonic()
    result = await chain.complete_with_fallback(llm_msgs, max_tokens=300, temperature=0.7)

    # Parse JSON array from response
    import json
    import re

    suggestions: list[str] = []
    try:
        raw = result.content.strip()
        # Extract JSON array even if wrapped in markdown code blocks
        match = re.search(r"\[.*\]", raw, re.DOTALL)
        if match:
            suggestions = json.loads(match.group())
        else:
            suggestions = json.loads(raw)
    except (json.JSONDecodeError, ValueError):
        # Fallback: split by newlines
        suggestions = [
            line.lstrip("0123456789.-) ").strip()
            for line in result.content.splitlines()
            if line.strip()
        ][: req.num_suggestions]

    if langfuse:
        trace = langfuse.trace(
            name="ai.suggest_replies",
            metadata={"conversation_id": req.conversation_id},
        )
        trace.generation(
            name="llm.suggest_replies",
            model=result.model,
            input=[m.model_dump() for m in llm_msgs],
            output=result.content,
            usage={"input": result.input_tokens, "output": result.output_tokens},
            metadata={
                "cost_usd": result.cost_usd,
                "latency_ms": round((time.monotonic() - start) * 1000),
                "conversation_id": req.conversation_id,
            },
        )
        langfuse.flush()

    return SuggestRepliesResponse(suggestions=suggestions[: req.num_suggestions])


# ---------------------------------------------------------------------------
# POST /extract-entities
# ---------------------------------------------------------------------------


@router.post("/extract-entities", response_model=ExtractEntitiesResponse)
async def extract_entities(
    req: ExtractEntitiesRequest, _: None = Depends(_verify_secret)
) -> ExtractEntitiesResponse:
    """Extract structured entities from text."""
    langfuse = _get_langfuse()

    entity_list = ", ".join(req.entity_types)
    prompt = (
        f"Extract the following entity types from the text: {entity_list}.\n"
        f"Return ONLY a valid JSON object where keys are entity type names and values are "
        f"either a string (single value) or an array of strings (multiple values). "
        f"If an entity is not found, omit the key.\n\n"
        f"Text: {req.text}"
    )

    llm_msgs = [
        LLMMessage(role="system", content="You are an information extraction assistant. Return only valid JSON."),
        LLMMessage(role="user", content=prompt),
    ]

    chain = get_chain()
    start = time.monotonic()
    result = await chain.complete_with_fallback(llm_msgs, max_tokens=500, temperature=0.0)

    import json
    import re

    entities: dict[str, str | list[str]] = {}
    try:
        raw = result.content.strip()
        match = re.search(r"\{.*\}", raw, re.DOTALL)
        if match:
            entities = json.loads(match.group())
        else:
            entities = json.loads(raw)
    except (json.JSONDecodeError, ValueError):
        log.warning("extract_entities.parse.failed", raw=result.content[:200])

    if langfuse:
        trace = langfuse.trace(name="ai.extract_entities")
        trace.generation(
            name="llm.extract_entities",
            model=result.model,
            input=[m.model_dump() for m in llm_msgs],
            output=result.content,
            usage={"input": result.input_tokens, "output": result.output_tokens},
            metadata={
                "cost_usd": result.cost_usd,
                "latency_ms": round((time.monotonic() - start) * 1000),
                "entity_types": req.entity_types,
            },
        )
        langfuse.flush()

    return ExtractEntitiesResponse(entities=entities)


# ---------------------------------------------------------------------------
# POST /classify-intent
# ---------------------------------------------------------------------------


@router.post("/classify-intent", response_model=ClassifyIntentResponse)
async def classify_intent(
    req: ClassifyIntentRequest, _: None = Depends(_verify_secret)
) -> ClassifyIntentResponse:
    """Classify text into one of the provided intent categories."""
    langfuse = _get_langfuse()

    intents_str = "\n".join(f"- {i}" for i in req.intents)
    prompt = (
        f"Classify the following text into exactly one of these intents:\n"
        f"{intents_str}\n\n"
        f"Return ONLY a valid JSON object with keys \"intent\" (string) and "
        f"\"confidence\" (float 0.0-1.0). No other text.\n\n"
        f"Text: {req.text}"
    )

    llm_msgs = [
        LLMMessage(role="system", content="You are an intent classification assistant. Return only valid JSON."),
        LLMMessage(role="user", content=prompt),
    ]

    chain = get_chain()
    start = time.monotonic()
    result = await chain.complete_with_fallback(llm_msgs, max_tokens=100, temperature=0.0)

    import json
    import re

    intent = req.intents[0] if req.intents else "unknown"
    confidence = 0.0
    try:
        raw = result.content.strip()
        match = re.search(r"\{.*\}", raw, re.DOTALL)
        parsed = json.loads(match.group() if match else raw)
        intent = str(parsed.get("intent", intent))
        confidence = float(parsed.get("confidence", 0.0))
    except (json.JSONDecodeError, ValueError, KeyError):
        log.warning("classify_intent.parse.failed", raw=result.content[:200])

    # Clamp confidence to [0, 1]
    confidence = max(0.0, min(1.0, confidence))

    if langfuse:
        trace = langfuse.trace(name="ai.classify_intent")
        trace.generation(
            name="llm.classify_intent",
            model=result.model,
            input=[m.model_dump() for m in llm_msgs],
            output=result.content,
            usage={"input": result.input_tokens, "output": result.output_tokens},
            metadata={
                "cost_usd": result.cost_usd,
                "latency_ms": round((time.monotonic() - start) * 1000),
                "intents": req.intents,
            },
        )
        langfuse.flush()

    return ClassifyIntentResponse(intent=intent, confidence=confidence)
