"""RAG query pipeline — retrieve + rerank."""
from __future__ import annotations

import structlog
from qdrant_client import QdrantClient
from qdrant_client.models import ScoredPoint

from app.config import settings
from app.llm.embeddings import embed_texts

log = structlog.get_logger()


def _qdrant() -> QdrantClient:
    return QdrantClient(host=settings.qdrant_host, port=settings.qdrant_port)


def _collection_name(bot_id: str) -> str:
    return f"bot_{bot_id.replace('-', '_')}"


async def query_kb(bot_id: str, question: str, top_k: int | None = None) -> list[dict]:
    """
    Retrieve relevant passages for a question.
    Returns list of {text, title, score, doc_id, chunk_index}.
    """
    k = top_k or settings.rag_top_k
    client = _qdrant()
    collection = _collection_name(bot_id)

    existing = [c.name for c in client.get_collections().collections]
    if collection not in existing:
        log.info("kb.query.no_collection", bot_id=bot_id)
        return []

    # Embed the question
    query_vec = (await embed_texts([question]))[0]

    # Retrieve
    results: list[ScoredPoint] = client.search(
        collection_name=collection,
        query_vector=query_vec,
        limit=k,
        with_payload=True,
        score_threshold=0.3,
    )

    passages = [
        {
            "text": r.payload.get("text", ""),
            "title": r.payload.get("title", ""),
            "score": r.score,
            "doc_id": r.payload.get("doc_id", ""),
            "chunk_index": r.payload.get("chunk_index", 0),
        }
        for r in results
    ]

    log.info("kb.query.results", count=len(passages), bot_id=bot_id)
    return passages


def format_context(passages: list[dict]) -> str:
    """Format retrieved passages into the LLM context block."""
    if not passages:
        return ""
    lines = ["<knowledge_base_context>"]
    for i, p in enumerate(passages, 1):
        lines.append(f"[Source {i}: {p['title']}]")
        lines.append(p["text"])
        lines.append("")
    lines.append("</knowledge_base_context>")
    return "\n".join(lines)
