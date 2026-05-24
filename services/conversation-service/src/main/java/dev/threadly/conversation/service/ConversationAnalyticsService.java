package dev.threadly.conversation.service;

import dev.threadly.conversation.dto.ConversationAnalyticsDto;
import dev.threadly.conversation.entity.Conversation;
import dev.threadly.conversation.entity.Message;
import dev.threadly.conversation.exception.ConversationNotFoundException;
import dev.threadly.conversation.repository.ConversationRepository;
import dev.threadly.conversation.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Service class for conversation analytics.
 * Provides metrics like sentiment, duration, message counts, and resolution rates.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ConversationAnalyticsService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Calculate analytics for a conversation.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @return analytics DTO
     */
    public ConversationAnalyticsDto calculateAnalytics(String conversationId, String orgId) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        List<Message> messages = messageRepository.findAllByConversationId(conversationId);

        ConversationAnalyticsDto.ConversationAnalyticsDtoBuilder builder = ConversationAnalyticsDto.builder()
            .conversationId(conversationId)
            .messageCount(messages.size())
            .totalTokensUsed(conversation.getTokensUsed())
            .status(conversation.getStatus().toString());

        // Count messages by sender
        int visitorMessages = (int) messages.stream()
            .filter(m -> m.getSender() == Message.MessageSender.VISITOR)
            .count();
        int aiMessages = (int) messages.stream()
            .filter(m -> m.getSender() == Message.MessageSender.AI)
            .count();
        int humanMessages = (int) messages.stream()
            .filter(m -> m.getSender() == Message.MessageSender.HUMAN)
            .count();

        builder.visitorMessageCount(visitorMessages)
            .aiMessageCount(aiMessages)
            .humanMessageCount(humanMessages);

        // Calculate average tokens per message
        if (!messages.isEmpty()) {
            long totalTokens = messages.stream()
                .filter(m -> m.getTokensUsed() != null)
                .mapToLong(Message::getTokensUsed)
                .sum();
            builder.averageTokensPerMessage((double) totalTokens / messages.size());
        }

        // Calculate duration
        if (conversation.getEndedAt() != null) {
            Duration duration = Duration.between(conversation.getStartedAt(), conversation.getEndedAt());
            builder.duration(duration);
        } else {
            Duration duration = Duration.between(conversation.getStartedAt(), Instant.now());
            builder.duration(duration);
        }

        // Calculate time to first human response
        if (humanMessages > 0) {
            Message firstHumanMessage = messages.stream()
                .filter(m -> m.getSender() == Message.MessageSender.HUMAN)
                .findFirst()
                .orElse(null);

            if (firstHumanMessage != null) {
                Duration timeToFirstHuman = Duration.between(conversation.getStartedAt(), firstHumanMessage.getCreatedAt());
                builder.timeToFirstHumanResponse(timeToFirstHuman);
            }
        }

        // Calculate average response time
        if (messages.size() > 1) {
            long totalResponseTime = 0;
            for (int i = 1; i < messages.size(); i++) {
                Duration timeBetween = Duration.between(messages.get(i - 1).getCreatedAt(), messages.get(i).getCreatedAt());
                totalResponseTime += timeBetween.toSeconds();
            }
            long averageSeconds = totalResponseTime / (messages.size() - 1);
            builder.averageResponseTime(Duration.ofSeconds(averageSeconds));
        }

        // Determine resolution status
        builder.resolutionStatus(determineResolutionStatus(conversation, humanMessages));

        // Calculate sentiment (placeholder - would integrate with NLP service)
        builder.sentimentScore(calculateSentiment(messages));

        return builder.build();
    }

    /**
     * Determine the resolution status of a conversation.
     *
     * @param conversation the conversation
     * @param humanMessageCount the count of human messages
     * @return the resolution status
     */
    private String determineResolutionStatus(Conversation conversation, int humanMessageCount) {
        if (conversation.getStatus() == Conversation.ConversationStatus.CLOSED) {
            return "RESOLVED";
        }
        if (conversation.getStatus() == Conversation.ConversationStatus.HANDED_OFF && humanMessageCount > 0) {
            return "IN_PROGRESS";
        }
        if (humanMessageCount == 0) {
            return "UNRESOLVED";
        }
        return "ESCALATED";
    }

    /**
     * Calculate sentiment score for conversation messages.
     * Placeholder for actual sentiment analysis.
     *
     * @param messages the conversation messages
     * @return sentiment score (-1.0 to 1.0)
     */
    private Double calculateSentiment(List<Message> messages) {
        // Placeholder implementation
        // In production, would call sentiment analysis service (AWS Comprehend, Azure Text Analytics, etc.)
        if (messages.isEmpty()) {
            return 0.0;
        }

        // Simple heuristic: count positive/negative keywords
        int positiveCount = 0;
        int negativeCount = 0;
        String[] positiveKeywords = {"good", "great", "excellent", "happy", "thanks", "thank", "appreciate"};
        String[] negativeKeywords = {"bad", "terrible", "awful", "angry", "frustrated", "problem", "issue", "error"};

        for (Message message : messages) {
            String content = message.getContent().toLowerCase();
            for (String keyword : positiveKeywords) {
                if (content.contains(keyword)) positiveCount++;
            }
            for (String keyword : negativeKeywords) {
                if (content.contains(keyword)) negativeCount++;
            }
        }

        if (positiveCount + negativeCount == 0) {
            return 0.0;
        }

        return (double) (positiveCount - negativeCount) / (positiveCount + negativeCount);
    }

    /**
     * Get analytics for multiple conversations.
     *
     * @param orgId the organization ID
     * @return list of analytics DTOs
     */
    public List<ConversationAnalyticsDto> getOrganizationAnalytics(String orgId) {
        return conversationRepository.findAllByOrgId(orgId)
            .stream()
            .map(c -> calculateAnalytics(c.getId(), orgId))
            .toList();
    }

    /**
     * Calculate aggregate metrics for an organization.
     *
     * @param orgId the organization ID
     * @return map of aggregate metrics
     */
    public ConversationAnalyticsDto.ConversationAnalyticsDtoBuilder getAggregateMetrics(String orgId) {
        List<ConversationAnalyticsDto> allAnalytics = getOrganizationAnalytics(orgId);

        if (allAnalytics.isEmpty()) {
            return ConversationAnalyticsDto.builder();
        }

        long totalMessages = allAnalytics.stream().mapToLong(a -> a.getMessageCount()).sum();
        long totalTokens = allAnalytics.stream().mapToLong(a -> a.getTotalTokensUsed()).sum();
        double avgDuration = allAnalytics.stream()
            .mapToLong(a -> a.getDuration() != null ? a.getDuration().toSeconds() : 0)
            .average()
            .orElse(0);

        return ConversationAnalyticsDto.builder()
            .messageCount((int) totalMessages)
            .totalTokensUsed(totalTokens)
            .averageTokensPerMessage(totalTokens > 0 ? (double) totalTokens / totalMessages : 0);
    }
}
