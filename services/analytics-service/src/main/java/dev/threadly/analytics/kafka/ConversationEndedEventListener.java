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
 * Kafka listener for CONVERSATION_ENDED events.
 * Captures and tracks conversation completion events for analytics.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ConversationEndedEventListener {

    private final EventTrackingService eventTrackingService;

    /**
     * Handle CONVERSATION_ENDED event from Kafka.
     *
     * @param payload event payload with conversation completion data
     * @param partition kafka partition
     * @param offset kafka offset
     * @param ack manual acknowledgment
     */
    @KafkaListener(
        topics = "conversation-events",
        groupId = "analytics-service",
        filter = "conversationEndedEventFilter"
    )
    public void handleConversationEnded(
        @Payload Map<String, Object> payload,
        @Header("kafka_receivedPartitionId") int partition,
        @Header("kafka_offset") long offset,
        Acknowledgment ack
    ) {
        try {
            log.debug("Received CONVERSATION_ENDED event from partition {} offset {}", partition, offset);

            String eventType = "CONVERSATION_ENDED";
            String orgId = (String) payload.get("org_id");
            String botId = (String) payload.get("bot_id");
            String conversationId = (String) payload.get("conversation_id");

            // Validate required fields
            if (orgId == null || botId == null || conversationId == null) {
                log.warn("Missing required fields in CONVERSATION_ENDED event: {}", payload);
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

            log.debug("Successfully processed CONVERSATION_ENDED event for conversation: {}", conversationId);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing CONVERSATION_ENDED event", e);
            // Don't acknowledge on error - message will be retried
        }
    }

}
