package dev.threadly.workspace.bot.exception;

/**
 * Thrown when a bot is not found in the database.
 */
public class BotNotFoundException extends RuntimeException {

  /**
   * Create a new BotNotFoundException with a message.
   *
   * @param message error description
   */
  public BotNotFoundException(String message) {
    super(message);
  }

  /**
   * Create a new BotNotFoundException with a message and cause.
   *
   * @param message error description
   * @param cause underlying exception
   */
  public BotNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a new BotNotFoundException for a specific bot ID.
   *
   * @param botId the bot ID that was not found
   * @return new BotNotFoundException
   */
  public static BotNotFoundException forBotId(String botId) {
    return new BotNotFoundException(String.format("Bot with ID '%s' not found", botId));
  }

  /**
   * Create a new BotNotFoundException for a bot in an organization.
   *
   * @param botId the bot ID
   * @param orgId the organization ID
   * @return new BotNotFoundException
   */
  public static BotNotFoundException forBotInOrg(String botId, String orgId) {
    return new BotNotFoundException(
        String.format("Bot with ID '%s' not found in organization '%s'", botId, orgId));
  }
}
