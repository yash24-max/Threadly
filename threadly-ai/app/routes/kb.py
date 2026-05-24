"""KB ingestion endpoint — documents, web scraping, sitemap crawling."""
from __future__ import annotations

import asyncio
import xml.etree.ElementTree as ET
from typing import Annotated
from uuid import UUID, uuid4
import re

import httpx
import structlog
from fastapi import APIRouter, BackgroundTasks, Depends, Header, HTTPException
from pydantic import BaseModel, HttpUrl
from bs4 import BeautifulSoup
from langchain_text_splitters import RecursiveCharacterTextSplitter

from app.config import settings
from app.rag.ingest import ingest_document
from app.rag.query import query_kb
from app.llm.embeddings import embed_texts

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
    file_url: str | None = None


class QueryRequest(BaseModel):
    bot_id: str
    question: str
    top_k: int = 5


class ScrapeUrlRequest(BaseModel):
    bot_id: str
    url: HttpUrl
    max_pages: int = 1
    include_subpages: bool = False


class SitemapRequest(BaseModel):
    bot_id: str
    sitemap_url: HttpUrl
    max_pages: int = 50


def _extract_text_from_html(html: str) -> str:
    """Extract clean text from HTML, removing navigation and footer."""
    soup = BeautifulSoup(html, "html.parser")

    # Remove script, style, nav, footer, script tags
    for tag in soup(["script", "style", "nav", "footer", "noscript"]):
        tag.decompose()

    # Get text
    text = soup.get_text(separator=" ", strip=True)

    # Clean up whitespace
    text = re.sub(r"\s+", " ", text)
    return text.strip()


async def _fetch_url(url: str, timeout: int = 30) -> str:
    """Fetch URL content with httpx, following redirects."""
    async with httpx.AsyncClient(timeout=timeout, follow_redirects=True) as client:
        try:
            response = await client.get(url, headers={
                "User-Agent": "Threadly-KB-Scraper/1.0"
            })
            response.raise_for_status()
            return response.text
        except httpx.HTTPError as e:
            log.error("fetch.failed", url=url, error=str(e))
            raise


async def _chunk_text(text: str, chunk_size: int = 512, overlap: int = 50) -> list[str]:
    """Split text into chunks with overlap using LangChain."""
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=chunk_size,
        chunk_overlap=overlap,
        separators=["\n\n", "\n", ". ", " ", ""],
    )
    return splitter.split_text(text)


async def _upsert_chunks_to_qdrant(
    bot_id: str,
    doc_id: str,
    chunks: list[str],
    source_url: str,
) -> int:
    """Embed chunks and upsert to Qdrant."""
    from app.rag.ingest import _qdrant, VECTOR_DIM, BATCH_SIZE
    from qdrant_client.models import PointStruct

    client = _qdrant()
    collection_name = f"bot_{bot_id}"

    total_ingested = 0

    # Process in batches
    for i in range(0, len(chunks), BATCH_SIZE):
        batch = chunks[i:i + BATCH_SIZE]

        # Embed batch
        embeddings = await embed_texts(batch)

        # Create points
        points = [
            PointStruct(
                id=int(uuid4().int % (2**63 - 1)),
                vector=emb,
                payload={
                    "text": chunk,
                    "doc_id": doc_id,
                    "source_url": source_url,
                    "chunk_index": i + j,
                },
            )
            for j, (chunk, emb) in enumerate(zip(batch, embeddings))
        ]

        # Upsert to Qdrant
        try:
            client.upsert(
                collection_name=collection_name,
                points=points,
            )
            total_ingested += len(points)
        except Exception as e:
            log.error("qdrant.upsert.failed", doc_id=doc_id, error=str(e))
            raise

    return total_ingested


@router.post("/ingest")
async def ingest(req: IngestRequest, background_tasks: BackgroundTasks,
                 _: None = Depends(_verify_secret)) -> dict:
    """Ingest a document into the vector store. Runs in background and notifies core."""
    background_tasks.add_task(_run_ingest, req)
    return {"status": "accepted", "documentId": req.document_id}


async def _run_ingest(req: IngestRequest) -> None:
    try:
        storage_key = req.storage_key
        if not storage_key and req.file_url:
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


@router.post("/scrape-url")
async def scrape_url(req: ScrapeUrlRequest, background_tasks: BackgroundTasks,
                     _: None = Depends(_verify_secret)) -> dict:
    """Scrape single URL and ingest into KB."""
    doc_id = str(uuid4())
    background_tasks.add_task(_run_scrape_url, req, doc_id)
    return {"status": "accepted", "doc_id": doc_id, "url": str(req.url)}


async def _run_scrape_url(req: ScrapeUrlRequest, doc_id: str) -> None:
    """Background task to scrape URL, chunk, embed, and upsert."""
    try:
        url = str(req.url)

        # Fetch HTML
        html = await _fetch_url(url, timeout=30)

        # Extract text
        text = _extract_text_from_html(html)
        if not text or len(text) < 100:
            raise ValueError(f"No substantial content extracted from {url}")

        # Chunk text
        chunks = await _chunk_text(text, chunk_size=settings.chunk_size, overlap=settings.chunk_overlap)

        # Upsert to Qdrant
        ingested = await _upsert_chunks_to_qdrant(
            bot_id=req.bot_id,
            doc_id=doc_id,
            chunks=chunks,
            source_url=url,
        )

        log.info("kb.scrape_url.complete", doc_id=doc_id, url=url, chunks=ingested)

    except Exception as e:
        log.error("kb.scrape_url.failed", doc_id=doc_id, url=str(req.url), error=str(e))


@router.post("/scrape-sitemap")
async def scrape_sitemap(req: SitemapRequest, background_tasks: BackgroundTasks,
                         _: None = Depends(_verify_secret)) -> dict:
    """Scrape sitemap.xml and ingest URLs."""
    batch_id = str(uuid4())
    background_tasks.add_task(_run_scrape_sitemap, req, batch_id)
    return {"status": "accepted", "batch_id": batch_id, "sitemap_url": str(req.sitemap_url)}


async def _run_scrape_sitemap(req: SitemapRequest, batch_id: str) -> None:
    """Background task to scrape sitemap, extract URLs, and process them."""
    try:
        sitemap_url = str(req.sitemap_url)

        # Fetch sitemap
        sitemap_xml = await _fetch_url(sitemap_url, timeout=30)

        # Parse sitemap
        root = ET.fromstring(sitemap_xml)

        # Extract URLs (handle namespaced XML)
        namespace = {"ns": "http://www.sitemaps.org/schemas/sitemap/0.9"}
        urls = []
        for url_elem in root.findall("ns:url", namespace):
            loc = url_elem.find("ns:loc", namespace)
            if loc is not None and loc.text:
                urls.append(loc.text)

        if not urls:
            # Try without namespace
            for url_elem in root.findall("url"):
                loc = url_elem.find("loc")
                if loc is not None and loc.text:
                    urls.append(loc.text)

        # Limit to max_pages
        urls = urls[:req.max_pages]

        if not urls:
            raise ValueError(f"No URLs found in sitemap {sitemap_url}")

        log.info("kb.sitemap.parsed", batch_id=batch_id, url_count=len(urls))

        # Process URLs concurrently (max 5 concurrent)
        semaphore = asyncio.Semaphore(5)

        async def process_url(url: str) -> tuple[str, int | None, str | None]:
            """Process single URL, return (url, chunk_count, error)."""
            async with semaphore:
                try:
                    html = await _fetch_url(url, timeout=30)
                    text = _extract_text_from_html(html)

                    if not text or len(text) < 100:
                        return (url, None, "No substantial content")

                    chunks = await _chunk_text(text, chunk_size=settings.chunk_size, overlap=settings.chunk_overlap)
                    doc_id = str(uuid4())

                    ingested = await _upsert_chunks_to_qdrant(
                        bot_id=req.bot_id,
                        doc_id=doc_id,
                        chunks=chunks,
                        source_url=url,
                    )

                    return (url, ingested, None)

                except Exception as e:
                    log.error("kb.sitemap.process_url.failed", url=url, error=str(e))
                    return (url, None, str(e))

        results = await asyncio.gather(*[process_url(url) for url in urls])

        # Count results
        successful = sum(1 for _, count, err in results if count is not None)
        failed = sum(1 for _, count, err in results if err is not None)
        total_chunks = sum(count for _, count, _ in results if count is not None)

        log.info("kb.scrape_sitemap.complete", batch_id=batch_id, successful=successful,
                 failed=failed, total_chunks=total_chunks)

    except Exception as e:
        log.error("kb.scrape_sitemap.failed", batch_id=batch_id, url=str(req.sitemap_url), error=str(e))


@router.post("/query")
async def query(req: QueryRequest, _: None = Depends(_verify_secret)) -> dict:
    """Retrieve relevant passages for a question."""
    passages = await query_kb(req.bot_id, req.question, top_k=req.top_k)
    return {"passages": passages}
