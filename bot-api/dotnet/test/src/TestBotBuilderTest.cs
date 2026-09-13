using System;
using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Unit tests for TestBotBuilder.
/// </summary>
[TestFixture]
[Category("Unit")]
[Category("UTL")]
[Property("ID", "TR-API-UTL-005")]
public class TestBotBuilderTest
{
    private const int BotReadyTimeoutMs = 10_000;
    private const int CallbackTimeoutMs = 5_000;

    private MockedServer _server = null!;

    [SetUp]
    public void SetUp()
    {
        _server = new MockedServer();
        _server.Start();
    }

    [TearDown]
    public void TearDown()
    {
        _server.Stop();
    }

    [Test]
    [Description("TestBotBuilder creates bot with default passive behavior")]
    public void TestDefaultPassiveBehavior()
    {
        var bot = TestBotBuilder.Create()
            .WithBehavior(TestBotBuilder.BotBehavior.Passive)
            .Build();

        Assert.That(bot, Is.Not.Null);
    }

    [Test]
    [Description("TestBotBuilder creates bot with custom name")]
    public void TestCustomName()
    {
        var bot = TestBotBuilder.Create()
            .WithName("CustomBot")
            .WithVersion("2.0")
            .WithAuthors("Author1", "Author2")
            .Build();

        Assert.That(bot, Is.Not.Null);
    }

    [Test]
    [Description("TestBotBuilder creates bot with aggressive behavior")]
    public void TestAggressiveBehavior()
    {
        var bot = TestBotBuilder.Create()
            .WithBehavior(TestBotBuilder.BotBehavior.Aggressive)
            .Build();

        Assert.That(bot, Is.Not.Null);
    }

    [Test]
    [Description("TestBotBuilder creates bot with scanning behavior")]
    public void TestScanningBehavior()
    {
        var bot = TestBotBuilder.Create()
            .WithBehavior(TestBotBuilder.BotBehavior.Scanning)
            .Build();

        Assert.That(bot, Is.Not.Null);
    }

    [Test]
    [Description("TestBotBuilder onTick callback is invoked")]
    [Timeout(15000)]
    public void TestOnTickCallback()
    {
        using var tickCalled = new ManualResetEventSlim(false);

        var bot = TestBotBuilder.Create()
            .OnTick(_ => tickCalled.Set())
            .Build(_server.ServerUrl);

        // Start bot in separate thread
        var botThread = new Thread(() => bot.Start());
        botThread.Start();

        try
        {
            // Wait for bot to be ready and receive tick
            Assert.That(_server.AwaitBotReady(BotReadyTimeoutMs), Is.True);

            Assert.That(tickCalled.Wait(CallbackTimeoutMs), Is.True, "Tick callback was not invoked");
        }
        finally
        {
            // Cleanup
            botThread.Interrupt();
            botThread.Join(1000);
        }
    }

    [Test]
    [Description("TestBotBuilder onRun callback is invoked")]
    [Timeout(15000)]
    public void TestOnRunCallback()
    {
        using var runCalled = new ManualResetEventSlim(false);

        var bot = TestBotBuilder.Create()
            .OnRun(runCalled.Set)
            .Build(_server.ServerUrl);

        // Start bot in separate thread
        var botThread = new Thread(() => bot.Start());
        botThread.Start();

        try
        {
            // Wait for bot to be ready
            Assert.That(_server.AwaitBotReady(BotReadyTimeoutMs), Is.True);

            Assert.That(runCalled.Wait(CallbackTimeoutMs), Is.True, "Run callback was not invoked");
        }
        finally
        {
            // Cleanup
            botThread.Interrupt();
            botThread.Join(1000);
        }
    }

    [Test]
    [Description("TestBotBuilder multiple callbacks can be chained")]
    public void TestCallbackChaining()
    {
        var callbackCount = 0;

        var bot = TestBotBuilder.Create()
            .WithName("ChainedBot")
            .WithBehavior(TestBotBuilder.BotBehavior.Custom)
            .OnTick(_ => Interlocked.Increment(ref callbackCount))
            .OnScannedBot(_ => Interlocked.Increment(ref callbackCount))
            .OnHitBot(_ => Interlocked.Increment(ref callbackCount))
            .OnHitWall(_ => Interlocked.Increment(ref callbackCount))
            .OnDeath(_ => Interlocked.Increment(ref callbackCount))
            .Build();

        Assert.That(bot, Is.Not.Null);
    }

    [Test]
    [Description("TestBotBuilder custom behavior relies on callbacks only")]
    [Timeout(15000)]
    public void TestCustomBehavior()
    {
        using var customTickHandled = new ManualResetEventSlim(false);

        var bot = TestBotBuilder.Create()
            .WithBehavior(TestBotBuilder.BotBehavior.Custom)
            .OnTick(_ => customTickHandled.Set())
            .Build(_server.ServerUrl);

        // Start bot in separate thread
        var botThread = new Thread(() => bot.Start());
        botThread.Start();

        try
        {
            // Wait for bot to be ready and receive tick
            Assert.That(_server.AwaitBotReady(BotReadyTimeoutMs), Is.True);

            Assert.That(customTickHandled.Wait(CallbackTimeoutMs), Is.True, "Custom tick callback was not invoked");
        }
        finally
        {
            // Cleanup
            botThread.Interrupt();
            botThread.Join(1000);
        }
    }

    [Test]
    [Description("TestBotBuilder can build multiple bots from same builder")]
    public void TestMultipleBotsFromSameBuilder()
    {
        var builder = TestBotBuilder.Create()
            .WithName("ReusableBot")
            .WithBehavior(TestBotBuilder.BotBehavior.Passive);

        var bot1 = builder.Build();
        var bot2 = builder.Build();

        Assert.That(bot1, Is.Not.Null);
        Assert.That(bot2, Is.Not.Null);
        Assert.That(bot1, Is.Not.SameAs(bot2));
    }
}
