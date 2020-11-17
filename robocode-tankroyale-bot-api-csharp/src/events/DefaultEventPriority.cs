namespace Robocode.TankRoyale.BotApi.Events
{
  /// <summary>
  /// Default event priorities. The lower value, the higher priority.
  /// </summary>
  public class DefaultEventPriority
  {
    public const int OnWonRound = 10;
    public const int OnSkippedTurn = 20;
    public const int OnTick = 30;
    public const int OnCondition = 40;
    //  public const int OnTeamMessage = 50; // Reserved for future
    public const int OnBotDeath = 60;
    public const int OnBulletFired = 70;
    public const int OnBulletHitWall = 80;
    public const int OnBulletHitBullet = 90;
    public const int OnBulletHit = 100;
    public const int OnHitByBullet = 110;
    public const int OnHitWall = 120;
    public const int OnHitBot = 130;
    public const int OnScannedBot = 140;
    public const int OnDeath = 150;
  }
}