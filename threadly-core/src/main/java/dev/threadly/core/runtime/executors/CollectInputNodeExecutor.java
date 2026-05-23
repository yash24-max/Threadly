package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.conversation.Message;
import dev.threadly.core.conversation.MessageRepository;
import dev.threadly.core.outbox.OutboxService;
import dev.threadly.core.runtime.FlowGraph;
import dev.threadly.core.runtime.NodeExecutionResult;
import dev.threadly.core.runtime.NodeExecutor;
import dev.threadly.core.runtime.Session;
import dev.threadly.core.workspace.Bot;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Prompts the user for input, stores the value in a named session variable once received, and
 * optionally validates it.
 *
 * <p>Node data:
 *
 * <pre>{@code
 * {
 *   "variable": "phone",
 *   "validation": "phone|email|none",
 *   "prompt": "Please enter your phone number"
 * }
 * }</pre>
 *
 * <p>On first execution (no pending input): emits the prompt and pauses.
 * On resume (last_input present in session): validates and stores the value, then advances.
 * If validation fails: re-emits prompt and pauses again.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectInputNodeExecutor implements NodeExecutor {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PHONE_PATTERN =
      Pattern.compile("^\\+?[0-9\\s\\-().]{7,20}$");

  private static final String PENDING_INPUT_FLAG = "_collecting_input_node_";

  private final MessageRepository messageRepository;
  private final OutboxService outboxService;

  @Override
  public String nodeType() {
    return "collect_input";
  }

  @Override
  public NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String variable = (String) data.getOrDefault("variable", "input");
    String validation = (String) data.getOrDefault("validation", "none");
    String prompt = TemplateEngine.render(
        (String) data.getOrDefault("prompt", "Please enter a value"), session.getVariables());
    String pendingKey = PENDING_INPUT_FLAG + node.getId();

    Map<String, Object> vars = session.getVariables();
    String lastInput = (String) vars.get("session.last_input");
    boolean isPending = vars.containsKey(pendingKey);

    if (isPending && lastInput != null && !lastInput.isBlank()) {
      // We have input — validate it
      if (isValid(lastInput, validation)) {
        vars.put(variable, lastInput);
        vars.remove(pendingKey);
        vars.remove("session.last_input");
        session.setVariables(vars);
        log.debug(
            "CollectInputNode {}: collected '{}' = '{}'", node.getId(), variable, lastInput);
        return NodeExecutionResult.next();
      } else {
        // Validation failed — re-prompt
        String retryPrompt = buildRetryPrompt(validation, prompt);
        emitPrompt(retryPrompt, node.getId(), session, conversation, bot, orgId);
        return NodeExecutionResult.pause();
      }
    }

    // First pass — emit prompt and pause
    vars.put(pendingKey, "true");
    session.setVariables(vars);
    emitPrompt(prompt, node.getId(), session, conversation, bot, orgId);
    return NodeExecutionResult.pause();
  }

  private void emitPrompt(
      String text,
      String nodeId,
      Session session,
      Conversation conversation,
      Bot bot,
      UUID orgId) {
    messageRepository.save(
        Message.builder()
            .conversation(conversation)
            .orgId(orgId)
            .role("ai")
            .content(text)
            .nodeId(nodeId)
            .metadata("{}")
            .build());

    outboxService.publishChatEvent(
        bot.getId(),
        session.getVisitorId(),
        "message",
        Map.of("role", "ai", "content", text, "nodeId", nodeId));
  }

  private boolean isValid(String value, String validation) {
    return switch (validation) {
      case "email" -> EMAIL_PATTERN.matcher(value.trim()).matches();
      case "phone" -> PHONE_PATTERN.matcher(value.trim()).matches();
      default -> true;
    };
  }

  private String buildRetryPrompt(String validation, String originalPrompt) {
    return switch (validation) {
      case "email" -> "That doesn't look like a valid email address. " + originalPrompt;
      case "phone" -> "That doesn't look like a valid phone number. " + originalPrompt;
      default -> originalPrompt;
    };
  }
}
