"""Tests for configuration module."""

import os

from app.config import Settings


def test_settings_load():
    """Test settings can be loaded."""
    settings = Settings()
    assert settings.APP_NAME == "Threadly AI"
    assert settings.APP_VERSION == "0.1.0"
    assert settings.DEFAULT_LLM_PROVIDER in ["anthropic", "openai", "gemini"]


def test_settings_from_env():
    """Test settings load from environment variables."""
    os.environ["APP_NAME"] = "Test AI"
    os.environ["DEBUG"] = "true"

    settings = Settings()
    assert settings.APP_NAME == "Test AI"
    assert settings.DEBUG is True

    # Cleanup
    del os.environ["APP_NAME"]
    del os.environ["DEBUG"]


def test_settings_validation():
    """Test settings field validation."""
    settings = Settings(
        DEFAULT_TEMPERATURE=0.5,
        DEFAULT_MAX_TOKENS=1500,
    )
    assert settings.DEFAULT_TEMPERATURE == 0.5
    assert settings.DEFAULT_MAX_TOKENS == 1500
