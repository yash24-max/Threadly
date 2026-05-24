package dev.threadly.core.integration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Central registry and router for integration connectors.
 *
 * <p>
 * Discovers all {@link IntegrationConnector} beans at startup and provides a
 * unified interface for executing actions. Acts as a facade between the
 * application logic and individual connector implementations.
 * </p>
 *
 * <p>
 * Usage pattern:
 * </p>
 *
 * <pre>
 * IntegrationResult result = registry.execute(
 *   "SLACK",
 *   "send_message",
 *   Map.of("channel_id", "C123", "text", "Hello"),
 *   integration
 * );
 * if (result.isSuccess()) {
 *   String messageId = result.getStringOutput("id");
 * }
 * </pre>
 *
 * <p>
 * Thread-safe. The connector list is immutable after initialization.
 * </p>
 *
 * @see IntegrationConnector
 * @see IntegrationResult
 * @see IntegrationAction
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationRegistry {

  /** Auto-wired list of all IntegrationConnector beans from Spring context. */
  private final List<IntegrationConnector> connectors;

  /**
   * Retrieves a connector by type.
   *
   * @param type the connector type (e.g., "SLACK", "GMAIL")
   * @return the connector, or empty Optional if not found
   */
  public Optional<IntegrationConnector> getConnector(String type) {
    return connectors.stream()
        .filter(c -> c.getType().equalsIgnoreCase(type))
        .findFirst();
  }

  /**
   * Retrieves a connector or throws an exception.
   *
   * @param type the connector type
   * @return the connector
   * @throws IllegalArgumentException if connector not found
   */
  public IntegrationConnector getConnectorOrThrow(String type) {
    return getConnector(type)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("No connector found for type: %s", type)));
  }

  /**
   * Lists all available connectors in a catalog format.
   *
   * <p>
   * Each entry includes type, display name, category, icon, and supported
   * actions. Used for UI rendering and discovery.
   * </p>
   *
   * @return list of catalog entries, sorted by type
   */
  public List<IntegrationCatalogEntry> getCatalog() {
    return connectors.stream()
        .map(
            c ->
                IntegrationCatalogEntry.builder()
                    .type(c.getType())
                    .displayName(c.getDisplayName())
                    .category(c.getCategory())
                    .iconName(c.getIconName())
                    .description(c.getDescription())
                    .actions(c.getSupportedActions())
                    .build())
        .sorted((a, b) -> a.getType().compareTo(b.getType()))
        .collect(Collectors.toList());
  }

  /**
   * Gets catalog entries filtered by category.
   *
   * @param category the category to filter by (e.g., "MESSAGING", "CRM")
   * @return list of matching catalog entries
   */
  public List<IntegrationCatalogEntry> getCatalogByCategory(String category) {
    return getCatalog().stream()
        .filter(e -> e.getCategory().equalsIgnoreCase(category))
        .collect(Collectors.toList());
  }

  /**
   * Executes an action against an integration.
   *
   * <p>
   * This is the main entry point. The caller is responsible for:
   * </p>
   *
   * <ul>
   *   <li>Validating the type is registered
   *   <li>Resolving all parameter values (including from credential store)
   *   <li>Passing a non-null integration
   * </ul>
   *
   * <p>
   * The registry routes to the correct connector and handles common errors.
   * </p>
   *
   * @param type the connector type (e.g., "SLACK")
   * @param action the action ID (e.g., "send_message")
   * @param resolvedParams map of parameter name → resolved value. Must include
   *     all required params.
   * @param integration the Integration entity with credentials
   * @return a normalized IntegrationResult
   * @throws IllegalArgumentException if type not found, action not supported,
   *     or required params missing
   * @throws NullPointerException if integration is null
   */
  public IntegrationResult execute(
      String type, String action, Map<String, Object> resolvedParams, Integration integration) {

    if (integration == null) {
      throw new NullPointerException("Integration cannot be null");
    }

    if (resolvedParams == null) {
      resolvedParams = Map.of();
    }

    try {
      // Get connector
      IntegrationConnector connector = getConnectorOrThrow(type);
      log.debug(
          "Executing action '{}' on connector '{}' for integration '{}'",
          action,
          type,
          integration.getId());

      // Find and validate action
      IntegrationAction actionDef =
          connector.getSupportedActions().stream()
              .filter(a -> a.getId().equalsIgnoreCase(action))
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          String.format(
                              "Action '%s' not supported by connector '%s'", action, type)));

      // Validate required params
      actionDef.validateParams(resolvedParams);

      // Execute
      IntegrationResult result = connector.execute(action, resolvedParams, integration);

      if (result.isSuccess()) {
        log.info("Action '{}' on '{}' succeeded", action, type);
      } else {
        log.warn(
            "Action '{}' on '{}' failed with status {}: {}",
            action,
            type,
            result.getStatusCode(),
            result.getErrorMessage());
      }

      return result;

    } catch (IllegalArgumentException | NullPointerException e) {
      log.warn(
          "Validation error executing action '{}' on '{}': {}",
          action,
          type,
          e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error(
          "Unexpected error executing action '{}' on '{}': {}",
          action,
          type,
          e.getMessage(),
          e);
      return IntegrationResult.failure(
          String.format("Unexpected error: %s", e.getMessage()), 500);
    }
  }

  /**
   * Tests a connector's connection health.
   *
   * @param type the connector type
   * @param integration the integration entity
   * @return true if connection is healthy
   */
  public boolean testConnection(String type, Integration integration) {
    try {
      IntegrationConnector connector = getConnectorOrThrow(type);
      boolean result = connector.testConnection(integration);
      if (result) {
        log.info("Connection test passed for '{}' integration '{}'", type, integration.getId());
      } else {
        log.warn("Connection test failed for '{}' integration '{}'", type, integration.getId());
      }
      return result;
    } catch (Exception e) {
      log.error(
          "Connection test error for '{}' integration '{}': {}",
          type,
          integration.getId(),
          e.getMessage(),
          e);
      return false;
    }
  }

  /**
   * Gets the count of registered connectors.
   *
   * @return the number of connectors
   */
  public int getConnectorCount() {
    return connectors.size();
  }

  /**
   * Lists all registered connector types.
   *
   * @return list of type strings (e.g., ["SLACK", "GMAIL", "STRIPE"])
   */
  public List<String> getAvailableTypes() {
    return connectors.stream()
        .map(IntegrationConnector::getType)
        .sorted()
        .collect(Collectors.toList());
  }

  /** Catalog entry for UI discovery. */
  @lombok.Value
  @lombok.Builder
  public static class IntegrationCatalogEntry {
    String type;

    String displayName;

    String category;

    String iconName;

    String description;

    List<IntegrationAction> actions;
  }
}
