package dev.threadly.core.common;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

@Component
public class ThreadlyMetrics {
    private final MeterRegistry registry;

    public ThreadlyMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    // Increment messages processed count
    public void incrementMessagesProcessed(String botId, String nodeType) {
        Counter.builder("threadly.messages.processed")
            .tag("bot_id", botId)
            .tag("node_type", nodeType)
            .register(registry)
            .increment();
    }

    // Record flow execution duration
    public Timer.Sample startFlowTimer() {
        return Timer.start(registry);
    }

    public void stopFlowTimer(Timer.Sample sample, String botId, boolean success) {
        sample.stop(Timer.builder("threadly.flow.execution.duration")
            .tag("bot_id", botId)
            .tag("success", String.valueOf(success))
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry));
    }

    // Record AI token usage
    public void recordTokenUsage(String provider, String model, String botId,
                                  long inputTokens, long outputTokens, double costUsd) {
        Counter.builder("threadly.ai.input_tokens")
            .tag("provider", provider).tag("model", model).tag("bot_id", botId)
            .register(registry).increment(inputTokens);

        Counter.builder("threadly.ai.output_tokens")
            .tag("provider", provider).tag("model", model).tag("bot_id", botId)
            .register(registry).increment(outputTokens);

        Counter.builder("threadly.ai.cost_usd")
            .tag("provider", provider).tag("model", model).tag("bot_id", botId)
            .register(registry).increment(costUsd);
    }

    // Increment handoff count
    public void incrementHandoffs(String botId) {
        Counter.builder("threadly.handoffs")
            .tag("bot_id", botId)
            .register(registry)
            .increment();
    }

    // Increment webhook deliveries
    public void incrementWebhookDeliveries(String botId, boolean success) {
        Counter.builder("threadly.webhook.deliveries")
            .tag("bot_id", botId)
            .tag("success", String.valueOf(success))
            .register(registry)
            .increment();
    }
}
