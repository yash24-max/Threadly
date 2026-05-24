package dev.threadly.knowledge.repository;

import dev.threadly.knowledge.entity.KbEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for KbEmbedding entity.
 * Provides CRUD operations for embedding vectors.
 */
@Repository
public interface KbEmbeddingRepository extends JpaRepository<KbEmbedding, String> {

  /**
   * Find embedding for a specific chunk.
   *
   * @param chunkId the chunk ID
   * @return optional containing the embedding
   */
  Optional<KbEmbedding> findByChunkId(String chunkId);

  /**
   * Find embeddings for chunks using a specific model.
   *
   * @param embeddingModel the model name
   * @return list of embeddings
   */
  List<KbEmbedding> findByEmbeddingModel(String embeddingModel);

  /**
   * Find embeddings by chunk ID and model.
   *
   * @param chunkId the chunk ID
   * @param embeddingModel the model name
   * @return optional containing the embedding
   */
  Optional<KbEmbedding> findByChunkIdAndEmbeddingModel(String chunkId, String embeddingModel);

  /**
   * Find all embeddings for chunks in a document.
   *
   * @param chunkIds list of chunk IDs from a document
   * @return list of embeddings
   */
  List<KbEmbedding> findByChunkIdIn(List<String> chunkIds);

  /**
   * Count embeddings by model.
   *
   * @param embeddingModel the model name
   * @return count of embeddings
   */
  long countByEmbeddingModel(String embeddingModel);

  /**
   * Delete embedding for a chunk (e.g., on re-embedding).
   *
   * @param chunkId the chunk ID
   */
  void deleteByChunkId(String chunkId);

  /**
   * Delete embeddings by model for batch updates.
   *
   * @param embeddingModel the model name
   */
  void deleteByEmbeddingModel(String embeddingModel);
}
