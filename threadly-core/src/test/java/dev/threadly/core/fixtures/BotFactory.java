package dev.threadly.core.fixtures;

import dev.threadly.core.workspace.BotController.CreateBotRequest;
import dev.threadly.core.workspace.BotController.UpdateBotRequest;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Builds {@link CreateBotRequest} and {@link UpdateBotRequest} DTOs for tests.
 */
public final class BotFactory {

    private static final AtomicLong SEQ = new AtomicLong(0);

    private BotFactory() {}

    /** Returns a valid {@link CreateBotRequest} with a unique name. */
    public static CreateBotRequest createRequest() {
        CreateBotRequest req = new CreateBotRequest();
        req.setName("Test Bot " + SEQ.incrementAndGet());
        req.setDescription("A bot created by the test suite.");
        req.setLanguage("en");
        return req;
    }

    /** Returns a valid {@link CreateBotRequest} with the specified name. */
    public static CreateBotRequest createRequest(String name) {
        CreateBotRequest req = new CreateBotRequest();
        req.setName(name);
        req.setDescription("Test bot: " + name);
        req.setLanguage("en");
        return req;
    }

    /** Returns an {@link UpdateBotRequest} that renames a bot and changes its color. */
    public static UpdateBotRequest updateRequest(String newName) {
        UpdateBotRequest req = new UpdateBotRequest();
        req.setName(newName);
        req.setTheme("{\"color\":\"#10b981\",\"position\":\"bottom-right\"}");
        return req;
    }
}
