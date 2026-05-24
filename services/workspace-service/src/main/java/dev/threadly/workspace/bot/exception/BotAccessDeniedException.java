package dev.threadly.workspace.bot.exception;

/**
 * Thrown when a user lacks sufficient permissions to perform an operation on a bot.
 */
public class BotAccessDeniedException extends RuntimeException {

  /**
   * Create a new BotAccessDeniedException with a message.
   *
   * @param message error description
   */
  public BotAccessDeniedException(String message) {
    super(message);
  }

  /**
   * Create a new BotAccessDeniedException with a message and cause.
   *
   * @param message error description
   * @param cause underlying exception
   */
  public BotAccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a new BotAccessDeniedException for missing role.
   *
   * @param userId the user ID
   * @param botId the bot ID
   * @param requiredRole the minimum role required
   * @return new BotAccessDeniedException
   */
  public static BotAccessDeniedException insufficientRole(
      String userId, String botId, String requiredRole) {
    return new BotAccessDeniedException(
        String.format(
            "User '%s' lacks required role '%s' for bot '%s'",
            userId, requiredRole, botId));
  }

  /**
   * Create a new BotAccessDeniedException for user not a member.
   *
   * @param userId the user ID
   * @param botId the bot ID
   * @return new BotAccessDeniedException
   */
  public static BotAccessDeniedException notAMember(String userId, String botId) {
    return new BotAccessDeniedException(
        String.format("User '%s' is not a member of bot '%s'", userId, botId));
  }

  /**
   * Create a new BotAccessDeniedException for operation not allowed.
   *
   * @param operation the operation attempted
   * @param reason the reason it's not allowed
   * @return new BotAccessDeniedException
   */
  public static BotAccessDeniedException operationNotAllowed(
      String operation, String reason) {
    return new BotAccessDeniedException(
        String.format("Operation '%s' not allowed: %s", operation, reason));
  }
}
