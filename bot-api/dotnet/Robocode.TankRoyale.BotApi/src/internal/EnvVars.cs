using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Util;

namespace Robocode.TankRoyale.BotApi.Internal
{
  /// <summary>
  /// Utility class for reading environment variables.
  /// </summary>
  internal static class EnvVars
  {
    /// <summary>Name of environment variable for server URL.</summary>
    internal const string ServerUrl = "SERVER_URL";

    /// <summary>Name of environment variable for server secret.</summary>
    internal const string ServerSecret = "SERVER_SECRET";

    /// <summary>Name of environment variable for bot name.</summary>
    internal const string BotName = "BOT_NAME";

    /// <summary>Name of environment variable for bot version.</summary>
    internal const string BotVersion = "BOT_VERSION";

    /// <summary>Name of environment variable for bot author(s).</summary>
    internal const string BotAuthors = "BOT_AUTHORS";

    /// <summary>Name of environment variable for bot description.</summary>
    internal const string BotDescription = "BOT_DESCRIPTION";

    /// <summary>Name of environment variable for bot homepage URL.</summary>
    internal const string BotHomepage = "BOT_HOMEPAGE";

    /// <summary>Name of environment variable for bot country code(s).</summary>
    internal const string BotCountryCodes = "BOT_COUNTRY_CODES";

    /// <summary>Name of environment variable for bot game type(s).</summary>
    internal const string BotGameTypes = "BOT_GAME_TYPES";

    /// <summary>Name of environment variable for bot platform.</summary>
    internal const string BotPlatform = "BOT_PLATFORM";

    /// <summary>Name of environment variable for bot programming language.</summary>
    internal const string BotProgrammingLang = "BOT_PROG_LANG";

    internal const string IncorrectEnvValue = "Incorrect or missing value for environment variable: ";

    /// <summary>
    /// Gets the bot info from environment variables.
    /// </summary>
    /// <returns>The bot info.</returns>
    internal static BotInfo GetBotInfo()
    {
      if (string.IsNullOrWhiteSpace(GetBotName()))
      {
        throw new BotException(IncorrectEnvValue + BotName);
      }
      if (string.IsNullOrWhiteSpace(GetBotVersion()))
      {
        throw new BotException(IncorrectEnvValue + BotVersion);
      }
      if (GetBotAuthors().IsNullOrEmptyOrContainsBlanks())
      {
        throw new BotException(IncorrectEnvValue + BotAuthors);
      }
      if (GetBotGameTypes().Count == 0)
      {
        throw new BotException(IncorrectEnvValue + BotGameTypes);
      }
      return new BotInfo(
        GetBotName(),
        GetBotVersion(),
        GetBotAuthors(),
        GetBotDescription(),
        GetBotHomepage(),
        GetBotCountryCodes(),
        GetBotGameTypes(),
        GetBotPlatform(),
        GetBotProgrammingLang()
      );
    }

    /// <summary>
    /// Gets the server URL from environment variables.
    /// </summary>
    /// <returns>The server URL.</returns>
    internal static string GetServerUrl()
    {
      return Environment.GetEnvironmentVariable(ServerUrl);
    }

    /// <summary>
    /// Gets the server secret from environment variables.
    /// </summary>
    /// <returns>The server secret.</returns>
    internal static string GetServerSecret()
    {
      return Environment.GetEnvironmentVariable(ServerSecret);
    }

    /// <summary>
    /// Gets the bot name from environment variable.
    /// </summary>
    /// <returns>The bot name.</returns>
    internal static string GetBotName()
    {
      return Environment.GetEnvironmentVariable(BotName);
    }

    /// <summary>
    /// Gets the bot version from environment variable.
    /// </summary>
    /// <returns>The bot version.</returns>
    internal static string GetBotVersion()
    {
      return Environment.GetEnvironmentVariable(BotVersion);
    }

    /// <summary>
    /// Gets the bot author(s) from environment variable.
    /// </summary>
    /// <returns>The bot author(s).</returns>
    internal static ICollection<string> GetBotAuthors()
    {
      return GetEnvVarAsList(BotAuthors);
    }

    /// <summary>
    /// Gets the bot description from environment variable.
    /// </summary>
    /// <returns>The bot description.</returns>
    internal static string GetBotDescription()
    {
      return Environment.GetEnvironmentVariable(BotDescription);
    }

    /// <summary>
    /// Gets the bot homepage URL from environment variable.
    /// </summary>
    /// <returns>The bot homepage URL.</returns>
    internal static string GetBotHomepage()
    {
      return Environment.GetEnvironmentVariable(BotHomepage);
    }

    /// <summary>
    /// Gets the bot country code(s) from environment variable.
    /// </summary>
    /// <returns>The bot country code(s).</returns>
    internal static ICollection<string> GetBotCountryCodes()
    {
      return GetEnvVarAsList(BotCountryCodes);
    }

    /// <summary>
    /// Gets the list of game type(s) supported by the bot from environment variable.
    /// </summary>
    /// <returns>The list of game type(s) supported.</returns>
    internal static ICollection<string> GetBotGameTypes()
    {
      return GetEnvVarAsList(BotGameTypes);
    }

    /// <summary>
    /// Gets the platform used for running the game from environment variable.
    /// </summary>
    /// <returns>The platform used for running the game.</returns>
    internal static string GetBotPlatform()
    {
      return Environment.GetEnvironmentVariable(BotPlatform);
    }

    /// <summary>
    /// Gets the programming language used for running the game from environment variable.
    /// </summary>
    /// <returns>The platform used for running the game.</returns>
    internal static string GetBotProgrammingLang()
    {
      return Environment.GetEnvironmentVariable(BotProgrammingLang);
    }

    private static ICollection<string> GetEnvVarAsList(string envVarName)
    {
      var value = Environment.GetEnvironmentVariable(envVarName);
      return string.IsNullOrWhiteSpace(value) ?
        new List<string>() :
        new List<string>(value.Split("\\s*,\\s*"));
    }
  }
}