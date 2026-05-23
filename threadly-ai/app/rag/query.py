"""RAG query pipeline — hybrid dense + sparse (BM25) retrieval with RRF fusion."""
from __future__ import annotations

import math
import os
from typing import Any

import structlog
from pydantic import BaseModel
from qdrant_client import AsyncQdrantClient
from qdrant_client.models import NamedSparseVector, NamedVector, ScoredPoint, SearchRequest

from app.config import settings
from app.llm.embeddings import embed_texts

log = structlog.get_logger()

# ---------------------------------------------------------------------------
# Models
# ---------------------------------------------------------------------------


class RetrievedPassage(BaseModel):
    id: str
    text: str
    score: float
    document_id: str
    document_name: str
    page_number: int | None
    chunk_index: int


# ---------------------------------------------------------------------------
# Qdrant helpers
# ---------------------------------------------------------------------------


def _collection_name(bot_id: str) -> str:
    return f"bot_{bot_id.replace('-', '_')}"


async def _get_client() -> AsyncQdrantClient:
    return AsyncQdrantClient(host=settings.qdrant_host, port=settings.qdrant_port)


# ---------------------------------------------------------------------------
# BM25 sparse vector helper
# ---------------------------------------------------------------------------

def _bm25_sparse_vector(text: str) -> dict[int, float]:
    """
    Simple in-process BM25-inspired term frequency weighting.
    In production this should match what was used at index time.
    We use a stable vocabulary mapping: term → int via hash truncated to 32 bits.
    """
    import re
    from collections import Counter

    tokens = re.findall(r"\w+", text.lower())
    if not tokens:
        return {}
    tf = Counter(tokens)
    total = len(tokens)
    # TF-IDF-style weight, no IDF available without corpus stats → just TF norm
    k1, b = 1.5, 0.75
    avg_dl = 100  # assumed average doc length
    dl = total
    sparse: dict[int, float] = {}
    for term, freq in tf.items():
        term_id = abs(hash(term)) % (2**20)  # 20-bit vocab
        tf_score = (freq * (k1 + 1)) / (freq + k1 * (1 - b + b * dl / avg_dl))
        sparse[term_id] = tf_score
    return sparse


# ---------------------------------------------------------------------------
# RRF fusion
# ---------------------------------------------------------------------------

def _rrf_fusion(
    dense_hits: list[ScoredPoint],
    sparse_hits: list[ScoredPoint],
    k: int = 60,
) -> list[tuple[str, float, ScoredPoint]]:
    """Reciprocal Rank Fusion of two ranked lists."""
    scores: dict[str, float] = {}
    point_map: dict[str, ScoredPoint] = {}

    for rank, hit in enumerate(dense_hits, start=1):
        pid = str(hit.id)
        scores[pid] = scores.get(pid, 0.0) + 1.0 / (k + rank)
        point_map[pid] = hit

    for rank, hit in enumerate(sparse_hits, start=1):
        pid = str(hit.id)
        scores[pid] = scores.get(pid, 0.0) + 1.0 / (k + rank)
        if pid not in point_map:
            point_map[pid] = hit

    ranked = sorted(scores.items(), key=lambda x: x[1], reverse=True)
    return [(pid, score, point_map[pid]) for pid, score in ranked]


# ---------------------------------------------------------------------------
# Main hybrid query
# ---------------------------------------------------------------------------


async def hybrid_query(
    bot_id: str,
    query: str,
    top_k: int = 5,
    use_rerank: bool = False,
) -> list[RetrievedPassage]:
    """
    1. Dense retrieval: embed query → Qdrant vector search
    2. Sparse retrieval: BM25 keyword search via Qdrant sparse vectors
    3. RRF fusion: combine dense + sparse scores
    4. Optional Cohere reranking if use_rerank=True and COHERE_API_KEY set
    5. Return top_k passages with citation metadata
    """
    client = await _get_client()
    collection = _collection_name(bot_id)

    # Check collection exists
    try:
        collections = await client.get_collections()
        existing_names = [c.name for c in collections.collections]
    except Exception as exc:
        log.warning("qdrant.connection.error", error=str(exc))
        return []

    if collection not in existing_names:
        log.info("kb.query.no_collection", bot_id=bot_id)
        return []

    # 1. Dense retrieval
    query_vec = (await embed_texts([query]))[0]
    fetch_k = max(top_k * 3, 20)  # over-fetch for fusion

    try:
        dense_hits: list[ScoredPoint] = await client.search(
            collection_name=collection,
            query_vector=("dense", query_vec),
            limit=fetch_k,
            with_payload=True,
            score_threshold=0.2,
        )
    except Exception:
        # Fallback: try without named vector (older collection schema)
        dense_hits = await client.search(
            collection_name=collection,
            query_vector=query_vec,
            limit=fetch_k,
            with_payload=True,
            score_threshold=0.2,
        )

    # 2. Sparse retrieval (BM25 via Qdrant sparse vectors)
    sparse_vec = _bm25_sparse_vector(query)
    sparse_hits: list[ScoredPoint] = []
    if sparse_vec:
        try:
            sparse_hits = await client.search(
                collection_name=collection,
                query_vector=NamedSparseVector(
                    name="sparse",
                    vector={"indices": list(sparse_vec.keys()), "values": list(sparse_vec.values())},
                ),
                limit=fetch_k,
                with_payload=True,
            )
        except Exception as exc:
            # Collection may not have sparse vectors (old schema) — degrade gracefully
            log.debug("sparse.search.unavailable", error=str(exc))

    # 3. RRF fusion
    if sparse_hits:
        fused = _rrf_fusion(dense_hits, sparse_hits)
    else:
        fused = [(str(h.id), h.score, h) for h in dense_hits]

    top_hits = fused[:top_k]

    # 4. Optional Cohere reranking
    cohere_key = os.environ.get("COHERE_API_KEY", "")
    if use_rerank and cohere_key and top_hits:
        try:
            import cohere  # type: ignore

            co = cohere.Client(cohere_key)
            docs = [h.payload.get("text", "") for _, _, h in top_hits if h.payload]
            rr = co.rerank(
                model="rerank-english-v3.0",
                query=query,
                documents=docs,
                top_n=top_k,
            )
            reranked_hits = [top_hits[r.index] for r in rr.results]
            top_hits = [(pid, r.relevance_score, point) for (pid, _, point), r in zip(reranked_hits, rr.results)]
        except Exception as exc:
            log.warning("cohere.rerank.failed", error=str(exc))

    passages: list[RetrievedPassage] = []
    for pid, score, hit in top_hits:
        payload = hit.payload or {}
        passages.append(
            RetrievedPassage(
                id=pid,
                text=payload.get("text", ""),
                score=score,
                document_id=payload.get("doc_id", ""),
                document_name=payload.get("document_name", payload.get("title", "")),
                page_number=payload.get("page_number"),
                chunk_index=payload.get("chunk_index", 0),
            )
        )

    log.info("kb.query.results", count=len(passages), bot_id=bot_id, mode="hybrid")
    return passages


# ---------------------------------------------------------------------------
# Legacy compatibility shim (used by routes/ai.py)
# ---------------------------------------------------------------------------


async def query_kb(bot_id: str, question: str, top_k: int | None = None) -> list[dict[str, Any]]:
    """Wraps hybrid_query for backward-compat with the existing route."""
    k = top_k or settings.rag_top_k
    passages = await hybrid_query(bot_id, question, top_k=k)
    return [
        {
            "text": p.text,
            "title": p.document_name,
            "score": p.score,
            "doc_id": p.document_id,
            "chunk_index": p.chunk_index,
            "page_number": p.page_number,
        }
        for p in passages
    ]


def format_context(passages: list[dict[str, Any]]) -> str:
    """Format retrieved passages into the LLM context block (legacy)."""
    if not passages:
        return ""
    lines = ["<knowledge_base_context>"]
    for i, p in enumerate(passages, 1):
        lines.append(f"[Source {i}: {p.get('title', '')}]")
        lines.append(p["text"])
        lines.append("")
    lines.append("</knowledge_base_context>")
    return "\n".join(lines)


def format_context_with_citations(
    passages: list[RetrievedPassage],
) -> tuple[str, list[dict[str, Any]]]:
    """
    Format passages for citation-aware prompting.
    Returns (context_block, citation_list).
    Citations list is [{index, document_name, page_number, passage_text}].
    """
    if not passages:
        return "", []
    lines = ["<knowledge_base_context>"]
    citations: list[dict[str, Any]] = []
    for i, p in enumerate(passages, 1):
        lines.append(f"[{i}] {p.document_name}" + (f" (p.{p.page_number})" if p.page_number else ""))
        lines.append(p.text)
        lines.append("")
        citations.append(
            {
                "index": i,
                "document_name": p.document_name,
                "page_number": p.page_number,
                "passage_text": p.text,
            }
        )
    lines.append("</knowledge_base_context>")
    lines.append(
        "\nWhen citing sources in your response, use inline references like [1], [2], etc."
    )
    return "\n".join(lines), citations
