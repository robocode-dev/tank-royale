using System;
using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Class for reading environment variables.
  /// </summary>
  public static class EnvVars
  {
    /** Name of environment variable for server URI */
    public const string SERVER_URI = "ROBOCODE_SERVER_URI";
    /** Name of environment variable for bot name */
    public const string BOT_NAME = "BOT_NAME";
    /** Name of environment variable for bot version */
    public const string BOT_VERSION = "BOT_VERSION";
    /** Name of environment variable for bot author */
    public const string BOT_AUTHOR = "BOT_AUTHOR";
    /** Name of environment variable for bot description */
    public const string BOT_DESCRIPTION = "BOT_DESCRIPTION";
    /** Name of environment variable for bot url */
    public const string BOT_URL = "BOT_URL";
    /** Name of environment variable for bot country code */
    public const string BOT_COUNTRY_CODE = "BOT_COUNTRY_CODE";
    /** Name of environment variable for bot game types */
    public const string BOT_GAME_TYPES = "BOT_GAME_TYPES";
    /** Name of environment variable for bot platform */
    public const string BOT_PLATFORM = "BOT_PLATFORM";
    /** Name of environment variable for bot programming language */
    public const string BOT_PROG_LANG = "BOT_PROG_LANG";

    private const string NO_ENV_VALUE = "No value for environment variable: ";

    public static BotInfo GetBotInfo()
    {
      if (string.IsNullOrWhiteSpace(GetBotName()))
      {
        throw new BotException(NO_ENV_VALUE + BOT_NAME);
      }
      if (string.IsNullOrWhiteSpace(GetBotVersion()))
      {
        throw new BotException(NO_ENV_VALUE + BOT_VERSION);
      }
      if (string.IsNullOrWhiteSpace(GetBotAuthor()))
      {
        throw new BotException(NO_ENV_VALUE + BOT_AUTHOR);
      }
      if (GetBotGameTypes().Count == 0)
      {
        throw new BotException(NO_ENV_VALUE + BOT_GAME_TYPES);
      }
      return new BotInfo
      (
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

    /// <summary>Server URI</summary>
    public static string GetServerUri()
    {
      return Environment.GetEnvironmentVariable(SERVER_URI);
    }

    /// <summary>Bot name</summary>
    public static string GetBotName()
    {
      return Environment.GetEnvironmentVariable(BOT_NAME);
    }

    /// <summary>Bot version</summary>
    public static string GetBotVersion()
    {
      return Environment.GetEnvironmentVariable(BOT_VERSION);
    }

    /// <summary>Bot author</summary>
    public static string GetBotAuthor()
    {
      return Environment.GetEnvironmentVariable(BOT_AUTHOR);
    }

    /// <summary>Bot description</summary>
    public static string GetBotDescription()
    {
      return Environment.GetEnvironmentVariable(BOT_DESCRIPTION);
    }

    /// <summary>Bot url</summary>
    public static string GetBotUrl()
    {
      return Environment.GetEnvironmentVariable(BOT_URL);
    }

    /// <summary>Bot country code</summary>
    public static string GetBotCountryCode()
    {
      return Environment.GetEnvironmentVariable(BOT_COUNTRY_CODE);
    }

    /// <summary>List of game types, which the bot supports</summary>
    public static ICollection<string> GetBotGameTypes()
    {
      var gameTypes = Environment.GetEnvironmentVariable(BOT_GAME_TYPES);
      if (string.IsNullOrWhiteSpace(gameTypes))
      {
        return new List<string>();
      }
      return new List<string>(gameTypes.Split("\\s*,\\s*"));
    }

    /// <summary>Platform used for running the bot</summary>
    public static string GetBotPlatform()
    {
      return Environment.GetEnvironmentVariable(BOT_PLATFORM);
    }

    /// <summary>Language used for programming the bot</summary>
    public static string GetBotProgrammingLang()
    {
      return Environment.GetEnvironmentVariable(BOT_PROG_LANG);
    }
  }
}