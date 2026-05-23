"""Tests for GET /health — the liveness probe endpoint."""
import pytest
import httpx


@pytest.mark.asyncio
async def test_health_returns_ok(client: httpx.AsyncClient) -> None:
    """Health endpoint must respond with HTTP 200 and status='ok'."""
    response = await client.get("/health")

    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"


@pytest.mark.asyncio
async def test_health_shows_service_name(client: httpx.AsyncClient) -> None:
    """Health endpoint must include the service name so load-balancer health
    checks can distinguish this sidecar from other services."""
    response = await client.get("/health")

    assert response.status_code == 200
    data = response.json()
    assert data["service"] == "threadly-ai"
