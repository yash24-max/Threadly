"""KB document ingestion pipeline."""
from __future__ import annotations

import io
import uuid
from pathlib import Path

import boto3
import structlog
from langchain_text_splitters import RecursiveCharacterTextSplitter
from pypdf import PdfReader
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, PointStruct, VectorParams

from app.config import settings
from app.llm.embeddings import embed_texts

log = structlog.get_logger()

VECTOR_DIM = 1024


def _qdrant() -> QdrantClient:
    return QdrantClient(host=settings.qdrant_host, port=settings.qdrant_port)


def _s3():
    return boto3.client(
        "s3",
        endpoint_url=settings.s3_endpoint,
        aws_access_key_id=settings.s3_access_key,
        aws_secret_access_key=settings.s3_secret_key,
    )


def _collection_name(bot_id: str) -> str:
    return f"bot_{bot_id.replace('-', '_')}"


async def ingest_document(
    bot_id: str,
    document_id: str,
    storage_key: str | None = None,
    source_url: str | None = None,
    doc_name: str = "",
    doc_type: str = "pdf",
) -> int:
    """Ingest a document. Returns number of chunks created."""
    log.info("kb.ingest.start", bot_id=bot_id, doc_id=document_id, type=doc_type)

    # 1. Load raw text
    if source_url and doc_type == "url":
        text = await _load_url(source_url)
    elif storage_key:
        text = _load_from_s3(storage_key, doc_type)
    else:
        raise ValueError("Either storage_key or source_url must be provided")

    if not text.strip():
        log.warning("kb.ingest.empty", document_id=document_id)
        return 0

    # 2. Chunk
    splitter = RecursiveCharacterTextSplitter(chunk_size=settings.chunk_size, chunk_overlap=settings.chunk_overlap)
    chunks = splitter.split_text(text)
    log.info("kb.ingest.chunks", count=len(chunks))

    # 3. Embed
    embeddings = await embed_texts(chunks)

    # 4. Upsert to Qdrant
    client = _qdrant()
    collection = _collection_name(bot_id)
    _ensure_collection(client, collection)

    # Delete old chunks for this document
    client.delete(
        collection_name=collection,
        points_selector={"filter": {"must": [{"key": "doc_id", "match": {"value": document_id}}]}},
    )

    points = [
        PointStruct(
            id=str(uuid.uuid4()),
            vector=embeddings[i],
            payload={
                "doc_id": document_id,
                "chunk_index": i,
                "text": chunks[i],
                "title": doc_name,
                "bot_id": bot_id,
            },
        )
        for i in range(len(chunks))
    ]
    client.upsert(collection_name=collection, points=points)
    log.info("kb.ingest.done", chunks=len(chunks), bot_id=bot_id)
    return len(chunks)


def _ensure_collection(client: QdrantClient, name: str) -> None:
    existing = [c.name for c in client.get_collections().collections]
    if name not in existing:
        client.create_collection(
            collection_name=name,
            vectors_config=VectorParams(size=VECTOR_DIM, distance=Distance.COSINE),
        )


def _load_from_s3(storage_key: str, doc_type: str) -> str:
    s3 = _s3()
    obj = s3.get_object(Bucket=settings.s3_bucket, Key=storage_key)
    raw = obj["Body"].read()
    if doc_type == "pdf":
        reader = PdfReader(io.BytesIO(raw))
        return "\n".join(page.extract_text() or "" for page in reader.pages)
    return raw.decode("utf-8", errors="replace")


async def _load_url(url: str) -> str:
    import httpx
    async with httpx.AsyncClient(timeout=30, follow_redirects=True) as client:
        resp = await client.get(url, headers={"User-Agent": "Threadly-KB-Crawler/1.0"})
        resp.raise_for_status()
        # Basic HTML strip
        text = resp.text
        import re
        text = re.sub(r"<[^>]+>", " ", text)
        text = re.sub(r"\s+", " ", text)
        return text.strip()
