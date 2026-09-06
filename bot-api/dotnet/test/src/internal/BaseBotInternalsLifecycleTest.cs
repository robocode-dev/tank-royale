using System.Collections.Generic;
using System.Reflection;
using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Internal;

namespace Robocode.TankRoyale.BotApi.Tests.Internal;

/// <summary>
/// Round-ownership lifecycle tests, the .NET twin of the Java
/// BaseBotInternalsLifecycleTest and the Python test_round_lifecycle.py.
/// </summary>
[TestFixture]
[Category("BOT")]
public class BaseBotInternalsLifecycleTest
{
    private class TestBot : BaseBot
    {
        public int DispatchedTicks { get; private set; }

        public TestBot() : base(new BotInfo("LifecycleTest", "1.0", new List<string> { "Test" },
            null, null, null, new HashSet<string> { "classic" }, null, null, null))
        {
        }

        public override void OnTick(TickEvent e) => DispatchedTicks++;
    }

    private static void SetTickEvent(BaseBotInternals internals, TickEvent tick)
    {
        var field = typeof(BaseBotInternals).GetField("_tickEvent", BindingFlags.NonPublic | BindingFlags.Instance);
        field!.SetValue(internals, tick);
    }

    private static void SetThread(BaseBotInternals internals, Thread thread)
    {
        var field = typeof(BaseBotInternals).GetField("_thread", BindingFlags.NonPublic | BindingFlags.Instance);
        field!.SetValue(internals, thread);
    }

    private static void SetRunning(BaseBotInternals internals, bool running)
    {
        var property = typeof(BaseBotInternals).GetProperty("IsRunning",
            BindingFlags.Public | BindingFlags.Instance);
        property!.SetValue(internals, running);
    }

    [Test]
    public void StaleThreadCannotDispatchAfterNextRoundTakesOwnership()
    {
        var internals = new TestBot().BaseBotInternals;
        SetRunning(internals, true);

        // The round is owned by some other thread — this one is stale.
        SetThread(internals, new Thread(() => { }));

        Assert.Throws<ThreadInterruptedException>(() => internals.DispatchEvents(1));
    }

    [Test]
    public void FinalTickEventsAreFlushedAfterTheBotThreadLosesOwnership()
    {
        var bot = new TestBot();
        var internals = bot.BaseBotInternals;

        var tick = new TickEvent(1, 1, null, new List<BulletState>(), new List<BotEvent>());
        SetTickEvent(internals, tick);
        internals.AddEventsFromTick(tick);

        // StopThread() invalidates bot-thread ownership, so the bot thread can no longer drain
        // the queue itself. The WebSocket thread must still be able to — this is what the
        // game-ended, game-aborted and disconnected paths rely on.
        internals.StopThread();
        internals.FlushFinalTurnEvents();

        Assert.That(bot.DispatchedTicks, Is.EqualTo(1));
    }

    [Test]
    public void StopThreadDoesNotThrowBeforeTheFirstTickOfARound()
    {
        var internals = new TestBot().BaseBotInternals;
        SetRunning(internals, true);

        // No tick has arrived yet. EnableEventHandling(false) must not throw here, otherwise
        // StopThread() aborts and leaves a stale thread owning the round.
        Assert.DoesNotThrow(() => internals.StopThread());
        Assert.That(internals.IsRunning, Is.False);
    }
}
