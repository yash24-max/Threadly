package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.runtime.FlowGraph;
import dev.threadly.core.runtime.NodeExecutionResult;
import dev.threadly.core.runtime.NodeExecutor;
import dev.threadly.core.runtime.Session;
import dev.threadly.core.workspace.Bot;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Pauses flow execution for a configurable number of seconds.
 *
 * <p>Node data: {@code { "seconds": 5 }}
 *
 * <p>Stores {@code resume_at} in session variables so the scheduler can resume the flow when the
 * delay has elapsed.
 */
@Slf4j
@Component
public class DelayNodeExecutor implements NodeExecutor {

  @Override
  public String nodeType() {
    return "delay";
  }

  @Override
  public NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    int seconds = 0;
    Object rawSeconds = data.get("seconds");
    if (rawSeconds instanceof Number num) {
      seconds = num.intValue();
    } else if (rawSeconds instanceof String s) {
      try {
        seconds = Integer.parseInt(s);
      } catch (NumberFormatException e) {
        log.warn("DelayNode {}: invalid seconds value '{}', defaulting to 0", node.getId(), rawSeconds);
      }
    }

    Instant resumeAt = Instant.now().plusSeconds(Math.max(0, seconds));

    Map<String, Object> vars = session.getVariables();
    vars.put("resume_at", resumeAt.toString());
    session.setVariables(vars);

    log.debug("DelayNode {}: will resume at {}", node.getId(), resumeAt);
    return NodeExecutionResult.waitUntil(resumeAt);
  }
}
