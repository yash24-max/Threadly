package dev.threadly.knowledge.controller;

import dev.threadly.knowledge.dto.KbDocumentDto;
import dev.threadly.knowledge.dto.UploadDocumentRequest;
import dev.threadly.knowledge.service.KbDocumentService;
import dev.threadly.knowledge.service.KbIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for knowledge base document management.
 * Handles document upload, retrieval, deletion, and metadata updates.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kb/documents")
@RequiredArgsConstructor
public class KbDocumentController {

  private final KbDocumentService documentService;
  private final KbIngestionService ingestionService;

  /**
   * Upload a document to the knowledge base.
   *
   * @param botId the bot ID
   * @param orgId the organization ID
   * @param file the file to upload
   * @param request optional metadata
   * @return created document DTO
   */
  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<KbDocumentDto> uploadDocument(
      @RequestParam String botId,
      @RequestParam String orgId,
      @RequestParam MultipartFile file,
      @RequestParam(required = false) String metadata) {

    log.info("Uploading document: {} for bot: {}", file.getOriginalFilename(), botId);

    try {
      // Create document
      KbDocumentDto document = documentService.uploadDocument(
          botId,
          orgId,
          file.getOriginalFilename(),
          file.getOriginalFilename(),
          file.getSize(),
          file.getContentType(),
          null
      );

      // Start async ingestion
      ingestionService.startIngestion(
          document.getId(),
          file.getInputStream(),
          file.getContentType(),
          file.getOriginalFilename()
      );

      return ResponseEntity.status(HttpStatus.CREATED).body(document);

    } catch (Exception e) {
      log.error("Document upload failed", e);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Get document details.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @return document DTO
   */
  @GetMapping("/{documentId}")
  public ResponseEntity<KbDocumentDto> getDocument(
      @PathVariable String documentId,
      @RequestParam String botId) {

    log.debug("Fetching document: {} for bot: {}", documentId, botId);

    KbDocumentDto document = documentService.getDocument(documentId, botId);
    return ResponseEntity.ok(document);
  }

  /**
   * List documents for a bot.
   *
   * @param botId the bot ID
   * @param status optional status filter
   * @return list of documents
   */
  @GetMapping
  public ResponseEntity<List<KbDocumentDto>> listDocuments(
      @RequestParam String botId,
      @RequestParam(required = false) String status) {

    log.debug("Listing documents for bot: {}", botId);

    List<KbDocumentDto> documents;
    if (status != null && !status.isEmpty()) {
      documents = documentService.listDocumentsByStatus(botId, status);
    } else {
      documents = documentService.listDocuments(botId);
    }

    return ResponseEntity.ok(documents);
  }

  /**
   * Delete a document.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @return no content response
   */
  @DeleteMapping("/{documentId}")
  public ResponseEntity<Void> deleteDocument(
      @PathVariable String documentId,
      @RequestParam String botId) {

    log.info("Deleting document: {} for bot: {}", documentId, botId);

    documentService.deleteDocument(documentId, botId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Update document metadata.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @param request metadata update
   * @return updated document DTO
   */
  @PatchMapping("/{documentId}")
  public ResponseEntity<KbDocumentDto> updateMetadata(
      @PathVariable String documentId,
      @RequestParam String botId,
      @RequestBody Map<String, Object> request) {

    log.info("Updating metadata for document: {}", documentId);

    KbDocumentDto updated = documentService.updateMetadata(documentId, botId, request);
    return ResponseEntity.ok(updated);
  }
}
