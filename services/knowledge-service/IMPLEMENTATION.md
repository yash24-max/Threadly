# Knowledge-Service Implementation Guide

Complete production-ready implementation of the Threadly Knowledge Base Service with RAG capabilities.

## Overview

The Knowledge Service provides:
- **Document Management**: Upload, store, and organize knowledge base documents
- **Semantic Chunking**: Intelligent document splitting with semantic boundaries
- **Vector Embeddings**: Integration with multiple embedding providers (Voyage, OpenAI, local)
- **Vector Search**: Qdrant-powered similarity search with metadata filtering
- **RAG Pipeline**: Format retrieved chunks as LLM-ready context
- **Async Processing**: Non-blocking document ingestion with job tracking
- **Multi-tenancy**: Bot and organization-level isolation

## Architecture

### Directory Structure

```
knowledge-service/
├── src/main/java/dev/threadly/knowledge/
│   ├── controller/                    # REST API endpoints
│   │   ├── KbDocumentController.java
│   │   ├── KbSearchController.java
│   │   └── KbIndexingController.java
│   ├── entity/                        # JPA entities
│   │   ├── KbDocument.java
│   │   ├── KbChunk.java
│   │   ├── KbEmbedding.java
│   │   └── KbIndexingJob.java
│   ├── repository/                    # Data access layer
│   │   ├── KbDocumentRepository.java
│   │   ├── KbChunkRepository.java
│   │   ├── KbEmbeddingRepository.java
│   │   └── KbIndexingJobRepository.java
│   ├── service/                       # Business logic
│   │   ├── KbDocumentService.java
│   │   ├── KbIngestionService.java
│   │   ├── KbSearchService.java
│   │   ├── DocumentParserService.java
│   │   ├── ChunkingService.java
│   │   ├── EmbeddingService.java
│   │   ├── VectorDbService.java
│   │   └── RagPipeline.java
│   ├── dto/                           # Data transfer objects
│   │   ├── KbDocumentDto.java
│   │   ├── KbChunkDto.java
│   │   ├── KbSearchRequest.java
│   │   ├── KbSearchResponse.java
│   │   ├── KbSearchResultDto.java
│   │   ├── RagContextDto.java
│   │   └── KbIndexingJobDto.java
│   ├── exception/                     # Custom exceptions
│   │   ├── DocumentNotFoundException.java
│   │   ├── DocumentIngestionException.java
│   │   ├── VectorSearchException.java
│   │   └── GlobalExceptionHandler.java
│   ├── event/                         # Kafka event listeners
│   │   ├── KbDocumentUploadedEventListener.java
│   │   └── KbIndexingCompletedEventListener.java
│   ├── config/                        # Spring configuration
│   │   ├── QdrantClientConfig.java
│   │   └── EmbeddingModelConfig.java
│   └── KnowledgeServiceApplication.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V6__knowledge_schema.sql
└── DEPENDENCIES.md
```

## Database Schema

### kb_document
- `id` (UUID): Unique identifier
- `bot_id` (UUID): Bot ownership for multi-tenancy
- `org_id` (UUID): Organization for isolation
- `filename`: Original filename
- `file_url`: S3 or storage URL
- `file_size`: Size in bytes
- `content_type`: MIME type
- `status`: PENDING, INDEXED, FAILED
- `chunk_count`: Number of chunks created
- `metadata`: Custom JSON metadata
- `upload_date`: Timestamp
- `updated_date`: Timestamp

Indexes: bot_id, org_id, status, upload_date

### kb_chunk
- `id` (UUID): Unique identifier
- `document_id` (FK): Reference to document
- `bot_id` (UUID): Multi-tenant isolation
- `chunk_number`: Sequential ordering
- `content`: Chunk text
- `tokens`: Token count estimate
- `embedding_vector`: Raw embedding bytes (optional)
- `metadata`: JSON with page/section info
- `is_embedded`: Boolean flag
- `source`: Page or section reference
- `created_at`: Timestamp

Indexes: document_id, bot_id, is_embedded, created_at

### kb_embedding
- `id` (UUID): Unique identifier
- `chunk_id` (FK, UNIQUE): Reference to chunk
- `embedding_model`: Model name (e.g., "voyage-ai-3")
- `embedding_json`: Vector as JSON array
- `dimension`: Vector dimension (1024, 1536, etc)
- `version`: Model version
- `created_at`: Timestamp

Indexes: chunk_id, embedding_model, created_at

### kb_indexing_job
- `id` (UUID): Unique identifier
- `document_id` (FK): Document being indexed
- `bot_id` (UUID): Multi-tenant isolation
- `status`: PENDING, PROCESSING, COMPLETE, FAILED
- `progress`: 0-100%
- `total_chunks`: Chunks to process
- `processed_chunks`: Chunks done
- `embedded_chunks`: Chunks with embeddings
- `created_at`: Job creation time
- `started_at`: When processing began
- `completed_at`: When finished
- `error_message`: Failure details
- `error_stack_trace`: Stack trace

Indexes: document_id, status, bot_id, created_at

## API Endpoints

### Document Management

**Upload Document**
```
POST /api/v1/kb/documents
Content-Type: multipart/form-data

Parameters:
  - botId (required)
  - orgId (required)
  - file (required)
  - metadata (optional)

Response:
  {
    "id": "doc-uuid",
    "filename": "document.pdf",
    "status": "PENDING",
    "uploadDate": "2026-05-24T12:00:00Z"
  }
```

**List Documents**
```
GET /api/v1/kb/documents?botId=bot-123&status=INDEXED
```

**Get Document**
```
GET /api/v1/kb/documents/{documentId}?botId=bot-123
```

**Update Metadata**
```
PATCH /api/v1/kb/documents/{documentId}?botId=bot-123
Content-Type: application/json

{
  "tags": ["important", "faq"],
  "category": "product-help"
}
```

**Delete Document**
```
DELETE /api/v1/kb/documents/{documentId}?botId=bot-123
```

### Search

**Semantic Search**
```
POST /api/v1/kb/search
{
  "botId": "bot-123",
  "query": "How do I reset my password?",
  "limit": 10,
  "minScore": 0.5
}

Response:
{
  "query": "How do I reset my password?",
  "totalResults": 3,
  "resultCount": 3,
  "searchMode": "semantic",
  "results": [
    {
      "chunkId": "chunk-123",
      "content": "To reset your password...",
      "filename": "help.pdf",
      "source": "Page 2",
      "relevanceScore": 0.92,
      "tokens": 150
    }
  ],
  "executionTimeMs": 145
}
```

**Hybrid Search**
```
POST /api/v1/kb/search/hybrid
```

**Text Search**
```
POST /api/v1/kb/search/text
```

**RAG Context**
```
POST /api/v1/kb/search/rag-context
{
  "botId": "bot-123",
  "query": "What are the features?"
}

Response:
{
  "formattedContext": "Context from knowledge base:\n\n[chunks]",
  "totalTokens": 2500,
  "chunkCount": 5,
  "truncated": false,
  "confidence": 0.87,
  "citations": [...]
}
```

### Indexing Jobs

**List Jobs**
```
GET /api/v1/kb/indexing-jobs?botId=bot-123&status=PROCESSING
```

**Get Job**
```
GET /api/v1/kb/indexing-jobs/{jobId}
```

**Reindex Document**
```
POST /api/v1/kb/indexing-jobs/reindex/{documentId}?botId=bot-123
```

## Configuration

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=threadly
DB_USER=threadly
DB_PASSWORD=dev

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Qdrant Vector Database
QDRANT_HOST=localhost
QDRANT_PORT=6334
QDRANT_API_KEY=

# Embeddings (choose one)
VOYAGE_API_KEY=your-key
OPENAI_API_KEY=your-key
LOCAL_MODEL_PATH=/path/to/model

# Consul Discovery
CONSUL_HOST=localhost
CONSUL_PORT=8500

# Observability
OTEL_EXPORTER_OTLP_ENDPOINT=localhost:4317
```

### application.yml Settings

```yaml
# Chunking
chunking:
  max-chunk-size: 1000          # tokens
  min-chunk-size: 100
  overlap-size: 100
  strategy: semantic            # or "fixed" or "sentence"

# Embedding
embedding:
  default-model: voyage-ai-3
  model-dimension: 1024
  batch-size: 32

# RAG Pipeline
rag:
  max-context-tokens: 4000
  chunk-separator: "\n---\n"
```

## Document Processing Pipeline

1. **Upload**: File stored, document entity created with PENDING status
2. **Parsing**: Extract text from PDF/DOCX/HTML/TXT/MD
3. **Chunking**: Split into semantic chunks (500-1000 tokens with overlap)
4. **Embedding**: Generate vectors using Voyage/OpenAI/local model
5. **Vector Storage**: Store in Qdrant with metadata
6. **Database Storage**: Save chunks and embeddings to PostgreSQL
7. **Completion**: Update document status to INDEXED

All steps tracked in `kb_indexing_job` with progress updates.

## Search Workflow

### Semantic Search
1. Generate query embedding using same model as document
2. Search Qdrant for similar vectors
3. Return top-K chunks with relevance scores
4. Enrich with database metadata (filename, page numbers, etc)

### Hybrid Search
1. Run semantic search in parallel
2. Run text-based BM25 search
3. Merge results, deduplicating by chunk ID
4. Apply optional reranking (Cohere API)
5. Sort by combined relevance score

### RAG Context Formatting
1. Take search results
2. Build formatted context string with headers, sources, citations
3. Track token counts for context limits
4. Return structured RagContextDto for LLM injection

## Multi-Tenancy Implementation

- **Bot-level isolation**: All tables have `bot_id` column
- **Qdrant collections per bot**: `bot_bot-uuid` collection name
- **Query filtering**: All queries include `bot_id` WHERE clause
- **Organization-level cleanup**: Can delete all bot documents by org_id

## Performance Considerations

### Indexing
- Document parsing: Streaming to avoid memory bloat
- Chunking: Semantic boundaries reduce redundant embeddings
- Batch embedding: Process 32 chunks in parallel
- Async processing: Non-blocking via Spring async + Kafka

### Search
- Vector search in Qdrant: ~100ms for 100K vectors
- PostgreSQL for metadata lookups: Indexed on bot_id, document_id
- Connection pooling: HikariCP with 10 connections

### Optimization Tips
- Enable Qdrant HNSW indexing for faster searches
- Partition kb_chunk and kb_embedding by bot_id
- Archive old indexing jobs regularly
- Monitor Qdrant disk usage (vectors can be large)

## Testing

### Unit Tests
- Mock repositories
- Test chunking algorithms
- Test embedding generation

### Integration Tests
- Use Testcontainers for PostgreSQL and Qdrant
- Test full ingestion pipeline
- Test search across various document types

### Load Testing
- Benchmark embedding generation with batches
- Test Qdrant search latency at scale
- Monitor database query performance

## Deployment

### Prerequisites
- Java 21+
- PostgreSQL 13+
- Qdrant 1.7+
- Kafka 3.0+
- Consul (optional, for service discovery)

### Build
```bash
mvn clean package -DskipTests
```

### Run
```bash
java -jar knowledge-service-0.1.0.jar \
  --spring.datasource.url=jdbc:postgresql://db:5432/threadly \
  --qdrant.host=qdrant \
  --embedding.default-model=voyage-ai-3
```

### Docker Compose (Development)
```yaml
services:
  knowledge-db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: threadly
      POSTGRES_USER: threadly
      POSTGRES_PASSWORD: dev

  qdrant:
    image: qdrant/qdrant:latest
    ports:
      - "6334:6334"

  knowledge-service:
    build: .
    ports:
      - "3006:3006"
    depends_on:
      - knowledge-db
      - qdrant
    environment:
      DB_HOST: knowledge-db
      QDRANT_HOST: qdrant
```

## Future Enhancements

1. **Reranking**: Integrate Cohere or LLM-based reranking
2. **Batch Embedding**: GPU acceleration for large-scale indexing
3. **Multi-language**: Language detection and translate embeddings
4. **Document Versioning**: Track document update history
5. **Smart Chunking**: Extract table of contents for better segmentation
6. **Cache Layer**: Redis cache for frequent searches
7. **Analytics**: Track search queries and performance
8. **Citation Format**: BibTeX, APA, etc. export

## Support & Troubleshooting

**Connection to Qdrant fails**
- Check Qdrant is running: `curl http://localhost:6334/health`
- Verify network connectivity and API key

**Embeddings taking too long**
- Increase batch_size in config
- Consider using local model instead of API
- Check API rate limits (Voyage, OpenAI)

**Search returns no results**
- Verify documents are INDEXED status
- Check embedding model matches between documents and query
- Try text search to verify documents exist

**Database migrations fail**
- Ensure default_schema matches your PostgreSQL setup
- Check Flyway baseline status
- Run manual migrations if needed

## Contact

For issues or questions, contact the Threadly Platform team.
