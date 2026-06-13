package dev.threadly.core.flow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bots/{botId}/flow")
@RequiredArgsConstructor
@Tag(name = "Flows", description = "Bot flow builder")
public class FlowController {

  private final FlowService flowService;

  @GetMapping
  @Operation(summary = "Get draft flow for a bot")
  public FlowResponse getDraft(@PathVariable UUID botId) {
    return flowService.getDraftFlow(botId);
  }

  @PutMapping
  @Operation(summary = "Save draft flow")
  public FlowResponse saveDraft(@PathVariable UUID botId, @Valid @RequestBody SaveFlowRequest req) {
    return flowService.saveDraft(botId, req.getFlowJson());
  }

  @PostMapping("/publish")
  @Operation(summary = "Publish draft flow")
  public FlowResponse publish(@PathVariable UUID botId) {
    return flowService.publishFlow(botId);
  }

  @GetMapping("/versions")
  @Operation(summary = "List published versions")
  public List<FlowVersionResponse> listVersions(@PathVariable UUID botId) {
    return flowService.listVersions(botId);
  }

  @PostMapping("/versions/{versionNum}/rollback")
  @Operation(summary = "Rollback to a specific version")
  public FlowResponse rollback(@PathVariable UUID botId, @PathVariable int versionNum) {
    return flowService.rollback(botId, versionNum);
  }

  // ── DTOs ─────────────────────────────────────────────────────────

  @Data
  public static class SaveFlowRequest {
    private String flowJson; // raw JSON string
  }

  @Data
  public static class FlowResponse {
    private String id;
    private String botId;
    private String draftJson;
    private String publishedJson;
    private String publishedAt;
    private String updatedAt;
  }

  @Data
  public static class FlowVersionResponse {
    private String id;
    private int versionNum;
    private String snapshotJson;
    private String publishedBy;
    private String createdAt;
  }
}
