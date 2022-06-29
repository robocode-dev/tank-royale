using System;

namespace Robocode.TankRoyale.BotApi.Tests.Test_utils;

public static class EnvironmentVariables
{
    public const string ServerUrl = "SERVER_URL";
    public const string BotName = "BOT_NAME";
    public const string BotVersion = "BOT_VERSION";
    public const string BotAuthors = "BOT_AUTHORS";
    public const string BotGameTypes = "BOT_GAME_TYPES";
    public const string BotDescription = "BOT_DESCRIPTION";
    public const string BotHomepage = "BOT_HOMEPAGE";
    public const string BotCountryCodes = "BOT_COUNTRY_CODES";
    public const string BotPlatform = "BOT_PLATFORM";
    public const string BotProgrammingLang = "BOT_PROG_LANG";
    public const string BotInitialPosition = "BOT_INITIAL_POS";

    public static void SetAllEnvVars()
    {
        SetEnvVar(ServerUrl, "ws://localhost:7654");
        SetEnvVar(BotName, "MyBot");
        SetEnvVar(BotVersion, "1.0");
        SetEnvVar(BotAuthors, "Author1, Author2");
        SetEnvVar(BotGameTypes, "1v1, classic, melee");
        SetEnvVar(BotDescription, "Short description");
        SetEnvVar(BotHomepage, "https://somewhere.net/MyBot");
        SetEnvVar(BotCountryCodes, "gb, us");
        SetEnvVar(BotPlatform, ".Net 5");
        SetEnvVar(BotProgrammingLang, "C# 8");
        SetEnvVar(BotInitialPosition, "50,50, 90");
    }

    public static void SetEnvVar(string name, string value)
    {
        Environment.SetEnvironmentVariable(name, value);
    }
    
    public static void ClearEnvVar(string name)
    {
        Environment.SetEnvironmentVariable(name, null);
    }
}