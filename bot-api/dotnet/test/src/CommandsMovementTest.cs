using NUnit.Framework;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using static Robocode.TankRoyale.BotApi.Constants;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
[Description("TR-API-CMD-001 Movement commands")] 
public class CommandsMovementTest : AbstractBotTest
{
    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-001")]
    public void GivenMovementCommandsSet_whenGo_thenIntentContainsClampedValues()
    {
        // Arrange
    // Ensure all movement limits are unset so intent is always accepted
    Server.SetSpeedMinLimit(double.MinValue);
    Server.SetSpeedMaxLimit(double.MaxValue);
    Server.SetDirectionMinLimit(double.MinValue);
    Server.SetDirectionMaxLimit(double.MaxValue);
    Server.SetGunDirectionMinLimit(double.MinValue);
    Server.SetGunDirectionMaxLimit(double.MaxValue);
    Server.SetRadarDirectionMinLimit(double.MinValue);
    Server.SetRadarDirectionMaxLimit(double.MaxValue);
    var bot = StartAndAwaitTick();

        // Act: set values beyond limits to verify clamping
        bot.TurnRate = 999; // > MAX_TURN_RATE
        bot.GunTurnRate = -999; // < -MAX_GUN_TURN_RATE
        bot.RadarTurnRate = 1000; // > MAX_RADAR_TURN_RATE
        bot.TargetSpeed = 123; // > MAX_SPEED

        // Trigger sending of intent on next go
        GoAsync(bot);
        AwaitBotIntent();

        // Assert
        var intent = Server.BotIntent;
        Assert.That(intent, Is.Not.Null);
        Assert.That(intent.TurnRate, Is.EqualTo(MaxTurnRate));
        Assert.That(intent.GunTurnRate, Is.EqualTo(-MaxGunTurnRate));
        Assert.That(intent.RadarTurnRate, Is.EqualTo(MaxRadarTurnRate));
        Assert.That(intent.TargetSpeed, Is.EqualTo(MaxSpeed));
    }

    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-001")]
    public void GivenNaNValues_whenSettingMovementCommands_thenThrowArgumentException()
    {
    TestContext.WriteLine("Starting NaN movement command test");
    var bot = Start();
    TestContext.WriteLine("Bot started");
    var handshake = Server.AwaitBotHandshake(1000);
    TestContext.WriteLine($"AwaitBotHandshake: {handshake}");
    var gameStarted = Server.AwaitGameStarted(1000);
    TestContext.WriteLine($"AwaitGameStarted: {gameStarted}");
    var tick = Server.AwaitTick(1000);
    TestContext.WriteLine($"AwaitTick: {tick}");
    Assert.That(handshake, Is.True, "Bot handshake not received");
    Assert.That(gameStarted, Is.True, "Game did not start in time");
    Assert.That(tick, Is.True, "Tick not received in time");

    Assert.Throws<System.ArgumentException>(() => bot.TurnRate = double.NaN);
    Assert.Throws<System.ArgumentException>(() => bot.GunTurnRate = double.NaN);
    Assert.Throws<System.ArgumentException>(() => bot.RadarTurnRate = double.NaN);
    Assert.Throws<System.ArgumentException>(() => bot.TargetSpeed = double.NaN);
    }
}
