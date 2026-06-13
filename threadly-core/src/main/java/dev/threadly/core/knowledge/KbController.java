package dev.threadly.core.knowledge;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/bots/{botId}/kb")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base", description = "Upload and manage KB documents")
public class KbController {

  private final KbService kbService;

  @GetMapping
  @Operation(summary = "List KB documents for a bot")
  public List<KbDocResponse> list(@PathVariable UUID botId) {
    return kbService.listDocuments(botId);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload a document to the KB")
  public ResponseEntity<KbDocResponse> upload(
      @PathVariable UUID botId,
      @RequestPart("file") MultipartFile file) {
    return ResponseEntity.status(HttpStatus.CREATED).body(kbService.uploadDocument(botId, file));
  }

  @PostMapping("/url")
  @Operation(summary = "Add a URL to the KB")
  public ResponseEntity<KbDocResponse> addUrl(
      @PathVariable UUID botId,
      @RequestBody UrlRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(kbService.addUrl(botId, req.getUrl()));
  }

  @DeleteMapping("/{docId}")
  @Operation(summary = "Delete a KB document")
  public ResponseEntity<Void> delete(@PathVariable UUID botId, @PathVariable UUID docId) {
    kbService.deleteDocument(botId, docId);
    return ResponseEntity.noContent().build();
  }

  @Data
  public static class KbDocResponse {
    private String id, botId, name, type, status, storageKey, sourceUrl;
    private Integer chunkCount;
    private String createdAt, errorMsg;
  }

  @Data
  public static class UrlRequest {
    private String url;
    private String sourceUrl;  // alias used by frontend
    private String docName;
  }
}
