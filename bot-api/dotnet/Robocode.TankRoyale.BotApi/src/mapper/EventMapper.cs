using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Mapper;

public static class EventMapper
{
    public static TickEvent Map(string json, IBaseBot baseBot)
    {
        var tickEvent = JsonConvert.DeserializeObject<Schema.Game.TickEventForBot>(json);
        if (tickEvent == null)
            throw new BotException("TickEventForBot is missing in JSON message from server");

        var jsonTickEvent = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
        if (jsonTickEvent == null)
            throw new BotException("TickEventForBot dictionary is missing in JSON message from server");

        var events = (JArray)jsonTickEvent["events"];
    
        return new TickEvent
        (
            tickEvent.TurnNumber,
            tickEvent.RoundNumber,
            BotStateMapper.Map(tickEvent.BotState),
            BulletStateMapper.Map(tickEvent.BulletStates),
            Map(events, baseBot)
        );
    }

    private static IEnumerable<BotEvent> Map(JArray events, IBaseBot baseBot)
    {
        var gameEvents = new HashSet<BotEvent>();
        foreach (var jEvent in events)
        {
            var evt = (JObject)jEvent;
            gameEvents.Add(Map(evt, baseBot));
        }

        return gameEvents;
    }

    private static BotEvent Map(JObject evt, IBaseBot baseBot)
    {
        var type = evt.GetValue("type")?.ToString();

        return type switch
        {
            "BotDeathEvent" => Map(evt.ToObject<Schema.Game.BotDeathEvent>(), baseBot.MyId),
            "BotHitBotEvent" => Map(evt.ToObject<Schema.Game.BotHitBotEvent>()),
            "BotHitWallEvent" => Map(evt.ToObject<Schema.Game.BotHitWallEvent>()),
            "BulletFiredEvent" => Map(evt.ToObject<Schema.Game.BulletFiredEvent>()),
            "BulletHitBotEvent" => Map(evt.ToObject<Schema.Game.BulletHitBotEvent>(), baseBot.MyId),
            "BulletHitBulletEvent" => Map(evt.ToObject<Schema.Game.BulletHitBulletEvent>()),
            "BulletHitWallEvent" => Map(evt.ToObject<Schema.Game.BulletHitWallEvent>()),
            "ScannedBotEvent" => Map(evt.ToObject<Schema.Game.ScannedBotEvent>()),
            "SkippedTurnEvent" => Map(evt.ToObject<Schema.Game.SkippedTurnEvent>()),
            "WonRoundEvent" => Map(evt.ToObject<Schema.Game.WonRoundEvent>()),
            "TeamMessageEvent" => Map(evt.ToObject<Schema.Game.TeamMessageEvent>(), baseBot),
            _ => throw new BotException("No mapping exists for event type: " + type)
        };
    }

    private static BotEvent Map(Schema.Game.BotDeathEvent source, int myBotId) => source.VictimId == myBotId
        ? new DeathEvent(source.TurnNumber)
        : new BotDeathEvent(source.TurnNumber, source.VictimId);

    private static HitBotEvent Map(Schema.Game.BotHitBotEvent source) => new
    (
        source.TurnNumber,
        source.VictimId,
        source.Energy,
        source.X,
        source.Y,
        source.Rammed
    );

    private static HitWallEvent Map(Schema.Game.BotHitWallEvent source) => new(source.TurnNumber);

    private static BulletFiredEvent Map(Schema.Game.BulletFiredEvent source) => new
    (
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet)
    );

    private static BotEvent Map(Schema.Game.BulletHitBotEvent source, int myBotId)
    {
        var bullet = BulletStateMapper.Map(source.Bullet);
        if (source.VictimId == myBotId)
        {
            return new HitByBulletEvent(
                source.TurnNumber,
                bullet,
                source.Damage,
                source.Energy);
        }

        return new BulletHitBotEvent(
            source.TurnNumber,
            source.VictimId,
            bullet,
            source.Damage,
            source.Energy);
    }

    private static BulletHitBulletEvent Map(Schema.Game.BulletHitBulletEvent source) => new
    (
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet),
        BulletStateMapper.Map(source.HitBullet)
    );


    private static BulletHitWallEvent Map(Schema.Game.BulletHitWallEvent source) => new
    (
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet)
    );

    private static ScannedBotEvent Map(Schema.Game.ScannedBotEvent source) => new
    (
        source.TurnNumber,
        source.ScannedByBotId,
        source.ScannedBotId,
        source.Energy,
        source.X,
        source.Y,
        source.Direction,
        source.Speed
    );

    public static SkippedTurnEvent Map(Schema.Game.SkippedTurnEvent source) => new(source.TurnNumber);

    private static WonRoundEvent Map(Schema.Game.WonRoundEvent source) => new(source.TurnNumber);

    private static TeamMessageEvent Map(Schema.Game.TeamMessageEvent source, IBaseBot baseBot)
    {
        try
        {
            // type name of the base bot is required for the DLL name.
            // Otherwise, null is will be returned with Type.GetType(typeName).
            var typeName = source.MessageType + "," + baseBot.GetType().Name;
            var type = Type.GetType(typeName)!;

            var bytes = Convert.FromBase64String(source.Message);
            var decodedString = System.Text.Encoding.UTF8.GetString(bytes);

            var messageObject = JsonConvert.DeserializeObject(decodedString, type);

            return new TeamMessageEvent(
                source.TurnNumber,
                messageObject,
                source.SenderId
            );
        }
        catch (Exception e)
        {
            throw new BotException("Could not parse team message", e);
        }
    }
}