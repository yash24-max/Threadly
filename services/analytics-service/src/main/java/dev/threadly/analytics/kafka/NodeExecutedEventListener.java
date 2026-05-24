package dev.threadly.analytics.kafka;

import dev.threadly.analytics.service.EventTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka listener for NODE_EXECUTED events.
 * Captures and tracks flow node execution for analytics.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NodeExecutedEventListener {

    private final EventTrackingService eventTrackingService;

    /**
     * Handle NODE_EXECUTED event from Kafka.
     *
     * @param payload event payload with node execution data
     * @param partition kafka partition
     * @param offset kafka offset
     * @param ack manual acknowledgment
     */
    @KafkaListener(
        topics = "runtime-events",
        groupId = "analytics-service",
        filter = "nodeExecutedEventFilter"
    )
    public void handleNodeExecuted(
        @Payload Map<String, Object> payload,
        @Header("kafka_receivedPartitionId") int partition,
        @Header("kafka_offset") long offset,
        Acknowledgment ack
    ) {
        try {
            log.debug("Received NODE_EXECUTED event from partition {} offset {}", partition, offset);

            String eventType = "NODE_EXECUTED";
            String orgId = (String) payload.get("org_id");
            String botId = (String) payload.get("bot_id");
            String conversationId = (String) payload.get("conversation_id");

            // Validate required fields
            if (orgId == null || botId == null) {
                log.warn("Missing required fields in NODE_EXECUTED event: {}", payload);
                return;
            }

            // Track the event
            eventTrackingService.trackEvent(
                orgId,
                eventType,
                botId,
                conversationId,
                null,
                payload
            );

            log.debug("Successfully processed NODE_EXECUTED event for bot: {}", botId);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing NODE_EXECUTED event", e);
            // Don't acknowledge on error - message will be retried
        }
    }

}
