"""Application configuration management."""

from typing import Literal

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # Application metadata
    APP_NAME: str = "Threadly AI"
    APP_VERSION: str = "0.1.0"
    DEBUG: bool = False

    # Server configuration
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    WORKERS: int = 4

    # CORS & security
    CORS_ORIGINS: list[str] = ["*"]
    API_KEY_HEADER: str = "X-API-Key"
    API_KEY: str = ""  # Must be set in production

    # LLM configuration
    DEFAULT_LLM_PROVIDER: Literal["anthropic", "openai", "gemini"] = "anthropic"
    ANTHROPIC_API_KEY: str = ""
    ANTHROPIC_MODEL: str = "claude-3-5-sonnet-20241022"
    OPENAI_API_KEY: str = ""
    OPENAI_MODEL: str = "gpt-4-turbo"
    GOOGLE_API_KEY: str = ""
    GOOGLE_MODEL: str = "gemini-pro"

    # LLM defaults
    DEFAULT_TEMPERATURE: float = 0.7
    DEFAULT_MAX_TOKENS: int = 2000
    REQUEST_TIMEOUT: int = 300

    # Embedding configuration
    EMBEDDING_MODEL: str = "all-MiniLM-L6-v2"
    EMBEDDING_PROVIDER: Literal["local", "voyage"] = "local"
    VOYAGE_API_KEY: str = ""
    EMBEDDING_DIMENSION: int = 384  # For all-MiniLM-L6-v2

    # Vector database (Qdrant)
    QDRANT_HOST: str = "localhost"
    QDRANT_PORT: int = 6333
    QDRANT_GRPC_PORT: int = 6334
    QDRANT_API_KEY: str = ""
    QDRANT_TIMEOUT: int = 30

    # Document processing
    MAX_DOCUMENT_SIZE_MB: int = 50
    CHUNK_SIZE: int = 1000
    CHUNK_OVERLAP: int = 200
    MIN_CHUNK_SIZE: int = 100

    # Conversation service
    CONVERSATION_SERVICE_URL: str = "http://localhost:8080"
    CONVERSATION_API_KEY: str = ""

    # Redis caching
    REDIS_URL: str = "redis://localhost:6379"
    CACHE_TTL_SECONDS: int = 3600

    # RAG configuration
    KB_SEARCH_TOP_K: int = 5
    KB_SCORE_THRESHOLD: float = 0.5
    ENABLE_RERANKING: bool = False

    # Memory configuration
    MAX_CONVERSATION_TURNS: int = 10
    MEMORY_SUMMARY_THRESHOLD: int = 5

    # Logging
    LOG_LEVEL: str = "INFO"
    LOG_FORMAT: str = "json"  # or "text"

    class Config:
        """Pydantic configuration."""

        env_file = ".env"
        case_sensitive = True
        extra = "ignore"


# Global settings instance
settings = Settings()
