package dev.threadly.conversation.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.threadly.conversation.dto.AddMessageRequest;
import dev.threadly.conversation.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka event listener for message added events.
 * Stores messages in the conversation service when messages are added via runtime.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageAddedEventListener {
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    /**
     * Handle message added event.
     *
     * @param payload the event payload
     * @param headers the message headers
     */
    @KafkaListener(
        topics = "message-added",
        groupId = "conversation-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessageAdded(
        @Payload String payload,
        @Headers Map<String, Object> headers) {

        try {
            log.debug("Received message added event");

            // Parse the event
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);

            String orgId = (String) event.get("org_id");
            String conversationId = (String) event.get("conversation_id");
            String sender = (String) event.get("sender");
            String senderId = (String) event.get("sender_id");
            String content = (String) event.get("content");
            String metadataJson = (String) event.get("metadata_json");
            Long tokensUsed = event.get("tokens_used") != null ? ((Number) event.get("tokens_used")).longValue() : null;

            // Validate required fields
            if (orgId == null || conversationId == null || sender == null || content == null) {
                log.warn("Invalid message added event - missing required fields");
                return;
            }

            // Create message
            AddMessageRequest request = AddMessageRequest.builder()
                .sender(sender)
                .senderId(senderId)
                .content(content)
                .metadataJson(metadataJson)
                .tokensUsed(tokensUsed)
                .build();

            messageService.addMessage(conversationId, orgId, request);
            log.debug("Message stored for conversation: {}", conversationId);

        } catch (Exception e) {
            log.error("Error processing message added event", e);
            // In production, would send to DLQ
        }
    }
}
