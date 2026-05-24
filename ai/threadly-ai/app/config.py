"""App-wide settings loaded from environment variables."""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # Security
    core_service_secret: str = "dev_shared_secret"

    # LLM providers
    anthropic_api_key: str = ""
    openai_api_key: str = ""
    voyage_api_key: str = ""

    # Vector DB
    qdrant_host: str = "localhost"
    qdrant_port: int = 6333

    # Object storage (MinIO / R2)
    s3_endpoint: str = "http://localhost:9000"
    s3_bucket: str = "threadly-kb"
    s3_access_key: str = "minioadmin"
    s3_secret_key: str = "minioadmin"

    # Models
    default_llm_provider: str = "anthropic"  # anthropic | openai
    anthropic_model: str = "claude-sonnet-4-5"
    openai_model: str = "gpt-4o"
    embedding_model: str = "voyage-3-lite"  # voyage-3-lite | text-embedding-3-small
    fallback_provider: str = "openai"

    # RAG
    rag_top_k: int = 8
    rag_top_k_rerank: int = 3
    chunk_size: int = 512
    chunk_overlap: int = 64

    # Langfuse
    langfuse_public_key: str = ""
    langfuse_secret_key: str = ""
    langfuse_host: str = "http://localhost:3003"

    # Core callback URL
    core_url: str = "http://localhost:8080"


settings = Settings()
