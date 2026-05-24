package dev.threadly.core.integration;

import java.util.List;
import java.util.Map;

/**
 * Core abstraction for third-party service connectors.
 * <p>
 * Implementations provide a standardized interface for executing actions
 * against external systems (Slack, Gmail, Stripe, etc.). Each connector
 * manages auth, request formatting, error handling, and response parsing.
 * </p>
 *
 * <p>
 * Connectors are registered as Spring {@code @Component} beans and discovered
 * by {@link IntegrationRegistry} at runtime. This enables a plugin-like
 * architecture where new integrations can be added without modifying core logic.
 * </p>
 *
 * @see IntegrationRegistry
 * @see IntegrationAction
 * @see IntegrationResult
 */
public interface IntegrationConnector {

  /**
   * Returns the type identifier for this connector.
   *
   * <p>
   * Standard types: SLACK, GMAIL, STRIPE, SHOPIFY, HUBSPOT, GITHUB, etc.
   * Used as a lookup key in the integration registry.
   * </p>
   *
   * @return the connector type, never null or empty
   */
  String getType();

  /**
   * Returns a human-readable name for this connector.
   *
   * <p>
   * Example: "Slack", "Gmail", "Stripe". Used in UIs and logs.
   * </p>
   *
   * @return the display name, never null or empty
   */
  String getDisplayName();

  /**
   * Returns the functional category this connector belongs to.
   *
   * <p>
   * Standard categories include:
   * </p>
   *
   * <ul>
   *   <li>MESSAGING (Slack, Discord)
   *   <li>CRM (HubSpot, Salesforce)
   *   <li>PRODUCTIVITY (Google Workspace, Microsoft 365)
   *   <li>ANALYTICS (Mixpanel, Amplitude)
   *   <li>ECOMMERCE (Stripe, Shopify)
   *   <li>DEVELOPMENT (GitHub, GitLab)
   * </ul>
   *
   * @return the category, never null or empty
   */
  String getCategory();

  /**
   * Returns the name of a Lucide icon to represent this connector.
   *
   * <p>
   * Examples: "slack", "mail", "stripe", "github". Used for UI rendering.
   * </p>
   *
   * @return the icon name, never null or empty
   */
  String getIconName();

  /**
   * Returns a brief description of this connector's purpose.
   *
   * <p>
   * Example: "Connect to Slack to send messages and manage channels"
   * </p>
   *
   * @return the description, never null or empty
   */
  String getDescription();

  /**
   * Returns the list of actions this connector supports.
   *
   * <p>
   * Each action defines the parameters it requires/accepts and any output data
   * it returns. The registry uses this to validate and route requests.
   * </p>
   *
   * @return list of supported actions, never null (may be empty for read-only
   *     connectors)
   */
  List<IntegrationAction> getSupportedActions();

  /**
   * Executes an action against the external service.
   *
   * <p>
   * The caller must:
   * </p>
   *
   * <ol>
   *   <li>Validate the action exists (via {@link #getSupportedActions()})
   *   <li>Resolve all parameter values (including secrets from credential store)
   *   <li>Pass the resolved {@code Map<String, Object>}
   * </ol>
   *
   * <p>
   * The connector is responsible for:
   * </p>
   *
   * <ol>
   *   <li>Formatting the request for the external API
   *   <li>Sending the request (with timeouts, retries, circuit breakers)
   *   <li>Parsing the response and normalizing errors
   *   <li>Returning a structured {@link IntegrationResult}
   * </ol>
   *
   * @param action the action ID (e.g., "send_message", "create_customer")
   * @param resolvedParams a map of parameter name → value. All required
   *     parameters must be present. May include secrets (tokens, API keys).
   * @param integration the {@link Integration} entity, for logging and
   *     credential lookup
   * @return a {@link IntegrationResult} with success flag, output data, and
   *     error details
   * @throws IllegalArgumentException if the action is not supported or required
   *     params are missing
   * @see IntegrationRegistry#execute(String, String, Map, Integration)
   */
  IntegrationResult execute(
      String action, Map<String, Object> resolvedParams, Integration integration);

  /**
   * Tests the connection to the external service using the integration's
   * credentials.
   *
   * <p>
   * This is typically a minimal health check (e.g., verify API key is valid).
   * Implementations should timeout quickly and return a boolean without throwing.
   * </p>
   *
   * @param integration the {@link Integration} entity with credentials
   * @return true if connection is healthy, false otherwise
   */
  boolean testConnection(Integration integration);
}
