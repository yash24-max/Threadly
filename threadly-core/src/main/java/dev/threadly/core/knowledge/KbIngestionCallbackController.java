package dev.threadly.core.knowledge;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Called by threadly-ai when ingestion of a KB document completes (or fails).
 * Authenticated by the shared service secret header.
 */
@RestController
@RequestMapping("/v1/internal/kb")
@RequiredArgsConstructor
@Tag(name = "KB Internal", description = "Internal KB ingestion status callbacks")
public class KbIngestionCallbackController {

  private final KbDocumentRepository kbDocumentRepository;

  @Value("${threadly.ai.shared-secret:dev_shared_secret}")
  private String sharedSecret;

  @PostMapping("/{docId}/status")
  @Operation(summary = "AI sidecar reports ingestion result")
  public ResponseEntity<Void> updateStatus(
      @RequestHeader("X-Service-Secret") String secret,
      @PathVariable UUID docId,
      @RequestBody StatusRequest req) {

    if (!sharedSecret.equals(secret)) {
      return ResponseEntity.status(401).build();
    }

    KbDocument doc = kbDocumentRepository.findById(docId)
        .orElseThrow(() -> new EntityNotFoundException("Document not found: " + docId));

    doc.setStatus(req.getStatus());
    if (req.getChunkCount() != null) doc.setChunkCount(req.getChunkCount());
    if (req.getErrorMsg() != null) doc.setErrorMsg(req.getErrorMsg());
    kbDocumentRepository.save(doc);

    return ResponseEntity.noContent().build();
  }

  @Data
  public static class StatusRequest {
    private String status; // "ready" | "error"
    private Integer chunkCount;
    private String errorMsg;
  }
}
