package dev.threadly.core;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Utility that holds a single in-memory RSA key-pair for all integration tests.
 * Because {@link dev.threadly.core.identity.JwtService} reads PEM files from disk via @PostConstruct,
 * tests override it by supplying an {@link TestJwtHelper} which can sign tokens with the same pair
 * that was used to configure the bean — achieved via the test application.yml pointing to
 * generated PEM files (see {@code generate-test-keys.sh}).
 *
 * <p>This class also exposes a static helper for generating expired tokens used in auth tests.
 */
public final class TestJwtConfig {

    private static final KeyPair KEY_PAIR;

    static {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KEY_PAIR = gen.generateKeyPair();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private TestJwtConfig() {}

    public static KeyPair keyPair() {
        return KEY_PAIR;
    }

    /** Build an already-expired access token for testing 401 scenarios. */
    public static String expiredToken(UUID userId, UUID orgId, String role) {
        Instant past = Instant.now().minusSeconds(3600);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("org", orgId.toString())
                .claim("role", role)
                .issuedAt(Date.from(past.minusSeconds(60)))
                .expiration(Date.from(past))
                .signWith(KEY_PAIR.getPrivate())
                .compact();
    }
}
