package dev.threadly.core.knowledge;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.knowledge.KbController.KbDocResponse;
import dev.threadly.core.outbox.OutboxService;
import dev.threadly.core.workspace.BotRepository;
import dev.threadly.core.workspace.OrgRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class KbService {

  private final KbDocumentRepository kbDocumentRepository;
  private final BotRepository botRepository;
  private final OrgRepository orgRepository;
  private final S3Client s3Client;
  private final OutboxService outboxService;

  @Value("${threadly.storage.bucket}")
  private String bucket;

  public List<KbDocResponse> listDocuments(UUID botId) {
    verifyBotAccess(botId);
    return kbDocumentRepository.findAllByBotId(botId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public KbDocResponse uploadDocument(UUID botId, MultipartFile file) {
    verifyBotAccess(botId);
    String ext = getExtension(file.getOriginalFilename());
    String storageKey = "kb/" + botId + "/" + UUID.randomUUID() + "." + ext;

    try {
      s3Client.putObject(
          PutObjectRequest.builder().bucket(bucket).key(storageKey).build(),
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (Exception e) {
      log.error("Failed to upload KB file", e);
      throw new RuntimeException("Failed to upload file: " + e.getMessage());
    }

    KbDocument doc = kbDocumentRepository.save(KbDocument.builder()
        .bot(botRepository.getReferenceById(botId))
        .org(orgRepository.getReferenceById(TenantContext.getOrgId()))
        .name(file.getOriginalFilename())
        .type(ext)
        .storageKey(storageKey)
        .status("pending")
        .build());

    // Trigger ingestion via outbox
    outboxService.publishDashboardEvent(TenantContext.getOrgId(), "kb_ingest_requested",
        Map.of("documentId", doc.getId().toString(), "botId", botId.toString()));

    return toResponse(doc);
  }

  @Transactional
  public KbDocResponse addUrl(UUID botId, String url) {
    verifyBotAccess(botId);
    KbDocument doc = kbDocumentRepository.save(KbDocument.builder()
        .bot(botRepository.getReferenceById(botId))
        .org(orgRepository.getReferenceById(TenantContext.getOrgId()))
        .name(url)
        .type("url")
        .sourceUrl(url)
        .status("pending")
        .build());
    outboxService.publishDashboardEvent(TenantContext.getOrgId(), "kb_ingest_requested",
        Map.of("documentId", doc.getId().toString(), "botId", botId.toString()));
    return toResponse(doc);
  }

  @Transactional
  public void deleteDocument(UUID botId, UUID docId) {
    verifyBotAccess(botId);
    KbDocument doc = kbDocumentRepository.findByIdAndBotId(docId, botId)
        .orElseThrow(() -> new EntityNotFoundException("Document not found: " + docId));
    kbDocumentRepository.delete(doc);
  }

  private void verifyBotAccess(UUID botId) {
    if (!botRepository.existsByIdAndOrgId(botId, TenantContext.getOrgId())) {
      throw new EntityNotFoundException("Bot not found: " + botId);
    }
  }

  private String getExtension(String filename) {
    if (filename == null || !filename.contains(".")) return "bin";
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
  }

  private KbDocResponse toResponse(KbDocument d) {
    KbDocResponse r = new KbDocResponse();
    r.setId(d.getId().toString()); r.setBotId(d.getBot().getId().toString());
    r.setName(d.getName()); r.setType(d.getType());
    r.setStatus(d.getStatus()); r.setStorageKey(d.getStorageKey());
    r.setSourceUrl(d.getSourceUrl()); r.setChunkCount(d.getChunkCount());
    r.setErrorMsg(d.getErrorMsg());
    r.setCreatedAt(d.getCreatedAt() != null ? d.getCreatedAt().toString() : null);
    return r;
  }
}
