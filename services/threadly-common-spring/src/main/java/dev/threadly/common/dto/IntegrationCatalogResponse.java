package dev.threadly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper for integrations catalog endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationCatalogResponse {

  /** List of available integrations */
  private List<IntegrationCatalogDto> integrations;

  /** Total count of integrations */
  private Integer totalCount;
}
