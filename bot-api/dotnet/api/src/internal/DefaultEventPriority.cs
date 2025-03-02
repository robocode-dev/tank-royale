namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Default event priorities values. The lower value, the higher event priority. So the <see cref="DeathEvent"/>
/// has the highest priority (10), and <see cref="WonRoundEvent"/> has the lowest priority (150).
/// </summary>
public static class DefaultEventPriority
{
    /// <summary>
    /// Event priority for the <see cref="WonRoundEvent"/>
    /// </summary>
    public const int WonRound = 150;

    /// <summary>
    /// Event priority for the <see cref="SkippedTurnEvent"/>
    /// </summary>
    public const int SkippedTurn = 140;

    /// <summary>
    /// Event priority for the <see cref="TickEvent"/>
    /// </summary>
    public const int Tick = 130;

    /// <summary>
    /// Event priority for the <see cref="CustomEvent"/>
    /// </summary>
    public const int Custom = 120;

    /// <summary>
    /// Event priority for the <see cref="TeamMessageEvent"/>
    /// </summary>
    public const int TeamMessage = 110;
    
    /// <summary>
    /// Event priority for the <see cref="BotDeathEvent"/>
    /// </summary>
    public const int BotDeath = 100;

    /// <summary>
    /// Event priority for the <see cref="BulletHitWallEvent"/>
    /// </summary>
    public const int BulletHitWall = 90;

    /// <summary>
    /// Event priority for the <see cref="BulletHitBulletEvent"/>
    /// </summary>
    public const int BulletHitBullet = 80;
    
    /// <summary>
    /// Event priority for the <see cref="BulletHitBotEvent"/>
    /// </summary>
    public const int BulletHitBot = 70;

    /// <summary>
    /// Event priority for the <see cref="BulletFiredEvent"/>
    /// </summary>
    public const int BulletFired = 60;

    /// <summary>
    /// Event priority for the <see cref="HitByBulletEvent"/>
    /// </summary>
    public const int HitByBullet = 50;

    /// <summary>
    /// Event priority for the <see cref="HitWallEvent"/>
    /// </summary>
    public const int HitWall = 40;
    
    /// <summary>
    /// Event priority for the <see cref="HitBotEvent"/>
    /// </summary>
    public const int HitBot = 30;

    /// <summary>
    /// Event priority for the <see cref="ScannedBotEvent"/>
    /// </summary>
    public const int ScannedBot = 20;

    /// <summary>
    /// Event priority for the <see cref="DeathEvent"/>
    /// </summary>
    public const int Death = 10;
}