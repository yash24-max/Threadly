"""LLM provider abstraction with fallback."""
from __future__ import annotations

import time
from abc import ABC, abstractmethod
from collections.abc import AsyncIterator
from typing import Any

import anthropic
import structlog
from openai import AsyncOpenAI
from tenacity import retry, stop_after_attempt, wait_exponential

from app.config import settings

log = structlog.get_logger()


class LLMProvider(ABC):
    @abstractmethod
    async def stream_complete(
        self,
        system_prompt: str,
        messages: list[dict[str, str]],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> AsyncIterator[str]:
        ...

    @abstractmethod
    async def embed(self, texts: list[str]) -> list[list[float]]:
        ...


class AnthropicProvider(LLMProvider):
    def __init__(self) -> None:
        self._client = anthropic.AsyncAnthropic(api_key=settings.anthropic_api_key)
        self._model = settings.anthropic_model

    async def stream_complete(
        self,
        system_prompt: str,
        messages: list[dict[str, str]],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> AsyncIterator[str]:
        start = time.monotonic()
        async with self._client.messages.stream(
            model=self._model,
            max_tokens=max_tokens,
            system=system_prompt,
            messages=messages,
        ) as stream:
            async for text in stream.text_stream:
                yield text
        log.info("anthropic.complete", latency_ms=int((time.monotonic() - start) * 1000))

    async def embed(self, texts: list[str]) -> list[list[float]]:
        # Anthropic doesn't provide embeddings; delegate to voyage or openai
        from app.llm.embeddings import embed_texts
        return await embed_texts(texts)


class OpenAIProvider(LLMProvider):
    def __init__(self) -> None:
        self._client = AsyncOpenAI(api_key=settings.openai_api_key)
        self._model = settings.openai_model

    async def stream_complete(
        self,
        system_prompt: str,
        messages: list[dict[str, str]],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> AsyncIterator[str]:
        all_messages = [{"role": "system", "content": system_prompt}] + messages
        stream = await self._client.chat.completions.create(
            model=self._model,
            messages=all_messages,  # type: ignore[arg-type]
            max_tokens=max_tokens,
            temperature=temperature,
            stream=True,
        )
        async for chunk in stream:
            delta = chunk.choices[0].delta.content
            if delta:
                yield delta

    async def embed(self, texts: list[str]) -> list[list[float]]:
        resp = await self._client.embeddings.create(
            model="text-embedding-3-small", input=texts
        )
        return [item.embedding for item in resp.data]


def get_provider(name: str = "auto") -> LLMProvider:
    """Return the appropriate provider, falling back if primary unavailable."""
    if name == "openai":
        return OpenAIProvider()
    if settings.anthropic_api_key:
        return AnthropicProvider()
    if settings.openai_api_key:
        return OpenAIProvider()
    raise RuntimeError("No LLM provider configured. Set ANTHROPIC_API_KEY or OPENAI_API_KEY.")
