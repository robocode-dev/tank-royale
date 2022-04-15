using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Mapper
{
    public sealed class EventMapper
    {
        public static TickEvent Map(string json)
        {
            var tickEvent = JsonConvert.DeserializeObject<Schema.TickEventForBot>(json);
            if (tickEvent == null)
                throw new BotException("TickEventForBot is missing in JSON message from server");

            var jsonTickEvent = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
            if (jsonTickEvent == null)
                throw new BotException("TickEventForBot dictionary is missing in JSON message from server");

            var events = (JArray) jsonTickEvent["events"];
            return new TickEvent(
                tickEvent.TurnNumber,
                tickEvent.RoundNumber,
                tickEvent.EnemyCount,
                BotStateMapper.Map(tickEvent.BotState),
                BulletStateMapper.Map(tickEvent.BulletStates),
                Map(events)
            );
        }

        private static IEnumerable<BotEvent> Map(JArray events)
        {
            var gameEvents = new HashSet<BotEvent>();
            foreach (var jEvent in events)
            {
                var evt = (JObject) jEvent;
                gameEvents.Add(Map(evt));
            }

            return gameEvents;
        }

        public static BotEvent Map(JObject evt)
        {
            var type = evt.GetValue("$type")?.ToString();

            return type switch
            {
                "BotDeathEvent" => Map(evt.ToObject<Schema.BotDeathEvent>()),
                "BotHitBotEvent" => Map(evt.ToObject<Schema.BotHitBotEvent>()),
                "BotHitWallEvent" => Map(evt.ToObject<Schema.BotHitWallEvent>()),
                "BulletFiredEvent" => Map(evt.ToObject<Schema.BulletFiredEvent>()),
                "BulletHitBotEvent" => Map(evt.ToObject<Schema.BulletHitBotEvent>()),
                "BulletHitBulletEvent" => Map(evt.ToObject<Schema.BulletHitBulletEvent>()),
                "BulletHitWallEvent" => Map(evt.ToObject<Schema.BulletHitWallEvent>()),
                "ScannedBotEvent" => Map(evt.ToObject<Schema.ScannedBotEvent>()),
                "SkippedTurnEvent" => Map(evt.ToObject<Schema.SkippedTurnEvent>()),
                "WonRoundEvent" => Map(evt.ToObject<Schema.WonRoundEvent>()),
                _ => throw new BotException("No mapping exists for event type: " + type)
            };
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