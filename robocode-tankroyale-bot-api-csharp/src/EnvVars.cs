using System;
using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Utility class for reading environment variables.
  /// </summary>
  public static class EnvVars
  {
    /// <summary>Name of environment variable for server URL.</summary>
    public const string ServerUrl = "ROBOCODE_SERVER_URL";

    /// <summary>Name of environment variable for bot name.</summary>
    public const string BotName = "BOT_NAME";

    /// <summary>Name of environment variable for bot version.</summary>
    public const string BotVersion = "BOT_VERSION";

    /// <summary>Name of environment variable for bot author.</summary>
    public const string BotAuthor = "BOT_AUTHOR";

    /// <summary>Name of environment variable for bot description.</summary>
    public const string BotDescription = "BOT_DESCRIPTION";

    /// <summary>Name of environment variable for bot url.</summary>
    public const string BotUrl = "BOT_URL";

    /// <summary>Name of environment variable for bot country code.</summary>
    public const string BotCountryCode = "BOT_COUNTRY_CODE";

    /// <summary>Name of environment variable for bot game types.</summary>
    public const string BotGameTypes = "BOT_GAME_TYPES";

    /// <summary>Name of environment variable for bot platform.</summary>
    public const string BotPlatform = "BOT_PLATFORM";

    /// <summary>Name of environment variable for bot programming language.</summary>
    public const string BotProgLang = "BOT_PROG_LANG";

    private const string NoEnvValue = "No value for environment variable: ";

    /// <summary>
    /// Gets the bot info from environment variables.
    /// </summary>
    /// <returns>The bot info.</returns>
    public static BotInfo GetBotInfo()
    {
      if (string.IsNullOrWhiteSpace(GetBotName()))
      {
        throw new BotException(NoEnvValue + BotName);
      }
      if (string.IsNullOrWhiteSpace(GetBotVersion()))
      {
        throw new BotException(NoEnvValue + BotVersion);
      }
      if (string.IsNullOrWhiteSpace(GetBotAuthor()))
      {
        throw new BotException(NoEnvValue + BotAuthor);
      }
      if (GetBotGameTypes().Count == 0)
      {
        throw new BotException(NoEnvValue + BotGameTypes);
      }
      return new BotInfo(
        GetBotName(),
        GetBotVersion(),
        GetBotAuthor(),
        GetBotDescription(),
        GetBotUrl(),
        GetBotCountryCode(),
        GetBotGameTypes(),
        GetBotPlatform(),
        GetBotProgrammingLang()
      );
    }

    /// <summary>
    /// Gets the server URL from environment variables.
    /// </summary>
    /// <returns>The server URL.</returns>
    public static string GetServerUrl()
    {
      return Environment.GetEnvironmentVariable(ServerUrl);
    }

    /// <summary>
    /// Gets the bot name from environment variable.
    /// </summary>
    /// <returns>The bot name.</returns>
    public static string GetBotName()
    {
      return Environment.GetEnvironmentVariable(BotName);
    }

    /// <summary>
    /// Gets the bot version from environment variable.
    /// </summary>
    /// <returns>The bot version.</returns>
    public static string GetBotVersion()
    {
      return Environment.GetEnvironmentVariable(BotVersion);
    }

    /// <summary>
    /// Gets the bot author from environment variable.
    /// </summary>
    /// <returns>The bot author.</returns>
    public static string GetBotAuthor()
    {
      return Environment.GetEnvironmentVariable(BotAuthor);
    }

    /// <summary>
    /// Gets the bot description from environment variable.
    /// </summary>
    /// <returns>The bot description.</returns>
    public static string GetBotDescription()
    {
      return Environment.GetEnvironmentVariable(BotDescription);
    }

    /// <summary>
    /// Gets the bot URL from environment variable.
    /// </summary>
    /// <returns>The bot URL.</returns>
    public static string GetBotUrl()
    {
      return Environment.GetEnvironmentVariable(BotUrl);
    }

    /// <summary>
    /// Gets the bot country code from environment variable.
    /// </summary>
    /// <returns>The bot country code.</returns>
    public static string GetBotCountryCode()
    {
      return Environment.GetEnvironmentVariable(BotCountryCode);
    }

    /// <summary>
    /// Gets the list of game types supported by the bot from environment variable.
    /// </summary>
    /// <returns>The list of game types supported.</returns>
    public static ICollection<string> GetBotGameTypes()
    {
      var gameTypes = Environment.GetEnvironmentVariable(BotGameTypes);
      if (string.IsNullOrWhiteSpace(gameTypes))
      {
        return new List<string>();
      }
      return new List<string>(gameTypes.Split("\\s*,\\s*"));
    }

    /// <summary>
    /// Gets the platform used for running the game from environment variable.
    /// </summary>
    /// <returns>The platform used for running the game.</returns>
    public static string GetBotPlatform()
    {
      return Environment.GetEnvironmentVariable(BotPlatform);
    }

    /// <summary>
    /// Gets the programming language used for running the game from environment variable.
    /// </summary>
    /// <returns>The platform used for running the game.</returns>
    public static string GetBotProgrammingLang()
    {
      return Environment.GetEnvironmentVariable(BotProgLang);
    }
  }
}