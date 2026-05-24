"""Conversation endpoints combining RAG and LLM."""

import json
from typing import Optional

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field

from app.config import settings
from app.llm import get_provider
from app.logger import get_logger
from app.memory import MemoryBuilder
from app.rag import RAGPipeline

logger = get_logger(__name__)

router = APIRouter(prefix="/chat", tags=["conversation"])


class RAGReplyRequest(BaseModel):
    """RAG-powered reply request."""

    query: str = Field(..., description="User query", min_length=1)
    bot_id: str = Field(..., description="Bot/workspace ID")
    session_id: str = Field(..., description="Conversation session ID")
    provider: Optional[str] = Field(default=None, description="LLM provider")
    temperature: float = Field(default=0.7, ge=0.0, le=2.0)
    max_tokens: int = Field(default=2000, ge=1, le=4000)
    include_kb: bool = Field(default=True, description="Include KB context")


class MemoryBuildRequest(BaseModel):
    """Build conversation memory request."""

    bot_id: str = Field(..., description="Bot/workspace ID")
    session_id: str = Field(..., description="Conversation session ID")
    recent_turns: int = Field(default=5, ge=1, le=20)


class MemoryBuildResponse(BaseModel):
    """Memory context response."""

    system_prompt: str = Field(..., description="System prompt for LLM")
    context_text: str = Field(..., description="Full context string")
    token_count: int = Field(..., description="Estimated token count")


@router.post("/memory/build", response_model=MemoryBuildResponse)
async def build_memory(request: MemoryBuildRequest) -> MemoryBuildResponse:
    """Assemble conversation memory for context.

    Args:
        request: Memory build request

    Returns:
        System prompt and context
    """
    try:
        logger.info(
            f"Building memory for bot {request.bot_id}, "
            f"session {request.session_id}"
        )

        memory_builder = MemoryBuilder()
        context = memory_builder.build_context(
            bot_id=request.bot_id,
            session_id=request.session_id,
            recent_turns=request.recent_turns,
        )
        memory_builder.close()

        return MemoryBuildResponse(
            system_prompt=context.system_prompt,
            context_text=context.context_text,
            token_count=context.token_count,
        )

    except Exception as e:
        logger.error(f"Error building memory: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )


@router.post("/rag-reply", response_class=None)
async def rag_reply(request: RAGReplyRequest) -> None:
    """Stream RAG-powered reply with citations.

    Args:
        request: RAG reply request

    Yields:
        Server-sent events with tokens and metadata
    """
    from fastapi.responses import StreamingResponse

    provider_name = request.provider or settings.DEFAULT_LLM_PROVIDER

    try:
        logger.info(
            f"RAG reply request: provider={provider_name}, "
            f"bot={request.bot_id}, query={request.query[:50]}..."
        )

        # Validate provider
        if provider_name not in ["anthropic", "openai", "gemini"]:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid provider: {provider_name}",
            )

        def generate() -> None:
            """Generate streaming responses with RAG context."""
            try:
                # Get KB context
                passages = []
                citations = {}
                if request.include_kb:
                    rag = RAGPipeline()
                    passages = rag.search(
                        query=request.query,
                        bot_id=request.bot_id,
                        top_k=settings.KB_SEARCH_TOP_K,
                    )
                    # Build citations
                    for p in passages[:3]:
                        citations[p["doc_id"]] = p["source"]

                # Build memory context
                memory_builder = MemoryBuilder()
                memory_context = memory_builder.build_context(
                    bot_id=request.bot_id,
                    session_id=request.session_id,
                    kb_passages=[p["passage"] for p in passages[:3]],
                )
                memory_builder.close()

                # Build system prompt with context
                system_prompt = memory_context.system_prompt

                # Stream completion
                provider = get_provider(provider_name)
                token_count = 0
                for token in provider.complete(
                    prompt=request.query,
                    system=system_prompt,
                    temperature=request.temperature,
                    max_tokens=request.max_tokens,
                ):
                    token_count += 1
                    data = {"token": token, "index": token_count}
                    yield f"data: {json.dumps(data)}\n\n"

                # Send completion with citations
                completion_data = {
                    "done": True,
                    "total_tokens": token_count,
                    "citations": citations,
                }
                yield f"data: {json.dumps(completion_data)}\n\n"

            except Exception as e:
                logger.error(f"Error in RAG reply stream: {e}")
                yield f"data: {json.dumps({'error': str(e)})}\n\n"

        return StreamingResponse(generate(), media_type="text/event-stream")

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in RAG reply: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )
