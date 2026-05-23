"""KB ingestion endpoint."""
from __future__ import annotations

import asyncio
from typing import Annotated
from uuid import UUID

import httpx
import structlog
from fastapi import APIRouter, BackgroundTasks, Depends, Header, HTTPException
from pydantic import BaseModel

from app.config import settings
from app.rag.ingest import ingest_document
from app.rag.query import query_kb

log = structlog.get_logger()
router = APIRouter()


def _verify_secret(x_service_secret: Annotated[str, Header()] = "") -> None:
    if x_service_secret != settings.core_service_secret:
        raise HTTPException(status_code=403, detail="Invalid service secret")


async def _notify_core(document_id: str, status: str, chunk_count: int | None, error_msg: str | None) -> None:
    """Post ingestion result back to threadly-core."""
    url = f"{settings.core_url}/v1/internal/kb/{document_id}/status"
    payload: dict = {"status": status}
    if chunk_count is not None:
        payload["chunkCount"] = chunk_count
    if error_msg:
        payload["errorMsg"] = error_msg
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            await client.post(url, json=payload,
                              headers={"X-Service-Secret": settings.core_service_secret})
    except Exception as e:
        log.error("kb.callback.failed", document_id=document_id, error=str(e))


class IngestRequest(BaseModel):
    bot_id: str
    document_id: str
    storage_key: str | None = None
    source_url: str | None = None
    doc_name: str = ""
    doc_type: str = "pdf"


class QueryRequest(BaseModel):
    bot_id: str
    question: str
    top_k: int = 5


class IngestRequest(BaseModel):
    bot_id: str
    document_id: str
    storage_key: str | None = None
    source_url: str | None = None
    doc_name: str = ""
    doc_type: str = "pdf"
    file_url: str | None = None  # presigned/internal URL from MinIO


@router.post("/ingest")
async def ingest(req: IngestRequest, background_tasks: BackgroundTasks,
                 _: None = Depends(_verify_secret)) -> dict:
    """Ingest a document into the vector store. Runs in background and notifies core."""
    background_tasks.add_task(_run_ingest, req)
    return {"status": "accepted", "documentId": req.document_id}


async def _run_ingest(req: IngestRequest) -> None:
    try:
        # Resolve storage_key from file_url if needed
        storage_key = req.storage_key
        if not storage_key and req.file_url:
            # Download to temp and use raw content approach
            storage_key = None

        chunks = await ingest_document(
            bot_id=req.bot_id,
            document_id=req.document_id,
            storage_key=storage_key,
            source_url=req.source_url or req.file_url,
            doc_name=req.doc_name,
            doc_type=req.doc_type,
        )
        await _notify_core(req.document_id, "ready", chunks, None)
        log.info("kb.ingest.complete", document_id=req.document_id, chunks=chunks)
    except Exception as e:
        log.error("kb.ingest.failed", document_id=req.document_id, error=str(e))
        await _notify_core(req.document_id, "error", None, str(e))


@router.post("/query")
async def query(req: QueryRequest, _: None = Depends(_verify_secret)) -> dict:
    """Retrieve relevant passages for a question."""
    passages = await query_kb(req.bot_id, req.question, top_k=req.top_k)
    return {"passages": passages}
