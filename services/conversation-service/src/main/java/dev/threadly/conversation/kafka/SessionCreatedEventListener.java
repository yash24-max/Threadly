package dev.threadly.conversation.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.conversation.dto.CreateConversationRequest;
import dev.threadly.conversation.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka event listener for session creation events.
 * Creates a new conversation when a session starts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCreatedEventListener {
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    /**
     * Handle session created event.
     *
     * @param payload the event payload
     * @param headers the message headers
     */
    @KafkaListener(
        topics = "session-created",
        groupId = "conversation-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onSessionCreated(
        @Payload String payload,
        @Headers Map<String, Object> headers) {

        try {
            log.debug("Received session created event: {}", payload);

            // Parse the event
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);

            String orgId = (String) event.get("org_id");
            String botId = (String) event.get("bot_id");
            String flowId = (String) event.get("flow_id");
            String visitorId = (String) event.get("visitor_id");

            // Validate required fields
            if (orgId == null || botId == null || visitorId == null) {
                log.warn("Invalid session created event - missing required fields: {}", payload);
                return;
            }

            // Create conversation
            CreateConversationRequest request = CreateConversationRequest.builder()
                .botId(botId)
                .flowId(flowId)
                .visitorId(visitorId)
                .build();

            conversationService.createConversation(orgId, request);
            log.info("Conversation created for session: {} in org: {}", visitorId, orgId);

        } catch (Exception e) {
            log.error("Error processing session created event: {}", payload, e);
            // In production, would send to DLQ
        }
    }
}
