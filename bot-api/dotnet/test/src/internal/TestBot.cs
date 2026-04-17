using System;
using System.Collections.Generic;
using System.Linq;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Tests.Internal;

public class TestBot : Robocode.TankRoyale.BotApi.IBaseBot
{
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
    public IEnumerable<Robocode.TankRoyale.BotApi.BulletState> BulletStates => Enumerable.Empty<Robocode.TankRoyale.BotApi.BulletState>();
    public IList<Robocode.TankRoyale.BotApi.Events.BotEvent> Events => new List<Robocode.TankRoyale.BotApi.Events.BotEvent>();
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
    public Robocode.TankRoyale.BotApi.Graphics.Color? BodyColor { get; set; }
    public Robocode.TankRoyale.BotApi.Graphics.Color? TurretColor { get; set; }
    public Robocode.TankRoyale.BotApi.Graphics.Color? RadarColor { get; set; }
    public Robocode.TankRoyale.BotApi.Graphics.Color? BulletColor { get; set; }
    public Robocode.TankRoyale.BotApi.Graphics.Color? ScanColor { get; set; }
    public Robocode.TankRoyale.BotApi.Graphics.Color? TracksColor { get; set; }
    public Robocode.TankRoyale.BotApi.Graphics.Color? GunColor { get; set; }
    public bool IsDebuggingEnabled => false;
    public Robocode.TankRoyale.BotApi.Graphics.IGraphics Graphics => null;
    public void OnConnected(Robocode.TankRoyale.BotApi.Events.ConnectedEvent e) {}
    public void OnDisconnected(Robocode.TankRoyale.BotApi.Events.DisconnectedEvent e) {}
    public void OnConnectionError(Robocode.TankRoyale.BotApi.Events.ConnectionErrorEvent e) {}
    public void OnGameStarted(Robocode.TankRoyale.BotApi.Events.GameStartedEvent e) {}
    public void OnGameEnded(Robocode.TankRoyale.BotApi.Events.GameEndedEvent e) {}
    public void OnRoundStarted(Robocode.TankRoyale.BotApi.Events.RoundStartedEvent e) {}
    public void OnRoundEnded(Robocode.TankRoyale.BotApi.Events.RoundEndedEvent e) {}
    public void OnTick(Robocode.TankRoyale.BotApi.Events.TickEvent e) {}
    public void OnBotDeath(Robocode.TankRoyale.BotApi.Events.BotDeathEvent e) {}
    public void OnDeath(Robocode.TankRoyale.BotApi.Events.DeathEvent e) {}
    public void OnHitBot(Robocode.TankRoyale.BotApi.Events.HitBotEvent e) {}
    public void OnHitWall(Robocode.TankRoyale.BotApi.Events.HitWallEvent e) {}
    public void OnBulletFired(Robocode.TankRoyale.BotApi.Events.BulletFiredEvent e) {}
    public void OnHitByBullet(Robocode.TankRoyale.BotApi.Events.HitByBulletEvent e) {}
    public void OnBulletHit(Robocode.TankRoyale.BotApi.Events.BulletHitBotEvent e) {}
    public void OnBulletHitBullet(Robocode.TankRoyale.BotApi.Events.BulletHitBulletEvent e) {}
    public void OnBulletHitWall(Robocode.TankRoyale.BotApi.Events.BulletHitWallEvent e) {}
    public void OnScannedBot(Robocode.TankRoyale.BotApi.Events.ScannedBotEvent e) {}
    public void OnSkippedTurn(Robocode.TankRoyale.BotApi.Events.SkippedTurnEvent e) {}
    public void OnWonRound(WonRoundEvent e) {}
    public void OnCustomEvent(CustomEvent e) {}
    public void OnTeamMessage(TeamMessageEvent e) {}
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
