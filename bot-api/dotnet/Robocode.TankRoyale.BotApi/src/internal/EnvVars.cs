using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;
using Robocode.TankRoyale.BotApi.Util;

[assembly: InternalsVisibleTo("Robocode.TankRoyale.BotApi.Tests")]

namespace Robocode.TankRoyale.BotApi.Internal;

/// <summary>
/// Utility class for reading environment variables.
/// </summary>
internal static class EnvVars
{
    /// <summary>Name of environment variable for server URL.</summary>
    private const string ServerUrl = "SERVER_URL";

    /// <summary>Name of environment variable for server secret.</summary>
    private const string ServerSecret = "SERVER_SECRET";

    /// <summary>Name of environment variable for bot name.</summary>
    private const string BotName = "BOT_NAME";

    /// <summary>Name of environment variable for bot version.</summary>
    private const string BotVersion = "BOT_VERSION";

    /// <summary>Name of environment variable for bot author(s).</summary>
    private const string BotAuthors = "BOT_AUTHORS";

    /// <summary>Name of environment variable for bot description.</summary>
    private const string BotDescription = "BOT_DESCRIPTION";

    /// <summary>Name of environment variable for bot homepage URL.</summary>
    private const string BotHomepage = "BOT_HOMEPAGE";

    /// <summary>Name of environment variable for bot country code(s).</summary>
    private const string BotCountryCodes = "BOT_COUNTRY_CODES";

    /// <summary>Name of environment variable for bot game type(s).</summary>
    private const string BotGameTypes = "BOT_GAME_TYPES";

    /// <summary>Name of environment variable for bot platform.</summary>
    private const string BotPlatform = "BOT_PLATFORM";

    /// <summary>Name of environment variable for bot programming language.</summary>
    private const string BotProgrammingLang = "BOT_PROG_LANG";

    /// <summary>Name of environment variable for bot initial position.</summary>
    private const string BotInitialPosition = "BOT_INITIAL_POS";

    private const string MissingEnvValue = "Missing environment variable: ";

    /// <summary>
    /// Gets the bot info from environment variables.
    /// </summary>
    /// <returns>The bot info.</returns>
    internal static BotInfo GetBotInfo()
    {
        if (string.IsNullOrWhiteSpace(GetBotName()))
        {
            throw new BotException(MissingEnvValue + BotName);
        }

        if (string.IsNullOrWhiteSpace(GetBotVersion()))
        {
            throw new BotException(MissingEnvValue + BotVersion);
        }

        if (GetBotAuthors().IsNullOrEmptyOrContainsBlanks())
        {
            throw new BotException(MissingEnvValue + BotAuthors);
        }

        if (GetBotGameTypes().Count == 0)
        {
            throw new BotException(MissingEnvValue + BotGameTypes);
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
            GetBotProgrammingLang(),
            GetBotInitialPosition()
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
    private static string GetBotName()
    {
        return Environment.GetEnvironmentVariable(BotName);
    }

    /// <summary>
    /// Gets the bot version from environment variable.
    /// </summary>
    /// <returns>The bot version.</returns>
    private static string GetBotVersion()
    {
        return Environment.GetEnvironmentVariable(BotVersion);
    }

    /// <summary>
    /// Gets the bot author(s) from environment variable.
    /// </summary>
    /// <returns>The bot author(s).</returns>
    private static ICollection<string> GetBotAuthors()
    {
        return GetEnvVarAsList(BotAuthors);
    }

    /// <summary>
    /// Gets the bot description from environment variable.
    /// </summary>
    /// <returns>The bot description.</returns>
    private static string GetBotDescription()
    {
        return Environment.GetEnvironmentVariable(BotDescription);
    }

    /// <summary>
    /// Gets the bot homepage URL from environment variable.
    /// </summary>
    /// <returns>The bot homepage URL.</returns>
    private static string GetBotHomepage()
    {
        return Environment.GetEnvironmentVariable(BotHomepage);
    }

    /// <summary>
    /// Gets the bot country code(s) from environment variable.
    /// </summary>
    /// <returns>The bot country code(s).</returns>
    private static ICollection<string> GetBotCountryCodes()
    {
        return GetEnvVarAsList(BotCountryCodes);
    }

    /// <summary>
    /// Gets the list of game type(s) supported by the bot from environment variable.
    /// </summary>
    /// <returns>The list of game type(s) supported.</returns>
    private static ICollection<string> GetBotGameTypes()
    {
        return GetEnvVarAsList(BotGameTypes);
    }

    /// <summary>
    /// Gets the platform used for running the game from environment variable.
    /// </summary>
    /// <returns>The platform used for running the game.</returns>
    private static string GetBotPlatform()
    {
        return Environment.GetEnvironmentVariable(BotPlatform);
    }

    /// <summary>
    /// Gets the programming language used for running the game from environment variable.
    /// </summary>
    /// <returns>The platform used for running the game.</returns>
    private static string GetBotProgrammingLang()
    {
        return Environment.GetEnvironmentVariable(BotProgrammingLang);
    }

    /// <summary>
    /// Gets the initial starting position for the bot used for debugging from environment variable.
    /// </summary>
    /// <returns>The initial starting position for the bot used for debugging.</returns>
    private static InitialPosition GetBotInitialPosition()
    {
        return InitialPosition.FromString(Environment.GetEnvironmentVariable(BotInitialPosition));
    }

    private static ICollection<string> GetEnvVarAsList(string envVarName)
    {
        var value = Environment.GetEnvironmentVariable(envVarName);
        return string.IsNullOrWhiteSpace(value)
            ? new List<string>()
            : new List<string>(Regex.Split(value, @"\s*,\s*"));
    }
}