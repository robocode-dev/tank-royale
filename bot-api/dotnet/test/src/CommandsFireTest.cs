using System;
using NUnit.Framework;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Tests for fire commands (TR-API-CMD-002).
///
/// These tests verify the behavior of fire-related methods:
/// - Firepower is clamped to valid range [0.1, 3.0]
/// - Fire fails when gun is hot (gunHeat > 0)
/// - Fire fails when energy is too low
/// - NaN firepower throws ArgumentException
/// </summary>
[TestFixture]
[Category("TR-API-CMD-002")]
[Description("Fire Commands (TR-API-CMD-002)")]
public class CommandsFireTest : AbstractBotTest
{
    /// <summary>
    /// Helper to test SetFire and capture the resulting intent.
    /// After calling SetFire, we need to trigger Go() to actually send the intent.
    /// </summary>
    private CommandResult<bool> SetFireAndGetIntent(BaseBot bot, double firepower)
    {
        Server.ResetBotIntentEvent();
        bool result = bot.SetFire(firepower);
        // Fire command just sets the intent value; we need Go() to send it
        GoAsync(bot);
        AwaitBotIntent();
        return new CommandResult<bool>(result, Server.BotIntent);
    }

    [Test]
    [Description("Firepower below 0.1 is sent as-is (server clamps)")]
    public void TestFirepowerBelowMinSentAsIs()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with value below minimum
        var result = SetFireAndGetIntent(bot, 0.05);

        // Fire should succeed - API sends raw value, server will clamp
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(0.05));
    }

    [Test]
    [Description("Firepower above 3.0 is sent as-is (server clamps)")]
    public void TestFirepowerAboveMaxSentAsIs()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with value of 5.0 - this is above max but passes energy check (100 > 5)
        var result = SetFireAndGetIntent(bot, 5.0);

        // Fire should succeed - API sends raw value, server will clamp
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(5.0));
    }

    [Test]
    [Description("Valid firepower (1.0) is preserved in intent")]
    public void TestValidFirepowerIsPreserved()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with valid value
        var result = SetFireAndGetIntent(bot, 1.0);

        // Fire should succeed (true) with exact value
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(1.0));
    }

    [Test]
    [Description("Fire fails when gun is hot (gunHeat > 0)")]
    public void TestFireFailsWhenGunIsHot()
    {
        // Start bot and wait for game started
        var bot = Start();
        AwaitGameStarted(bot);

        // Set high gun heat so bot cannot fire - use SetBotStateAndAwaitTick to send state to bot
        Server.SetBotStateAndAwaitTick(100.0, 5.0, null, null, null, null);
        // Wait for bot to process the updated state
        AwaitCondition(() => bot.GunHeat == 5.0, 1000);

        // Execute fire - should fail due to gun heat
        var result = SetFireAndGetIntent(bot, 1.0);

        // Fire should fail (false) and firepower should be null
        Assert.That(result.Result, Is.False);
        Assert.That(result.Intent.Firepower, Is.Null);
    }

    [Test]
    [Description("Fire fails when energy is too low for firepower")]
    public void TestFireFailsWhenEnergyTooLow()
    {
        // Start bot and wait for game started
        var bot = Start();
        AwaitGameStarted(bot);

        // Set low energy and no gun heat - use SetBotStateAndAwaitTick to send state to bot
        Server.SetBotStateAndAwaitTick(0.5, 0.0, null, null, null, null);
        // Wait for bot to process the updated state
        AwaitCondition(() => bot.Energy == 0.5 && bot.GunHeat == 0.0, 1000);

        // Execute fire with high firepower - should fail due to energy
        var result = SetFireAndGetIntent(bot, 3.0);

        // Fire should fail (false) and firepower should be null
        Assert.That(result.Result, Is.False);
        Assert.That(result.Intent.Firepower, Is.Null);
    }

    [Test]
    [Description("Fire with NaN throws ArgumentException")]
    public void TestFireWithNaNThrowsException()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with NaN - should throw
        Assert.Throws<ArgumentException>(() => bot.SetFire(double.NaN));
    }

    [Test]
    [Description("Fire with negative value sets raw value (API does not clamp)")]
    public void TestFireWithNegativeValueSetsRawValue()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with negative value - API does not validate/clamp
        var result = SetFireAndGetIntent(bot, -1.0);

        // Fire succeeds because energy check passes (100.0 >= -1.0)
        // The raw value is sent to the server (no clamping in client API)
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(-1.0));
    }

    [Test]
    [Description("Fire with Infinity fails because energy is insufficient")]
    public void TestFireWithInfinityFailsEnergyCheck()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with Infinity - fails because energy < Infinity
        var result = SetFireAndGetIntent(bot, double.PositiveInfinity);

        // Fire should fail (false) because energy (100.0) < Infinity
        Assert.That(result.Result, Is.False);
        Assert.That(result.Intent.Firepower, Is.Null);
    }

    [Test]
    [Description("Fire with exact minimum (0.1) succeeds")]
    public void TestFireWithExactMinimumSucceeds()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with exact minimum
        var result = SetFireAndGetIntent(bot, 0.1);

        // Fire should succeed (true)
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(0.1));
    }

    [Test]
    [Description("Fire with exact maximum (3.0) succeeds")]
    public void TestFireWithExactMaximumSucceeds()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with exact maximum
        var result = SetFireAndGetIntent(bot, 3.0);

        // Fire should succeed (true)
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(3.0));
    }
}
