using System;
using System.Threading;
using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Tests for radar commands (TR-API-CMD-003).
/// </summary>
[TestFixture]
[Category("LEGACY")]
[Category("TR-API-CMD-003")]
[Description("Radar Commands (TR-API-CMD-003)")]
public class CommandsRadarTest : AbstractBotTest
{
    private class RadarTestBot : Bot
    {
        public RadarTestBot(Uri serverUrl) : base(BotInfo, serverUrl) { }
    }

    private RadarTestBot StartRadarBot()
    {
        var bot = new RadarTestBot(Server.ServerUrl);
        StartAsync(bot);
        AwaitGameStarted(bot);
        AwaitTick(bot);
        return bot;
    }

    /// <summary>
    /// Helper to wait for an intent that satisfies a predicate.
    /// This handles draining multiple intents if the bot is looping automatically.
    /// </summary>
    private void AwaitExpectedIntent(Predicate<BotIntent> predicate)
    {
        var start = DateTime.Now;
        while ((DateTime.Now - start).TotalSeconds < 10)
        {
            Server.ContinueBotIntent();
            if (Server.AwaitBotIntent(2000))
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

        // Run Rescan in a separate task because it's blocking
        Task.Run(() => bot.Rescan());
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
