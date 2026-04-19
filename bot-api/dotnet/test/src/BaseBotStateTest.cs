using System;
using System.Collections.Generic;
using NUnit.Framework;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
[Category("BOT")]
public class BaseBotStateTest
{
    private class TestBot : BaseBot
    {
        public TestBot() : base(new BotInfo("TestBot", "1.0", new List<string> { "Author" }, null, null, null, new HashSet<string> { "classic" }, null, null, null))
        {
        }
    }

    [Test]
    [Property("ID", "TR-API-BOT-007")]
    [Category("LEGACY")]
    public void Test_TR_API_BOT_007_Base_Bot_Accessor_Defaults()
    {
        var bot = new TestBot();

        // Metadata accessors should throw BotException when not connected
        Assert.Throws<BotException>(() => _ = bot.MyId);
        Assert.Throws<BotException>(() => _ = bot.Variant);
        Assert.Throws<BotException>(() => _ = bot.Version);

        // State-dependent accessors should throw BotException when no state is available
        Assert.Throws<BotException>(() => _ = bot.Energy);
        Assert.Throws<BotException>(() => _ = bot.X);
        Assert.Throws<BotException>(() => _ = bot.Y);
        Assert.Throws<BotException>(() => _ = bot.Direction);
        Assert.Throws<BotException>(() => _ = bot.GunDirection);
        Assert.Throws<BotException>(() => _ = bot.RadarDirection);
        Assert.That(bot.Speed, Is.EqualTo(0));
        Assert.That(bot.GunHeat, Is.EqualTo(0));
        Assert.That(bot.BulletStates, Is.Empty);
        Assert.Throws<BotException>(() => _ = bot.Events);

        // Game setup accessors should throw BotException when no game setup is available
        Assert.Throws<BotException>(() => _ = bot.ArenaWidth);
        Assert.Throws<BotException>(() => _ = bot.ArenaHeight);
        Assert.Throws<BotException>(() => _ = bot.GameType);
    }

    [Test]
    [Property("ID", "TR-API-BOT-008")]
    [Category("LEGACY")]
    public void Test_TR_API_BOT_008_Adjustment_Flags_Default_False()
    {
        var bot = new TestBot();

        Assert.That(bot.AdjustGunForBodyTurn, Is.False);
        Assert.That(bot.AdjustRadarForBodyTurn, Is.False);
        Assert.That(bot.AdjustRadarForGunTurn, Is.False);
    }
}
