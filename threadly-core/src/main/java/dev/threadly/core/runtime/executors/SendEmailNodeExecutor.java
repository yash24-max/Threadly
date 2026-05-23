package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.outbox.OutboxService;
import dev.threadly.core.runtime.FlowGraph;
import dev.threadly.core.runtime.NodeExecutionResult;
import dev.threadly.core.runtime.NodeExecutor;
import dev.threadly.core.runtime.Session;
import dev.threadly.core.workspace.Bot;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves template variables in "to", "subject", and "body", then writes an outbox event of type
 * {@code email.send} for async delivery.
 *
 * <p>Node data:
 *
 * <pre>{@code
 * {
 *   "to": "{{session.email}}",
 *   "subject": "Thanks for contacting us",
 *   "body": "Hello {{session.name}}, ..."
 * }
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SendEmailNodeExecutor implements NodeExecutor {

  private final OutboxService outboxService;

  @Override
  public String nodeType() {
    return "send_email";
  }

  @Override
  public NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    Map<String, Object> vars = session.getVariables();

    String to = TemplateEngine.render((String) data.getOrDefault("to", ""), vars);
    String subject = TemplateEngine.render((String) data.getOrDefault("subject", ""), vars);
    String body = TemplateEngine.render((String) data.getOrDefault("body", ""), vars);

    if (to.isBlank()) {
      log.warn("SendEmailNode {}: 'to' resolved to blank — skipping", node.getId());
      return NodeExecutionResult.next();
    }

    outboxService.publishDashboardEvent(
        orgId,
        "email.send",
        Map.of(
            "to", to,
            "subject", subject,
            "body", body,
            "botId", bot.getId().toString(),
            "conversationId", conversation.getId().toString(),
            "nodeId", node.getId()));

    log.debug("SendEmailNode {}: queued email to '{}'", node.getId(), to);
    return NodeExecutionResult.next();
  }
}
