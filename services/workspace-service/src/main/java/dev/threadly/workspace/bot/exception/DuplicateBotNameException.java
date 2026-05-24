package dev.threadly.workspace.bot.exception;

/**
 * Thrown when attempting to create a bot with a name that already exists in the organization.
 */
public class DuplicateBotNameException extends RuntimeException {

  /**
   * Create a new DuplicateBotNameException with a message.
   *
   * @param message error description
   */
  public DuplicateBotNameException(String message) {
    super(message);
  }

  /**
   * Create a new DuplicateBotNameException with a message and cause.
   *
   * @param message error description
   * @param cause underlying exception
   */
  public DuplicateBotNameException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a new DuplicateBotNameException for a duplicate name.
   *
   * @param name the bot name
   * @param orgId the organization ID
   * @return new DuplicateBotNameException
   */
  public static DuplicateBotNameException forName(String name, String orgId) {
    return new DuplicateBotNameException(
        String.format(
            "A bot named '%s' already exists in organization '%s'", name, orgId));
  }
}
