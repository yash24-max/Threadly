package dev.threadly.conversation.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.conversation.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka event listener for session ended events.
 * Closes the conversation when a session ends.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionEndedEventListener {
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    /**
     * Handle session ended event.
     *
     * @param payload the event payload
     * @param headers the message headers
     */
    @KafkaListener(
        topics = "session-ended",
        groupId = "conversation-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onSessionEnded(
        @Payload String payload,
        @Headers Map<String, Object> headers) {

        try {
            log.debug("Received session ended event");

            // Parse the event
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);

            String orgId = (String) event.get("org_id");
            String conversationId = (String) event.get("conversation_id");

            // Validate required fields
            if (orgId == null || conversationId == null) {
                log.warn("Invalid session ended event - missing required fields");
                return;
            }

            // Close conversation
            conversationService.closeConversation(conversationId, orgId);
            log.info("Conversation closed for session: {} in org: {}", conversationId, orgId);

        } catch (Exception e) {
            log.error("Error processing session ended event", e);
            // In production, would send to DLQ
        }
    }
}
