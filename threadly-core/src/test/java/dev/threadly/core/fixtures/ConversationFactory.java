package dev.threadly.core.fixtures;

import dev.threadly.core.conversation.Conversation;
import dev.threadly.core.conversation.ConversationRepository;
import dev.threadly.core.conversation.Message;
import dev.threadly.core.conversation.MessageRepository;
import dev.threadly.core.workspace.Bot;
import java.util.UUID;

/**
 * Builds test {@link Conversation} and {@link Message} entities and persists them directly
 * via repositories (bypassing the controller) for test setup.
 */
public final class ConversationFactory {

    private ConversationFactory() {}

    /**
     * Creates and persists a minimal open conversation owned by the given org and bot.
     */
    public static Conversation createConversation(
            ConversationRepository conversationRepo, Bot bot, UUID orgId) {
        Conversation conv = Conversation.builder()
                .bot(bot)
                .orgId(orgId)
                .visitorId("visitor-" + UUID.randomUUID().toString().substring(0, 8))
                .status("open")
                .channel("website")
                .build();
        return conversationRepo.save(conv);
    }

    /**
     * Adds {@code count} alternating user/assistant messages to a conversation.
     */
    public static void addMessages(
            MessageRepository messageRepo, Conversation conversation, int count) {
        for (int i = 0; i < count; i++) {
            String role = (i % 2 == 0) ? "user" : "assistant";
            Message msg = Message.builder()
                    .conversation(conversation)
                    .orgId(conversation.getOrgId())
                    .role(role)
                    .content("Message " + (i + 1) + " from " + role)
                    .metadata("{}")
                    .build();
            messageRepo.save(msg);
        }
    }
}
