package dev.threadly.core.knowledge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Polls for PENDING KB documents and dispatches them to threadly-ai for ingestion.
 * Runs every 5 seconds. Once threadly-ai completes ingestion, it should call back
 * POST /v1/bots/{botId}/kb/{docId}/status with {"status":"ready","chunkCount":N}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KbIngestionJob {

  private final KbDocumentRepository kbDocumentRepository;

  @Value("${threadly.ai.url:http://localhost:8081}")
  private String aiUrl;

  @Value("${threadly.ai.shared-secret:dev_shared_secret}")
  private String sharedSecret;

  @Value("${threadly.storage.endpoint:http://localhost:9000}")
  private String storageEndpoint;

  @Value("${threadly.storage.bucket:threadly-kb}")
  private String bucket;

  private final WebClient webClient = WebClient.builder()
      .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
      .build();

  @Scheduled(fixedDelay = 5000)
  public void dispatchPending() {
    List<KbDocument> pending = kbDocumentRepository.findTop20ByStatusOrderByCreatedAtAsc("pending");
    if (pending.isEmpty()) return;

    for (KbDocument doc : pending) {
      try {
        doc.setStatus("indexing");
        kbDocumentRepository.save(doc);

        Map<String, Object> payload = buildPayload(doc);

        webClient.post()
            .uri(aiUrl + "/kb/ingest")
            .header("X-Service-Secret", sharedSecret)
            .header("Content-Type", "application/json")
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(30))
            .subscribe(
                null,
                err -> {
                  log.error("KB ingestion dispatch failed for doc {}: {}", doc.getId(), err.getMessage());
                  doc.setStatus("error");
                  doc.setErrorMsg(err.getMessage());
                  kbDocumentRepository.save(doc);
                }
            );

        log.info("Dispatched KB ingestion for doc {} (bot {})", doc.getId(), doc.getBot().getId());
      } catch (Exception e) {
        log.error("Error dispatching KB doc {}", doc.getId(), e);
        doc.setStatus("error");
        doc.setErrorMsg(e.getMessage());
        kbDocumentRepository.save(doc);
      }
    }
  }

  private Map<String, Object> buildPayload(KbDocument doc) {
    if ("url".equals(doc.getType())) {
      return Map.of(
          "documentId", doc.getId().toString(),
          "botId", doc.getBot().getId().toString(),
          "type", "url",
          "sourceUrl", doc.getSourceUrl() != null ? doc.getSourceUrl() : ""
      );
    }
    String fileUrl = storageEndpoint + "/" + bucket + "/" + doc.getStorageKey();
    return Map.of(
        "documentId", doc.getId().toString(),
        "botId", doc.getBot().getId().toString(),
        "type", doc.getType(),
        "fileUrl", fileUrl,
        "fileName", doc.getName() != null ? doc.getName() : "document"
    );
  }
}
