"""Tests for main FastAPI application."""

from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_root():
    """Test root endpoint."""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Threadly AI"
    assert "version" in data
    assert "endpoints" in data


def test_health():
    """Test health endpoint."""
    response = client.get("/health")
    assert response.status_code in [200, 503]  # May fail if Qdrant not available
    data = response.json()
    assert "status" in data or "alive" in data


def test_ready():
    """Test readiness probe."""
    response = client.get("/health/ready")
    assert response.status_code in [200, 503]


def test_live():
    """Test liveness probe."""
    response = client.get("/health/live")
    assert response.status_code == 200
    data = response.json()
    assert data["alive"] is True


def test_metrics():
    """Test metrics endpoint."""
    response = client.get("/health/metrics")
    assert response.status_code == 200
    data = response.json()
    assert "app_version" in data
    assert "default_llm_provider" in data


def test_llm_complete_missing_prompt():
    """Test LLM complete with missing prompt."""
    response = client.post(
        "/llm/complete",
        json={"provider": "anthropic"},
    )
    assert response.status_code == 422  # Validation error


def test_llm_classify_missing_params():
    """Test LLM classify with missing parameters."""
    response = client.post(
        "/llm/classify",
        json={"text": "test"},
    )
    assert response.status_code == 422


def test_rag_search_missing_params():
    """Test RAG search with missing parameters."""
    response = client.post(
        "/kb/search",
        json={"query": "test"},
    )
    assert response.status_code == 422
