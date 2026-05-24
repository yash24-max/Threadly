"""Tests for RAG document parser module."""

import tempfile
from pathlib import Path

import pytest

from app.rag.parser import parse_document, _parse_txt


def test_parse_txt():
    """Test parsing plain text file."""
    with tempfile.NamedTemporaryFile(mode="w", suffix=".txt", delete=False) as f:
        f.write("This is test content.\nSecond line.")
        f.flush()

        content = _parse_txt(f.name)
        assert "This is test content." in content
        assert "Second line." in content

        Path(f.name).unlink()


def test_parse_document_txt():
    """Test parse_document with TXT file."""
    with tempfile.NamedTemporaryFile(mode="w", suffix=".txt", delete=False) as f:
        f.write("Test content for parsing.")
        f.flush()

        content = parse_document(f.name)
        assert "Test content" in content

        Path(f.name).unlink()


def test_parse_document_unsupported():
    """Test parse_document with unsupported file type."""
    with tempfile.NamedTemporaryFile(suffix=".xyz", delete=False) as f:
        f.write(b"content")
        f.flush()

        with pytest.raises(ValueError, match="Unsupported file format"):
            parse_document(f.name)

        Path(f.name).unlink()
