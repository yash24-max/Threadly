package dev.threadly.conversation.exception;

/**
 * Exception thrown when lead capture fails.
 */
public class LeadCaptureException extends RuntimeException {
    private final String conversationId;
    private final String reason;

    /**
     * Create a new LeadCaptureException.
     *
     * @param conversationId the conversation ID
     * @param reason the reason for the capture failure
     */
    public LeadCaptureException(String conversationId, String reason) {
        super(String.format("Failed to capture lead from conversation '%s': %s", conversationId, reason));
        this.conversationId = conversationId;
        this.reason = reason;
    }

    /**
     * Create a new LeadCaptureException.
     *
     * @param message the error message
     * @param conversationId the conversation ID
     * @param reason the reason for the capture failure
     * @param cause the root cause
     */
    public LeadCaptureException(String message, String conversationId, String reason, Throwable cause) {
        super(message, cause);
        this.conversationId = conversationId;
        this.reason = reason;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getReason() {
        return reason;
    }
}
