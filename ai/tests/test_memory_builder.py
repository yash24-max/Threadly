"""Tests for conversation memory builder."""

import pytest

from app.memory.builder import MemoryBuilder, MemoryContext


def test_memory_builder_init():
    """Test MemoryBuilder initialization."""
    builder = MemoryBuilder()
    assert builder.http_client is not None


def test_memory_context_model():
    """Test MemoryContext pydantic model."""
    context = MemoryContext(
        system_prompt="Test prompt",
        context_text="Test context",
        token_count=100,
    )
    assert context.system_prompt == "Test prompt"
    assert context.context_text == "Test context"
    assert context.token_count == 100
    assert len(context.recent_turns) == 0


def test_estimate_tokens():
    """Test token estimation."""
    builder = MemoryBuilder()
    text = "This is a test sentence with multiple words in it."
    tokens = builder._estimate_tokens(text)
    # Should be approximately len(text) // 4
    assert tokens > 0
    assert tokens < len(text)


def test_build_system_prompt():
    """Test system prompt building."""
    builder = MemoryBuilder()
    prompt = builder._build_system_prompt("bot123", "Some context")
    assert "helpful customer support assistant" in prompt
    assert "Some context" in prompt


def test_close():
    """Test closing builder."""
    builder = MemoryBuilder()
    builder.close()
    # Should not raise error
    assert True
