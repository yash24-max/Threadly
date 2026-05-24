package dev.threadly.conversation.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object for transcript data.
 * Contains formatted conversation data for export/display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptDto {
    private String conversationId;
    private String botName;
    private String visitorName;
    private Instant startTime;
    private Instant endTime;
    private Integer messageCount;
    private Long totalTokensUsed;
    private String format; // plaintext, html, pdf
    private String content; // formatted transcript content
    private List<TranscriptMessage> messages;

    /**
     * Nested DTO for individual messages in transcript.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TranscriptMessage {
        private String sender;
        private String senderName;
        private Instant timestamp;
        private String content;
        private Long tokensUsed;
    }
}
