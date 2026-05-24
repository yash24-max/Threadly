"""KB document ingestion pipeline — dense + sparse vectors, progress callbacks."""
from __future__ import annotations

import io
import math
import re
import uuid
from pathlib import Path
from typing import Any

import boto3
import httpx
import structlog
from langchain_text_splitters import RecursiveCharacterTextSplitter
from pypdf import PdfReader
from qdrant_client import QdrantClient
from qdrant_client.models import (
    Distance,
    PointStruct,
    SparseIndexParams,
    SparseVectorParams,
    VectorParams,
    VectorsConfig,
)

from app.config import settings
from app.llm.embeddings import embed_texts

log = structlog.get_logger()

VECTOR_DIM = 1024
CHUNK_SIZE = 512
CHUNK_OVERLAP = 50
BATCH_SIZE = 50  # chunks per upsert + progress callback batch


# ---------------------------------------------------------------------------
# Qdrant / S3 helpers
# ---------------------------------------------------------------------------


def _qdrant() -> QdrantClient:
    return QdrantClient(host=settings.qdrant_host, port=settings.qdrant_port)


def _s3() -> Any:
    return boto3.client(
        "s3",
        endpoint_url=settings.s3_endpoint,
        aws_access_key_id=settings.s3_access_key,
        aws_secret_access_key=settings.s3_secret_key,
    )


def _collection_name(bot_id: str) -> str:
    return f"bot_{bot_id.replace('-', '_')}"


# ---------------------------------------------------------------------------
# BM25 sparse vector (same hash vocabulary as query side)
# ---------------------------------------------------------------------------


def _bm25_sparse_vector(text: str) -> tuple[list[int], list[float]]:
    """Return (indices, values) for a BM25 sparse vector of the text."""
    from collections import Counter

    tokens = re.findall(r"\w+", text.lower())
    if not tokens:
        return [], []
    tf = Counter(tokens)
    total = len(tokens)
    k1, b = 1.5, 0.75
    avg_dl = 100
    dl = total
    indices: list[int] = []
    values: list[float] = []
    for term, freq in tf.items():
        term_id = abs(hash(term)) % (2**20)
        tf_score = (freq * (k1 + 1)) / (freq + k1 * (1 - b + b * dl / avg_dl))
        indices.append(term_id)
        values.append(tf_score)
    return indices, values


# ---------------------------------------------------------------------------
# Collection management
# ---------------------------------------------------------------------------


def _ensure_collection(client: QdrantClient, name: str) -> None:
    existing = [c.name for c in client.get_collections().collections]
    if name not in existing:
        client.create_collection(
            collection_name=name,
            vectors_config={
                "dense": VectorParams(size=VECTOR_DIM, distance=Distance.COSINE),
            },
            sparse_vectors_config={
                "sparse": SparseVectorParams(
                    index=SparseIndexParams(on_disk=False)
                )
            },
        )
        log.info("qdrant.collection.created", name=name)


# ---------------------------------------------------------------------------
# Core callback helpers
# ---------------------------------------------------------------------------


async def _post_progress(job_id: str, processed: int, total: int) -> None:
    url = f"{settings.core_url}/kb/ingest-progress/{job_id}"
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            await client.post(
                url,
                json={"processed": processed, "total": total},
                headers={"x-service-secret": settings.core_service_secret},
            )
    except Exception as exc:
        log.warning("ingest.progress.callback.failed", job_id=job_id, error=str(exc))


async def _post_complete(job_id: str, chunk_count: int, document_id: str) -> None:
    url = f"{settings.core_url}/kb/ingest-complete/{job_id}"
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            await client.post(
                url,
                json={"chunkCount": chunk_count, "documentId": document_id},
                headers={"x-service-secret": settings.core_service_secret},
            )
    except Exception as exc:
        log.warning("ingest.complete.callback.failed", job_id=job_id, error=str(exc))


# ---------------------------------------------------------------------------
# Metadata extraction
# ---------------------------------------------------------------------------


def _extract_pdf_metadata(raw: bytes) -> tuple[str, int]:
    """Return (title_from_metadata_or_empty, page_count)."""
    reader = PdfReader(io.BytesIO(raw))
    page_count = len(reader.pages)
    title: str = ""
    if reader.metadata and reader.metadata.title:
        title = str(reader.metadata.title)
    return title, page_count


def _extract_text_from_pdf(raw: bytes) -> tuple[str, int]:
    reader = PdfReader(io.BytesIO(raw))
    pages_text = []
    for page in reader.pages:
        pages_text.append(page.extract_text() or "")
    return "\n".join(pages_text), len(reader.pages)


def _filename_to_doc_name(storage_key: str, provided_name: str) -> str:
    if provided_name:
        return provided_name
    return Path(storage_key).stem.replace("_", " ").replace("-", " ").title()


# ---------------------------------------------------------------------------
# Main ingestion entry point
# ---------------------------------------------------------------------------


async def ingest_document(
    bot_id: str,
    document_id: str,
    storage_key: str | None = None,
    source_url: str | None = None,
    doc_name: str = "",
    doc_type: str = "pdf",
    job_id: str | None = None,
) -> int:
    """
    Ingest a document into Qdrant with dense + sparse vectors.
    Returns number of chunks created.
    """
    log.info("kb.ingest.start", bot_id=bot_id, doc_id=document_id, type=doc_type)

    # 1. Load raw text + metadata
    page_count: int | None = None
    resolved_doc_name = doc_name

    if source_url and doc_type == "url":
        text = await _load_url(source_url)
    elif storage_key:
        raw_bytes, text, page_count = _load_from_s3(storage_key, doc_type)
        if not resolved_doc_name:
            resolved_doc_name = _filename_to_doc_name(storage_key, doc_name)
    else:
        raise ValueError("Either storage_key or source_url must be provided")

    if not text.strip():
        log.warning("kb.ingest.empty", document_id=document_id)
        return 0

    # 2. Chunk — 512 tokens, 50 overlap
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=CHUNK_SIZE,
        chunk_overlap=CHUNK_OVERLAP,
        length_function=len,
    )
    chunks = splitter.split_text(text)
    total = len(chunks)
    log.info("kb.ingest.chunks", count=total)

    # 3. Set up Qdrant collection
    client = _qdrant()
    collection = _collection_name(bot_id)
    _ensure_collection(client, collection)

    # Delete old chunks for this document
    try:
        client.delete(
            collection_name=collection,
            points_selector={
                "filter": {
                    "must": [{"key": "doc_id", "match": {"value": document_id}}]
                }
            },
        )
    except Exception as exc:
        log.warning("kb.ingest.delete_old.failed", error=str(exc))

    # 4. Embed + upsert in batches of BATCH_SIZE
    processed = 0
    for batch_start in range(0, total, BATCH_SIZE):
        batch = chunks[batch_start: batch_start + BATCH_SIZE]
        embeddings = await embed_texts(batch)

        points: list[PointStruct] = []
        for i, (chunk_text, dense_vec) in enumerate(zip(batch, embeddings)):
            chunk_idx = batch_start + i
            sparse_indices, sparse_values = _bm25_sparse_vector(chunk_text)

            payload: dict[str, Any] = {
                "doc_id": document_id,
                "document_name": resolved_doc_name,
                "title": resolved_doc_name,  # legacy compat
                "chunk_index": chunk_idx,
                "text": chunk_text,
                "bot_id": bot_id,
            }
            if page_count is not None:
                # Estimate page number from chunk position
                payload["page_number"] = max(
                    1, math.ceil((chunk_idx / total) * page_count)
                )
            else:
                payload["page_number"] = None

            vec_payload: dict[str, Any] = {
                "dense": dense_vec,
            }
            if sparse_indices:
                vec_payload["sparse"] = {
                    "indices": sparse_indices,
                    "values": sparse_values,
                }

            points.append(
                PointStruct(
                    id=str(uuid.uuid4()),
                    vector=vec_payload,
                    payload=payload,
                )
            )

        client.upsert(collection_name=collection, points=points)
        processed += len(batch)

        # Progress callback
        if job_id:
            await _post_progress(job_id, processed, total)

        log.info("kb.ingest.batch", processed=processed, total=total)

    log.info("kb.ingest.done", chunks=total, bot_id=bot_id)

    # Completion callback
    if job_id:
        await _post_complete(job_id, total, document_id)

    return total


# ---------------------------------------------------------------------------
# S3 / URL loaders
# ---------------------------------------------------------------------------


def _load_from_s3(storage_key: str, doc_type: str) -> tuple[bytes, str, int | None]:
    """Returns (raw_bytes, text, page_count | None)."""
    s3 = _s3()
    obj = s3.get_object(Bucket=settings.s3_bucket, Key=storage_key)
    raw: bytes = obj["Body"].read()

    if doc_type == "pdf":
        text, page_count = _extract_text_from_pdf(raw)
        return raw, text, page_count

    text = raw.decode("utf-8", errors="replace")
    return raw, text, None


async def _load_url(url: str) -> str:
    async with httpx.AsyncClient(timeout=30, follow_redirects=True) as client:
        resp = await client.get(url, headers={"User-Agent": "Threadly-KB-Crawler/1.0"})
        resp.raise_for_status()
        text = resp.text
        text = re.sub(r"<[^>]+>", " ", text)
        text = re.sub(r"\s+", " ", text)
        return text.strip()
