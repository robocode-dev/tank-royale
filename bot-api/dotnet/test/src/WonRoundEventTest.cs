using System;
using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
[Category("TCK")]
[Property("ID", "TR-API-TCK-005")]
public class WonRoundEventTest : AbstractBotTest
{
    /// <summary>
    /// BaseBot has no internal thread and no Go() loop of its own in this scenario, so the only
    /// path that can deliver a WonRoundEvent carried by the round's final tick is the
    /// RoundEnded-triggered dispatch in BaseBotInternals.HandleRoundEnded (regression coverage
    /// for the "final-tick events dropped" bug fix). Mirrors the Java counterpart
    /// (WonRoundEventTest.baseBot_whenTickContainsWonRoundEvent_thenOnWonRoundIsCalled), which
    /// likewise never calls go() and relies solely on the server sending RoundEndedEventForBot
    /// after the winning tick.
    /// </summary>
    [Test]
    public void BaseBot_WhenTickContainsWonRoundEvent_ThenOnWonRoundIsCalled()
    {
        var wonRoundLatch = new CountdownEvent(1);
        var bot = new TestWonRoundBot(Server.ServerUrl, wonRoundLatch);

        StartAsync(bot);

        AwaitBotHandshake();
        // MockedServer automatically sends GameStarted, RoundStarted, and the first tick
        // once the bot replies with BotReady.
        Assert.That(Server.AwaitTick(2000), Is.True);

        // Add WonRoundEvent to the next ("winning") tick.
        Server.AddEvent(new Robocode.TankRoyale.Schema.WonRoundEvent {
            Type = "WonRoundEvent",
            TurnNumber = 2
        });
        Assert.That(Server.SetBotStateAndAwaitTick(), Is.True);

        // No bot.Go() here — deliberately. Delivery must come from RoundEnded dispatch.
        Server.SendRoundEndedForBot(1, 2);

        bool received = wonRoundLatch.Wait(TimeSpan.FromSeconds(5));
        Assert.That(received, Is.True, "onWonRound() should be called within 5 seconds");
    }

    private class TestWonRoundBot : BaseBot
    {
        private readonly CountdownEvent _latch;
        public TestWonRoundBot(Uri serverUrl, CountdownEvent latch) : base(BotInfo, serverUrl)
        {
            _latch = latch;
        }

        public override void OnWonRound(WonRoundEvent ev)
        {
            _latch.Signal();
        }

        public override void OnTick(TickEvent ev)
        {
            // We can also call Go() here if we want it to be automatic
        }
    }
}
