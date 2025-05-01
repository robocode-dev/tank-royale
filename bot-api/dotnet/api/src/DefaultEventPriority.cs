using JetBrains.Annotations;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Default event priorities values. The lower value, the higher event priority. So the <see cref="Events.DeathEvent"/>
/// has the highest priority (10), and <see cref="Events.WonRoundEvent"/> has the lowest priority (150).
/// </summary>
[PublicAPI]
public static class DefaultEventPriority
{
    /// <summary>
    /// Event priority for the <see cref="Events.WonRoundEvent"/>
    /// </summary>
    public const int WonRound = 150;

    /// <summary>
    /// Event priority for the <see cref="Events.SkippedTurnEvent"/>
    /// </summary>
    public const int SkippedTurn = 140;

    /// <summary>
    /// Event priority for the <see cref="Events.TickEvent"/>
    /// </summary>
    public const int Tick = 130;

    /// <summary>
    /// Event priority for the <see cref="Events.CustomEvent"/>
    /// </summary>
    public const int Custom = 120;

    /// <summary>
    /// Event priority for the <see cref="Events.TeamMessageEvent"/>
    /// </summary>
    public const int TeamMessage = 110;
    
    /// <summary>
    /// Event priority for the <see cref="Events.BotDeathEvent"/>
    /// </summary>
    public const int BotDeath = 100;

    /// <summary>
    /// Event priority for the <see cref="Events.BulletHitWallEvent"/>
    /// </summary>
    public const int BulletHitWall = 90;

    /// <summary>
    /// Event priority for the <see cref="Events.BulletHitBulletEvent"/>
    /// </summary>
    public const int BulletHitBullet = 80;
    
    /// <summary>
    /// Event priority for the <see cref="Events.BulletHitBotEvent"/>
    /// </summary>
    public const int BulletHitBot = 70;

    /// <summary>
    /// Event priority for the <see cref="Events.BulletFiredEvent"/>
    /// </summary>
    public const int BulletFired = 60;

    /// <summary>
    /// Event priority for the <see cref="Events.HitByBulletEvent"/>
    /// </summary>
    public const int HitByBullet = 50;

    /// <summary>
    /// Event priority for the <see cref="Events.HitWallEvent"/>
    /// </summary>
    public const int HitWall = 40;
    
    /// <summary>
    /// Event priority for the <see cref="Events.HitBotEvent"/>
    /// </summary>
    public const int HitBot = 30;

    /// <summary>
    /// Event priority for the <see cref="Events.ScannedBotEvent"/>
    /// </summary>
    public const int ScannedBot = 20;

    /// <summary>
    /// Event priority for the <see cref="Events.DeathEvent"/>
    /// </summary>
    public const int Death = 10;
}