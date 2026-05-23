package dev.threadly.core.flow;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.flow.FlowController.*;
import dev.threadly.core.workspace.Bot;
import dev.threadly.core.workspace.BotRepository;
import dev.threadly.core.workspace.OrgRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlowService {

  private final FlowRepository flowRepository;
  private final FlowVersionRepository flowVersionRepository;
  private final BotRepository botRepository;
  private final OrgRepository orgRepository;

  public FlowResponse getDraftFlow(UUID botId) {
    Flow flow = getOrCreateFlow(botId);
    return toResponse(flow);
  }

  @Transactional
  public FlowResponse saveDraft(UUID botId, String flowJson) {
    Flow flow = getOrCreateFlow(botId);
    flow.setDraftJson(flowJson);
    return toResponse(flowRepository.save(flow));
  }

  @Transactional
  public FlowResponse publishFlow(UUID botId) {
    Flow flow = getOrCreateFlow(botId);
    validateFlowJson(flow.getDraftJson());
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
    return toResponse(flow);
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
   * Validates that flow JSON is well-formed and contains at least a "start" node.
   * Throws IllegalArgumentException if invalid.
   */
  private void validateFlowJson(String json) {
    if (json == null || json.isBlank()) {
      throw new IllegalArgumentException("Cannot publish an empty flow.");
    }
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
      com.fasterxml.jackson.databind.JsonNode nodes = root.get("nodes");
      if (nodes == null || !nodes.isArray() || nodes.size() == 0) {
        throw new IllegalArgumentException("Flow must contain at least one node.");
      }
      boolean hasStart = false;
      for (com.fasterxml.jackson.databind.JsonNode node : nodes) {
        com.fasterxml.jackson.databind.JsonNode type = node.path("type");
        if ("start".equals(type.asText())) { hasStart = true; break; }
      }
      if (!hasStart) {
        throw new IllegalArgumentException("Flow must contain a Start node.");
      }
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalArgumentException("Flow JSON is malformed: " + e.getMessage());
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
