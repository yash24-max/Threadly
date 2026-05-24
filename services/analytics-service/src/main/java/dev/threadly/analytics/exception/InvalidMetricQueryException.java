package dev.threadly.analytics.exception;

/**
 * Exception thrown when a metric query is invalid.
 */
public class InvalidMetricQueryException extends RuntimeException {

    public InvalidMetricQueryException(String message) {
        super(message);
    }

    public InvalidMetricQueryException(String message, Throwable cause) {
        super(message, cause);
    }

}
