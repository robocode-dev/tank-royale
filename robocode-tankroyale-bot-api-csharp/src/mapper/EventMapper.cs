using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi
{
  public sealed class EventMapper
  {
    public static TickEvent Map(string json)
    {
      var source = JsonConvert.DeserializeObject<Schema.TickEventForBot>(json);

      var jsonTickEvent = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
      JArray events = (JArray)jsonTickEvent["events"];

      return new TickEvent(
        source.TurnNumber,
        source.RoundNumber,
        source.EnemyCount,
        BotStateMapper.Map(source.BotState),
        BulletStateMapper.Map(source.BulletStates),
        Map(events)
      );
    }

    private static HashSet<BotEvent> Map(JArray events)
    {
      var gameEvents = new HashSet<BotEvent>();
      foreach (JObject evt in events)
      {
        gameEvents.Add(Map(evt));
      }
      return gameEvents;
    }

    public static BotEvent Map(JObject evt)
    {
      string type = evt.GetValue("$type").ToString();

      switch (type)
      {
        case "DeathEvent":
          return Map(evt.ToObject<Schema.BotDeathEvent>());
        case "BotHitBotEvent":
          return Map(evt.ToObject<Schema.BotHitBotEvent>());
        case "BotHitWallEvent":
          return Map(evt.ToObject<Schema.BotHitWallEvent>());
        case "BulletFiredEvent":
          return Map(evt.ToObject<Schema.BulletFiredEvent>());
        case "BulletHitBotEvent":
          return Map(evt.ToObject<Schema.BulletHitBotEvent>());
        case "BulletHitBulletEvent":
          return Map(evt.ToObject<Schema.BulletHitBulletEvent>());
        case "BulletHitWallEvent":
          return Map(evt.ToObject<Schema.BulletHitWallEvent>());
        case "ScannedBotEvent":
          return Map(evt.ToObject<Schema.ScannedBotEvent>());
        case "SkippedTurnEvent":
          return Map(evt.ToObject<Schema.SkippedTurnEvent>());
        case "WonRoundEvent":
          return Map(evt.ToObject<Schema.WonRoundEvent>());
        default:
          throw new BotException("No mapping exists for event type: " + type);
      }
    }

    private static DeathEvent Map(Schema.BotDeathEvent source)
    {
      return new DeathEvent(
        source.TurnNumber,
        source.VictimId
      );
    }

    private static HitBotEvent Map(Schema.BotHitBotEvent source)
    {
      return new HitBotEvent(
        source.TurnNumber,
        source.VictimId,
        source.Energy,
        source.X,
        source.Y,
        source.Rammed
      );
    }

    private static HitWallEvent Map(Schema.BotHitWallEvent source)
    {
      return new HitWallEvent(source.TurnNumber);
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

    public static SkippedTurnEvent Map(Schema.SkippedTurnEvent source)
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