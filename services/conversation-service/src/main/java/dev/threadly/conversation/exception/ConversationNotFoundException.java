package dev.threadly.conversation.exception;

/**
 * Exception thrown when a conversation is not found.
 */
public class ConversationNotFoundException extends RuntimeException {
    private final String conversationId;
    private final String orgId;

    /**
     * Create a new ConversationNotFoundException.
     *
     * @param conversationId the conversation ID that was not found
     * @param orgId the organization ID
     */
    public ConversationNotFoundException(String conversationId, String orgId) {
        super(String.format("Conversation with ID '%s' not found in organization '%s'", conversationId, orgId));
        this.conversationId = conversationId;
        this.orgId = orgId;
    }

    /**
     * Create a new ConversationNotFoundException with a custom message.
     *
     * @param message the error message
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     */
    public ConversationNotFoundException(String message, String conversationId, String orgId) {
        super(message);
        this.conversationId = conversationId;
        this.orgId = orgId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getOrgId() {
        return orgId;
    }
}
