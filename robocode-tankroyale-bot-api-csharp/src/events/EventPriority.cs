namespace Robocode.TankRoyale.BotApi.Events
{
  /// <summary>
  /// Effective event priorities. The lower value, the higher priority.
  /// </summary>
  public class EventPriority
  {
    public const int OnWonRound = DefaultEventPriority.OnWonRound;
    public const int OnSkippedTurn = DefaultEventPriority.OnSkippedTurn;
    public const int OnTick = DefaultEventPriority.OnTick;
    public const int OnCondition = DefaultEventPriority.OnCondition;
    //  public const int OnTeamMessage = DefaultEventPriority.OnTeamMessage; // Reserved for future
    public const int OnBotDeath = DefaultEventPriority.OnBotDeath;
    public const int OnBulletFired = DefaultEventPriority.OnBulletFired;
    public const int OnBulletHitWall = DefaultEventPriority.OnBulletHitWall;
    public const int OnBulletHitBullet = DefaultEventPriority.OnHitByBullet;
    public const int OnBulletHit = DefaultEventPriority.OnBulletHit;
    public const int OnHitByBullet = DefaultEventPriority.OnHitByBullet;
    public const int OnHitWall = DefaultEventPriority.OnHitWall;
    public const int OnHitBot = DefaultEventPriority.OnHitBot;
    public const int OnScannedBot = DefaultEventPriority.OnScannedBot;
    public const int OnDeath = DefaultEventPriority.OnDeath;
  }
}