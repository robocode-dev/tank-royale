using System;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

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
    [Test]
    [Description("Firepower below 0.1 is clamped to 0.1")]
    public void TestFirepowerBelowMinIsClamped()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with value below minimum
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(0.05));

        // Fire should succeed (true) with clamped value
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(0.1));
    }

    [Test]
    [Description("Firepower above 3.0 is clamped to 3.0")]
    public void TestFirepowerAboveMaxIsClamped()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with value above maximum
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(5.0));

        // Fire should succeed (true) with clamped value
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(3.0));
    }

    [Test]
    [Description("Valid firepower (1.0) is preserved in intent")]
    public void TestValidFirepowerIsPreserved()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with valid value
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(1.0));

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

        // Set high gun heat so bot cannot fire
        Server.SetInitialBotState(100.0, 5.0, null, null, null, null);

        // Execute fire - should fail due to gun heat
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(1.0));

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

        // Set low energy and no gun heat
        Server.SetInitialBotState(0.5, 0.0, null, null, null, null);

        // Execute fire with high firepower - should fail due to energy
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(3.0));

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
    [Description("Fire with negative value is clamped to minimum (0.1)")]
    public void TestFireWithNegativeValueIsClamped()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with negative value - should be clamped to 0.1
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(-1.0));

        // Fire should succeed (true) with clamped value
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(0.1));
    }

    [Test]
    [Description("Fire with Infinity is clamped to maximum (3.0)")]
    public void TestFireWithInfinityIsClamped()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with Infinity - should be clamped to 3.0
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(double.PositiveInfinity));

        // Fire should succeed (true) with clamped value
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(3.0));
    }

    [Test]
    [Description("Fire with exact minimum (0.1) succeeds")]
    public void TestFireWithExactMinimumSucceeds()
    {
        var bot = StartAndPrepareForFire();

        // Execute fire with exact minimum
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(0.1));

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
        var result = ExecuteCommandAndGetIntent(() => bot.SetFire(3.0));

        // Fire should succeed (true)
        Assert.That(result.Result, Is.True);
        Assert.That(result.Intent.Firepower, Is.EqualTo(3.0));
    }
}
