package dev.threadly.identity.exception;

/**
 * Exception thrown when a requested resource (user, organization, team, etc.) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String resourceType;
  private final String resourceId;

  /**
   * Constructs exception with resource type and ID.
   *
   * @param resourceType the type of resource (e.g., "User", "Organization")
   * @param resourceId the ID of the resource that wasn't found
   */
  public ResourceNotFoundException(String resourceType, String resourceId) {
    super(resourceType + " not found: " + resourceId);
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  /**
   * Constructs exception with custom message.
   *
   * @param message the error message
   */
  public ResourceNotFoundException(String message) {
    super(message);
    this.resourceType = null;
    this.resourceId = null;
  }

  /**
   * Get the resource type.
   *
   * @return the resource type
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Get the resource ID.
   *
   * @return the resource ID
   */
  public String getResourceId() {
    return resourceId;
  }
}
