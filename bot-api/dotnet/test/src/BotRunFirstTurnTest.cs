using System.Threading;
using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using static Robocode.TankRoyale.BotApi.Constants;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Regression tests for issue #202: first-turn skip caused by bot thread starting during turn 1.
///
/// The pre-warm fix starts the bot thread at <c>RoundStarted</c> (before any tick).
/// The thread blocks until tick 1 arrives, after which <c>Run()</c> executes with valid bot state.
/// </summary>
[TestFixture]
[Description("TR-API-TCK-004 First-turn state availability (regression: issue #202)")]
public class BotRunFirstTurnTest : AbstractBotTest
{
    /// <summary>
    /// Spins its radar every turn. Mirrors <c>RadarSpinBotCSharp</c> in
    /// <c>bot-api/tests/bots/csharp/</c> but uses <see cref="MockedServer.ServerUrl"/>
    /// so it connects to the in-process mocked server.
    /// </summary>
    private class RadarSpinBot : Bot
    {
        public double? RadarDirectionOnFirstRun { get; private set; }
        public bool SkippedFirstTurn { get; private set; }

        public RadarSpinBot() : base(BotInfo, MockedServer.ServerUrl) { }

        public override void Run()
        {
            RadarDirectionOnFirstRun = RadarDirection;
            while (IsRunning)
            {
                SetTurnRadarLeft(MaxRadarTurnRate);
                Go();
            }
        }

        public override void OnSkippedTurn(SkippedTurnEvent e)
        {
            if (e.TurnNumber == 1)
                SkippedFirstTurn = true;
        }
    }

    [Test]
    [Category("TCK")]
    [Category("TR-API-TCK-004")]
    [Description("TR-API-TCK-004a run() receives valid bot state on turn 1")]
    public void Test_TR_API_TCK_004a_Run_Sees_First_Tick_State()
    {
        var bot = new RadarSpinBot();
        StartAsync(bot);
        AwaitBotHandshake();
        AwaitBotIntent();

        Assert.That(bot.RadarDirectionOnFirstRun,
            Is.EqualTo(MockedServer.BotRadarDirection),
            "Run() must not execute before first-tick state is available (regression: issue #202)");
    }

    [Test]
    [Category("TCK")]
    [Category("TR-API-TCK-004")]
    [Description("TR-API-TCK-004b first intent contains radar turn rate set in Run()")]
    public void Test_TR_API_TCK_004b_First_Intent_Contains_Radar_Turn_Rate()
    {
        var bot = new RadarSpinBot();
        StartAsync(bot);
        AwaitBotHandshake();
        AwaitBotIntent();

        var intent = Server.BotIntent;
        Assert.That(intent, Is.Not.Null);
        Assert.That(intent.RadarTurnRate,
            Is.EqualTo((double)MaxRadarTurnRate),
            "First intent must include the radar turn rate set in Run() (regression: issue #202)");
    }
}
