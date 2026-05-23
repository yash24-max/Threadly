"""Tests for the Knowledge Base ingestion and query endpoints."""
from __future__ import annotations

import pytest
import httpx
import respx


# ---------------------------------------------------------------------------
# POST /kb/query
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_query_with_no_collection_returns_empty(
    client: httpx.AsyncClient,
    auth_headers: dict[str, str],
    respx_mock: respx.MockRouter,
) -> None:
    """Querying a bot that has no knowledge base must return an empty passages
    list rather than raising an error."""
    # Mock the Qdrant vector DB to return no results
    respx_mock.post("http://localhost:6333/collections/bot-nonexistent/points/search").mock(
        return_value=httpx.Response(404, json={"status": {"error": "Not found"}})
    )
    # Also handle query_kb's internal Qdrant search attempt
    respx_mock.post(
        url__regex=r"http://localhost:6333/.*",
    ).mock(return_value=httpx.Response(404, json={}))

    payload = {
        "bot_id": "nonexistent-bot",
        "question": "What is the return policy?",
        "top_k": 5,
    }

    response = await client.post("/kb/query", json=payload, headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert "passages" in data
    assert isinstance(data["passages"], list)


@pytest.mark.asyncio
async def test_query_missing_auth_returns_403(client: httpx.AsyncClient) -> None:
    """KB query endpoint must reject requests without a service secret."""
    payload = {"bot_id": "some-bot", "question": "test", "top_k": 3}
    response = await client.post("/kb/query", json=payload)

    assert response.status_code == 403


# ---------------------------------------------------------------------------
# POST /kb/ingest
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_ingest_endpoint_accepts_job_payload(
    client: httpx.AsyncClient,
    auth_headers: dict[str, str],
    respx_mock: respx.MockRouter,
) -> None:
    """The ingest endpoint must accept a valid job payload and respond with
    202 Accepted (async processing) immediately without waiting for ingestion."""
    # Prevent real S3/MinIO calls from leaving the process
    respx_mock.get(url__regex=r"http://localhost:9000/.*").mock(
        return_value=httpx.Response(200, content=b"%PDF test content")
    )
    # Prevent the background callback to threadly-core from hanging
    respx_mock.post("http://localhost:8080/v1/internal/kb/doc-ingest-001/status").mock(
        return_value=httpx.Response(200, json={"ok": True})
    )

    payload = {
        "bot_id": "00000000-0000-0000-0000-000000000001",
        "document_id": "doc-ingest-001",
        "doc_name": "Return Policy.pdf",
        "doc_type": "pdf",
        "source_url": "http://localhost:9000/threadly-kb/return-policy.pdf",
    }

    response = await client.post("/kb/ingest", json=payload, headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "accepted"
    assert data["documentId"] == "doc-ingest-001"


@pytest.mark.asyncio
async def test_ingest_missing_auth_returns_403(client: httpx.AsyncClient) -> None:
    """Ingest endpoint must reject requests without a service secret."""
    payload = {
        "bot_id": "some-bot",
        "document_id": "doc-001",
        "doc_type": "pdf",
    }
    response = await client.post("/kb/ingest", json=payload)

    assert response.status_code == 403


@pytest.mark.asyncio
async def test_ingest_missing_bot_id_returns_422(
    client: httpx.AsyncClient,
    auth_headers: dict[str, str],
) -> None:
    """Ingest endpoint must return 422 Unprocessable Entity when required fields
    are absent from the request body."""
    # Missing bot_id and document_id
    response = await client.post(
        "/kb/ingest",
        json={"doc_type": "pdf"},
        headers=auth_headers,
    )

    assert response.status_code == 422
