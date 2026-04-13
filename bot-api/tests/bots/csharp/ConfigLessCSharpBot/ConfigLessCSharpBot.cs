using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.BotApi.Tests
{
    public class ConfigLessCSharpBot : Bot
    {
        static void Main(string[] args)
        {
            new ConfigLessCSharpBot().Start();
        }

        public ConfigLessCSharpBot() : base(new BotInfo(
            "ConfigLessCSharpBot",
            "1.0.0",
            new List<string> { "Author" },
            "A bot without a .json file",
            null,
            new List<string> { "US" },
            new HashSet<string> { "classic" },
            "dotnet",
            "csharp",
            null
        ))
        {
        }

        public override void Run()
        {
            while (IsRunning)
            {
                Forward(100);
                TurnLeft(90);
            }
        }
    }
}
