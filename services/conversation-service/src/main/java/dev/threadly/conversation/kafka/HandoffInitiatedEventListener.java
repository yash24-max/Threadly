package dev.threadly.conversation.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.conversation.service.HandoffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka event listener for handoff initiated events.
 * Updates conversation status when a handoff to human agent is initiated.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HandoffInitiatedEventListener {
    private final HandoffService handoffService;
    private final ObjectMapper objectMapper;

    /**
     * Handle handoff initiated event.
     *
     * @param payload the event payload
     * @param headers the message headers
     */
    @KafkaListener(
        topics = "handoff-initiated",
        groupId = "conversation-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onHandoffInitiated(
        @Payload String payload,
        @Headers Map<String, Object> headers) {

        try {
            log.debug("Received handoff initiated event");

            // Parse the event
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);

            String orgId = (String) event.get("org_id");
            String conversationId = (String) event.get("conversation_id");
            String agentId = (String) event.get("agent_id");
            String reason = (String) event.get("reason");

            // Validate required fields
            if (orgId == null || conversationId == null || agentId == null) {
                log.warn("Invalid handoff initiated event - missing required fields");
                return;
            }

            // Initiate handoff
            handoffService.initiateHandoff(conversationId, orgId, agentId, reason);
            log.info("Conversation handed off to agent: {} for conversation: {}", agentId, conversationId);

        } catch (Exception e) {
            log.error("Error processing handoff initiated event", e);
            // In production, would send to DLQ
        }
    }
}
