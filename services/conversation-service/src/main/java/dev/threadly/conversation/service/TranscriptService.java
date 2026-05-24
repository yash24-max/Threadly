package dev.threadly.conversation.service;

import dev.threadly.conversation.dto.TranscriptDto;
import dev.threadly.conversation.entity.Conversation;
import dev.threadly.conversation.entity.Message;
import dev.threadly.conversation.exception.ConversationNotFoundException;
import dev.threadly.conversation.repository.ConversationRepository;
import dev.threadly.conversation.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for transcript generation and export.
 * Provides formatting options: plaintext, HTML, and PDF.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TranscriptService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Generate a transcript for a conversation in the specified format.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param format the format: plaintext, html, or pdf
     * @return the transcript DTO
     */
    public TranscriptDto generateTranscript(String conversationId, String orgId, String format) {
        Conversation conversation = conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        List<Message> messages = messageRepository.findAllByConversationId(conversationId);

        List<TranscriptDto.TranscriptMessage> transcriptMessages = messages.stream()
            .map(m -> TranscriptDto.TranscriptMessage.builder()
                .sender(m.getSender().toString())
                .senderName(getSenderName(m))
                .timestamp(m.getCreatedAt())
                .content(m.getContent())
                .tokensUsed(m.getTokensUsed())
                .build())
            .collect(Collectors.toList());

        String content = formatTranscript(transcriptMessages, format);

        log.debug("Transcript generated for conversation: {} in {} format", conversationId, format);

        return TranscriptDto.builder()
            .conversationId(conversationId)
            .botName("Bot") // Would be fetched from bot service in production
            .visitorName("Visitor") // Would be fetched from visitor data in production
            .startTime(conversation.getStartedAt())
            .endTime(conversation.getEndedAt())
            .messageCount(messages.size())
            .totalTokensUsed(messages.stream()
                .mapToLong(m -> m.getTokensUsed() != null ? m.getTokensUsed() : 0)
                .sum())
            .format(format)
            .content(content)
            .messages(transcriptMessages)
            .build();
    }

    /**
     * Format transcript messages into the specified format.
     *
     * @param messages the transcript messages
     * @param format the format type
     * @return formatted transcript content
     */
    private String formatTranscript(List<TranscriptDto.TranscriptMessage> messages, String format) {
        return switch (format.toLowerCase()) {
            case "html" -> formatAsHtml(messages);
            case "pdf" -> formatAsPlaintext(messages); // PDF generation handled separately
            default -> formatAsPlaintext(messages);
        };
    }

    /**
     * Format messages as plaintext.
     *
     * @param messages the transcript messages
     * @return plaintext formatted content
     */
    private String formatAsPlaintext(List<TranscriptDto.TranscriptMessage> messages) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (TranscriptDto.TranscriptMessage msg : messages) {
            sb.append(String.format("[%s] %s: %s\n",
                msg.getTimestamp().atZone(java.time.ZoneId.systemDefault()).format(formatter),
                msg.getSenderName(),
                msg.getContent()
            ));
            if (msg.getTokensUsed() != null && msg.getTokensUsed() > 0) {
                sb.append(String.format("  (Tokens: %d)\n", msg.getTokensUsed()));
            }
        }

        return sb.toString();
    }

    /**
     * Format messages as HTML.
     *
     * @param messages the transcript messages
     * @return HTML formatted content
     */
    private String formatAsHtml(List<TranscriptDto.TranscriptMessage> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>");
        sb.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        sb.append(".message { margin-bottom: 15px; padding: 10px; border-left: 3px solid #ccc; }");
        sb.append(".visitor { border-left-color: #4CAF50; }");
        sb.append(".ai { border-left-color: #2196F3; }");
        sb.append(".human { border-left-color: #FF9800; }");
        sb.append(".timestamp { color: #666; font-size: 0.9em; }");
        sb.append(".sender { font-weight: bold; margin-bottom: 5px; }");
        sb.append(".tokens { color: #999; font-size: 0.8em; margin-top: 5px; }");
        sb.append("</style></head><body>");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (TranscriptDto.TranscriptMessage msg : messages) {
            String senderClass = msg.getSender().toLowerCase();
            sb.append(String.format("<div class='message %s'>", senderClass));
            sb.append(String.format("<div class='timestamp'>%s</div>",
                msg.getTimestamp().atZone(java.time.ZoneId.systemDefault()).format(formatter)
            ));
            sb.append(String.format("<div class='sender'>%s</div>", escapeHtml(msg.getSenderName())));
            sb.append(String.format("<div class='content'>%s</div>", escapeHtml(msg.getContent())));

            if (msg.getTokensUsed() != null && msg.getTokensUsed() > 0) {
                sb.append(String.format("<div class='tokens'>Tokens: %d</div>", msg.getTokensUsed()));
            }

            sb.append("</div>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * Escape HTML special characters.
     *
     * @param text the text to escape
     * @return escaped HTML text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /**
     * Get a display name for the sender.
     *
     * @param message the message
     * @return the sender's display name
     */
    private String getSenderName(Message message) {
        return switch (message.getSender()) {
            case VISITOR -> "Visitor";
            case AI -> "Bot";
            case HUMAN -> "Agent";
        };
    }

    /**
     * Export transcript to a file format.
     * This is a placeholder for actual file export logic.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param format the export format
     * @return the file path or URL
     */
    public String exportTranscript(String conversationId, String orgId, String format) {
        TranscriptDto transcript = generateTranscript(conversationId, orgId, format);

        // In production, this would write to S3, GCS, or filesystem
        log.info("Transcript exported for conversation: {} in {} format", conversationId, format);

        return String.format("/transcripts/%s.%s", conversationId, getFileExtension(format));
    }

    /**
     * Get file extension for format.
     *
     * @param format the format type
     * @return the file extension
     */
    private String getFileExtension(String format) {
        return switch (format.toLowerCase()) {
            case "html" -> "html";
            case "pdf" -> "pdf";
            default -> "txt";
        };
    }
}
