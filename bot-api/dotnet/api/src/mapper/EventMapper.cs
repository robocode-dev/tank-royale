using System;
using System.Collections.Generic;
using System.Linq;
using Robocode.TankRoyale.BotApi.Events;
using Newtonsoft.Json.Linq;
using Robocode.TankRoyale.BotApi.Internal.Json;

namespace Robocode.TankRoyale.BotApi.Mapper;

static class EventMapper
{
    internal static TickEvent Map(string json, IBaseBot baseBot)
    {
        var tickEvent = JsonConverter.FromJson<Schema.TickEventForBot>(json);
        if (tickEvent == null)
            throw new BotException("TickEventForBot is missing in JSON message from server");

        var jsonTickEvent = JsonConverter.FromJson<Dictionary<string, object>>(json);
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
            "BotDeathEvent" => Map(evt.ToObject<Schema.BotDeathEvent>(), baseBot.MyId),
            "BotHitBotEvent" => Map(evt.ToObject<Schema.BotHitBotEvent>()),
            "BotHitWallEvent" => Map(evt.ToObject<Schema.BotHitWallEvent>()),
            "BulletFiredEvent" => Map(evt.ToObject<Schema.BulletFiredEvent>()),
            "BulletHitBotEvent" => Map(evt.ToObject<Schema.BulletHitBotEvent>(), baseBot.MyId),
            "BulletHitBulletEvent" => Map(evt.ToObject<Schema.BulletHitBulletEvent>()),
            "BulletHitWallEvent" => Map(evt.ToObject<Schema.BulletHitWallEvent>()),
            "ScannedBotEvent" => Map(evt.ToObject<Schema.ScannedBotEvent>()),
            "SkippedTurnEvent" => Map(evt.ToObject<Schema.SkippedTurnEvent>()),
            "WonRoundEvent" => Map(evt.ToObject<Schema.WonRoundEvent>()),
            "TeamMessageEvent" => Map(evt.ToObject<Schema.TeamMessageEvent>(), baseBot),
            _ => throw new BotException("No mapping exists for event type: " + type)
        };
    }

    private static BotEvent Map(Schema.BotDeathEvent source, int myBotId) => source.VictimId == myBotId
        ? new DeathEvent(source.TurnNumber)
        : new BotDeathEvent(source.TurnNumber, source.VictimId);

    private static HitBotEvent Map(Schema.BotHitBotEvent source) => new
    (
        source.TurnNumber,
        source.VictimId,
        source.Energy,
        source.X,
        source.Y,
        source.Rammed
    );

    private static HitWallEvent Map(Schema.BotHitWallEvent source) => new(source.TurnNumber);

    private static BulletFiredEvent Map(Schema.BulletFiredEvent source) => new
    (
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet)
    );

    private static BotEvent Map(Schema.BulletHitBotEvent source, int myBotId)
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

    private static BulletHitBulletEvent Map(Schema.BulletHitBulletEvent source) => new
    (
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet),
        BulletStateMapper.Map(source.HitBullet)
    );


    private static BulletHitWallEvent Map(Schema.BulletHitWallEvent source) => new
    (
        source.TurnNumber,
        BulletStateMapper.Map(source.Bullet)
    );

    private static ScannedBotEvent Map(Schema.ScannedBotEvent source) => new
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

    public static SkippedTurnEvent Map(Schema.SkippedTurnEvent source) => new(source.TurnNumber);

    private static WonRoundEvent Map(Schema.WonRoundEvent source) => new(source.TurnNumber);

    private static TeamMessageEvent Map(Schema.TeamMessageEvent source, IBaseBot baseBot)
    {
        try
        {
            // Load the message type from the RECEIVING bot's assembly, not the sender's.
            // This allows each bot to have its own version of the message class.
            // For example, if MyFirstLeader sends a "RobotColors" message to MyFirstDroid,
            // the message is serialized to JSON on the leader side, sent with the type name "RobotColors",
            // then deserialized into MyFirstDroid's own "RobotColors" class definition.
            // This is similar to how Java's ClassLoader.loadClass() works.
            var botAssembly = baseBot.GetType().Assembly;
            Type? type = null;

            // Strategy 1: Try to find the type directly in the bot's assembly
            // This works for types in the global namespace or with full namespace
            type = botAssembly.GetType(source.MessageType);

            // Strategy 2: Search all types in the bot's assembly by name
            // This handles cases where the simple name matches but namespace differs
            if (type == null)
            {
                var messageTypeName = source.MessageType;
                // Extract just the class name if it contains namespace or assembly info
                var lastDotIndex = messageTypeName.LastIndexOf('.');
                var simpleTypeName = lastDotIndex >= 0 ? messageTypeName.Substring(lastDotIndex + 1) : messageTypeName;

                foreach (var t in botAssembly.GetTypes())
                {
                    if (t.Name == simpleTypeName || t.FullName == messageTypeName)
                    {
                        type = t;
                        break;
                    }
                }
            }

            // Strategy 3: Try with assembly-qualified name
            if (type == null)
            {
                var typeName = source.MessageType + "," + botAssembly.GetName().Name;
                type = Type.GetType(typeName);
            }

            // Strategy 4: Try across all loaded assemblies
            if (type == null)
            {
                type = Type.GetType(source.MessageType);
            }

            if (type == null)
            {
                throw new BotException($"Could not find type '{source.MessageType}' in bot assembly '{botAssembly.GetName().Name}' or other loaded assemblies");
            }

            var messageObject = JsonConverter.FromJson(source.Message, type);

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
