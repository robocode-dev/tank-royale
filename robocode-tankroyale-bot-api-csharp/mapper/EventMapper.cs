using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  public sealed class EventMapper
  {
    public static TickEvent Map(Schema.TickEventForBot source)
    {
      return new TickEvent(
        source.TurnNumber,
        source.RoundNumber,
        BotStateMapper.Map(source.BotState),
        BulletStateMapper.Map(source.BulletStates),
        Map(source.Events)
      );
    }

    private static HashSet<Event> Map(IEnumerable<Schema.Event> source)
    {
      var gameEvents = new HashSet<Event>();
      foreach (var evt in source)
      {
        gameEvents.Add(Map(evt));
      }
      return gameEvents;
    }

    public static Event Map(Schema.Event source)
    {
      switch (source)
      {
        case Schema.BotDeathEvent botDeathEvent:
          return Map(botDeathEvent);
        case Schema.BotHitBotEvent hitBotEvent:
          return Map(hitBotEvent);
        case Schema.BotHitWallEvent botHitWallEvent:
          return Map(botHitWallEvent);
        case Schema.BulletFiredEvent bulletFiredEvent:
          return Map(bulletFiredEvent);
        case Schema.BulletHitBotEvent bulletHitBotEvent:
          return Map(bulletHitBotEvent);
        case Schema.BulletHitBulletEvent bulletHitBulletEvent:
          return Map(bulletHitBulletEvent);
        case Schema.BulletHitWallEvent bulletHitWallEvent:
          return Map(bulletHitWallEvent);
        case Schema.ScannedBotEvent scannedBotEvent:
          return Map(scannedBotEvent);
        case Schema.SkippedTurnEvent skippedTurnEvent:
          return Map(skippedTurnEvent);
        case Schema.WonRoundEvent wonRoundEvent:
          return Map(wonRoundEvent);
        default:
          throw new BotException("No mapping exists for event type: " + source.GetType().Name);
      }
    }

    private static BotDeathEvent Map(Schema.BotDeathEvent source)
    {
      return new BotDeathEvent(
        source.TurnNumber,
        source.VictimId
      );
    }

    private static BotHitBotEvent Map(Schema.BotHitBotEvent source)
    {
      return new BotHitBotEvent(
        source.TurnNumber,
        source.BotId,
        source.VictimId,
        source.Energy,
        source.X,
        source.Y,
        source.Rammed
      );
    }

    private static BotHitWallEvent Map(Schema.BotHitWallEvent source)
    {
      return new BotHitWallEvent(
        source.TurnNumber,
        source.VictimId
      );
    }

    private static BulletFiredEvent Map(Schema.BulletFiredEvent source)
    {
      return new BulletFiredEvent(
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet)
      );
    }

    private static BulletHitBotEvent Map(Schema.BulletHitBotEvent source)
    {
      return new BulletHitBotEvent(
        source.TurnNumber,
        source.VictimId,
        BulletStateMapper.Map(source.Bullet),
        source.Damage,
        source.Energy
      );
    }

    private static BulletHitBulletEvent Map(Schema.BulletHitBulletEvent source)
    {
      return new BulletHitBulletEvent(
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet),
        BulletStateMapper.Map(source.HitBullet)
      );
    }

    private static BulletHitWallEvent Map(Schema.BulletHitWallEvent source)
    {
      return new BulletHitWallEvent(
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet)
      );
    }

    private static ScannedBotEvent Map(Schema.ScannedBotEvent source)
    {
      return new ScannedBotEvent(
        source.TurnNumber,
        source.ScannedByBotId,
        source.ScannedBotId,
        source.Energy,
        source.X,
        source.Y,
        source.Direction,
        source.Speed
      );
    }

    private static SkippedTurnEvent Map(Schema.SkippedTurnEvent source)
    {
      return new SkippedTurnEvent(
        source.TurnNumber
      );
    }

    private static WonRoundEvent Map(Schema.WonRoundEvent source)
    {
      return new WonRoundEvent(
        source.TurnNumber
      );
    }
  }
}