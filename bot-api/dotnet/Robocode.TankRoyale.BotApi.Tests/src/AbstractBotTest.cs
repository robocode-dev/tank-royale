using System;
using System.Threading;
using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

public class AbstractBotTest
{
    protected MockedServer Server;

    protected static readonly BotInfo BotInfo = BotInfo.Builder()
        .SetName("TestBot")
        .SetVersion("1.0")
        .AddAuthor("Author 1")
        .AddAuthor("Author 2")
        .SetDescription("Short description")
        .SetHomepage("https://testbot.robocode.dev")
        .AddCountryCode("gb")
        .AddCountryCode("us")
        .AddGameType("classic")
        .AddGameType("1v1")
        .AddGameType("melee")
        .SetPlatform(".Net 6")
        .SetProgrammingLang("C# 10")
        .SetInitialPosition(InitialPosition.FromString("10, 20, 30"))
        .Build();
    
    private class TestBot : BaseBot {
        public TestBot() : base(BotInfo, MockedServer.ServerUrl)
        {
        }
    }
    
    [SetUp]
    public void SetUp()
    {
        Server = new MockedServer();
        Server.Start();
    }

    [TearDown]
    public void Teardown()
    {
        Server.Stop();
    }
    
    protected static BaseBot Start()
    {
        var bot = new TestBot();
        StartAsync(bot);
        return bot;
    }

    protected static Task StartAsync(BaseBot bot) => Task.Run(bot.Start);

    protected static void GoAsync(BaseBot bot)
    {
        Task.Run(bot.Go);
    }

    protected BaseBot StartAndAwaitHandshake()
    {
        var bot = Start();
        AwaitBotHandshake();
        return bot;
    }

    protected BaseBot StartAndAwaitTick()
    {
        var bot = Start();
        AwaitTick(bot);
        return bot;
    }

    protected BaseBot StartAndAwaitGameStarted()
    {
        var bot = Start();
        AwaitGameStarted(bot);
        return bot;
    }

    protected void AwaitBotHandshake()
    {
        Assert.That(Server.AwaitBotHandshake(1000), Is.True);
    }

    private void AwaitGameStarted(BaseBot bot)
    {
        Assert.That(Server.AwaitGameStarted(1000), Is.True);

        var startMillis = DateTimeOffset.Now.ToUnixTimeMilliseconds();
        var noException = false;
        do {
            try
            {
                var gameType = bot.GameType;
                noException = true;
            } catch (BotException) {
                Thread.Yield();
            }
        } while (!noException && DateTimeOffset.Now.ToUnixTimeMilliseconds() - startMillis < 1000);
    }

    protected void AwaitTick(BaseBot bot)
    {
        Assert.That(Server.AwaitTick(1000), Is.True);

        var startMillis = DateTimeOffset.Now.ToUnixTimeMilliseconds();
        var noException = false;
        do {
            try
            {
                var energy = bot.Energy;
                noException = true;
            } catch (BotException) {
                Thread.Yield();
            }
        } while (!noException && DateTimeOffset.Now.ToUnixTimeMilliseconds() - startMillis < 1000);
    }

    protected void AwaitBotIntent()
    {
        Assert.That(Server.AwaitBotIntent(1000), Is.True);
    }
    
    protected static bool ExceptionContainsEnvVarName(BotException botException, string envVarName) =>
        botException != null && botException.Message.ToUpper().Contains(envVarName);
}