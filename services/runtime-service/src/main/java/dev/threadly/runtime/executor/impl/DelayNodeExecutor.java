package dev.threadly.runtime.executor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import dev.threadly.runtime.executor.ExecutionContext;
import dev.threadly.runtime.executor.ExecutionResult;
import dev.threadly.runtime.executor.NodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DelayNodeExecutor introduces a pause in flow execution.
 * Can be used for rate limiting or creating natural conversation delays.
 */
@Component
@Slf4j
public class DelayNodeExecutor extends NodeExecutor {

  private static final Long DEFAULT_DELAY_MS = 1000L;
  private static final Long MAX_DELAY_MS = 300000L; // 5 minutes

  @Override
  public String getName() {
    return "Delay Executor";
  }

  @Override
  public String getType() {
    return "DELAY";
  }

  @Override
  public boolean validate(ExecutionContext context) {
    if (!super.validate(context)) {
      return false;
    }
    JsonNode node = context.getCurrentNode();
    return node.has("duration") || node.has("seconds") || node.has("milliseconds");
  }

  @Override
  public String getValidationError(ExecutionContext context) {
    return "Delay node must have 'duration', 'seconds', or 'milliseconds' field";
  }

  @Override
  public ExecutionResult execute(ExecutionContext context) {
    long startTime = System.currentTimeMillis();
    context.setMDCContext();

    try {
      log.debug("Executing DELAY node: {}", context.getCurrentNode().get("id"));

      JsonNode node = context.getCurrentNode();

      // Extract delay duration in milliseconds
      long delayMs = extractDelayMs(node);

      // Enforce maximum delay to prevent abuse
      if (delayMs > MAX_DELAY_MS) {
        log.warn("Delay {} ms exceeds maximum {}, capping to maximum", delayMs, MAX_DELAY_MS);
        delayMs = MAX_DELAY_MS;
      }

      log.info("Delaying execution for {} ms", delayMs);

      // Apply delay
      try {
        Thread.sleep(delayMs);
      } catch (InterruptedException e) {
        log.warn("Delay interrupted", e);
        Thread.currentThread().interrupt();
      }

      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.SUCCESS)
          .statusMessage("Delay completed")
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .nextNodeId(node.has("next") ? node.get("next").asText() : null)
          .build();

    } catch (Exception e) {
      log.error("Error executing DELAY node", e);
      return ExecutionResult.builder()
          .status(ExecutionResult.ExecutionStatus.FAILURE)
          .statusMessage("Failed to apply delay")
          .errorMessage(e.getMessage())
          .exception(e)
          .executionTimeMs(System.currentTimeMillis() - startTime)
          .build();
    } finally {
      context.clearMDCContext();
    }
  }

  /**
   * Extract delay duration in milliseconds from various field formats
   */
  private long extractDelayMs(JsonNode node) {
    if (node.has("milliseconds")) {
      return node.get("milliseconds").asLong();
    }
    if (node.has("seconds")) {
      return node.get("seconds").asLong() * 1000;
    }
    if (node.has("duration")) {
      JsonNode durationNode = node.get("duration");
      if (durationNode.isNumber()) {
        return durationNode.asLong();
      }
      // Parse string like "5s", "100ms"
      String duration = durationNode.asText();
      if (duration.endsWith("s")) {
        return Long.parseLong(duration.substring(0, duration.length() - 1)) * 1000;
      }
      if (duration.endsWith("ms")) {
        return Long.parseLong(duration.substring(0, duration.length() - 2));
      }
    }
    return DEFAULT_DELAY_MS;
  }
}
