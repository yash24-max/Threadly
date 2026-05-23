package dev.threadly.core.conversation;

import dev.threadly.core.common.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Conversation inbox and transcripts")
public class ConversationController {

  private final ConversationRepository conversationRepository;
  private final MessageRepository messageRepository;

  @GetMapping
  @Operation(summary = "List conversations for current org")
  public Page<ConvSummary> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size) {
    return conversationRepository
        .findAllByOrgIdOrderByCreatedAtDesc(TenantContext.getOrgId(), PageRequest.of(page, size))
        .map(this::toSummary);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get conversation with all messages")
  public ConvDetail get(@PathVariable UUID id) {
    Conversation conv = conversationRepository.findByIdAndOrgId(id, TenantContext.getOrgId())
        .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + id));
    List<MsgResponse> messages = messageRepository
        .findAllByConversationIdOrderByCreatedAtAsc(id).stream()
        .map(this::toMsgResponse)
        .collect(Collectors.toList());
    ConvDetail detail = new ConvDetail();
    detail.setConversation(toSummary(conv));
    detail.setMessages(messages);
    return detail;
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update conversation status")
  public ConvSummary update(@PathVariable UUID id, @RequestBody UpdateConvRequest req) {
    Conversation conv = conversationRepository.findByIdAndOrgId(id, TenantContext.getOrgId())
        .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + id));
    if (req.getStatus() != null) conv.setStatus(req.getStatus());
    return toSummary(conversationRepository.save(conv));
  }

  @GetMapping("/{id}/messages")
  @Operation(summary = "Get messages for a conversation")
  public List<MsgResponse> listMessages(@PathVariable UUID id) {
    conversationRepository.findByIdAndOrgId(id, TenantContext.getOrgId())
        .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + id));
    return messageRepository.findAllByConversationIdOrderByCreatedAtAsc(id).stream()
        .map(this::toMsgResponse)
        .collect(Collectors.toList());
  }

  @PostMapping("/{id}/handoff")
  @Operation(summary = "Hand off conversation to human agent")
  public ConvSummary handoff(@PathVariable UUID id) {
    Conversation conv = conversationRepository.findByIdAndOrgId(id, TenantContext.getOrgId())
        .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + id));
    conv.setStatus("HANDED_OFF");
    return toSummary(conversationRepository.save(conv));
  }

  @PostMapping("/{id}/close")
  @Operation(summary = "Close a conversation")
  public ConvSummary close(@PathVariable UUID id) {
    Conversation conv = conversationRepository.findByIdAndOrgId(id, TenantContext.getOrgId())
        .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + id));
    conv.setStatus("CLOSED");
    return toSummary(conversationRepository.save(conv));
  }

  @PostMapping("/{id}/messages")
  @Operation(summary = "Agent sends a message in a conversation")
  public ResponseEntity<Void> sendAgentMessage(@PathVariable UUID id,
      @RequestBody AgentMessageRequest req) {
    Conversation conv = conversationRepository.findByIdAndOrgId(id, TenantContext.getOrgId())
        .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + id));
    messageRepository.save(Message.builder()
        .conversation(conv)
        .orgId(TenantContext.getOrgId())
        .role("agent")
        .content(req.getContent())
        .metadata("{}")
        .build());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/bulk-close")
  @Operation(summary = "Bulk close multiple conversations")
  @Transactional
  public BulkOperationResult bulkClose(@Valid @RequestBody BulkIdsRequest req) {
    UUID orgId = TenantContext.getOrgId();
    int updated = 0;
    for (UUID id : req.getIds()) {
      conversationRepository.findByIdAndOrgId(id, orgId).ifPresent(conv -> {
        conv.setStatus("CLOSED");
        conversationRepository.save(conv);
      });
      updated++;
    }
    return new BulkOperationResult(updated);
  }

  @PostMapping("/bulk-assign")
  @Operation(summary = "Bulk assign multiple conversations to an agent")
  @Transactional
  public BulkOperationResult bulkAssign(@Valid @RequestBody BulkAssignRequest req) {
    UUID orgId = TenantContext.getOrgId();
    int updated = 0;
    for (UUID id : req.getIds()) {
      Conversation conv = conversationRepository.findByIdAndOrgId(id, orgId).orElse(null);
      if (conv != null) {
        String meta = conv.getMetadata();
        // Store assignedAgentId in metadata JSON
        String updatedMeta = meta.endsWith("}") && meta.length() > 2
            ? meta.substring(0, meta.length() - 1) + ",\"assignedAgentId\":\""
                + req.getAgentId() + "\"}"
            : "{\"assignedAgentId\":\"" + req.getAgentId() + "\"}";
        conv.setMetadata(updatedMeta);
        conv.setStatus("ASSIGNED");
        conversationRepository.save(conv);
        updated++;
      }
    }
    return new BulkOperationResult(updated);
  }

  @GetMapping("/export")
  @Operation(summary = "Stream conversations as CSV download")
  public void exportCsv(
      @RequestParam(required = false) UUID botId,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      HttpServletResponse response) throws IOException {
    UUID orgId = TenantContext.getOrgId();
    Instant fromInstant = parseInstantOrMin(from);
    Instant toInstant = parseInstantOrMax(to);

    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"conversations.csv\"");

    PrintWriter writer = response.getWriter();
    writer.println("id,botId,visitorId,status,channel,createdAt,updatedAt,messageCount");

    List<Conversation> conversations =
        botId != null
            ? conversationRepository.findByOrgIdAndBotIdAndCreatedAtBetween(
                orgId, botId, fromInstant, toInstant)
            : conversationRepository.findByOrgIdAndCreatedAtBetween(orgId, fromInstant, toInstant);

    for (Conversation c : conversations) {
      long msgCount = messageRepository.countByConversationId(c.getId());
      writer.printf(
          "%s,%s,%s,%s,%s,%s,%s,%d%n",
          c.getId(),
          c.getBot().getId(),
          escapeCsv(c.getVisitorId()),
          c.getStatus(),
          c.getChannel(),
          c.getCreatedAt(),
          c.getUpdatedAt(),
          msgCount);
    }
    writer.flush();
  }

  private Instant parseInstantOrMin(String s) {
    if (s == null || s.isBlank()) return Instant.EPOCH;
    try {
      return Instant.parse(s);
    } catch (DateTimeParseException e) {
      return Instant.EPOCH;
    }
  }

  private Instant parseInstantOrMax(String s) {
    if (s == null || s.isBlank()) return Instant.now().plusSeconds(86400L * 365 * 100);
    try {
      return Instant.parse(s);
    } catch (DateTimeParseException e) {
      return Instant.now();
    }
  }

  private String escapeCsv(String value) {
    if (value == null) return "";
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  // ── DTOs ──────────────────────────────────────────────────────────

  @Data
  public static class ConvSummary {
    private String id, botId, orgId, visitorId, status, channel, createdAt, updatedAt;
    private long messageCount;
  }

  @Data
  public static class ConvDetail {
    private ConvSummary conversation;
    private List<MsgResponse> messages;
  }

  @Data
  public static class MsgResponse {
    private String id, role, content, nodeId, createdAt;
    private Integer latencyMs;
  }

  @Data
  public static class UpdateConvRequest { private String status; }

  @Data
  public static class AgentMessageRequest { private String content; }

  @Data
  public static class BulkIdsRequest {
    @NotEmpty private List<UUID> ids;
  }

  @Data
  public static class BulkAssignRequest {
    @NotEmpty private List<UUID> ids;
    @NotNull private UUID agentId;
  }

  @Data
  public static class BulkOperationResult {
    private final int affected;
  }

  private ConvSummary toSummary(Conversation c) {
    ConvSummary s = new ConvSummary();
    s.setId(c.getId().toString()); s.setBotId(c.getBot().getId().toString());
    s.setOrgId(c.getOrgId().toString()); s.setVisitorId(c.getVisitorId());
    s.setStatus(c.getStatus()); s.setChannel(c.getChannel());
    s.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
    s.setUpdatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);
    s.setMessageCount(messageRepository.countByConversationId(c.getId()));
    return s;
  }

  private MsgResponse toMsgResponse(Message m) {
    MsgResponse r = new MsgResponse();
    r.setId(m.getId().toString()); r.setRole(m.getRole());
    r.setContent(m.getContent()); r.setNodeId(m.getNodeId());
    r.setLatencyMs(m.getLatencyMs());
    r.setCreatedAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
    return r;
  }
}
