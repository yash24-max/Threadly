package dev.threadly.knowledge.repository;

import dev.threadly.knowledge.entity.KbChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for KbChunk entity.
 * Provides CRUD and custom query methods for document chunks.
 */
@Repository
public interface KbChunkRepository extends JpaRepository<KbChunk, String> {

  /**
   * Find all chunks for a specific document.
   *
   * @param documentId the document ID
   * @return list of chunks in order
   */
  List<KbChunk> findByDocumentIdOrderByChunkNumber(String documentId);

  /**
   * Find all chunks for a document sorted by chunk number.
   *
   * @param documentId the document ID
   * @return ordered list of chunks
   */
  List<KbChunk> findByDocumentId(String documentId);

  /**
   * Find all chunks for a bot.
   *
   * @param botId the bot ID
   * @return list of chunks
   */
  List<KbChunk> findByBotId(String botId);

  /**
   * Find chunks that have been embedded.
   *
   * @param botId the bot ID
   * @param isEmbedded true to find embedded chunks
   * @return list of embedded chunks
   */
  List<KbChunk> findByBotIdAndIsEmbedded(String botId, Boolean isEmbedded);

  /**
   * Find chunks that need embedding for a document.
   *
   * @param documentId the document ID
   * @return list of unembed chunks
   */
  List<KbChunk> findByDocumentIdAndIsEmbeddedFalse(String documentId);

  /**
   * Count chunks for a document.
   *
   * @param documentId the document ID
   * @return count of chunks
   */
  long countByDocumentId(String documentId);

  /**
   * Count embedded chunks for a document.
   *
   * @param documentId the document ID
   * @return count of embedded chunks
   */
  @Query("SELECT COUNT(c) FROM KbChunk c WHERE c.documentId = :documentId AND c.isEmbedded = true")
  long countEmbeddedByDocumentId(@Param("documentId") String documentId);

  /**
   * Delete all chunks for a document.
   *
   * @param documentId the document ID
   */
  void deleteByDocumentId(String documentId);

  /**
   * Find a specific chunk with security check.
   *
   * @param id the chunk ID
   * @param botId the bot ID
   * @return optional containing the chunk
   */
  Optional<KbChunk> findByIdAndBotId(String id, String botId);

  /**
   * Get total token count for a document.
   *
   * @param documentId the document ID
   * @return sum of tokens
   */
  @Query("SELECT COALESCE(SUM(c.tokens), 0) FROM KbChunk c WHERE c.documentId = :documentId")
  long getTotalTokens(@Param("documentId") String documentId);
}
