package dev.threadly.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.threadly.knowledge.entity.KbIndexingJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for KbIndexingJob.
 * Used for exposing job status and progress via API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KbIndexingJobDto {

  private String id;
  private String documentId;
  private String botId;
  private String status;
  private Integer progress;
  private Integer totalChunks;
  private Integer processedChunks;
  private Integer embeddedChunks;
  private Instant createdAt;
  private Instant startedAt;
  private Instant completedAt;
  private String errorMessage;

  /**
   * Create DTO from entity.
   *
   * @param job the entity
   * @return the DTO
   */
  public static KbIndexingJobDto fromEntity(KbIndexingJob job) {
    return KbIndexingJobDto.builder()
        .id(job.getId())
        .documentId(job.getDocumentId())
        .botId(job.getBotId())
        .status(job.getStatus().name())
        .progress(job.getProgress())
        .totalChunks(job.getTotalChunks())
        .processedChunks(job.getProcessedChunks())
        .embeddedChunks(job.getEmbeddedChunks())
        .createdAt(job.getCreatedAt())
        .startedAt(job.getStartedAt())
        .completedAt(job.getCompletedAt())
        .errorMessage(job.getErrorMessage())
        .build();
  }
}
