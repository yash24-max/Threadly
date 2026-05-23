package dev.threadly.core.runtime.executors;

import dev.threadly.core.ai.AiClient;
import dev.threadly.core.ai.AiClient.AiRequest;
import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.conversation.Message;
import dev.threadly.core.conversation.MessageRepository;
import dev.threadly.core.outbox.OutboxService;
import dev.threadly.core.runtime.*;
import dev.threadly.core.workspace.Bot;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReplyNodeExecutor implements NodeExecutor {

  private final AiClient aiClient;
  private final MessageRepository messageRepository;
  private final OutboxService outboxService;

  @Override
  public String nodeType() { return "ai_reply"; }

  @Override
  public NodeExecutionResult execute(FlowGraph.Node node, Session session,
      Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String systemPrompt = TemplateEngine.render(
        (String) data.getOrDefault("system_prompt", "You are a helpful assistant."),
        session.getVariables());
    boolean useKb = Boolean.TRUE.equals(data.get("use_kb"));
    int maxTokens = ((Number) data.getOrDefault("max_tokens", 500)).intValue();
    String provider = (String) data.getOrDefault("provider", "auto");

    // Signal typing indicator to widget via Centrifugo
    outboxService.publishChatEvent(bot.getId(), session.getVisitorId(), "typing",
        Map.of("typing", true));

    long start = Instant.now().toEpochMilli();
    try {
      AiRequest req = AiRequest.builder()
          .botId(bot.getId())
          .orgId(orgId)
          .conversationId(conversation.getId())
          .systemPrompt(systemPrompt)
          .userMessage((String) session.getVariables().getOrDefault("session.last_input", ""))
          .useKb(useKb)
          .maxTokens(maxTokens)
          .provider(provider)
          .build();

      // AI client streams tokens; each token is published to Centrifugo
      String fullReply = aiClient.complete(req, token ->
          outboxService.publishChatEvent(bot.getId(), session.getVisitorId(), "token",
              Map.of("token", token)));

      int latencyMs = (int) (Instant.now().toEpochMilli() - start);

      messageRepository.save(Message.builder()
          .conversation(conversation)
          .orgId(orgId)
          .role("ai")
          .content(fullReply)
          .latencyMs(latencyMs)
          .nodeId(node.getId())
          .metadata("{}")
          .build());

      // Stop typing indicator
      outboxService.publishChatEvent(bot.getId(), session.getVisitorId(), "typing",
          Map.of("typing", false));

    } catch (Exception e) {
      log.error("AI reply failed for bot {} node {}", bot.getId(), node.getId(), e);
      outboxService.publishChatEvent(bot.getId(), session.getVisitorId(), "message",
          Map.of("role", "ai", "content", "I'm sorry, I encountered an error. Please try again."));
    }

    return NodeExecutionResult.next();
  }
}
