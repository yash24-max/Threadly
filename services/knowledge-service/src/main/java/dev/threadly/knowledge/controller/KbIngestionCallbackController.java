package dev.threadly.knowledge.controller;

import dev.threadly.knowledge.entity.KbDocument;
import dev.threadly.knowledge.service.KbDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal callback endpoint called by threadly-ai when KB ingestion completes.
 *
 * POST /api/v1/bots/{botId}/kb/{docId}/status
 * Body: { "status": "ready"|"error", "chunkCount": N, "errorMsg": "..." }
 *
 * This matches the monolith's KbIngestionCallbackController contract.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/bots/{botId}/kb")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base", description = "KB ingestion callbacks from threadly-ai")
public class KbIngestionCallbackController {

    private final KbDocumentService kbDocumentService;

    @PostMapping("/{docId}/status")
    @Operation(summary = "Callback from threadly-ai when document ingestion completes")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String botId,
            @PathVariable String docId,
            @RequestBody StatusCallback body) {

        log.info("KB ingestion callback: docId={} botId={} status={} chunks={}",
                docId, botId, body.getStatus(), body.getChunkCount());

        try {
            KbDocument.DocumentStatus status = KbDocument.DocumentStatus.valueOf(
                    body.getStatus().toUpperCase());
            kbDocumentService.updateDocumentStatus(docId, status);

            if (body.getChunkCount() != null && body.getChunkCount() > 0) {
                kbDocumentService.updateChunkCount(docId, body.getChunkCount());
            }

            if ("ERROR".equalsIgnoreCase(body.getStatus()) && body.getErrorMsg() != null) {
                log.warn("Ingestion failed for doc {}: {}", docId, body.getErrorMsg());
            }
        } catch (Exception e) {
            log.error("Failed to process KB ingestion callback for doc {}: {}", docId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.noContent().build();
    }

    @Data
    public static class StatusCallback {
        @NotBlank
        private String  status;     // ready | error
        private Integer chunkCount;
        private String  errorMsg;
    }
}
