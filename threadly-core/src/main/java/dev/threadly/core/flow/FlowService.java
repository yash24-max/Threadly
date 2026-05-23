package dev.threadly.core.flow;

import dev.threadly.core.common.AuditService;
import dev.threadly.core.common.TenantContext;
import dev.threadly.core.flow.FlowController.FlowResponse;
import dev.threadly.core.flow.FlowController.FlowVersionResponse;
import dev.threadly.core.workspace.Bot;
import dev.threadly.core.workspace.BotRepository;
import dev.threadly.core.workspace.OrgRepository;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dev.threadly.core.flow.FlowSchemaValidator.ValidationResult;

@Service
@RequiredArgsConstructor
public class FlowService {

  private final FlowRepository flowRepository;
  private final FlowVersionRepository flowVersionRepository;
  private final BotRepository botRepository;
  private final OrgRepository orgRepository;
  private final FlowSchemaValidator flowSchemaValidator;
  private final AuditService auditService;

  public FlowResponse getDraftFlow(UUID botId) {
    Flow flow = getOrCreateFlow(botId);
    return toResponse(flow);
  }

  @Transactional
  public FlowResponse saveDraft(UUID botId, String flowJson) {
    runSchemaValidation(flowJson);
    Flow flow = getOrCreateFlow(botId);
    flow.setDraftJson(flowJson);
    return toResponse(flowRepository.save(flow));
  }

  @Transactional
  public FlowResponse publishFlow(UUID botId) {
    Flow flow = getOrCreateFlow(botId);
    runSchemaValidation(flow.getDraftJson());
    flow.setPublishedJson(flow.getDraftJson());
    flow.setPublishedAt(Instant.now());
    flowRepository.save(flow);

    // create version snapshot
    int nextVersion = flowVersionRepository.countByFlowId(flow.getId()) + 1;
    FlowVersion version = FlowVersion.builder()
        .flow(flow)
        .org(flow.getOrg())
        .versionNum(nextVersion)
        .snapshotJson(flow.getDraftJson())
        .publishedBy(TenantContext.getUserId())
        .build();
    flowVersionRepository.save(version);
    FlowResponse published = toResponse(flow);
    auditService.log("FLOW_PUBLISHED", "FLOW", flow.getId(), null, published);
    return published;
  }

  public List<FlowVersionResponse> listVersions(UUID botId) {
    Flow flow = findFlowForBot(botId);
    return flowVersionRepository.findAllByFlowIdOrderByVersionNumDesc(flow.getId()).stream()
        .map(this::toVersionResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public FlowResponse rollback(UUID botId, int versionNum) {
    Flow flow = findFlowForBot(botId);
    FlowVersion version = flowVersionRepository
        .findByFlowIdAndVersionNum(flow.getId(), versionNum)
        .orElseThrow(() -> new EntityNotFoundException("Version " + versionNum + " not found"));
    flow.setDraftJson(version.getSnapshotJson());
    FlowResponse rolled = toResponse(flowRepository.save(flow));
    auditService.log("FLOW_ROLLED_BACK", "FLOW", flow.getId(),
        null, java.util.Map.of("versionNum", versionNum));
    return rolled;
  }

  public byte[] exportFlow(UUID botId, UUID flowId) {
    UUID orgId = TenantContext.getOrgId();
    Flow flow = flowRepository.findByIdAndBotIdAndOrgId(flowId, botId, orgId)
        .orElseThrow(() -> new EntityNotFoundException("Flow not found: " + flowId));
    String json = flow.getDraftJson() != null ? flow.getDraftJson() : "{}";
    return json.getBytes(StandardCharsets.UTF_8);
  }

  @Transactional
  public FlowResponse importFlow(UUID botId, String flowJson) {
    runSchemaValidation(flowJson);
    UUID orgId = TenantContext.getOrgId();
    // If a flow already exists for this bot, replace the draft (import = new draft)
    Flow flow = flowRepository.findByBotIdAndOrgId(botId, orgId)
        .map(existing -> {
          existing.setDraftJson(flowJson);
          return existing;
        })
        .orElseGet(() -> {
          Bot bot = botRepository.findByIdAndOrgId(botId, orgId)
              .orElseThrow(() -> new EntityNotFoundException("Bot not found: " + botId));
          return Flow.builder()
              .bot(bot)
              .org(orgRepository.getReferenceById(orgId))
              .draftJson(flowJson)
              .build();
        });
    return toResponse(flowRepository.save(flow));
  }

  private Flow getOrCreateFlow(UUID botId) {
    UUID orgId = TenantContext.getOrgId();
    return flowRepository.findByBotIdAndOrgId(botId, orgId)
        .orElseGet(() -> {
          Bot bot = botRepository.findByIdAndOrgId(botId, orgId)
              .orElseThrow(() -> new EntityNotFoundException("Bot not found: " + botId));
          return flowRepository.save(Flow.builder()
              .bot(bot)
              .org(orgRepository.getReferenceById(orgId))
              .build());
        });
  }

  private Flow findFlowForBot(UUID botId) {
    return flowRepository.findByBotIdAndOrgId(botId, TenantContext.getOrgId())
        .orElseThrow(() -> new EntityNotFoundException("No flow found for bot: " + botId));
  }

  /**
   * Validates flow JSON using the full schema validator.
   * Throws {@link FlowValidationException} if validation fails.
   */
  private void runSchemaValidation(String json) {
    ValidationResult result = flowSchemaValidator.validate(json);
    if (!result.valid()) {
      throw new FlowValidationException(result.errors());
    }
  }

  private FlowResponse toResponse(Flow f) {
    FlowResponse r = new FlowResponse();
    r.setId(f.getId().toString());
    r.setBotId(f.getBot().getId().toString());
    r.setDraftJson(f.getDraftJson());
    r.setPublishedJson(f.getPublishedJson());
    r.setPublishedAt(f.getPublishedAt() != null ? f.getPublishedAt().toString() : null);
    r.setUpdatedAt(f.getUpdatedAt() != null ? f.getUpdatedAt().toString() : null);
    return r;
  }

  private FlowVersionResponse toVersionResponse(FlowVersion v) {
    FlowVersionResponse r = new FlowVersionResponse();
    r.setId(v.getId().toString());
    r.setVersionNum(v.getVersionNum());
    r.setSnapshotJson(v.getSnapshotJson());
    r.setPublishedBy(v.getPublishedBy() != null ? v.getPublishedBy().toString() : null);
    r.setCreatedAt(v.getCreatedAt() != null ? v.getCreatedAt().toString() : null);
    return r;
  }
}
