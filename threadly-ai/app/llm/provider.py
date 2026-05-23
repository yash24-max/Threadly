"""LLM provider abstraction with multi-provider support, streaming, and fallback."""
from __future__ import annotations

from abc import ABC, abstractmethod
from collections.abc import AsyncIterator
from typing import Any

import anthropic
import structlog
from openai import AsyncOpenAI
from pydantic import BaseModel

from app.config import settings

log = structlog.get_logger()

# ---------------------------------------------------------------------------
# Token cost table (USD per token)
# ---------------------------------------------------------------------------
COSTS: dict[str, dict[str, float]] = {
    "claude-3-5-haiku-20241022": {"input": 0.00000080, "output": 0.000004},
    "claude-3-5-sonnet-20241022": {"input": 0.000003, "output": 0.000015},
    "claude-sonnet-4-5": {"input": 0.000003, "output": 0.000015},
    "claude-opus-4-5": {"input": 0.000015, "output": 0.000075},
    "gpt-4o-mini": {"input": 0.00000015, "output": 0.0000006},
    "gpt-4o": {"input": 0.0000025, "output": 0.00001},
}


def _calc_cost(model: str, input_tokens: int, output_tokens: int) -> float:
    pricing = COSTS.get(model, {"input": 0.000003, "output": 0.000015})
    return pricing["input"] * input_tokens + pricing["output"] * output_tokens


# ---------------------------------------------------------------------------
# Pydantic models
# ---------------------------------------------------------------------------
class LLMMessage(BaseModel):
    role: str  # "system" | "user" | "assistant"
    content: str


class LLMResponse(BaseModel):
    content: str
    model: str
    input_tokens: int
    output_tokens: int
    cost_usd: float


# ---------------------------------------------------------------------------
# Abstract base
# ---------------------------------------------------------------------------
class LLMProvider(ABC):
    @abstractmethod
    async def complete(
        self,
        messages: list[LLMMessage],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> LLMResponse: ...

    @abstractmethod
    async def stream(
        self,
        messages: list[LLMMessage],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> AsyncIterator[str]: ...

    # Legacy-compat: routes/ai.py uses stream_complete(system_prompt, messages, ...)
    async def stream_complete(
        self,
        system_prompt: str,
        messages: list[dict[str, str]],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> AsyncIterator[str]:
        llm_msgs: list[LLMMessage] = [LLMMessage(role="system", content=system_prompt)]
        for m in messages:
            llm_msgs.append(LLMMessage(role=m["role"], content=m["content"]))
        async for token in self.stream(llm_msgs, max_tokens=max_tokens, temperature=temperature):
            yield token

    @property
    def model_name(self) -> str:
        return "unknown"


# ---------------------------------------------------------------------------
# Anthropic provider
# ---------------------------------------------------------------------------
class AnthropicProvider(LLMProvider):
    def __init__(self, model: str | None = None) -> None:
        self._client = anthropic.AsyncAnthropic(api_key=settings.anthropic_api_key)
        self._model = model or settings.anthropic_model or "claude-3-5-haiku-20241022"

    @property
    def model_name(self) -> str:
        return self._model

    async def complete(
        self,
        messages: list[LLMMessage],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> LLMResponse:
        system_parts = [m.content for m in messages if m.role == "system"]
        system_text = "\n".join(system_parts) if system_parts else None
        api_msgs = [
            {"role": m.role, "content": m.content}
            for m in messages
            if m.role != "system"
        ]

        kwargs: dict[str, Any] = dict(
            model=self._model,
            max_tokens=max_tokens,
            messages=api_msgs,
        )
        if system_text:
            kwargs["system"] = system_text
        # temperature is not supported for claude-3-5-haiku in some API versions;
        # pass it anyway — SDK ignores unsupported params gracefully
        if temperature != 1.0:
            kwargs["temperature"] = temperature

        resp = await self._client.messages.create(**kwargs)
        content = "".join(
            block.text for block in resp.content if hasattr(block, "text")
        )
        in_tok = resp.usage.input_tokens
        out_tok = resp.usage.output_tokens
        return LLMResponse(
            content=content,
            model=self._model,
            input_tokens=in_tok,
            output_tokens=out_tok,
            cost_usd=_calc_cost(self._model, in_tok, out_tok),
        )

    async def stream(
        self,
        messages: list[LLMMessage],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> AsyncIterator[str]:
        system_parts = [m.content for m in messages if m.role == "system"]
        system_text = "\n".join(system_parts) if system_parts else None
        api_msgs = [
            {"role": m.role, "content": m.content}
            for m in messages
            if m.role != "system"
        ]

        kwargs: dict[str, Any] = dict(
            model=self._model,
            max_tokens=max_tokens,
            messages=api_msgs,
        )
        if system_text:
            kwargs["system"] = system_text
        if temperature != 1.0:
            kwargs["temperature"] = temperature

        async with self._client.messages.stream(**kwargs) as stream:
            async for text in stream.text_stream:
                yield text

    async def embed(self, texts: list[str]) -> list[list[float]]:
        from app.llm.embeddings import embed_texts
        return await embed_texts(texts)


# ---------------------------------------------------------------------------
# OpenAI provider
# ---------------------------------------------------------------------------
class OpenAIProvider(LLMProvider):
    def __init__(self, model: str | None = None) -> None:
        self._client = AsyncOpenAI(api_key=settings.openai_api_key)
        self._model = model or settings.openai_model or "gpt-4o-mini"

    @property
    def model_name(self) -> str:
        return self._model

    def _build_openai_messages(
        self, messages: list[LLMMessage]
    ) -> list[dict[str, str]]:
        return [{"role": m.role, "content": m.content} for m in messages]

    async def complete(
        self,
        messages: list[LLMMessage],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> LLMResponse:
        resp = await self._client.chat.completions.create(
            model=self._model,
            messages=self._build_openai_messages(messages),  # type: ignore[arg-type]
            max_tokens=max_tokens,
            temperature=temperature,
        )
        content = resp.choices[0].message.content or ""
        in_tok = resp.usage.prompt_tokens if resp.usage else 0
        out_tok = resp.usage.completion_tokens if resp.usage else 0
        return LLMResponse(
            content=content,
            model=self._model,
            input_tokens=in_tok,
            output_tokens=out_tok,
            cost_usd=_calc_cost(self._model, in_tok, out_tok),
        )

    async def stream(
        self,
        messages: list[LLMMessage],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> AsyncIterator[str]:
        stream = await self._client.chat.completions.create(
            model=self._model,
            messages=self._build_openai_messages(messages),  # type: ignore[arg-type]
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


# ---------------------------------------------------------------------------
# Provider chain with fallback
# ---------------------------------------------------------------------------
class ProviderChain:
    """Try providers in order, falling back on rate-limit or error."""

    def __init__(self, providers: list[LLMProvider]) -> None:
        if not providers:
            raise ValueError("ProviderChain requires at least one provider")
        self.providers = providers

    async def complete_with_fallback(
        self,
        messages: list[LLMMessage],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> LLMResponse:
        last_exc: Exception | None = None
        for provider in self.providers:
            try:
                return await provider.complete(messages, max_tokens=max_tokens, temperature=temperature)
            except Exception as exc:
                log.warning(
                    "provider.complete.fallback",
                    provider=type(provider).__name__,
                    error=str(exc),
                )
                last_exc = exc
        raise RuntimeError(f"All LLM providers failed. Last error: {last_exc}") from last_exc

    async def stream_with_fallback(
        self,
        messages: list[LLMMessage],
        max_tokens: int = 500,
        temperature: float = 0.7,
    ) -> AsyncIterator[str]:
        last_exc: Exception | None = None
        for provider in self.providers:
            try:
                # Consume first token to verify the provider works before yielding
                buf: list[str] = []
                gen = provider.stream(messages, max_tokens=max_tokens, temperature=temperature)
                async for token in gen:
                    buf.append(token)
                    # Once we have at least one token, delegate remaining tokens
                    yield token
                    async for remaining in gen:
                        yield remaining
                    return
            except Exception as exc:
                log.warning(
                    "provider.stream.fallback",
                    provider=type(provider).__name__,
                    error=str(exc),
                )
                last_exc = exc
        raise RuntimeError(f"All LLM providers failed. Last error: {last_exc}") from last_exc

    @property
    def primary(self) -> LLMProvider:
        return self.providers[0]


# ---------------------------------------------------------------------------
# Factory helpers
# ---------------------------------------------------------------------------
def _build_chain() -> ProviderChain:
    providers: list[LLMProvider] = []
    if settings.anthropic_api_key:
        providers.append(AnthropicProvider())
    if settings.openai_api_key:
        providers.append(OpenAIProvider())
    if not providers:
        raise RuntimeError("No LLM provider configured. Set ANTHROPIC_API_KEY or OPENAI_API_KEY.")
    return ProviderChain(providers)


def get_provider(name: str = "auto") -> LLMProvider:
    """Return a single provider for simple usage."""
    if name == "openai":
        if not settings.openai_api_key:
            raise RuntimeError("OPENAI_API_KEY not set")
        return OpenAIProvider()
    if name == "anthropic":
        if not settings.anthropic_api_key:
            raise RuntimeError("ANTHROPIC_API_KEY not set")
        return AnthropicProvider()
    # auto: prefer anthropic, fall back to openai
    if settings.anthropic_api_key:
        return AnthropicProvider()
    if settings.openai_api_key:
        return OpenAIProvider()
    raise RuntimeError("No LLM provider configured. Set ANTHROPIC_API_KEY or OPENAI_API_KEY.")


def get_chain(name: str = "auto") -> ProviderChain:
    """Return a ProviderChain for fallback-capable usage."""
    if name == "openai":
        return ProviderChain([OpenAIProvider()])
    if name == "anthropic":
        return ProviderChain([AnthropicProvider()])
    return _build_chain()
