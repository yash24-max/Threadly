package dev.threadly.core.analytics;

import dev.threadly.core.common.TenantContext;
import dev.threadly.core.conversation.ConversationRepository;
import dev.threadly.core.conversation.MessageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard metrics")
public class AnalyticsController {

  private final ConversationRepository conversationRepository;
  private final MessageRepository messageRepository;

  @GetMapping("/overview")
  @Operation(summary = "Summary cards for dashboard")
  public Map<String, Object> overview() {
    return stats();
  }

  @GetMapping("/stats")
  @Operation(summary = "Dashboard stat cards")
  public Map<String, Object> stats() {
    var orgId = TenantContext.getOrgId();
    long open = conversationRepository.countByOrgIdAndStatus(orgId, "OPEN");
    long handedOff = conversationRepository.countByOrgIdAndStatus(orgId, "HANDED_OFF");
    long closed = conversationRepository.countByOrgIdAndStatus(orgId, "CLOSED");
    long total = open + handedOff + closed;
    long totalMessages = conversationRepository.sumMessageCountByOrgId(orgId);
    return Map.of(
        "openConversations", open,
        "handoffConversations", handedOff,
        "closedConversations", closed,
        "totalConversations", total,
        "totalMessages", totalMessages,
        "p50ResponseMs", p50(orgId)
    );
  }

  private long p50(java.util.UUID orgId) {
    Double val = messageRepository.p50LatencyByOrgId(orgId);
    return val != null ? Math.round(val) : 0L;
  }
}
