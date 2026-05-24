package dev.threadly.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.threadly.knowledge.entity.KbChunk;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for KbChunk.
 * Used for representing chunks in search results and API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KbChunkDto {

  private String id;
  private String documentId;
  private String botId;
  private Integer chunkNumber;
  private String content;
  private Integer tokens;
  private Map<String, Object> metadata;
  private Boolean isEmbedded;
  private String source;

  /**
   * Create DTO from entity.
   *
   * @param chunk the entity
   * @return the DTO
   */
  public static KbChunkDto fromEntity(KbChunk chunk) {
    return KbChunkDto.builder()
        .id(chunk.getId())
        .documentId(chunk.getDocumentId())
        .botId(chunk.getBotId())
        .chunkNumber(chunk.getChunkNumber())
        .content(chunk.getContent())
        .tokens(chunk.getTokens())
        .isEmbedded(chunk.getIsEmbedded())
        .source(chunk.getSource())
        .build();
  }
}
