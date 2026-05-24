package dev.threadly.workspace.bot.exception;

/**
 * Thrown when bot configuration is invalid or violates business rules.
 */
public class InvalidBotConfigException extends RuntimeException {

  /**
   * Create a new InvalidBotConfigException with a message.
   *
   * @param message error description
   */
  public InvalidBotConfigException(String message) {
    super(message);
  }

  /**
   * Create a new InvalidBotConfigException with a message and cause.
   *
   * @param message error description
   * @param cause underlying exception
   */
  public InvalidBotConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a new InvalidBotConfigException for invalid settings.
   *
   * @param setting the setting name
   * @param reason why it's invalid
   * @return new InvalidBotConfigException
   */
  public static InvalidBotConfigException invalidSetting(String setting, String reason) {
    return new InvalidBotConfigException(
        String.format("Invalid setting '%s': %s", setting, reason));
  }

  /**
   * Create a new InvalidBotConfigException for invalid theme color.
   *
   * @param color the color value
   * @return new InvalidBotConfigException
   */
  public static InvalidBotConfigException invalidThemeColor(String color) {
    return new InvalidBotConfigException(
        String.format("Invalid theme color '%s'. Expected hex color code (e.g., #3B82F6)", color));
  }

  /**
   * Create a new InvalidBotConfigException for invalid webhook URL.
   *
   * @param url the webhook URL
   * @return new InvalidBotConfigException
   */
  public static InvalidBotConfigException invalidWebhookUrl(String url) {
    return new InvalidBotConfigException(
        String.format("Invalid webhook URL '%s'. Must be a valid HTTPS URL", url));
  }
}
