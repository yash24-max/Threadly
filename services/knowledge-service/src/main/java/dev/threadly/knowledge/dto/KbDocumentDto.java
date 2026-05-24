package dev.threadly.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.threadly.knowledge.entity.KbDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Data Transfer Object for KbDocument.
 * Used for API responses and data serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KbDocumentDto {

  private String id;
  private String botId;
  private String orgId;
  private String filename;
  private String fileUrl;
  private Long fileSize;
  private String contentType;
  private String status;
  private Integer chunkCount;
  private Map<String, Object> metadata;
  private Instant uploadDate;
  private Instant updatedDate;

  /**
   * Create DTO from entity.
   *
   * @param document the entity
   * @return the DTO
   */
  public static KbDocumentDto fromEntity(KbDocument document) {
    return KbDocumentDto.builder()
        .id(document.getId())
        .botId(document.getBotId())
        .orgId(document.getOrgId())
        .filename(document.getFilename())
        .fileUrl(document.getFileUrl())
        .fileSize(document.getFileSize())
        .contentType(document.getContentType())
        .status(document.getStatus().name())
        .chunkCount(document.getChunkCount())
        .uploadDate(document.getUploadDate())
        .updatedDate(document.getUpdatedDate())
        .build();
  }
}
