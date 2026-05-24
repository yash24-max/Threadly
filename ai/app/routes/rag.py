"""RAG (Knowledge Base) endpoints."""

from typing import Optional

from fastapi import APIRouter, File, HTTPException, UploadFile, status
from pydantic import BaseModel, Field

from app.config import settings
from app.logger import get_logger
from app.rag import RAGPipeline

logger = get_logger(__name__)

router = APIRouter(prefix="/kb", tags=["knowledge-base"])


class SearchRequest(BaseModel):
    """Knowledge base search request."""

    query: str = Field(..., description="Search query", min_length=1)
    bot_id: str = Field(..., description="Bot/workspace ID")
    top_k: int = Field(default=5, ge=1, le=20, description="Number of results")
    score_threshold: Optional[float] = Field(
        default=None,
        ge=0.0,
        le=1.0,
        description="Minimum similarity score",
    )


class SearchResult(BaseModel):
    """Single search result."""

    passage: str = Field(..., description="Retrieved passage text")
    score: float = Field(..., description="Similarity score 0-1")
    doc_id: str = Field(..., description="Document ID")
    source: str = Field(..., description="Document source filename")
    chunk_index: int = Field(..., description="Chunk index in document")


class SearchResponse(BaseModel):
    """Search results response."""

    results: list[SearchResult] = Field(default_factory=list)
    total_count: int = Field(default=0)
    query: str = Field(..., description="Original query")


class IngestResponse(BaseModel):
    """Document ingestion response."""

    ingestion_id: str = Field(..., description="Ingestion operation ID")
    doc_id: str = Field(..., description="Document ID")
    status: str = Field(..., description="Status: processing, completed, failed")
    chunk_count: int = Field(default=0)
    error: Optional[str] = Field(default=None)
    created_at: Optional[str] = Field(default=None)


class KBStatusResponse(BaseModel):
    """Knowledge base status response."""

    bot_id: str = Field(..., description="Bot/workspace ID")
    collection_name: str = Field(..., description="Qdrant collection name")
    point_count: int = Field(..., description="Number of indexed chunks")
    status: str = Field(..., description="Status: ready, not_found")
    error: Optional[str] = Field(default=None)


@router.post("/ingest", response_model=IngestResponse)
async def ingest_document(
    file: UploadFile,
    bot_id: str,
) -> IngestResponse:
    """Upload and process document for RAG.

    Args:
        file: Document file (PDF, TXT, DOCX)
        bot_id: Bot/workspace ID

    Returns:
        Ingestion result with document ID and chunk count
    """
    import tempfile
    from pathlib import Path

    if not file.filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No filename provided",
        )

    # Validate file type
    supported_types = {".pdf", ".txt", ".docx"}
    file_ext = Path(file.filename).suffix.lower()
    if file_ext not in supported_types:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Unsupported file type: {file_ext}. Supported: {supported_types}",
        )

    try:
        logger.info(f"Ingesting document: {file.filename} for bot {bot_id}")

        # Save to temporary file
        with tempfile.NamedTemporaryFile(
            suffix=file_ext,
            delete=False,
        ) as tmp:
            content = await file.read()
            tmp.write(content)
            tmp_path = tmp.name

        try:
            # Ingest document
            rag = RAGPipeline()
            result = rag.ingest_document(tmp_path, bot_id)

            logger.info(f"Ingestion result: {result}")
            return IngestResponse(**result)

        finally:
            # Cleanup
            Path(tmp_path).unlink(missing_ok=True)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error ingesting document: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )


@router.post("/search", response_model=SearchResponse)
async def search(request: SearchRequest) -> SearchResponse:
    """Semantic search across knowledge base.

    Args:
        request: Search request

    Returns:
        Search results with passages and scores
    """
    try:
        logger.info(f"Search query: '{request.query}' for bot {request.bot_id}")

        rag = RAGPipeline()
        passages = rag.search(
            query=request.query,
            bot_id=request.bot_id,
            top_k=request.top_k,
            score_threshold=request.score_threshold,
        )

        results = [SearchResult(**p) for p in passages]

        return SearchResponse(
            results=results,
            total_count=len(results),
            query=request.query,
        )

    except Exception as e:
        logger.error(f"Error searching KB: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )


@router.get("/status/{bot_id}", response_model=KBStatusResponse)
async def kb_status(bot_id: str) -> KBStatusResponse:
    """Get knowledge base status for a bot.

    Args:
        bot_id: Bot/workspace ID

    Returns:
        KB status and statistics
    """
    try:
        rag = RAGPipeline()
        status_dict = rag.get_kb_status(bot_id)
        return KBStatusResponse(**status_dict)

    except Exception as e:
        logger.error(f"Error getting KB status: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )


@router.delete("/documents/{doc_id}")
async def delete_document(doc_id: str, bot_id: str) -> dict:
    """Delete document from knowledge base.

    Args:
        doc_id: Document ID to delete
        bot_id: Bot/workspace ID

    Returns:
        Deletion status
    """
    try:
        logger.info(f"Deleting document {doc_id} for bot {bot_id}")

        rag = RAGPipeline()
        success = rag.delete_document(doc_id, bot_id)

        if success:
            return {"status": "deleted", "doc_id": doc_id}
        else:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Document not found: {doc_id}",
            )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error deleting document: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e),
        )
