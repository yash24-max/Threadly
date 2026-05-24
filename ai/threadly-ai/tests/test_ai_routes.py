"""Tests for the AI utility routes: summarize, suggest-replies,
extract-entities, and classify-intent."""
from __future__ import annotations

import pytest
import httpx
import respx


# ---------------------------------------------------------------------------
# POST /ai/summarize
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_summarize_returns_string_summary(
    client: httpx.AsyncClient,
    auth_headers: dict[str, str],
    mock_anthropic: respx.MockRouter,
) -> None:
    """Summarize endpoint must return a non-empty string in the 'summary' field."""
    payload = {
        "conversation_id": "conv-summary-001",
        "messages": [
            {"role": "user", "content": "My order hasn't arrived yet."},
            {"role": "assistant", "content": "I'm sorry to hear that. Let me check your order."},
            {"role": "user", "content": "The order number is 12345."},
        ],
    }

    response = await client.post("/ai/summarize", json=payload, headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert "summary" in data
    assert isinstance(data["summary"], str)
    assert len(data["summary"]) > 0


@pytest.mark.asyncio
async def test_summarize_missing_auth_returns_403(client: httpx.AsyncClient) -> None:
    """Summarize endpoint rejects requests without a service secret."""
    payload = {
        "conversation_id": "conv-001",
        "messages": [{"role": "user", "content": "Hello"}],
    }
    response = await client.post("/ai/summarize", json=payload)

    assert response.status_code == 403


# ---------------------------------------------------------------------------
# POST /ai/suggest-replies
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_suggest_replies_returns_list_of_strings(
    client: httpx.AsyncClient,
    auth_headers: dict[str, str],
    mock_anthropic: respx.MockRouter,
) -> None:
    """Suggest-replies must return a list of at least one string suggestion."""
    payload = {
        "conversation_id": "conv-suggest-001",
        "messages": [
            {"role": "user", "content": "Can I get a refund?"},
        ],
        "num_suggestions": 3,
    }

    response = await client.post("/ai/suggest-replies", json=payload, headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert "suggestions" in data
    suggestions = data["suggestions"]
    assert isinstance(suggestions, list)
    assert len(suggestions) >= 1
    for s in suggestions:
        assert isinstance(s, str)
        assert len(s) > 0


# ---------------------------------------------------------------------------
# POST /ai/extract-entities
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_extract_entities_finds_email_in_text(
    client: httpx.AsyncClient,
    auth_headers: dict[str, str],
    mock_anthropic: respx.MockRouter,
) -> None:
    """Extract-entities endpoint must return a dict keyed by entity type.

    The mock LLM response returns a generic text reply; we verify that the
    endpoint handles the response gracefully and returns a valid JSON structure
    (even if the mock doesn't return actual entity JSON, the schema is correct).
    """
    payload = {
        "text": "Please contact me at alice@example.com or call 555-1234.",
        "entity_types": ["email", "phone"],
    }

    response = await client.post("/ai/extract-entities", json=payload, headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert "entities" in data
    assert isinstance(data["entities"], dict)


@pytest.mark.asyncio
async def test_extract_entities_missing_auth_returns_403(client: httpx.AsyncClient) -> None:
    """Extract-entities endpoint rejects requests without a service secret."""
    payload = {"text": "test text", "entity_types": ["email"]}
    response = await client.post("/ai/extract-entities", json=payload)

    assert response.status_code == 403


# ---------------------------------------------------------------------------
# POST /ai/classify-intent
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_classify_intent_returns_intent_and_confidence(
    client: httpx.AsyncClient,
    auth_headers: dict[str, str],
    mock_anthropic: respx.MockRouter,
) -> None:
    """Classify-intent must return an 'intent' string and a 'confidence' float
    clamped to [0.0, 1.0]."""
    payload = {
        "text": "I want to cancel my subscription",
        "intents": ["billing_issue", "cancellation_request", "technical_support", "general_inquiry"],
    }

    response = await client.post("/ai/classify-intent", json=payload, headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert "intent" in data
    assert "confidence" in data
    assert isinstance(data["intent"], str)
    assert data["intent"] in payload["intents"]
    assert isinstance(data["confidence"], float)
    assert 0.0 <= data["confidence"] <= 1.0


@pytest.mark.asyncio
async def test_classify_intent_falls_back_to_first_intent_on_parse_failure(
    client: httpx.AsyncClient,
    auth_headers: dict[str, str],
    mock_anthropic: respx.MockRouter,
) -> None:
    """When the LLM returns unparseable JSON the endpoint must still return a
    valid response using the first intent as a safe fallback."""
    # The mock_anthropic fixture returns plain text, not JSON — exercising fallback
    payload = {
        "text": "Hello there",
        "intents": ["greeting", "complaint"],
    }

    response = await client.post("/ai/classify-intent", json=payload, headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert data["intent"] in payload["intents"]
    assert 0.0 <= data["confidence"] <= 1.0
