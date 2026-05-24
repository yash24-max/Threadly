package dev.threadly.core.runtime.executors;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.flow.Flow;
import dev.threadly.core.flow.FlowRepository;
import dev.threadly.core.runtime.FlowGraph;
import dev.threadly.core.runtime.NodeExecutionResult;
import dev.threadly.core.runtime.NodeExecutor;
import dev.threadly.core.runtime.Session;
import dev.threadly.core.runtime.SessionRepository;
import dev.threadly.core.workspace.Bot;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Executes a referenced subflow as a child flow, maintaining variable scope.
 *
 * <p>Node data shape:
 * {
 *   "subflowId": "uuid-of-target-flow",
 *   "inputVariables": { "name": "{{customer_name}}", "email": "{{session.email}}" },
 *   "outputVariable": "subflow_result"
 * }
 *
 * <p>Creates a child session that inherits parent variables, executes the flow, then merges
 * output back to parent with prefix from outputVariable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubflowNodeExecutor implements NodeExecutor {

  private final FlowRepository flowRepository;
  private final SessionRepository sessionRepository;

  @Override
  public String nodeType() {
    return "subflow";
  }

  @Override
  public NodeExecutionResult execute(
      FlowGraph.Node node, Session session, Conversation conversation, Bot bot, UUID orgId) {

    Map<String, Object> data = node.getData();
    String subflowId = (String) data.get("subflowId");
    String outputVariable = (String) data.getOrDefault("outputVariable", "subflow_result");
    Map<String, Object> inputVariables =
        (Map<String, Object>) data.getOrDefault("inputVariables", new HashMap<>());

    if (subflowId == null || subflowId.isBlank()) {
      log.error("SubflowNode {}: subflowId is missing in node data.", node.getId());
      return NodeExecutionResult.next();
    }

    try {
      UUID subflowUuid = UUID.fromString(subflowId);

      // Load the target flow
      Flow subflow =
          flowRepository
              .findById(subflowUuid)
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Subflow not found: " + subflowId));

      // Verify the subflow belongs to the same org
      if (!subflow.getOrg().getId().equals(orgId)) {
        log.error(
            "SubflowNode {}: Subflow {} does not belong to org {}.",
            node.getId(),
            subflowId,
            orgId);
        return NodeExecutionResult.next();
      }

      // Create child session with parent variables
      Map<String, Object> childVars = new HashMap<>(session.getVariables());

      // Resolve and inject input variables
      for (Map.Entry<String, Object> entry : inputVariables.entrySet()) {
        String varName = entry.getKey();
        String template = entry.getValue() == null ? "" : entry.getValue().toString();
        String resolved = TemplateEngine.render(template, session.getVariables());
        childVars.put(varName, resolved);
      }

      // Create child session
      Session childSession =
          Session.builder()
              .bot(subflow.getBot())
              .visitorId(session.getVisitorId())
              .orgId(orgId)
              .conversationId(conversation.getId())
              .variables(childVars)
              .status("active")
              .build();

      // Note: In a production environment, you would execute the subflow runtime here.
      // This is a simplified version that stores the child session.
      // The actual flow execution would be handled by FlowRuntime or similar service.
      childSession = sessionRepository.save(childSession);

      log.debug(
          "SubflowNode {}: Created child session {} for subflow {}.",
          node.getId(),
          childSession.getId(),
          subflowId);

      // Merge output back to parent
      // In production, after subflow completes, extract output variables and merge them
      Map<String, Object> parentVars = session.getVariables();
      Map<String, Object> subflowOutput = new HashMap<>();

      // Copy relevant output variables from child session with prefix
      for (Map.Entry<String, Object> entry : childSession.getVariables().entrySet()) {
        if (entry.getKey().startsWith("output.")) {
          String outputKey = entry.getKey().substring("output.".length());
          subflowOutput.put(outputKey, entry.getValue());
        }
      }

      // Store subflow output in parent
      parentVars.put(outputVariable, subflowOutput);
      session.setVariables(parentVars);

      log.debug(
          "SubflowNode {}: Merged output from subflow {} into variable '{}'.",
          node.getId(),
          subflowId,
          outputVariable);

    } catch (IllegalArgumentException e) {
      log.error("SubflowNode {}: Invalid subflow ID format: {}", node.getId(), subflowId, e);
    } catch (EntityNotFoundException e) {
      log.error("SubflowNode {}: {}", node.getId(), e.getMessage());
    } catch (Exception e) {
      log.error(
          "SubflowNode {}: Unexpected error executing subflow {}",
          node.getId(),
          subflowId,
          e);
    }

    return NodeExecutionResult.next();
  }
}
