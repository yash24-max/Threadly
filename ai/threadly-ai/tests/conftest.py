"""Shared pytest fixtures for threadly-ai test suite."""
from __future__ import annotations

import pytest
import httpx
import respx

from app.main import app


@pytest.fixture
async def client():
    """ASGI test client wrapping the FastAPI application.

    Uses httpx.AsyncClient so all tests are async-native with no real
    network calls leaving the process.
    """
    async with httpx.AsyncClient(app=app, base_url="http://test") as c:
        yield c


@pytest.fixture
def auth_headers() -> dict[str, str]:
    """Returns the internal service-secret header used by threadly-core.

    The value matches ``core_service_secret`` default in ``app/config.py``.
    """
    return {"X-Service-Secret": "dev_shared_secret"}


@pytest.fixture
def mock_anthropic(respx_mock: respx.MockRouter) -> respx.MockRouter:
    """Mocks the Anthropic Messages API so no real API key is needed.

    Returns a canned single-turn response with predictable token counts.
    """
    respx_mock.post("https://api.anthropic.com/v1/messages").mock(
        return_value=httpx.Response(
            200,
            json={
                "id": "msg_test_001",
                "type": "message",
                "role": "assistant",
                "content": [{"type": "text", "text": "Hello! How can I help you?"}],
                "model": "claude-3-5-haiku-20241022",
                "stop_reason": "end_turn",
                "stop_sequence": None,
                "usage": {"input_tokens": 50, "output_tokens": 20},
            },
        )
    )
    return respx_mock


@pytest.fixture
def mock_openai(respx_mock: respx.MockRouter) -> respx.MockRouter:
    """Mocks the OpenAI Chat Completions API for fallback-provider tests."""
    respx_mock.post("https://api.openai.com/v1/chat/completions").mock(
        return_value=httpx.Response(
            200,
            json={
                "id": "chatcmpl-test001",
                "object": "chat.completion",
                "choices": [
                    {
                        "index": 0,
                        "message": {"role": "assistant", "content": "Fallback response from OpenAI."},
                        "finish_reason": "stop",
                    }
                ],
                "usage": {"prompt_tokens": 40, "completion_tokens": 15, "total_tokens": 55},
                "model": "gpt-4o",
            },
        )
    )
    return respx_mock


@pytest.fixture
def mock_anthropic_failing(respx_mock: respx.MockRouter) -> respx.MockRouter:
    """Mocks Anthropic to return a 500 error to trigger the fallback path."""
    respx_mock.post("https://api.anthropic.com/v1/messages").mock(
        return_value=httpx.Response(500, json={"error": {"message": "Internal server error"}})
    )
    return respx_mock
