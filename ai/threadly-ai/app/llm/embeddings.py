"""Text embedding utilities — Voyage AI primary, OpenAI fallback."""
from __future__ import annotations

import httpx
import structlog
from openai import AsyncOpenAI

from app.config import settings

log = structlog.get_logger()

EMBEDDING_DIM = 1024  # voyage-3-lite / text-embedding-3-small


async def embed_texts(texts: list[str]) -> list[list[float]]:
    """Embed a list of texts. Returns list of embedding vectors."""
    if settings.voyage_api_key:
        return await _voyage_embed(texts)
    if settings.openai_api_key:
        return await _openai_embed(texts)
    raise RuntimeError("No embedding provider configured. Set VOYAGE_API_KEY or OPENAI_API_KEY.")


async def _voyage_embed(texts: list[str]) -> list[list[float]]:
    async with httpx.AsyncClient() as client:
        resp = await client.post(
            "https://api.voyageai.com/v1/embeddings",
            headers={"Authorization": f"Bearer {settings.voyage_api_key}"},
            json={"model": "voyage-3-lite", "input": texts},
            timeout=30,
        )
        resp.raise_for_status()
        data = resp.json()
        return [item["embedding"] for item in data["data"]]


async def _openai_embed(texts: list[str]) -> list[list[float]]:
    client = AsyncOpenAI(api_key=settings.openai_api_key)
    resp = await client.embeddings.create(model="text-embedding-3-small", input=texts)
    return [item.embedding for item in resp.data]
