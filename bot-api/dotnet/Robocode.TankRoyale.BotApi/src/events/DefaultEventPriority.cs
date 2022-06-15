namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Default event priorities values. The higher value, the higher event priority. So the <see cref="DeathEvent"/> has the
/// highest priority (<see cref="Death"/>), and <see cref="WonRoundEvent"/> has the lowest priority (<see cref="WonRound"/>)
/// </summary>
public static class DefaultEventPriority
{
    /// <summary>
    /// Event priority for the <see cref="DeathEvent"/>
    /// </summary>
    public const int Death = 150;

    /// <summary>
    /// Event priority for the <see cref="ScannedBotEvent"/>
    /// </summary>
    public const int ScannedBot = 140;

    /// <summary>
    /// Event priority for the <see cref="HitBotEvent"/>
    /// </summary>
    public const int HitBot = 130;

    /// <summary>
    /// Event priority for the <see cref="HitWallEvent"/>
    /// </summary>
    public const int HitWall = 120;

    /// <summary>
    /// Event priority for the <see cref="HitByBulletEvent"/>
    /// </summary>
    public const int HitByBullet = 110;

    /// <summary>
    /// Event priority for the <see cref="BulletFiredEvent"/>
    /// </summary>
    public const int BulletFired = 100;

    /// <summary>
    /// Event priority for the <see cref="BulletHitBotEvent"/>
    /// </summary>
    public const int BulletHitBot = 90;

    /// <summary>
    /// Event priority for the <see cref="BulletHitBulletEvent"/>
    /// </summary>
    public const int BulletHitBullet = 80;
    
    /// <summary>
    /// Event priority for the <see cref="BulletHitWallEvent"/>
    /// </summary>
    public const int BulletHitWall = 70;

    /// <summary>
    /// Event priority for the <see cref="BotDeathEvent"/>
    /// </summary>
    public const int BotDeath = 60;
    
    //  public const int TeamMessage = 50; // Reserved for future

    /// <summary>
    /// Event priority for the <see cref="CustomEvent"/>
    /// </summary>
    public const int Custom = 40;
    
    /// <summary>
    /// Event priority for the <see cref="TickEvent"/>
    /// </summary>
    public const int Tick = 30;

    /// <summary>
    /// Event priority for the <see cref="SkippedTurnEvent"/>
    /// </summary>
    public const int SkippedTurn = 20;

    /// <summary>
    /// Event priority for the <see cref="WonRoundEvent"/>
    /// </summary>
    public const int WonRound = 10;
}