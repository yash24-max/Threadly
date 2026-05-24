"""LLM completion and classification endpoints."""

import json
from typing import Optional

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field

from app.config import settings
from app.llm import get_provider
from app.logger import get_logger

logger = get_logger(__name__)

router = APIRouter(prefix="/llm", tags=["llm"])


class CompleteRequest(BaseModel):
    """Text completion request."""

    prompt: str = Field(..., description="User prompt", min_length=1)
    provider: Optional[str] = Field(
        default=None,
        description="LLM provider (anthropic, openai, gemini)",
    )
    system_prompt: Optional[str] = Field(default=None, description="System prompt")
    temperature: float = Field(default=0.7, ge=0.0, le=2.0)
    max_tokens: int = Field(default=2000, ge=1, le=4000)


class ClassifyRequest(BaseModel):
    """Text classification request."""

    text: str = Field(..., description="Text to classify", min_length=1)
    categories: list[str] = Field(
        ...,
        description="List of categories to classify into",
        min_items=2,
    )
    provider: Optional[str] = Field(default=None, description="LLM provider")
    system_prompt: Optional[str] = Field(default=None, description="System prompt")


class ClassifyResponse(BaseModel):
    """Classification response."""

    category: str = Field(..., description="Classified category")
    confidence: float = Field(..., description="Confidence score 0-1", ge=0.0, le=1.0)


@router.post("/complete", response_class=None)
async def complete(request: CompleteRequest) -> None:
    """Stream text completion.

    Args:
        request: Completion request

    Yields:
        Server-sent events with tokens
    """
    from fastapi.responses import StreamingResponse

    provider_name = request.provider or settings.DEFAULT_LLM_PROVIDER

    try:
        logger.info(f"Completion request: provider={provider_name}, prompt_len={len(request.prompt)}")

        # Validate provider
        if provider_name not in ["anthropic", "openai", "gemini"]:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid provider: {provider_name}",
            )

        provider = get_provider(provider_name)

        def generate() -> None:
            """Generate streaming responses."""
            try:
                token_count = 0
                for token in provider.complete(
                    prompt=request.prompt,
                    system=request.system_prompt,
                    temperature=request.temperature,
                    max_tokens=request.max_tokens,
                ):
                    token_count += 1
                    data = {"token": token, "index": token_count}
                    yield f"data: {json.dumps(data)}\n\n"

                # Send completion event
                yield f"data: {json.dumps({'done': True, 'total_tokens': token_count})}\n\n"
            except Exception as e:
                logger.error(f"Error in completion stream: {e}")
                yield f"data: {json.dumps({'error': str(e)})}\n\n"

        return StreamingResponse(generate(), media_type="text/event-stream")

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in completion: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )


@router.post("/classify", response_model=ClassifyResponse)
async def classify(request: ClassifyRequest) -> ClassifyResponse:
    """Classify text into categories.

    Args:
        request: Classification request

    Returns:
        Classification result
    """
    provider_name = request.provider or settings.DEFAULT_LLM_PROVIDER

    try:
        logger.info(
            f"Classification request: provider={provider_name}, "
            f"categories={request.categories}, text_len={len(request.text)}"
        )

        if provider_name not in ["anthropic", "openai", "gemini"]:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid provider: {provider_name}",
            )

        provider = get_provider(provider_name)
        category, confidence = provider.classify(
            text=request.text,
            categories=request.categories,
            system=request.system_prompt,
        )

        logger.info(f"Classification result: {category} ({confidence:.2f})")

        return ClassifyResponse(category=category, confidence=confidence)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in classification: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )
