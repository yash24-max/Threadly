package dev.threadly.core.fixtures;

import dev.threadly.core.identity.AuthController.SignupRequest;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Builds test {@link SignupRequest} objects with random, unique e-mail addresses.
 */
public final class UserFactory {

    private static final AtomicLong SEQ = new AtomicLong(System.nanoTime() % 1_000_000);

    private UserFactory() {}

    /** Default password used by all factory-generated users unless overridden. */
    public static final String DEFAULT_PASSWORD = "TestPassword1!";

    /**
     * Returns a {@link SignupRequest} with a unique e-mail and the given org name.
     */
    public static SignupRequest signupRequest(String orgName) {
        SignupRequest req = new SignupRequest();
        req.setOrgName(orgName);
        req.setName("Test User " + SEQ.incrementAndGet());
        req.setEmail(uniqueEmail());
        req.setPassword(DEFAULT_PASSWORD);
        return req;
    }

    /**
     * Returns a {@link SignupRequest} with the specified e-mail.
     */
    public static SignupRequest signupRequest(String orgName, String email) {
        SignupRequest req = new SignupRequest();
        req.setOrgName(orgName);
        req.setName("Test User");
        req.setEmail(email);
        req.setPassword(DEFAULT_PASSWORD);
        return req;
    }

    public static String uniqueEmail() {
        return "user." + UUID.randomUUID().toString().substring(0, 8) + "@test.threadly.dev";
    }
}
