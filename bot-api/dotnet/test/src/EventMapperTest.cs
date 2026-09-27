using System.Collections.Generic;
using System.Linq;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Mapper;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
[Category("Unit")]
public class EventMapperTest
{
    [Test]
    [Property("ID", "TR-API-EVT-015")]
    public void PreservesOrderedDuplicateTeamMessagesFromATick()
    {
        const string eventJson = "{\"type\":\"TickEventForBot\",\"roundNumber\":1,\"turnNumber\":7,"
            + "\"botState\":{\"isDroid\":false,\"energy\":100,\"x\":0,\"y\":0,\"direction\":0,\"gunDirection\":0,\"radarDirection\":0,\"radarSweep\":0,\"speed\":0,\"turnRate\":0,\"gunTurnRate\":0,\"radarTurnRate\":0,\"gunHeat\":0,\"enemyCount\":0,\"isDebuggingEnabled\":false},"
            + "\"bulletStates\":[],\"events\":["
            + "{\"type\":\"TeamMessageEvent\",\"turnNumber\":7,\"message\":\"\\\"first\\\"\",\"messageType\":\"System.String\",\"senderId\":3},"
            + "{\"type\":\"TeamMessageEvent\",\"turnNumber\":7,\"message\":\"\\\"first\\\"\",\"messageType\":\"System.String\",\"senderId\":3},"
            + "{\"type\":\"TeamMessageEvent\",\"turnNumber\":7,\"message\":\"\\\"last\\\"\",\"messageType\":\"System.String\",\"senderId\":3}]}";

        var mapped = EventMapper.Map(eventJson, new TestBot());

        Assert.That(mapped.Events.OfType<TeamMessageEvent>().Select(e => e.Message), Is.EqualTo(new[] { "first", "first", "last" }));
    }

    private sealed class TestBot : BaseBot
    {
        internal TestBot() : base(new BotInfo("EventMapperTest", "1.0", new List<string> { "Test" }, null, null, null, new HashSet<string> { "classic" }, null, null, null))
        {
        }
    }
}
