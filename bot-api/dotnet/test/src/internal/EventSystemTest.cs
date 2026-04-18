using System;
using System.Collections.Generic;
using System.Reflection;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Internal;
using Robocode.TankRoyale.BotApi.Graphics;

namespace Robocode.TankRoyale.BotApi.Tests.Internal;

[TestFixture]
[Category("EVT")]
public class EventSystemTest
{
    private class BaseBotStub : IBaseBot
    {
        public List<BotEvent> FiredEvents { get; } = new();

        public void Start() {}
        public void Go() {}
        public int MyId => 1;
        public string Variant => "";
        public string Version => "";
        public string GameType => "";
        public int ArenaWidth => 800;
        public int ArenaHeight => 600;
        public int NumberOfRounds => 1;
        public double GunCoolingRate => 0.1;
        public int? MaxInactivityTurns => 450;
        public int TurnTimeout => 30000;
        public int TimeLeft => 0;
        public int RoundNumber => 1;
        public int TurnNumber => 1;
        public int EnemyCount => 0;
        public double Energy { get; set; } = 100;
        public bool IsDisabled => Energy <= 0;
        public double X => 0;
        public double Y => 0;
        public double Direction => 0;
        public double GunDirection => 0;
        public double RadarDirection => 0;
        public double Speed => 0;
        public double GunHeat { get; set; } = 0;
        public IEnumerable<BulletState> BulletStates => new List<BulletState>();
        public IList<BotEvent> Events => new List<BotEvent>();
        public void ClearEvents() {}
        public double TurnRate { get; set; }
        public double MaxTurnRate { get; set; } = 10;
        public double GunTurnRate { get; set; }
        public double MaxGunTurnRate { get; set; } = 20;
        public double RadarTurnRate { get; set; }
        public double MaxRadarTurnRate { get; set; } = 45;
        public double TargetSpeed { get; set; }
        public double MaxSpeed { get; set; } = 8;
        public bool SetFire(double firepower) => false;
        public double Firepower => 0;
        public void SetRescan() {}
        public void SetFireAssist(bool enable) {}
        public bool Interruptible { set {} }
        public bool AdjustGunForBodyTurn { get; set; }
        public bool AdjustRadarForBodyTurn { get; set; }
        public bool AdjustRadarForGunTurn { get; set; }
        public void SetStop() {}
        public void SetStop(bool overwrite) {}
        public void SetResume() {}
        public bool IsStopped => false;
        public ICollection<int> TeammateIds => new List<int>();
        public bool IsTeammate(int botId) => false;
        public void BroadcastTeamMessage(object message) {}
        public void SendTeamMessage(int teammateId, object message) {}
        public Color? BodyColor { get; set; }
        public Color? TurretColor { get; set; }
        public Color? RadarColor { get; set; }
        public Color? BulletColor { get; set; }
        public Color? ScanColor { get; set; }
        public Color? TracksColor { get; set; }
        public Color? GunColor { get; set; }
        public bool IsDebuggingEnabled => false;
        public IGraphics Graphics => null;

        public void OnConnected(ConnectedEvent e) {}
        public void OnDisconnected(DisconnectedEvent e) {}
        public void OnConnectionError(ConnectionErrorEvent e) {}
        public void OnGameStarted(GameStartedEvent e) {}
        public void OnGameEnded(GameEndedEvent e) {}
        public void OnRoundStarted(RoundStartedEvent e) {}
        public void OnRoundEnded(RoundEndedEvent e) {}
        public void OnTick(TickEvent e) => FiredEvents.Add(e);
        public void OnBotDeath(BotDeathEvent e) => FiredEvents.Add(e);
        public void OnDeath(DeathEvent e) => FiredEvents.Add(e);
        public void OnHitBot(HitBotEvent e) => FiredEvents.Add(e);
        public void OnHitWall(HitWallEvent e) => FiredEvents.Add(e);
        public void OnBulletFired(BulletFiredEvent e) => FiredEvents.Add(e);
        public void OnHitByBullet(HitByBulletEvent e) => FiredEvents.Add(e);
        public void OnBulletHit(BulletHitBotEvent e) => FiredEvents.Add(e);
        public void OnBulletHitBullet(BulletHitBulletEvent e) => FiredEvents.Add(e);
        public void OnBulletHitWall(BulletHitWallEvent e) => FiredEvents.Add(e);
        public void OnScannedBot(ScannedBotEvent e) => FiredEvents.Add(e);
        public void OnSkippedTurn(SkippedTurnEvent e) => FiredEvents.Add(e);
        public void OnWonRound(WonRoundEvent e) => FiredEvents.Add(e);
        public void OnCustomEvent(CustomEvent e) => FiredEvents.Add(e);
        public void OnTeamMessage(TeamMessageEvent e) => FiredEvents.Add(e);

        public bool AddCustomEvent(Condition condition) => false;
        public bool RemoveCustomEvent(Condition condition) => false;
        public int GetEventPriority(Type t) => 0;
        public void SetEventPriority(Type t, int p) {}
        public double CalcMaxTurnRate(double s) => 0;
        public double CalcBulletSpeed(double f) => 0;
        public double CalcGunHeat(double f) => 0;
        public double CalcBearing(double d) => 0;
        public double CalcGunBearing(double d) => 0;
        public double CalcRadarBearing(double d) => 0;
        public double DirectionTo(double x, double y) => 0;
        public double BearingTo(double x, double y) => 0;
        public double GunBearingTo(double x, double y) => 0;
        public double RadarBearingTo(double x, double y) => 0;
        public double DistanceTo(double x, double y) => 0;
        public double NormalizeAbsoluteAngle(double a) => 0;
        public double NormalizeRelativeAngle(double a) => 0;
        public double CalcDeltaAngle(double t, double s) => 0;
    }

    private BaseBotStub botStub;
    private BaseBotInternals internals;
    private EventQueue queue;

    [SetUp]
    public void SetUp()
    {
        botStub = new BaseBotStub();
        var botInfo = new BotInfo("dummy", "1.0", new List<string> { "dummy" }, null, null, null, null, null, null, null);
        internals = new BaseBotInternals(botStub, botInfo, new Uri("ws://localhost:7654"), null);
        queue = new EventQueue(internals, internals.BotEventHandlers);
    }

    [Test]
    [Property("ID", "TR-API-EVT-001")]
    public void Test_TR_API_EVT_001_Event_Constructors()
    {
        // TickEvent
        var te = new TickEvent(1, 2, null, new List<BulletState>(), new List<BotEvent>());
        Assert.That(te.TurnNumber, Is.EqualTo(1));
        Assert.That(te.RoundNumber, Is.EqualTo(2));

        // ScannedBotEvent
        var sbe = new ScannedBotEvent(3, 1, 2, 80.0, 100.0, 200.0, 45.0, 5.0);
        Assert.That(sbe.TurnNumber, Is.EqualTo(3));
        Assert.That(sbe.ScannedByBotId, Is.EqualTo(1));
        Assert.That(sbe.ScannedBotId, Is.EqualTo(2));
        Assert.That(sbe.Energy, Is.EqualTo(80.0));
        Assert.That(sbe.X, Is.EqualTo(100.0));
        Assert.That(sbe.Y, Is.EqualTo(200.0));
        Assert.That(sbe.Direction, Is.EqualTo(45.0));
        Assert.That(sbe.Speed, Is.EqualTo(5.0));

        // HitBotEvent
        var hbe = new HitBotEvent(4, 5, 90.0, 10.0, 20.0, true);
        Assert.That(hbe.TurnNumber, Is.EqualTo(4));
        Assert.That(hbe.VictimId, Is.EqualTo(5));
        Assert.That(hbe.Energy, Is.EqualTo(90.0));
        Assert.That(hbe.X, Is.EqualTo(10.0));
        Assert.That(hbe.Y, Is.EqualTo(20.0));
        Assert.That(hbe.IsRammed, Is.True);

        // HitByBulletEvent
        var bullet = new BulletState(1, 1, 3.0, 100.0, 200.0, 45.0, null);
        var hbbe = new HitByBulletEvent(6, bullet, 5.0, 95.0);
        Assert.That(hbbe.TurnNumber, Is.EqualTo(6));
        Assert.That(hbbe.Bullet, Is.EqualTo(bullet));
        Assert.That(hbbe.Damage, Is.EqualTo(5.0));
        Assert.That(hbbe.Energy, Is.EqualTo(95.0));

        // HitWallEvent
        var hwe = new HitWallEvent(7);
        Assert.That(hwe.TurnNumber, Is.EqualTo(7));

        // BulletFiredEvent
        var bfe = new BulletFiredEvent(8, bullet);
        Assert.That(bfe.TurnNumber, Is.EqualTo(8));
        Assert.That(bfe.Bullet, Is.EqualTo(bullet));

        // BulletHitBotEvent
        var bhbe = new BulletHitBotEvent(9, 10, bullet, 5.0, 90.0);
        Assert.That(bhbe.TurnNumber, Is.EqualTo(9));
        Assert.That(bhbe.VictimId, Is.EqualTo(10));
        Assert.That(bhbe.Bullet, Is.EqualTo(bullet));
        Assert.That(bhbe.Damage, Is.EqualTo(5.0));
        Assert.That(bhbe.Energy, Is.EqualTo(90.0));

        // BulletHitBulletEvent
        var otherBullet = new BulletState(2, 2, 3.0, 150.0, 250.0, 90.0, null);
        var bhbue = new BulletHitBulletEvent(10, bullet, otherBullet);
        Assert.That(bhbue.TurnNumber, Is.EqualTo(10));
        Assert.That(bhbue.Bullet, Is.EqualTo(bullet));
        Assert.That(bhbue.HitBullet, Is.EqualTo(otherBullet));

        // BulletHitWallEvent
        var bhwe = new BulletHitWallEvent(11, bullet);
        Assert.That(bhwe.TurnNumber, Is.EqualTo(11));
        Assert.That(bhwe.Bullet, Is.EqualTo(bullet));

        // BotDeathEvent
        var bde = new BotDeathEvent(12, 13);
        Assert.That(bde.TurnNumber, Is.EqualTo(12));
        Assert.That(bde.VictimId, Is.EqualTo(13));

        // DeathEvent
        var de = new DeathEvent(14);
        Assert.That(de.TurnNumber, Is.EqualTo(14));

        // SkippedTurnEvent
        var ste = new SkippedTurnEvent(15);
        Assert.That(ste.TurnNumber, Is.EqualTo(15));

        // WonRoundEvent
        var wre = new WonRoundEvent(16);
        Assert.That(wre.TurnNumber, Is.EqualTo(16));

        // TeamMessageEvent
        var tme = new TeamMessageEvent(17, "hello", 18);
        Assert.That(tme.TurnNumber, Is.EqualTo(17));
        Assert.That(tme.Message, Is.EqualTo("hello"));
        Assert.That(tme.SenderId, Is.EqualTo(18));

        // CustomEvent
        var condition = new TestCondition();
        var ce = new CustomEvent(19, condition);
        Assert.That(ce.TurnNumber, Is.EqualTo(19));
        Assert.That(ce.Condition, Is.EqualTo(condition));
    }

    [Test]
    [Property("ID", "TR-API-EVT-005")]
    public void Test_TR_API_EVT_005_Event_Queue_Priority()
    {
        var wre = new WonRoundEvent(1); // Critical, Priority 150
        var de = new DeathEvent(1);     // Critical, Priority 10
        var sbe = new ScannedBotEvent(1, 1, 2, 100, 100, 100, 0, 0); // Non-critical, Priority 20

        queue.AddEvent(de);
        queue.AddEvent(wre);
        queue.AddEvent(sbe);

        queue.DispatchEvents(1);

        Assert.That(botStub.FiredEvents, Has.Count.EqualTo(3));
        Assert.That(botStub.FiredEvents[0], Is.InstanceOf<WonRoundEvent>());
        Assert.That(botStub.FiredEvents[1], Is.InstanceOf<DeathEvent>());
        Assert.That(botStub.FiredEvents[2], Is.InstanceOf<ScannedBotEvent>());
    }

    [Test]
    [Property("ID", "TR-API-EVT-006")]
    public void Test_TR_API_EVT_006_Event_Queue_Age_Culling()
    {
        var oldEvent = new ScannedBotEvent(7, 1, 2, 100, 100, 100, 0, 0);
        var fineEvent = new ScannedBotEvent(8, 1, 2, 100, 100, 100, 0, 0);
        var oldCritical = new WonRoundEvent(7);

        queue.AddEvent(oldEvent);
        queue.AddEvent(fineEvent);
        queue.AddEvent(oldCritical);

        queue.DispatchEvents(10);

        Assert.That(botStub.FiredEvents, Has.Count.EqualTo(2));
        Assert.That(botStub.FiredEvents[0], Is.InstanceOf<WonRoundEvent>());
        Assert.That(botStub.FiredEvents[1], Is.InstanceOf<ScannedBotEvent>());
    }

    [Test]
    [Property("ID", "TR-API-EVT-007")]
    public void Test_TR_API_EVT_007_Event_Queue_Size_Cap()
    {
        for (int i = 0; i < 266; i++)
        {
            queue.AddEvent(new SkippedTurnEvent(i));
        }
        Assert.That(queue.Events(300), Has.Count.EqualTo(256));
    }

    [Test]
    [Property("ID", "TR-API-EVT-008")]
    public void Test_TR_API_EVT_008_Condition_Test_Callable()
    {
        var c1 = new TestCondition(true);
        Assert.That(c1.Test(), Is.True);

        var c2 = new TestCondition(false);
        Assert.That(c2.Test(), Is.False);
    }

    [Test]
    [Property("ID", "TR-API-EVT-009")]
    public void Test_TR_API_EVT_009_Custom_Event_Firing()
    {
        var condTrue = new TestCondition(true);
        var condFalse = new TestCondition(false);

        internals.AddCondition(condTrue);
        internals.AddCondition(condFalse);
        
        var tick = new TickEvent(5, 1, null, new List<BulletState>(), new List<BotEvent>());
        SetTickEvent(tick);

        queue.DispatchEvents(5);

        Assert.That(botStub.FiredEvents, Has.Count.EqualTo(1));
        Assert.That(botStub.FiredEvents[0], Is.InstanceOf<CustomEvent>());
        Assert.That(((CustomEvent)botStub.FiredEvents[0]).Condition, Is.EqualTo(condTrue));
    }

    private void SetTickEvent(TickEvent tick)
    {
        var field = typeof(BaseBotInternals).GetField("_tickEvent", BindingFlags.NonPublic | BindingFlags.Instance);
        field.SetValue(internals, tick);
    }

    private class TestCondition : Condition
    {
        private readonly bool _result;
        public TestCondition(bool result = true) : base("test") { _result = result; }
        public override bool Test() => _result;
    }
}
