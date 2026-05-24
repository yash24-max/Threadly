package dev.threadly.core.integration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;

/**
 * Metadata for a single action supported by an integration connector.
 *
 * <p>
 * Actions describe what operations a connector can perform: send_message,
 * create_customer, list_orders, etc. Each action specifies its required and
 * optional parameters, and optionally describes output data.
 * </p>
 *
 * <p>
 * Immutable by design. Built via {@link #builder()}.
 * </p>
 */
@Value
@Builder(toBuilder = true)
public class IntegrationAction {

  /**
   * Unique identifier for this action within its connector.
   *
   * <p>
   * Examples: "send_message", "create_order", "list_channels"
   * </p>
   *
   * <p>
   * Used as a routing key in {@link IntegrationRegistry#execute}.
   * </p>
   */
  String id;

  /**
   * Human-readable name for this action.
   *
   * <p>
   * Example: "Send Message", "Create Order"
   * </p>
   */
  String name;

  /**
   * Brief description of what this action does.
   *
   * <p>
   * Example: "Send a text message to a user in a channel"
   * </p>
   */
  String description;

  /**
   * List of required parameters.
   *
   * <p>
   * The caller must provide a value for each required parameter. Null values
   * are not acceptable.
   * </p>
   *
   * <p>
   * Defaults to an empty list if not specified.
   * </p>
   */
  @Builder.Default List<IntegrationParameter> requiredParams = Collections.emptyList();

  /**
   * List of optional parameters.
   *
   * <p>
   * The caller may omit these. Implementations should gracefully handle missing
   * optional parameters, either using defaults or treating them as null.
   * </p>
   *
   * <p>
   * Defaults to an empty list if not specified.
   * </p>
   */
  @Builder.Default List<IntegrationParameter> optionalParams = Collections.emptyList();

  /**
   * Description of the output data structure returned by this action.
   *
   * <p>
   * Optional. Example: "Returns a message object with id, text, timestamp,
   * sender."
   * </p>
   */
  String outputDescription;

  /**
   * Validates that all required parameters are present in the provided map.
   *
   * @param params the parameter map to validate
   * @throws IllegalArgumentException if any required parameter is missing
   */
  public void validateParams(java.util.Map<String, Object> params) {
    for (IntegrationParameter req : requiredParams) {
      if (!params.containsKey(req.getName()) || params.get(req.getName()) == null) {
        throw new IllegalArgumentException(
            String.format(
                "Required parameter '%s' is missing for action '%s'", req.getName(), id));
      }
    }
  }

  /**
   * Immutable parameter definition (name, type, description).
   */
  @Value
  @Builder(toBuilder = true)
  public static class IntegrationParameter {
    /**
     * Parameter name, used as a key in the params map.
     *
     * <p>
     * Examples: "channel_id", "message", "customer_email"
     * </p>
     */
    String name;

    /**
     * Expected type: "string", "number", "boolean", "object", "array".
     *
     * <p>
     * Guidance for callers on what type of value to provide.
     * </p>
     */
    String type;

    /**
     * Description of this parameter's purpose and constraints.
     *
     * <p>
     * Example: "The Slack channel ID. Required format: C01234567890"
     * </p>
     */
    String description;

    /**
     * Example value to show in documentation.
     *
     * <p>
     * Optional. Example: "C01234567890"
     * </p>
     */
    Object example;
  }

  @Override
  public String toString() {
    return String.format("IntegrationAction{id='%s', name='%s'}", id, name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IntegrationAction)) return false;
    IntegrationAction that = (IntegrationAction) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
