package dev.threadly.knowledge.service;

import dev.threadly.knowledge.dto.KbDocumentDto;
import dev.threadly.knowledge.entity.KbDocument;
import dev.threadly.knowledge.exception.DocumentNotFoundException;
import dev.threadly.knowledge.repository.KbChunkRepository;
import dev.threadly.knowledge.repository.KbDocumentRepository;
import dev.threadly.knowledge.repository.KbEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing knowledge base documents.
 * Handles CRUD operations and document lifecycle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbDocumentService {

  private final KbDocumentRepository documentRepository;
  private final KbChunkRepository chunkRepository;
  private final KbEmbeddingRepository embeddingRepository;
  private final VectorDbService vectorDbService;

  /**
   * Upload and register a new document.
   *
   * @param botId the bot ID
   * @param orgId the organization ID
   * @param filename the filename
   * @param fileUrl the file URL/path
   * @param fileSize the file size in bytes
   * @param contentType the MIME type
   * @param metadata optional metadata
   * @return the created document DTO
   */
  @Transactional
  public KbDocumentDto uploadDocument(String botId, String orgId, String filename,
                                      String fileUrl, Long fileSize, String contentType,
                                      Map<String, Object> metadata) {
    log.info("Uploading document: {} for bot: {}", filename, botId);

    String documentId = UUID.randomUUID().toString();
    KbDocument document = KbDocument.builder()
        .id(documentId)
        .botId(botId)
        .orgId(orgId)
        .filename(filename)
        .fileUrl(fileUrl)
        .fileSize(fileSize)
        .contentType(contentType)
        .status(KbDocument.DocumentStatus.PENDING)
        .chunkCount(0)
        .metadata(metadata != null ? serializeMetadata(metadata) : null)
        .build();

    KbDocument saved = documentRepository.save(document);
    log.info("Document uploaded with ID: {}", documentId);

    return KbDocumentDto.fromEntity(saved);
  }

  /**
   * Get document details.
   *
   * @param documentId the document ID
   * @param botId the bot ID (security check)
   * @return the document DTO
   */
  @Transactional(readOnly = true)
  public KbDocumentDto getDocument(String documentId, String botId) {
    log.debug("Fetching document: {} for bot: {}", documentId, botId);

    KbDocument document = documentRepository.findByIdAndBotId(documentId, botId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId, botId));

    return KbDocumentDto.fromEntity(document);
  }

  /**
   * List documents for a bot.
   *
   * @param botId the bot ID
   * @return list of document DTOs
   */
  @Transactional(readOnly = true)
  public List<KbDocumentDto> listDocuments(String botId) {
    log.debug("Listing documents for bot: {}", botId);

    return documentRepository.findByBotId(botId).stream()
        .map(KbDocumentDto::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * List documents by status.
   *
   * @param botId the bot ID
   * @param status the status filter
   * @return list of document DTOs
   */
  @Transactional(readOnly = true)
  public List<KbDocumentDto> listDocumentsByStatus(String botId, String status) {
    log.debug("Listing documents for bot: {} with status: {}", botId, status);

    KbDocument.DocumentStatus docStatus = KbDocument.DocumentStatus.valueOf(status.toUpperCase());
    return documentRepository.findByBotIdAndStatus(botId, docStatus).stream()
        .map(KbDocumentDto::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Update document metadata.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @param metadata the new metadata
   * @return updated document DTO
   */
  @Transactional
  public KbDocumentDto updateMetadata(String documentId, String botId, Map<String, Object> metadata) {
    log.info("Updating metadata for document: {}", documentId);

    KbDocument document = documentRepository.findByIdAndBotId(documentId, botId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId, botId));

    document.setMetadata(serializeMetadata(metadata));
    KbDocument updated = documentRepository.save(document);

    return KbDocumentDto.fromEntity(updated);
  }

  /**
   * Delete a document and all associated chunks/embeddings.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   */
  @Transactional
  public void deleteDocument(String documentId, String botId) {
    log.info("Deleting document: {} for bot: {}", documentId, botId);

    KbDocument document = documentRepository.findByIdAndBotId(documentId, botId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId, botId));

    // Remove from vector database
    try {
      vectorDbService.deleteCollectionForDocument(botId, documentId);
    } catch (Exception e) {
      log.warn("Failed to delete vector collection for document: {}", documentId, e);
    }

    // Delete embeddings
    var chunks = chunkRepository.findByDocumentId(documentId);
    chunks.forEach(chunk -> embeddingRepository.deleteByChunkId(chunk.getId()));

    // Delete chunks
    chunkRepository.deleteByDocumentId(documentId);

    // Delete document
    documentRepository.delete(document);
    log.info("Document deleted: {}", documentId);
  }

  /**
   * Update document status.
   *
   * @param documentId the document ID
   * @param status the new status
   */
  @Transactional
  public void updateDocumentStatus(String documentId, KbDocument.DocumentStatus status) {
    log.debug("Updating document status: {} to {}", documentId, status);

    KbDocument document = documentRepository.findById(documentId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId));

    document.setStatus(status);
    documentRepository.save(document);
  }

  /**
   * Update chunk count after ingestion.
   *
   * @param documentId the document ID
   * @param chunkCount the number of chunks created
   */
  @Transactional
  public void updateChunkCount(String documentId, Integer chunkCount) {
    log.debug("Updating chunk count for document: {} to {}", documentId, chunkCount);

    KbDocument document = documentRepository.findById(documentId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId));

    document.setChunkCount(chunkCount);
    documentRepository.save(document);
  }

  /**
   * Get document count by status for a bot.
   *
   * @param botId the bot ID
   * @param status the status
   * @return count of documents
   */
  @Transactional(readOnly = true)
  public long countByStatus(String botId, KbDocument.DocumentStatus status) {
    return documentRepository.countByBotIdAndStatus(botId, status);
  }

  /**
   * Serialize metadata to JSON string.
   *
   * @param metadata the metadata map
   * @return JSON string
   */
  private String serializeMetadata(Map<String, Object> metadata) {
    if (metadata == null) {
      return null;
    }
    try {
      return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(metadata);
    } catch (Exception e) {
      log.warn("Failed to serialize metadata", e);
      return null;
    }
  }
}
