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
 * Kafka listener for AI_REPLY_REQUESTED events.
 * Captures and tracks AI reply requests for performance analytics.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiReplyRequestedEventListener {

    private final EventTrackingService eventTrackingService;

    /**
     * Handle AI_REPLY_REQUESTED event from Kafka.
     *
     * @param payload event payload with AI request data
     * @param partition kafka partition
     * @param offset kafka offset
     * @param ack manual acknowledgment
     */
    @KafkaListener(
        topics = "runtime-events",
        groupId = "analytics-service",
        filter = "aiReplyRequestedEventFilter"
    )
    public void handleAiReplyRequested(
        @Payload Map<String, Object> payload,
        @Header("kafka_receivedPartitionId") int partition,
        @Header("kafka_offset") long offset,
        Acknowledgment ack
    ) {
        try {
            log.debug("Received AI_REPLY_REQUESTED event from partition {} offset {}", partition, offset);

            String eventType = "AI_REPLY_REQUESTED";
            String orgId = (String) payload.get("org_id");
            String botId = (String) payload.get("bot_id");
            String conversationId = (String) payload.get("conversation_id");

            // Validate required fields
            if (orgId == null || botId == null) {
                log.warn("Missing required fields in AI_REPLY_REQUESTED event: {}", payload);
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

            log.debug("Successfully processed AI_REPLY_REQUESTED event for bot: {}", botId);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing AI_REPLY_REQUESTED event", e);
            // Don't acknowledge on error - message will be retried
        }
    }

}
