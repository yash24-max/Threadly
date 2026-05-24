"""Threadly AI sidecar — main FastAPI application."""
from contextlib import asynccontextmanager

import structlog
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from prometheus_fastapi_instrumentator import Instrumentator

from app.config import settings
from app.routes import ai, kb, health

log = structlog.get_logger()


@asynccontextmanager
async def lifespan(app: FastAPI):  # type: ignore[type-arg]
    log.info("threadly-ai starting", version="0.1.0", qdrant_host=settings.qdrant_host)
    yield
    log.info("threadly-ai shutting down")


app = FastAPI(
    title="Threadly AI",
    description="LLM completions and RAG for Threadly chatbots",
    version="0.1.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

Instrumentator().instrument(app).expose(app)

app.include_router(health.router, prefix="", tags=["Health"])
app.include_router(ai.router, prefix="/ai", tags=["AI"])
app.include_router(kb.router, prefix="/kb", tags=["Knowledge Base"])
