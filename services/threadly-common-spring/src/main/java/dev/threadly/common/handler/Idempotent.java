package dev.threadly.common.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method as idempotent.
 *
 * When applied, the {@link IdempotencyKeyHandler} aspect intercepts the method
 * and caches the response keyed by the {@code Idempotency-Key} request header.
 * Duplicate requests with the same key return the cached response without re-execution.
 *
 * Usage:
 * <pre>
 * {@code
 * @PostMapping("/conversations")
 * @Idempotent
 * public ResponseEntity<ConversationDto> create(@RequestBody CreateConversationRequest req) { ... }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /** TTL in seconds for caching response (default 24 hours) */
    int ttlSeconds() default 86400;
}
