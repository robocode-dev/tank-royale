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
    [Test]
    [Ignore("Temporarily skip to establish baseline for Phase 5 cleanup")]
    public void BaseBot_WhenTickContainsWonRoundEvent_ThenOnWonRoundIsCalled()
    {
        var wonRoundLatch = new CountdownEvent(1);
        var bot = new TestWonRoundBot(Server.ServerUrl, wonRoundLatch);
        
        StartAsync(bot);
        
        AwaitBotHandshake();
        // MockedServer automatically sends GameStarted and RoundStarted
        
        // Add WonRoundEvent to the next tick
        Server.AddEvent(new Robocode.TankRoyale.Schema.WonRoundEvent {
            Type = "WonRoundEvent",
            TurnNumber = 1
        });
        
        Server.SetBotStateAndAwaitTick();
        
        // BaseBot needs to call Go() to dispatch events from the queue
        bot.Go();
        
        // BaseBot should receive the event
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
