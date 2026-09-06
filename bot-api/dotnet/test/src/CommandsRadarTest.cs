using System;
using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Tests for radar commands (TR-API-CMD-003).
/// </summary>
[TestFixture]
[Category("CMD")]
[Property("ID", "TR-API-CMD-003")]
[Description("Radar Commands (TR-API-CMD-003)")]
public class CommandsRadarTest : AbstractBotTest
{
    private class RadarTestBot : Bot
    {
        private int _blockingRescanRequested;

        public RadarTestBot(Uri serverUrl) : base(BotInfo, serverUrl) { }

        public override void Run()
        {
            while (IsRunning)
            {
                if (Interlocked.Exchange(ref _blockingRescanRequested, 0) == 1)
                {
                    Rescan();
                }
                else
                {
                    Go();
                }
            }
        }

        public void RequestBlockingRescan() => Interlocked.Exchange(ref _blockingRescanRequested, 1);
    }

    private RadarTestBot StartRadarBot()
    {
        var bot = new RadarTestBot(Server.ServerUrl);
        StartAsync(bot);
        AwaitGameStarted(bot);
        AwaitTick(bot);

        // Drain the initial automatic intent so subsequent captures start cleanly.
        Server.ResetBotIntentEvent();
        Server.ContinueBotIntent();
        AwaitBotIntent();
        Server.ResetBotIntentEvent();

        return bot;
    }

    /// <summary>
    /// Helper to wait for an intent that satisfies a predicate.
    /// This handles draining multiple intents if the bot is looping automatically.
    /// </summary>
    private void AwaitExpectedIntent(Predicate<BotIntent> predicate)
    {
        var start = DateTime.Now;
        while ((DateTime.Now - start).TotalSeconds < 30)
        {
            Server.ContinueBotIntent();
            if (Server.AwaitBotIntent(CiWaitMs))
            {
                if (predicate(Server.BotIntent))
                {
                    return;
                }
                Server.ResetBotIntentEvent();
            }
        }
        Assert.Fail("Timed out waiting for expected intent");
    }

    [Test]
    [Description("SetRescan() sets the rescan flag in the intent")]
    public void TestRescanIntent()
    {
        var bot = StartRadarBot();

        bot.SetRescan();
        AwaitExpectedIntent(intent => intent.Rescan == true);
    }

    [Test]
    [Description("Rescan() blocking call sets the rescan flag in the intent")]
    public void TestBlockingRescan()
    {
        var bot = StartRadarBot();

        // Rescan must run on the bot's managed thread because it blocks until the next turn.
        bot.RequestBlockingRescan();
        AwaitExpectedIntent(intent => intent.Rescan == true);
    }

    [Test]
    [Description("AdjustRadarForBodyTurn sets the flag in the intent")]
    public void TestAdjustRadarBody()
    {
        var bot = StartRadarBot();

        bot.AdjustRadarForBodyTurn = true;
        AwaitExpectedIntent(intent => intent.AdjustRadarForBodyTurn == true);

        bot.AdjustRadarForBodyTurn = false;
        AwaitExpectedIntent(intent => intent.AdjustRadarForBodyTurn == false);
    }

    [Test]
    [Description("AdjustRadarForGunTurn sets the flag in the intent")]
    public void TestAdjustRadarGun()
    {
        var bot = StartRadarBot();

        bot.AdjustRadarForGunTurn = true;
        AwaitExpectedIntent(intent => intent.AdjustRadarForGunTurn == true);

        bot.AdjustRadarForGunTurn = false;
        AwaitExpectedIntent(intent => intent.AdjustRadarForGunTurn == false);
    }
}
