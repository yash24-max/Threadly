"""RAG pipeline for document ingestion and retrieval."""

import uuid
from datetime import datetime
from pathlib import Path
from typing import Optional

from qdrant_client import QdrantClient
from qdrant_client.models import Distance, PointStruct, VectorParams

from app.config import settings
from app.logger import get_logger
from app.rag.embeddings import get_embedding_provider
from app.rag.parser import parse_document

logger = get_logger(__name__)


class RAGPipeline:
    """RAG pipeline for document management and semantic search."""

    def __init__(self) -> None:
        """Initialize RAG pipeline."""
        self.qdrant_client = self._init_qdrant()
        self.embedding_provider = get_embedding_provider()
        logger.info("Initialized RAG pipeline")

    def _init_qdrant(self) -> QdrantClient:
        """Initialize Qdrant client."""
        try:
            client = QdrantClient(
                host=settings.QDRANT_HOST,
                port=settings.QDRANT_PORT,
                api_key=settings.QDRANT_API_KEY if settings.QDRANT_API_KEY else None,
                timeout=settings.QDRANT_TIMEOUT,
            )
            # Test connection
            client.get_collections()
            logger.info("Connected to Qdrant successfully")
            return client
        except Exception as e:
            logger.error(f"Failed to connect to Qdrant: {e}")
            raise

    def _get_or_create_collection(self, collection_name: str) -> None:
        """Get or create a collection in Qdrant.

        Args:
            collection_name: Name of the collection
        """
        try:
            # Try to get existing collection
            self.qdrant_client.get_collection(collection_name)
            logger.info(f"Using existing collection: {collection_name}")
        except Exception:
            # Create new collection
            self.qdrant_client.create_collection(
                collection_name=collection_name,
                vectors_config=VectorParams(
                    size=self.embedding_provider.embedding_dim,
                    distance=Distance.COSINE,
                ),
            )
            logger.info(f"Created new collection: {collection_name}")

    def _chunk_text(self, text: str) -> list[str]:
        """Split text into semantic chunks.

        Args:
            text: Text to chunk

        Returns:
            List of text chunks
        """
        words = text.split()
        chunks = []
        current_chunk = []
        current_size = 0

        for word in words:
            word_tokens = len(word.split())
            if current_size + word_tokens > settings.CHUNK_SIZE:
                if current_chunk:
                    chunks.append(" ".join(current_chunk))
                current_chunk = [word]
                current_size = word_tokens
            else:
                current_chunk.append(word)
                current_size += word_tokens

        if current_chunk:
            chunks.append(" ".join(current_chunk))

        # Add overlap
        if len(chunks) > 1:
            overlapped = []
            for i, chunk in enumerate(chunks):
                if i > 0:
                    overlap_start = chunks[i - 1]
                    words = overlap_start.split()
                    overlap = " ".join(words[-settings.CHUNK_OVERLAP // 2 :])
                    overlapped.append(overlap + " " + chunk)
                else:
                    overlapped.append(chunk)
            chunks = overlapped

        return [c for c in chunks if len(c.split()) >= settings.MIN_CHUNK_SIZE]

    def ingest_document(self, doc_path: str, bot_id: str) -> dict:
        """Ingest and store document in vector DB.

        Args:
            doc_path: Path to document file
            bot_id: Bot/workspace ID

        Returns:
            Ingestion result dict with doc_id, chunk_count, etc.
        """
        ingestion_id = str(uuid.uuid4())
        doc_id = str(uuid.uuid4())

        try:
            # Parse document
            logger.info(f"Parsing document: {doc_path}")
            text = parse_document(doc_path)
            logger.info(f"Parsed document, length: {len(text)} chars")

            # Chunk text
            chunks = self._chunk_text(text)
            if not chunks:
                logger.warning("No chunks generated from document")
                return {
                    "ingestion_id": ingestion_id,
                    "doc_id": doc_id,
                    "status": "failed",
                    "error": "No content extracted from document",
                }

            logger.info(f"Generated {len(chunks)} chunks")

            # Get collection name
            collection_name = f"bot_{bot_id}"
            self._get_or_create_collection(collection_name)

            # Generate embeddings
            logger.info("Generating embeddings")
            embeddings = self.embedding_provider.embed(chunks)

            # Store in Qdrant
            points = [
                PointStruct(
                    id=hash(chunk) % (10**9),  # Use hash as ID
                    vector=embeddings[i].tolist(),
                    payload={
                        "doc_id": doc_id,
                        "chunk_index": i,
                        "text": chunk,
                        "source": Path(doc_path).name,
                        "bot_id": bot_id,
                        "created_at": datetime.utcnow().isoformat(),
                    },
                )
                for i, chunk in enumerate(chunks)
            ]

            self.qdrant_client.upsert(
                collection_name=collection_name,
                points=points,
            )

            logger.info(f"Successfully ingested {len(chunks)} chunks for doc {doc_id}")

            return {
                "ingestion_id": ingestion_id,
                "doc_id": doc_id,
                "status": "completed",
                "chunk_count": len(chunks),
                "created_at": datetime.utcnow().isoformat(),
            }

        except Exception as e:
            logger.error(f"Error ingesting document: {e}")
            return {
                "ingestion_id": ingestion_id,
                "doc_id": doc_id,
                "status": "failed",
                "error": str(e),
            }

    def search(
        self,
        query: str,
        bot_id: str,
        top_k: int = 5,
        score_threshold: Optional[float] = None,
    ) -> list[dict]:
        """Semantic search across knowledge base.

        Args:
            query: Search query
            bot_id: Bot/workspace ID
            top_k: Number of top results
            score_threshold: Minimum score threshold (0-1)

        Returns:
            List of search results with passages and scores
        """
        if score_threshold is None:
            score_threshold = settings.KB_SCORE_THRESHOLD

        try:
            # Generate query embedding
            query_embedding = self.embedding_provider.embed_single(query)

            # Search in Qdrant
            collection_name = f"bot_{bot_id}"
            results = self.qdrant_client.search(
                collection_name=collection_name,
                query_vector=query_embedding.tolist(),
                limit=top_k,
                score_threshold=score_threshold,
            )

            # Format results
            passages = []
            for result in results:
                passages.append(
                    {
                        "passage": result.payload["text"],
                        "score": result.score,
                        "doc_id": result.payload["doc_id"],
                        "source": result.payload["source"],
                        "chunk_index": result.payload["chunk_index"],
                    }
                )

            logger.info(f"Found {len(passages)} relevant passages")
            return passages

        except Exception as e:
            logger.error(f"Error searching knowledge base: {e}")
            return []

    def delete_document(self, doc_id: str, bot_id: str) -> bool:
        """Delete document from knowledge base.

        Args:
            doc_id: Document ID
            bot_id: Bot/workspace ID

        Returns:
            True if successful
        """
        try:
            collection_name = f"bot_{bot_id}"
            # Delete points with matching doc_id
            self.qdrant_client.delete(
                collection_name=collection_name,
                points_selector={
                    "filter": {
                        "must": [
                            {
                                "key": "doc_id",
                                "match": {"value": doc_id},
                            }
                        ]
                    }
                },
            )
            logger.info(f"Deleted document {doc_id}")
            return True
        except Exception as e:
            logger.error(f"Error deleting document: {e}")
            return False

    def get_kb_status(self, bot_id: str) -> dict:
        """Get knowledge base status.

        Args:
            bot_id: Bot/workspace ID

        Returns:
            Status dict with counts and info
        """
        try:
            collection_name = f"bot_{bot_id}"
            collection_info = self.qdrant_client.get_collection(collection_name)
            return {
                "bot_id": bot_id,
                "collection_name": collection_name,
                "point_count": collection_info.points_count,
                "status": "ready",
            }
        except Exception as e:
            logger.error(f"Error getting KB status: {e}")
            return {
                "bot_id": bot_id,
                "status": "not_found",
                "error": str(e),
            }
