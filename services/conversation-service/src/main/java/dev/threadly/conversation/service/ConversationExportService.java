package dev.threadly.conversation.service;

import dev.threadly.conversation.dto.ConversationDto;
import dev.threadly.conversation.entity.Conversation;
import dev.threadly.conversation.entity.Lead;
import dev.threadly.conversation.entity.Message;
import dev.threadly.conversation.repository.ConversationRepository;
import dev.threadly.conversation.repository.LeadRepository;
import dev.threadly.conversation.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service class for exporting conversations.
 * Supports CSV, JSON, and other formats for bulk export.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ConversationExportService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final LeadRepository leadRepository;

    /**
     * Export conversations as CSV.
     *
     * @param orgId the organization ID
     * @return CSV formatted string
     */
    public String exportConversationsAsCSV(String orgId) throws IOException {
        List<Conversation> conversations = conversationRepository.findAllByOrgId(orgId);

        StringWriter sw = new StringWriter();
        sw.write("ID,Bot ID,Visitor ID,Status,Message Count,Tokens Used,Started At,Ended At\n");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        for (Conversation conv : conversations) {
            sw.write(String.format("%s,%s,%s,%s,%d,%d,%s,%s\n",
                escapeCSV(conv.getId()),
                escapeCSV(conv.getBotId()),
                escapeCSV(conv.getVisitorId()),
                conv.getStatus(),
                conv.getMessageCount(),
                conv.getTokensUsed(),
                conv.getStartedAt().atZone(ZoneId.systemDefault()).format(formatter),
                conv.getEndedAt() != null ? conv.getEndedAt().atZone(ZoneId.systemDefault()).format(formatter) : ""
            ));
        }

        log.info("Exported {} conversations as CSV for org: {}", conversations.size(), orgId);
        return sw.toString();
    }

    /**
     * Export leads as CSV.
     *
     * @param orgId the organization ID
     * @return CSV formatted string
     */
    public String exportLeadsAsCSV(String orgId) throws IOException {
        List<Lead> leads = leadRepository.findAllByOrgId(orgId);

        StringWriter sw = new StringWriter();
        sw.write("ID,Conversation ID,Email,Phone,Name,Company,Status,Quality Score,Captured At\n");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        for (Lead lead : leads) {
            sw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%d,%s\n",
                escapeCSV(lead.getId()),
                escapeCSV(lead.getConversationId()),
                escapeCSV(lead.getEmail()),
                escapeCSV(lead.getPhone()),
                escapeCSV(lead.getName()),
                escapeCSV(lead.getCompany()),
                lead.getStatus(),
                lead.getQualityScore() != null ? lead.getQualityScore() : 0,
                lead.getCapturedAt().atZone(ZoneId.systemDefault()).format(formatter)
            ));
        }

        log.info("Exported {} leads as CSV for org: {}", leads.size(), orgId);
        return sw.toString();
    }

    /**
     * Export conversation messages as CSV.
     *
     * @param conversationId the conversation ID
     * @return CSV formatted string
     */
    public String exportMessagesAsCSV(String conversationId) throws IOException {
        List<Message> messages = messageRepository.findAllByConversationId(conversationId);

        StringWriter sw = new StringWriter();
        sw.write("ID,Sender,Sender ID,Content,Tokens Used,Created At\n");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        for (Message msg : messages) {
            sw.write(String.format("%s,%s,%s,%s,%d,%s\n",
                escapeCSV(msg.getId()),
                msg.getSender(),
                escapeCSV(msg.getSenderId()),
                escapeCSV(truncateContent(msg.getContent(), 100)),
                msg.getTokensUsed() != null ? msg.getTokensUsed() : 0,
                msg.getCreatedAt().atZone(ZoneId.systemDefault()).format(formatter)
            ));
        }

        log.info("Exported {} messages as CSV for conversation: {}", messages.size(), conversationId);
        return sw.toString();
    }

    /**
     * Export conversations as JSON.
     *
     * @param orgId the organization ID
     * @return JSON formatted string
     */
    public String exportConversationsAsJSON(String orgId) {
        List<Conversation> conversations = conversationRepository.findAllByOrgId(orgId);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"organization_id\": \"").append(orgId).append("\",\n");
        sb.append("  \"total_conversations\": ").append(conversations.size()).append(",\n");
        sb.append("  \"conversations\": [\n");

        for (int i = 0; i < conversations.size(); i++) {
            Conversation conv = conversations.get(i);
            sb.append("    {\n");
            sb.append("      \"id\": \"").append(conv.getId()).append("\",\n");
            sb.append("      \"bot_id\": \"").append(conv.getBotId()).append("\",\n");
            sb.append("      \"visitor_id\": \"").append(conv.getVisitorId()).append("\",\n");
            sb.append("      \"status\": \"").append(conv.getStatus()).append("\",\n");
            sb.append("      \"message_count\": ").append(conv.getMessageCount()).append(",\n");
            sb.append("      \"tokens_used\": ").append(conv.getTokensUsed()).append(",\n");
            sb.append("      \"started_at\": \"").append(conv.getStartedAt()).append("\",\n");
            sb.append("      \"ended_at\": \"").append(conv.getEndedAt()).append("\"\n");
            sb.append("    }");
            if (i < conversations.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");

        log.info("Exported {} conversations as JSON for org: {}", conversations.size(), orgId);
        return sb.toString();
    }

    /**
     * Escape CSV special characters.
     *
     * @param value the value to escape
     * @return escaped CSV value
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Truncate content for CSV export.
     *
     * @param content the content
     * @param maxLength the maximum length
     * @return truncated content
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() > maxLength) {
            return content.substring(0, maxLength) + "...";
        }
        return content;
    }

    /**
     * Generate export file name.
     *
     * @param orgId the organization ID
     * @param format the export format
     * @return file name
     */
    public String generateFileName(String orgId, String format) {
        return String.format("conversations_%s_%d.%s",
            orgId,
            System.currentTimeMillis() / 1000,
            getFileExtension(format)
        );
    }

    /**
     * Get file extension for format.
     *
     * @param format the format type
     * @return file extension
     */
    private String getFileExtension(String format) {
        return switch (format.toLowerCase()) {
            case "json" -> "json";
            case "csv" -> "csv";
            default -> "txt";
        };
    }
}
