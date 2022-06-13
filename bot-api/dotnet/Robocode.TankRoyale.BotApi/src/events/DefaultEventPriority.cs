namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Default event priorities. The higher value, the higher event priority.
/// </summary>
public static class DefaultEventPriority
{
    public const int OnTick = 150;
    public const int OnWonRound = 140;
    public const int OnSkippedTurn = 130;
    public const int OnCondition = 120;
    //  public const int OnTeamMessage = 110; // Reserved for future
    public const int OnBotDeath = 100;
    public const int OnBulletFired = 90;
    public const int OnBulletHitWall = 80;
    public const int OnBulletHitBullet = 70;
    public const int OnBulletHit = 60;
    public const int OnHitByBullet = 50;
    public const int OnHitWall = 40;
    public const int OnHitBot = 30;
    public const int OnScannedBot = 20;
    public const int OnDeath = 10;
}