package dev.threadly.analytics.processor;

import dev.threadly.analytics.entity.AnalyticsEvent;
import dev.threadly.analytics.entity.Metric;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Abstract base class for processing analytics events into metrics.
 * Implements the event processor pattern for flexible metric computation.
 * Subclasses handle specific event types and generate appropriate metrics.
 */
@Slf4j
public abstract class MetricProcessor {

    /**
     * Process an analytics event and generate metrics.
     *
     * @param event the analytics event to process
     * @return list of generated metrics (may be empty)
     */
    public abstract List<Metric> processEvent(AnalyticsEvent event);

    /**
     * Check if this processor can handle the given event type.
     *
     * @param eventType the event type to check
     * @return true if this processor handles this event type
     */
    public abstract boolean canHandle(String eventType);

    /**
     * Get the event type(s) this processor handles.
     *
     * @return array of event types
     */
    public abstract String[] getSupportedEventTypes();

    /**
     * Validate event data before processing.
     *
     * @param event the event to validate
     * @return true if event is valid
     */
    protected boolean isValidEvent(AnalyticsEvent event) {
        return event != null &&
               event.getOrgId() != null &&
               event.getBotId() != null &&
               event.getEventType() != null &&
               event.getEventDataJson() != null;
    }

    /**
     * Log event processing with context.
     *
     * @param event the event being processed
     * @param message log message
     */
    protected void logProcessing(AnalyticsEvent event, String message) {
        log.debug("Event Processing [{}] - {} - Event: {} from Bot: {}",
            event.getEventType(), message, event.getId(), event.getBotId());
    }

    /**
     * Log metric generation.
     *
     * @param metricName the generated metric name
     * @param value the metric value
     * @param eventType the source event type
     */
    protected void logMetricGenerated(String metricName, Double value, String eventType) {
        log.debug("Metric Generated [{}] - Name: {}, Value: {}", eventType, metricName, value);
    }

}
