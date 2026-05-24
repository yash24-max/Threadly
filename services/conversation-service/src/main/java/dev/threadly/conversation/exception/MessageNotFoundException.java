package dev.threadly.conversation.exception;

/**
 * Exception thrown when a message is not found.
 */
public class MessageNotFoundException extends RuntimeException {
    private final String messageId;
    private final String conversationId;

    /**
     * Create a new MessageNotFoundException.
     *
     * @param messageId the message ID that was not found
     * @param conversationId the conversation ID
     */
    public MessageNotFoundException(String messageId, String conversationId) {
        super(String.format("Message with ID '%s' not found in conversation '%s'", messageId, conversationId));
        this.messageId = messageId;
        this.conversationId = conversationId;
    }

    /**
     * Create a new MessageNotFoundException with a custom message.
     *
     * @param message the error message
     * @param messageId the message ID
     * @param conversationId the conversation ID
     */
    public MessageNotFoundException(String message, String messageId, String conversationId) {
        super(message);
        this.messageId = messageId;
        this.conversationId = conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getConversationId() {
        return conversationId;
    }
}
