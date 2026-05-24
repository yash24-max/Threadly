package dev.threadly.conversation.exception;

/**
 * Exception thrown when an invalid status transition is attempted on a conversation.
 */
public class InvalidConversationStatusException extends RuntimeException {
    private final String conversationId;
    private final String currentStatus;
    private final String requestedStatus;

    /**
     * Create a new InvalidConversationStatusException.
     *
     * @param conversationId the conversation ID
     * @param currentStatus the current status
     * @param requestedStatus the requested status
     */
    public InvalidConversationStatusException(String conversationId, String currentStatus, String requestedStatus) {
        super(String.format(
            "Invalid status transition for conversation '%s': cannot change from '%s' to '%s'",
            conversationId, currentStatus, requestedStatus
        ));
        this.conversationId = conversationId;
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    /**
     * Create a new InvalidConversationStatusException with a custom message.
     *
     * @param message the error message
     * @param conversationId the conversation ID
     * @param currentStatus the current status
     * @param requestedStatus the requested status
     */
    public InvalidConversationStatusException(String message, String conversationId, String currentStatus, String requestedStatus) {
        super(message);
        this.conversationId = conversationId;
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getRequestedStatus() {
        return requestedStatus;
    }
}
