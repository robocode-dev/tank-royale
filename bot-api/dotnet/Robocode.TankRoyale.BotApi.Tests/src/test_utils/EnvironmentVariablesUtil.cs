using System;
using static Robocode.TankRoyale.BotApi.Tests.Test_utils.EnvironmentVariablesConstants;

namespace Robocode.TankRoyale.BotApi.Tests.Test_utils
{
    public static class EnvironmentVariablesUtil
    {
        public static void SetAll()
        {
            SetVar(ServerUrl, "ws://localhost:7654");
            SetVar(BotName, "MyBot");
            SetVar(BotVersion, "1.0");
            SetVar(BotAuthors, "Author1, Author2");
            SetVar(BotGameTypes, "1v1, classic, melee");
            SetVar(BotDescription, "Short description");
            SetVar(BotHomepage, "https://somewhere.net/MyBot");
            SetVar(BotCountryCodes, "uk, us");
            SetVar(BotPlatform, ".Net 5");
            SetVar(BotProgrammingLang, "C# 8");
            SetVar(BotInitialPosition, "50,50, 90");
        }

        private static void SetVar(string name, string value)
        {
            Environment.SetEnvironmentVariable(name, value);
        }
    }
}