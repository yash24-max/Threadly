package dev.threadly.core.runtime;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.workspace.Bot;
import java.util.UUID;

/** SPI for node type executors. */
public interface NodeExecutor {

  /** Node type this executor handles (e.g. "message", "ai_reply"). */
  String nodeType();

  NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId);
}
