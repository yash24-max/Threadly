package dev.threadly.knowledge.repository;

import dev.threadly.knowledge.entity.KbDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for KbDocument entity.
 * Provides CRUD and custom query methods for knowledge base documents.
 */
@Repository
public interface KbDocumentRepository extends JpaRepository<KbDocument, String> {

  /**
   * Find all documents for a specific bot.
   *
   * @param botId the bot ID
   * @return list of documents for the bot
   */
  List<KbDocument> findByBotId(String botId);

  /**
   * Find all documents for a specific bot and organization.
   *
   * @param botId the bot ID
   * @param orgId the organization ID
   * @return list of documents
   */
  List<KbDocument> findByBotIdAndOrgId(String botId, String orgId);

  /**
   * Find documents by status.
   *
   * @param status the document status
   * @return list of documents with the given status
   */
  List<KbDocument> findByStatus(KbDocument.DocumentStatus status);

  /**
   * Find documents by bot ID and status.
   *
   * @param botId the bot ID
   * @param status the document status
   * @return list of documents
   */
  List<KbDocument> findByBotIdAndStatus(String botId, KbDocument.DocumentStatus status);

  /**
   * Find a document by ID and bot ID (security check).
   *
   * @param id the document ID
   * @param botId the bot ID
   * @return optional containing the document if found and belongs to bot
   */
  Optional<KbDocument> findByIdAndBotId(String id, String botId);

  /**
   * Find documents by filename pattern for a bot.
   *
   * @param botId the bot ID
   * @param filename the filename pattern
   * @return list of matching documents
   */
  @Query("SELECT d FROM KbDocument d WHERE d.botId = :botId AND LOWER(d.filename) LIKE LOWER(CONCAT('%', :filename, '%'))")
  List<KbDocument> findByBotIdAndFilenameContaining(@Param("botId") String botId, @Param("filename") String filename);

  /**
   * Find documents uploaded after a certain date.
   *
   * @param botId the bot ID
   * @param afterDate the cutoff date
   * @return list of documents
   */
  List<KbDocument> findByBotIdAndUploadDateAfter(String botId, Instant afterDate);

  /**
   * Count documents by status for a bot.
   *
   * @param botId the bot ID
   * @param status the document status
   * @return count of documents
   */
  long countByBotIdAndStatus(String botId, KbDocument.DocumentStatus status);

  /**
   * Delete all documents for a bot (cleanup).
   *
   * @param botId the bot ID
   */
  void deleteByBotId(String botId);

  /**
   * Delete documents by org and status.
   *
   * @param orgId the organization ID
   * @param status the document status
   */
  void deleteByOrgIdAndStatus(String orgId, KbDocument.DocumentStatus status);
}
