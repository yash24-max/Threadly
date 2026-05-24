package dev.threadly.common.feign;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for Flow Service (:3003).
 *
 * Endpoints:
 * - Flow CRUD
 * - Version management
 * - Publishing workflow
 */
@FeignClient(
    name = "flow-service",
    url = "${threadly.services.flow-service.url:http://flow-service:3003}"
)
public interface FlowServiceClient {

  /**
   * GET /flows/{botId} — List flows for bot.
   */
  @GetMapping("/flows/{botId}")
  FlowsListResponse listFlows(
      @PathVariable UUID botId,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /flows/{botId} — Create new flow.
   */
  @PostMapping("/flows/{botId}")
  FlowDTO createFlow(
      @PathVariable UUID botId,
      @RequestBody CreateFlowRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /flows/{flowId} — Get flow definition.
   */
  @GetMapping("/flows/{flowId}")
  FlowDTO getFlow(
      @PathVariable UUID flowId,
      @RequestHeader("Authorization") String token
  );

  /**
   * PATCH /flows/{flowId} — Update flow draft.
   */
  @PatchMapping("/flows/{flowId}")
  FlowDTO updateFlow(
      @PathVariable UUID flowId,
      @RequestBody UpdateFlowRequest request,
      @RequestHeader("Authorization") String token
  );

  /**
   * POST /flows/{flowId}/publish — Validate & publish flow.
   */
  @PostMapping("/flows/{flowId}/publish")
  FlowDTO publishFlow(
      @PathVariable UUID flowId,
      @RequestHeader("Authorization") String token
  );

  /**
   * GET /flows/{flowId}/versions — Get version history.
   */
  @GetMapping("/flows/{flowId}/versions")
  VersionsListResponse listVersions(
      @PathVariable UUID flowId,
      @RequestHeader("Authorization") String token
  );

  /**
   * DELETE /flows/{flowId} — Delete flow.
   */
  @DeleteMapping("/flows/{flowId}")
  void deleteFlow(
      @PathVariable UUID flowId,
      @RequestHeader("Authorization") String token
  );

  // DTOs

  record FlowsListResponse(List<FlowDTO> flows, int total) {}

  record FlowDTO(
      UUID flowId,
      UUID botId,
      String name,
      String description,
      JsonNode flowJson,
      int currentVersion,
      boolean isPublished,
      java.time.Instant createdAt,
      java.time.Instant updatedAt
  ) {}

  record CreateFlowRequest(
      String name,
      String description,
      JsonNode flowJson
  ) {}

  record UpdateFlowRequest(
      String name,
      String description,
      JsonNode flowJson
  ) {}

  record VersionsListResponse(
      List<FlowVersionDTO> versions,
      int total
  ) {}

  record FlowVersionDTO(
      UUID versionId,
      int versionNumber,
      JsonNode flowJson,
      UUID publishedBy,
      java.time.Instant publishedAt,
      boolean isPublished
  ) {}
}
