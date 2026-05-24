-- Knowledge Base Schema for Threadly Microservices
-- Supports document management, chunking, embeddings, and vector search

-- KB_DOCUMENT: Main table for knowledge base documents
CREATE TABLE kb_document (
    id VARCHAR(36) PRIMARY KEY,
    bot_id VARCHAR(36) NOT NULL,
    org_id VARCHAR(36) NOT NULL,
    filename VARCHAR(500) NOT NULL,
    file_url TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    metadata TEXT,
    chunk_count INT NOT NULL DEFAULT 0,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kb_document_bot_id ON kb_document(bot_id);
CREATE INDEX idx_kb_document_org_id ON kb_document(org_id);
CREATE INDEX idx_kb_document_status ON kb_document(status);
CREATE INDEX idx_kb_document_upload_date ON kb_document(upload_date);

-- KB_CHUNK: Semantic chunks of documents
CREATE TABLE kb_chunk (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    bot_id VARCHAR(36) NOT NULL,
    chunk_number INT NOT NULL,
    content TEXT NOT NULL,
    tokens INT NOT NULL,
    embedding_vector BYTEA,
    metadata TEXT,
    is_embedded BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(500),
    CONSTRAINT fk_kb_chunk_document FOREIGN KEY (document_id)
        REFERENCES kb_document(id) ON DELETE CASCADE
);

CREATE INDEX idx_kb_chunk_document_id ON kb_chunk(document_id);
CREATE INDEX idx_kb_chunk_bot_id ON kb_chunk(bot_id);
CREATE INDEX idx_kb_chunk_is_embedded ON kb_chunk(is_embedded);
CREATE INDEX idx_kb_chunk_created_at ON kb_chunk(created_at);

-- KB_EMBEDDING: Embedding vectors for chunks
CREATE TABLE kb_embedding (
    id VARCHAR(36) PRIMARY KEY,
    chunk_id VARCHAR(36) NOT NULL UNIQUE,
    embedding_model VARCHAR(100) NOT NULL,
    embedding_json TEXT NOT NULL,
    dimension INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_kb_embedding_chunk FOREIGN KEY (chunk_id)
        REFERENCES kb_chunk(id) ON DELETE CASCADE
);

CREATE INDEX idx_kb_embedding_chunk_id ON kb_embedding(chunk_id);
CREATE INDEX idx_kb_embedding_model ON kb_embedding(embedding_model);
CREATE INDEX idx_kb_embedding_created_at ON kb_embedding(created_at);

-- KB_INDEXING_JOB: Async indexing job tracking
CREATE TABLE kb_indexing_job (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    bot_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    progress INT NOT NULL DEFAULT 0,
    total_chunks INT,
    processed_chunks INT NOT NULL DEFAULT 0,
    embedded_chunks INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    error_stack_trace TEXT,
    CONSTRAINT fk_kb_indexing_job_document FOREIGN KEY (document_id)
        REFERENCES kb_document(id) ON DELETE CASCADE
);

CREATE INDEX idx_kb_indexing_job_document_id ON kb_indexing_job(document_id);
CREATE INDEX idx_kb_indexing_job_status ON kb_indexing_job(status);
CREATE INDEX idx_kb_indexing_job_bot_id ON kb_indexing_job(bot_id);
CREATE INDEX idx_kb_indexing_job_created_at ON kb_indexing_job(created_at);

-- Partitioning hints for large-scale deployments
-- For PostgreSQL: PARTITION BY RANGE (upload_date)
-- For MySQL: Can add PARTITION BY RANGE (YEAR(upload_date))

-- Sample data (optional, for testing)
-- INSERT INTO kb_document (id, bot_id, org_id, filename, file_url, file_size, content_type, status, chunk_count)
-- VALUES ('test-doc-1', 'bot-1', 'org-1', 'test.pdf', 's3://bucket/test.pdf', 1024, 'application/pdf', 'PENDING', 0);
