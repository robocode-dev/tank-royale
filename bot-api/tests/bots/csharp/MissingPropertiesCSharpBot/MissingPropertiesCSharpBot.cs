using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.BotApi.Tests
{
    public class MissingPropertiesCSharpBot : Bot
    {
        static void Main(string[] args)
        {
            new MissingPropertiesCSharpBot().Start();
        }

        public MissingPropertiesCSharpBot() : base(new BotInfo(
            null, // Name is missing!
            "1.0.0",
            new List<string> { "Author" },
            "A bot missing its name",
            null,
            null,
            null,
            null,
            null,
            null
        ))
        {
        }

        public override void Run()
        {
            while (IsRunning)
            {
                Forward(100);
            }
        }
    }
}
