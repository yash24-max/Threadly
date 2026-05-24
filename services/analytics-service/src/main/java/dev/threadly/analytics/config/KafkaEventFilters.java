package dev.threadly.analytics.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka event filter strategies for selective event processing.
 * Filters events at the Kafka listener level to avoid unnecessary processing.
 */
@Component
@Slf4j
public class KafkaEventFilters {

    /**
     * Filter for CONVERSATION_STARTED events.
     */
    @Bean("conversationStartedEventFilter")
    public RecordFilterStrategy<String, Map<String, Object>> conversationStartedEventFilter() {
        return consumerRecord -> {
            try {
                Map<String, Object> payload = consumerRecord.value();
                String eventType = (String) payload.get("event_type");
                return !"CONVERSATION_STARTED".equals(eventType);
            } catch (Exception e) {
                log.warn("Error in conversationStartedEventFilter", e);
                return false;
            }
        };
    }

    /**
     * Filter for CONVERSATION_ENDED events.
     */
    @Bean("conversationEndedEventFilter")
    public RecordFilterStrategy<String, Map<String, Object>> conversationEndedEventFilter() {
        return consumerRecord -> {
            try {
                Map<String, Object> payload = consumerRecord.value();
                String eventType = (String) payload.get("event_type");
                return !"CONVERSATION_ENDED".equals(eventType);
            } catch (Exception e) {
                log.warn("Error in conversationEndedEventFilter", e);
                return false;
            }
        };
    }

    /**
     * Filter for MESSAGE_ADDED events.
     */
    @Bean("messageAddedEventFilter")
    public RecordFilterStrategy<String, Map<String, Object>> messageAddedEventFilter() {
        return consumerRecord -> {
            try {
                Map<String, Object> payload = consumerRecord.value();
                String eventType = (String) payload.get("event_type");
                return !"MESSAGE_ADDED".equals(eventType);
            } catch (Exception e) {
                log.warn("Error in messageAddedEventFilter", e);
                return false;
            }
        };
    }

    /**
     * Filter for AI_REPLY_REQUESTED events.
     */
    @Bean("aiReplyRequestedEventFilter")
    public RecordFilterStrategy<String, Map<String, Object>> aiReplyRequestedEventFilter() {
        return consumerRecord -> {
            try {
                Map<String, Object> payload = consumerRecord.value();
                String eventType = (String) payload.get("event_type");
                return !"AI_REPLY_REQUESTED".equals(eventType);
            } catch (Exception e) {
                log.warn("Error in aiReplyRequestedEventFilter", e);
                return false;
            }
        };
    }

    /**
     * Filter for HANDOFF_INITIATED events.
     */
    @Bean("handoffInitiatedEventFilter")
    public RecordFilterStrategy<String, Map<String, Object>> handoffInitiatedEventFilter() {
        return consumerRecord -> {
            try {
                Map<String, Object> payload = consumerRecord.value();
                String eventType = (String) payload.get("event_type");
                return !"HANDOFF_INITIATED".equals(eventType);
            } catch (Exception e) {
                log.warn("Error in handoffInitiatedEventFilter", e);
                return false;
            }
        };
    }

    /**
     * Filter for NODE_EXECUTED events.
     */
    @Bean("nodeExecutedEventFilter")
    public RecordFilterStrategy<String, Map<String, Object>> nodeExecutedEventFilter() {
        return consumerRecord -> {
            try {
                Map<String, Object> payload = consumerRecord.value();
                String eventType = (String) payload.get("event_type");
                return !"NODE_EXECUTED".equals(eventType);
            } catch (Exception e) {
                log.warn("Error in nodeExecutedEventFilter", e);
                return false;
            }
        };
    }

}
