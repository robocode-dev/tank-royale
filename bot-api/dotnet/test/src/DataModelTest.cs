using System;
using System.Collections.Generic;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Graphics;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
[Category("MDL")]
public class DataModelTest
{
    [Test]
    [Property("ID", "TR-API-MDL-002")]
    public void Test_TR_API_MDL_002_BotState_Constructor()
    {
        var bodyColor = Color.FromRgb(0x11, 0x11, 0x11);
        var turretColor = Color.FromRgb(0x22, 0x22, 0x22);
        var radarColor = Color.FromRgb(0x33, 0x33, 0x33);
        var bulletColor = Color.FromRgb(0x44, 0x44, 0x44);
        var scanColor = Color.FromRgb(0x55, 0x55, 0x55);
        var tracksColor = Color.FromRgb(0x66, 0x66, 0x66);
        var gunColor = Color.FromRgb(0x77, 0x77, 0x77);

        var state = new BotState(
            true, 100.0, 50.0, 60.0, 45.0, 90.0, 135.0, 5.0, 1.0, 2.0, 3.0, 4.0, 0.5, 3,
            bodyColor, turretColor, radarColor, bulletColor, scanColor, tracksColor, gunColor, true
        );

        Assert.That(state.Energy, Is.EqualTo(100.0));
        Assert.That(state.X, Is.EqualTo(50.0));
        Assert.That(state.Y, Is.EqualTo(60.0));
        Assert.That(state.Direction, Is.EqualTo(45.0));
        Assert.That(state.GunDirection, Is.EqualTo(90.0));
        Assert.That(state.RadarDirection, Is.EqualTo(135.0));
        Assert.That(state.RadarSweep, Is.EqualTo(5.0));
        Assert.That(state.Speed, Is.EqualTo(1.0));
        Assert.That(state.TurnRate, Is.EqualTo(2.0));
        Assert.That(state.GunTurnRate, Is.EqualTo(3.0));
        Assert.That(state.RadarTurnRate, Is.EqualTo(4.0));
        Assert.That(state.GunHeat, Is.EqualTo(0.5));
        Assert.That(state.EnemyCount, Is.EqualTo(3));
        Assert.That(state.BodyColor, Is.EqualTo(bodyColor));
        Assert.That(state.TurretColor, Is.EqualTo(turretColor));
        Assert.That(state.RadarColor, Is.EqualTo(radarColor));
        Assert.That(state.BulletColor, Is.EqualTo(bulletColor));
        Assert.That(state.ScanColor, Is.EqualTo(scanColor));
        Assert.That(state.TracksColor, Is.EqualTo(tracksColor));
        Assert.That(state.GunColor, Is.EqualTo(gunColor));
        Assert.That(state.IsDebuggingEnabled, Is.True);
    }

    [Test]
    [Property("ID", "TR-API-MDL-003")]
    public void Test_TR_API_MDL_003_BotResults_Constructor()
    {
        var results = new BotResults(1, 100.0, 50.0, 30.0, 20.0, 10.0, 5.0, 215.0, 3, 2, 4);

        Assert.That(results.Rank, Is.EqualTo(1));
        Assert.That(results.Survival, Is.EqualTo(100.0));
        Assert.That(results.LastSurvivorBonus, Is.EqualTo(50.0));
        Assert.That(results.BulletDamage, Is.EqualTo(30.0));
        Assert.That(results.BulletKillBonus, Is.EqualTo(20.0));
        Assert.That(results.RamDamage, Is.EqualTo(10.0));
        Assert.That(results.RamKillBonus, Is.EqualTo(5.0));
        Assert.That(results.TotalScore, Is.EqualTo(215.0));
        Assert.That(results.FirstPlaces, Is.EqualTo(3));
        Assert.That(results.SecondPlaces, Is.EqualTo(2));
        Assert.That(results.ThirdPlaces, Is.EqualTo(4));
    }

    [Test]
    [Property("ID", "TR-API-MDL-004")]
    public void Test_TR_API_MDL_004_GameSetup_Constructor()
    {
        var setup = new GameSetup("classic", 800, 600, 10, 0.1, 450, 30000, 1000);

        Assert.That(setup.GameType, Is.EqualTo("classic"));
        Assert.That(setup.ArenaWidth, Is.EqualTo(800));
        Assert.That(setup.ArenaHeight, Is.EqualTo(600));
        Assert.That(setup.NumberOfRounds, Is.EqualTo(10));
        Assert.That(setup.GunCoolingRate, Is.EqualTo(0.1));
        Assert.That(setup.MaxInactivityTurns, Is.EqualTo(450));
        Assert.That(setup.TurnTimeout, Is.EqualTo(30000));
        Assert.That(setup.ReadyTimeout, Is.EqualTo(1000));
    }
}
