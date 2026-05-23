package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.conversation.Message;
import dev.threadly.core.conversation.MessageRepository;
import dev.threadly.core.outbox.OutboxService;
import dev.threadly.core.runtime.*;
import dev.threadly.core.workspace.Bot;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EndNodeExecutor implements NodeExecutor {

  private final MessageRepository messageRepository;
  private final OutboxService outboxService;

  @Override
  public String nodeType() { return "end"; }

  @Override
  public NodeExecutionResult execute(FlowGraph.Node node, Session session,
      Conversation conversation, Bot bot, UUID orgId) {

    String msg = (String) node.getData().getOrDefault("message", "");
    if (!msg.isBlank()) {
      messageRepository.save(Message.builder()
          .conversation(conversation).orgId(orgId).role("ai")
          .content(msg).nodeId(node.getId()).metadata("{}").build());
      outboxService.publishChatEvent(bot.getId(), session.getVisitorId(), "message",
          Map.of("role", "ai", "content", msg));
    }
    outboxService.publishChatEvent(bot.getId(), session.getVisitorId(), "end",
        Map.of("conversationId", conversation.getId().toString()));
    return NodeExecutionResult.end();
  }
}
