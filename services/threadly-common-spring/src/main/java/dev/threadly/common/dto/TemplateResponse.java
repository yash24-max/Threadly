package dev.threadly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper for templates endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateResponse {

  /** List of templates */
  private List<TemplateDto> templates;

  /** Total count of templates */
  private Integer totalCount;

  /** Optional pagination info */
  private String nextCursor;
}
