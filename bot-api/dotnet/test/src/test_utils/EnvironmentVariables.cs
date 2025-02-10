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

    public static void SetAllEnvVarsToDefaultValues()
    {
        SetEnvVar(ServerUrl, "ws://localhost:" + MockedServer.Port);
        SetEnvVar(BotName, "TestBot");
        SetEnvVar(BotVersion, "1.0");
        SetEnvVar(BotAuthors, "Author 1, Author 2");
        SetEnvVar(BotGameTypes, "classic, 1v1, melee");
        SetEnvVar(BotDescription, "Short description");
        SetEnvVar(BotHomepage, "https://testbot.robocode.dev");
        SetEnvVar(BotCountryCodes, "gb, us");
        SetEnvVar(BotPlatform, ".Net 6");
        SetEnvVar(BotProgrammingLang, "C# 10");
        SetEnvVar(BotInitialPosition, "50,50, 90");
    }

    public static void ClearAllEnvVars()
    {
        ClearEnvVar(ServerUrl);
        ClearEnvVar(BotName);
        ClearEnvVar(BotVersion);
        ClearEnvVar(BotAuthors);
        ClearEnvVar(BotGameTypes);
        ClearEnvVar(BotDescription);
        ClearEnvVar(BotHomepage);
        ClearEnvVar(BotCountryCodes);
        ClearEnvVar(BotPlatform);
        ClearEnvVar(BotProgrammingLang);
        ClearEnvVar(BotInitialPosition);
    }
    
    public static void SetEnvVar(string name, string value)
    {
        Environment.SetEnvironmentVariable(name, value);
    }
    
    public static void ClearEnvVar(string name)
    {
        Environment.SetEnvironmentVariable(name, null);
    }

    public static object GetEnvVar(string name)
    {
        return Environment.GetEnvironmentVariable(name);
    }
}