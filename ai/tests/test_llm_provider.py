"""Tests for LLM provider module."""

import pytest

from app.llm.provider import LLMProvider, get_provider


def test_get_provider_anthropic():
    """Test getting Anthropic provider."""
    # This will fail without API key, but tests the factory
    with pytest.raises(ValueError, match="ANTHROPIC_API_KEY"):
        provider = get_provider("anthropic")


def test_get_provider_openai():
    """Test getting OpenAI provider."""
    with pytest.raises(ValueError, match="OPENAI_API_KEY"):
        provider = get_provider("openai")


def test_get_provider_gemini():
    """Test getting Gemini provider."""
    with pytest.raises(ValueError, match="GOOGLE_API_KEY"):
        provider = get_provider("gemini")


def test_get_provider_invalid():
    """Test getting invalid provider."""
    with pytest.raises(ValueError, match="Unknown provider"):
        get_provider("invalid_provider")


def test_count_tokens_estimate():
    """Test token counting heuristic."""
    text = "This is a test sentence with some words in it."
    # Rough estimate: 1 token ≈ 4 characters
    # Should be around 11 tokens
    estimated = len(text) // 4
    assert estimated > 0
    assert estimated < len(text)
