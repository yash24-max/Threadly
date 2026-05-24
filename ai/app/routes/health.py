"""Health check endpoints."""

from typing import Any

from fastapi import APIRouter, Response
from qdrant_client.exceptions import UnexpectedResponse

from app.config import settings
from app.logger import get_logger
from app.rag import RAGPipeline

logger = get_logger(__name__)

router = APIRouter(prefix="/health", tags=["health"])


def _check_qdrant() -> dict[str, Any]:
    """Check Qdrant connectivity."""
    try:
        rag = RAGPipeline()
        collections = rag.qdrant_client.get_collections()
        return {
            "status": "ok",
            "collections": len(collections.collections),
        }
    except UnexpectedResponse as e:
        logger.error(f"Qdrant error: {e}")
        return {"status": "error", "error": str(e)}
    except Exception as e:
        logger.error(f"Qdrant connection failed: {e}")
        return {"status": "down", "error": str(e)}


def _check_embedding_model() -> dict[str, Any]:
    """Check embedding model availability."""
    try:
        from app.rag.embeddings import get_embedding_provider

        provider = get_embedding_provider()
        dim = provider.embedding_dim
        return {"status": "ok", "model": settings.EMBEDDING_MODEL, "dimension": dim}
    except Exception as e:
        logger.error(f"Embedding model error: {e}")
        return {"status": "error", "error": str(e)}


@router.get("")
async def health_check() -> dict[str, Any]:
    """Full health check with service status.

    Returns:
        Health status dict
    """
    return {
        "status": "ok",
        "version": settings.APP_VERSION,
        "services": {
            "qdrant": _check_qdrant(),
            "embeddings": _check_embedding_model(),
        },
    }


@router.get("/ready")
async def ready() -> dict[str, Any]:
    """Kubernetes readiness probe.

    Returns:
        Readiness status
    """
    try:
        # Check Qdrant
        rag = RAGPipeline()
        rag.qdrant_client.get_collections()

        # Check embeddings
        from app.rag.embeddings import get_embedding_provider

        get_embedding_provider()

        return {"ready": True}
    except Exception as e:
        logger.error(f"Readiness check failed: {e}")
        return Response(
            content='{"ready": false}',
            status_code=503,
            media_type="application/json",
        )


@router.get("/live")
async def live() -> dict[str, Any]:
    """Kubernetes liveness probe.

    Returns:
        Liveness status
    """
    return {"alive": True}


@router.get("/metrics")
async def metrics() -> dict[str, Any]:
    """Prometheus metrics (simplified).

    Returns:
        Basic metrics dict
    """
    return {
        "app_version": settings.APP_VERSION,
        "debug_mode": settings.DEBUG,
        "default_llm_provider": settings.DEFAULT_LLM_PROVIDER,
        "embedding_model": settings.EMBEDDING_MODEL,
    }
