package dev.threadly.knowledge.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing document indexing completion events.
 * Notifies other services when documents have been indexed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbIndexingCompletedEventListener {

  private final KafkaTemplate<String, IndexingCompletedEvent> kafkaTemplate;

  /**
   * Publish indexing completion event.
   * Called after successful document indexing.
   *
   * @param event the completion event
   */
  public void publishIndexingCompleted(IndexingCompletedEvent event) {
    log.info("Publishing indexing completed event for document: {}", event.getDocumentId());

    try {
      kafkaTemplate.send("threadly.documents.indexed", event.getDocumentId(), event);
      log.debug("Event published successfully");
    } catch (Exception e) {
      log.error("Failed to publish indexing completed event", e);
    }
  }

  /**
   * Event DTO for indexing completion.
   */
  public static class IndexingCompletedEvent {
    private String documentId;
    private String botId;
    private String jobId;
    private boolean success;
    private int totalChunks;
    private int embeddedChunks;
    private long processingTimeMs;
    private String errorMessage;

    public IndexingCompletedEvent() {
    }

    public IndexingCompletedEvent(String documentId, String botId, String jobId,
                                  boolean success, int totalChunks, int embeddedChunks,
                                  long processingTimeMs, String errorMessage) {
      this.documentId = documentId;
      this.botId = botId;
      this.jobId = jobId;
      this.success = success;
      this.totalChunks = totalChunks;
      this.embeddedChunks = embeddedChunks;
      this.processingTimeMs = processingTimeMs;
      this.errorMessage = errorMessage;
    }

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

    public String getJobId() {
      return jobId;
    }

    public void setJobId(String jobId) {
      this.jobId = jobId;
    }

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public int getTotalChunks() {
      return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
      this.totalChunks = totalChunks;
    }

    public int getEmbeddedChunks() {
      return embeddedChunks;
    }

    public void setEmbeddedChunks(int embeddedChunks) {
      this.embeddedChunks = embeddedChunks;
    }

    public long getProcessingTimeMs() {
      return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
      this.processingTimeMs = processingTimeMs;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }
}
