using System;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using static Robocode.TankRoyale.BotApi.Tests.Test_utils.EnvironmentVariables;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
public class BaseBotConstructorPrecedenceTest : AbstractBotTest
{
    private class TestBot : BaseBot
    {
        public TestBot() { }
        public TestBot(BotInfo botInfo, Uri serverUrl) : base(botInfo, serverUrl) { }
    }

    [SetUp]
    public new void SetUp()
    {
        SetAllEnvVarsToDefaultValues();
    }

    [Test]
    [Category("BOT")]
    [Property("ID", "TR-API-BOT-001c")]
    public void GivenEnvSet_whenPassingExplicitServerUrl_thenExplicitOverridesEnv()
    {
        // Arrange: set ENV to bogus port
        SetEnvVar(ServerUrl, "ws://127.0.0.1:65535");
        var botInfo = new BotInfo("Bot", "1.0", new System.Collections.Generic.List<string> { "A" },
            null, null, new System.Collections.Generic.List<string>(), new System.Collections.Generic.HashSet<string>(), null, null, null);
        var explicitUri = new Uri("ws://127.0.0.1:" + MockedServer.Port);

        // Act
        var bot = new TestBot(botInfo, explicitUri);
        StartAsync(bot);

        // Assert: connects using explicit URI
        Assert.That(Server.AwaitConnection(10_000), Is.True);
    }
}
