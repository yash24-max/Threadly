package dev.threadly.runtime.centrifugo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * HTTP client for Centrifugo real-time server.
 * Handles publishing events to channels and generating connection/subscription tokens.
 */
@Slf4j
@Component
public class CentrifugoClient {

    @Value("${centrifugo.url:http://centrifugo:8000}")
    private String centrifugoUrl;

    @Value("${centrifugo.api-key:dev_api_key}")
    private String apiKey;

    @Value("${centrifugo.token-secret:dev_secret_change_in_prod}")
    private String tokenSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CentrifugoClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish a message to a Centrifugo channel.
     */
    public void publish(String channel, Object data) {
        try {
            Map<String, Object> body = Map.of(
                "method", "publish",
                "params", Map.of("channel", channel, "data", data)
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "apikey " + apiKey);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            restTemplate.postForObject(centrifugoUrl + "/api", entity, String.class);
            log.debug("Published to channel: {}", channel);
        } catch (Exception e) {
            log.warn("Failed to publish to Centrifugo channel {}: {}", channel, e.getMessage());
        }
    }

    /**
     * Generate a JWT connection token for an authenticated dashboard user.
     *
     * @param userId  the user's UUID
     * @param orgId   the org's UUID
     * @param ttlSecs token lifetime in seconds
     */
    public String generateConnectionToken(String userId, String orgId, long ttlSecs) {
        SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId)
                .claim("org", orgId)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(ttlSecs)))
                .signWith(key)
                .compact();
    }

    /**
     * Generate a JWT connection token for an anonymous widget visitor.
     *
     * @param visitorId anonymous visitor ID
     * @param botId     the bot this visitor belongs to
     * @param ttlSecs   token lifetime in seconds
     */
    public String generateVisitorToken(String visitorId, String botId, long ttlSecs) {
        SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("visitor:" + visitorId)
                .claim("bot", botId)
                .claim("anon", true)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(ttlSecs)))
                .signWith(key)
                .compact();
    }

    /**
     * Publish a session message event to the relevant org channel.
     */
    public void publishSessionMessage(String orgId, String sessionId, String botId, String message) {
        String channel = "org:" + orgId + "#session:" + sessionId;
        publish(channel, Map.of(
            "type", "MESSAGE",
            "sessionId", sessionId,
            "botId", botId,
            "message", message,
            "ts", Instant.now().toEpochMilli()
        ));
    }

    /**
     * Publish a session state change to the org dashboard channel.
     */
    public void publishSessionStateChange(String orgId, String sessionId, String state) {
        String channel = "org:" + orgId;
        publish(channel, Map.of(
            "type", "SESSION_STATE",
            "sessionId", sessionId,
            "state", state,
            "ts", Instant.now().toEpochMilli()
        ));
    }
}
