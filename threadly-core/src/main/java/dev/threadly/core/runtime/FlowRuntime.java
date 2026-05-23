package dev.threadly.core.runtime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.core.conversation.*;
import dev.threadly.core.flow.Flow;
import dev.threadly.core.flow.FlowRepository;
import dev.threadly.core.outbox.OutboxService;
import dev.threadly.core.workspace.Bot;
import dev.threadly.core.workspace.BotRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Executes a published flow for a given visitor session.
 * Called from the Centrifugo proxy when a visitor sends a message.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowRuntime {

  private final BotRepository botRepository;
  private final FlowRepository flowRepository;
  private final SessionRepository sessionRepository;
  private final ConversationRepository conversationRepository;
  private final MessageRepository messageRepository;
  private final NodeExecutorFactory nodeExecutorFactory;
  private final OutboxService outboxService;
  private final ObjectMapper objectMapper;

  @Transactional
  public void handleVisitorMessage(UUID botId, String visitorId, String text, UUID orgId) {
    Bot bot = botRepository.findByIdAndOrgId(botId, orgId)
        .orElseThrow(() -> new EntityNotFoundException("Bot not found: " + botId));

    Flow flow = flowRepository.findByBotIdAndOrgId(botId, orgId)
        .orElseThrow(() -> new EntityNotFoundException("No flow for bot: " + botId));

    String flowJson = flow.getPublishedJson();
    if (flowJson == null) {
      log.warn("Bot {} has no published flow. Ignoring message.", botId);
      return;
    }

    // Get or create session + conversation
    Session session = sessionRepository.findByBotIdAndVisitorId(botId, visitorId)
        .orElseGet(() -> createSession(bot, visitorId, orgId));

    Conversation conversation = getOrCreateConversation(session, bot, visitorId, orgId);

    // If handed off to human, ignore AI flow
    if ("handed_off".equals(session.getStatus())) {
      persistMessage(conversation, orgId, "user", text, null, null);
      outboxService.publishChatEvent(botId, visitorId, "message", Map.of(
          "role", "user", "content", text, "conversationId", conversation.getId().toString()));
      return;
    }

    // Persist visitor message
    persistMessage(conversation, orgId, "user", text, null, null);
    outboxService.publishChatEvent(botId, visitorId, "message", Map.of(
        "role", "user", "content", text, "conversationId", conversation.getId().toString()));

    // Update session variables with the input
    Map<String, Object> vars = session.getVariables();
    vars.put("session.last_input", text);
    session.setVariables(vars);

    // Parse flow graph
    FlowGraph graph;
    try {
      graph = FlowGraph.parse(flowJson, objectMapper);
    } catch (Exception e) {
      log.error("Failed to parse flow JSON for bot {}", botId, e);
      return;
    }

    // Determine starting node
    String currentNodeId = "waiting".equals(session.getStatus())
        ? session.getCurrentNodeId()
        : "start";

    if ("waiting".equals(session.getStatus())) {
      // Resume from the node that was waiting for input — advance to its next node
      currentNodeId = graph.nextNodeId(currentNodeId, "default");
      session.setStatus("active");
    }

    // Execute the flow graph from currentNodeId
    executeFrom(graph, currentNodeId, session, conversation, bot, orgId);

    sessionRepository.save(session);
    conversationRepository.save(conversation);
  }

  private void executeFrom(FlowGraph graph, String startNodeId, Session session,
      Conversation conversation, Bot bot, UUID orgId) {
    String nodeId = startNodeId;
    int maxSteps = 50; // prevent infinite loops

    while (nodeId != null && maxSteps-- > 0) {
      FlowGraph.Node node = graph.getNode(nodeId);
      if (node == null) break;

      session.setCurrentNodeId(nodeId);
      NodeExecutor executor = nodeExecutorFactory.getExecutor(node.getType());

      NodeExecutionResult result = executor.execute(node, session, conversation, bot, orgId);

      if (result.getJumpToNodeId() != null) {
        // Switch node — jump directly to a specific node
        nodeId = result.getJumpToNodeId();
        continue;
      }
      if (result.isPause()) {
        session.setStatus("waiting");
        if (result.getWaitUntil() != null) {
          // Delay node — store resume time in session variables
          Map<String, Object> vars = session.getVariables();
          vars.put("resume_at", result.getWaitUntil().toString());
          session.setVariables(vars);
        }
        break;
      }
      if (result.isHandoff()) {
        session.setStatus("handed_off");
        conversation.setStatus("handed_off");
        break;
      }
      if (result.isEnd()) {
        session.setStatus("completed");
        conversation.setStatus("closed");
        break;
      }

      nodeId = graph.nextNodeId(nodeId, result.getEdgeHandle());
    }
  }

  private Session createSession(Bot bot, String visitorId, UUID orgId) {
    Session s = Session.builder()
        .bot(bot)
        .visitorId(visitorId)
        .orgId(orgId)
        .status("active")
        .currentNodeId("start")
        .variables(new HashMap<>())
        .build();
    return sessionRepository.save(s);
  }

  private Conversation getOrCreateConversation(Session session, Bot bot, String visitorId, UUID orgId) {
    if (session.getConversationId() != null) {
      return conversationRepository.findById(session.getConversationId())
          .orElseGet(() -> createConversation(session, bot, visitorId, orgId));
    }
    Conversation conv = createConversation(session, bot, visitorId, orgId);
    session.setConversationId(conv.getId());
    return conv;
  }

  private Conversation createConversation(Session session, Bot bot, String visitorId, UUID orgId) {
    Conversation conv = Conversation.builder()
        .bot(bot)
        .orgId(orgId)
        .visitorId(visitorId)
        .status("open")
        .channel("website")
        .metadata("{}")
        .build();
    conv = conversationRepository.save(conv);
    // Publish new conversation event to dashboard channel
    outboxService.publishDashboardEvent(orgId, "new_conversation", Map.of(
        "botId", bot.getId().toString(),
        "conversationId", conv.getId().toString(),
        "visitorId", visitorId));
    return conv;
  }

  private void persistMessage(Conversation conv, UUID orgId, String role, String content,
      Integer latencyMs, String nodeId) {
    Message msg = Message.builder()
        .conversation(conv)
        .orgId(orgId)
        .role(role)
        .content(content)
        .latencyMs(latencyMs)
        .nodeId(nodeId)
        .metadata("{}")
        .build();
    messageRepository.save(msg);
  }
}
