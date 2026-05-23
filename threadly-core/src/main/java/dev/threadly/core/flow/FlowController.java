package dev.threadly.core.flow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

  @GetMapping("/{flowId}/export")
  @Operation(summary = "Export a flow as a JSON file download")
  public ResponseEntity<byte[]> export(@PathVariable UUID botId, @PathVariable UUID flowId) {
    byte[] content = flowService.exportFlow(botId, flowId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setContentDisposition(
        ContentDisposition.attachment().filename("flow-" + flowId + ".json").build());
    return ResponseEntity.ok().headers(headers).body(content);
  }

  @PostMapping("/import")
  @Operation(summary = "Import a flow from a JSON file upload")
  public ResponseEntity<FlowResponse> importFlow(
      @PathVariable UUID botId,
      @RequestParam("file") MultipartFile file) throws IOException {
    String json = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    FlowResponse response = flowService.importFlow(botId, json);
    return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
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
