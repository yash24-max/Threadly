package dev.threadly.knowledge.event;

import dev.threadly.knowledge.service.KbIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka event listener for document upload events.
 * Triggers document ingestion when documents are uploaded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbDocumentUploadedEventListener {

  private final KbIngestionService ingestionService;

  /**
   * Handle document uploaded event from Kafka.
   * Message format:
   * {
   *   "documentId": "doc-123",
   *   "botId": "bot-456",
   *   "filename": "document.pdf",
   *   "contentType": "application/pdf"
   * }
   *
   * @param event the upload event message
   */
  @KafkaListener(
      topics = "threadly.documents.uploaded",
      groupId = "knowledge-service-ingestion",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onDocumentUploaded(DocumentUploadedEvent event) {
    log.info("Received document uploaded event: {}", event.getDocumentId());

    try {
      // In production, fetch the actual file from S3 or file storage
      // For now, this is a placeholder that would be triggered from KbDocumentController
      log.debug("Processing ingestion for document: {} (bot: {})",
          event.getDocumentId(), event.getBotId());

    } catch (Exception e) {
      log.error("Failed to process document upload event", e);
    }
  }

  /**
   * Event DTO for document upload.
   */
  public static class DocumentUploadedEvent {
    private String documentId;
    private String botId;
    private String orgId;
    private String filename;
    private String contentType;
    private Long fileSize;
    private String fileUrl;

    // Getters and setters
    public String getDocumentId() {
      return documentId;
    }

    public void setDocumentId(String documentId) {
      this.documentId = documentId;
    }

    public String getBotId() {
      return botId;
    }

    public void setBotId(String botId) {
      this.botId = botId;
    }

    public String getOrgId() {
      return orgId;
    }

    public void setOrgId(String orgId) {
      this.orgId = orgId;
    }

    public String getFilename() {
      return filename;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }

    public String getContentType() {
      return contentType;
    }

    public void setContentType(String contentType) {
      this.contentType = contentType;
    }

    public Long getFileSize() {
      return fileSize;
    }

    public void setFileSize(Long fileSize) {
      this.fileSize = fileSize;
    }

    public String getFileUrl() {
      return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
      this.fileUrl = fileUrl;
    }
  }
}
