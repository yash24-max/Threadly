"""Embedding generation and management."""

from abc import ABC, abstractmethod
from typing import Optional

import numpy as np
from sentence_transformers import SentenceTransformer

from app.config import settings
from app.logger import get_logger

logger = get_logger(__name__)


class EmbeddingProvider(ABC):
    """Abstract base class for embedding providers."""

    @abstractmethod
    def embed(self, texts: list[str]) -> np.ndarray:
        """Generate embeddings for texts.

        Args:
            texts: List of text strings to embed

        Returns:
            NumPy array of shape (len(texts), embedding_dim)
        """
        pass

    @abstractmethod
    def embed_single(self, text: str) -> np.ndarray:
        """Generate embedding for single text.

        Args:
            text: Text string to embed

        Returns:
            1D NumPy array of embeddings
        """
        pass

    @property
    @abstractmethod
    def embedding_dim(self) -> int:
        """Get embedding dimension."""
        pass


class LocalEmbeddingProvider(EmbeddingProvider):
    """Local embedding provider using sentence-transformers."""

    def __init__(self, model_name: str = "all-MiniLM-L6-v2") -> None:
        """Initialize local embedding model.

        Args:
            model_name: HuggingFace model identifier
        """
        self.model_name = model_name
        try:
            self.model = SentenceTransformer(model_name)
            logger.info(f"Loaded embedding model: {model_name}")
        except Exception as e:
            logger.error(f"Failed to load embedding model {model_name}: {e}")
            raise

    def embed(self, texts: list[str]) -> np.ndarray:
        """Generate embeddings for multiple texts."""
        if not texts:
            return np.array([])
        try:
            embeddings = self.model.encode(texts, convert_to_numpy=True)
            return embeddings
        except Exception as e:
            logger.error(f"Error generating embeddings: {e}")
            raise

    def embed_single(self, text: str) -> np.ndarray:
        """Generate embedding for single text."""
        embeddings = self.embed([text])
        return embeddings[0] if len(embeddings) > 0 else np.array([])

    @property
    def embedding_dim(self) -> int:
        """Get embedding dimension."""
        return self.model.get_sentence_embedding_dimension()


class VoyageEmbeddingProvider(EmbeddingProvider):
    """Voyage AI embedding provider."""

    def __init__(self, api_key: Optional[str] = None) -> None:
        """Initialize Voyage embeddings.

        Args:
            api_key: Voyage API key (defaults to VOYAGE_API_KEY env var)
        """
        import voyageai

        self.api_key = api_key or settings.VOYAGE_API_KEY
        if not self.api_key:
            raise ValueError("Voyage API key not configured")

        self.client = voyageai.Client(api_key=self.api_key)
        self._embedding_dim = 1024  # Default for voyage-3-lite
        logger.info("Initialized Voyage AI embedding provider")

    def embed(self, texts: list[str]) -> np.ndarray:
        """Generate embeddings using Voyage API."""
        if not texts:
            return np.array([])
        try:
            response = self.client.embed(texts, model="voyage-3-lite")
            embeddings = np.array(response.embeddings)
            return embeddings
        except Exception as e:
            logger.error(f"Error with Voyage embeddings: {e}")
            raise

    def embed_single(self, text: str) -> np.ndarray:
        """Generate embedding for single text."""
        embeddings = self.embed([text])
        return embeddings[0] if len(embeddings) > 0 else np.array([])

    @property
    def embedding_dim(self) -> int:
        """Get embedding dimension."""
        return self._embedding_dim


def get_embedding_provider() -> EmbeddingProvider:
    """Factory function to get embedding provider.

    Returns:
        Embedding provider instance based on configuration
    """
    if settings.EMBEDDING_PROVIDER == "voyage":
        return VoyageEmbeddingProvider(settings.VOYAGE_API_KEY)
    else:
        return LocalEmbeddingProvider(settings.EMBEDDING_MODEL)
