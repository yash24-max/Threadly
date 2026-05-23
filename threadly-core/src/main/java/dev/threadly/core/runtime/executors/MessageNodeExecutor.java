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
public class MessageNodeExecutor implements NodeExecutor {

  private final MessageRepository messageRepository;
  private final OutboxService outboxService;

  @Override
  public String nodeType() { return "message"; }

  @Override
  public NodeExecutionResult execute(FlowGraph.Node node, Session session,
      Conversation conversation, Bot bot, UUID orgId) {

    String text = (String) node.getData().getOrDefault("text", "");
    text = TemplateEngine.render(text, session.getVariables());

    messageRepository.save(Message.builder()
        .conversation(conversation)
        .orgId(orgId)
        .role("ai")
        .content(text)
        .nodeId(node.getId())
        .metadata("{}")
        .build());

    outboxService.publishChatEvent(bot.getId(), session.getVisitorId(), "message",
        Map.of("role", "ai", "content", text, "nodeId", node.getId()));

    return NodeExecutionResult.next();
  }
}
