namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Default event priorities. The lower value, the higher priority.
/// </summary>
public sealed class DefaultEventPriority
{
    public const int OnScannedBot = 10;
    public const int OnHitBot = 20;
    public const int OnHitWall = 30;
    public const int OnHitByBullet = 40;
    public const int OnBulletHit = 50;
    public const int OnBulletHitBullet = 60;
    public const int OnBulletHitWall = 70;
    public const int OnBulletFired = 80;

    public const int OnBotDeath = 90;

    //  public const int OnTeamMessage = 100; // Reserved for future
    public const int OnCondition = 110;
    public const int OnTick = 120;
    public const int OnSkippedTurn = 130;
    public const int OnWonRound = 140;
    public const int OnDeath = 150;
}