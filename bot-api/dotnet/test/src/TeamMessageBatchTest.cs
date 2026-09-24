using NUnit.Framework;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Internal;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
[Category("TR-API-TCK-022")]
public class TeamMessageBatchTest
{
    [Test]
    public void BatchPayloadKeepsOrderAndUsesReservedMarker()
    {
        var batch = new TeamMessageBatch(new[] { "first", "second" });
        var payload = TeamMessageBatchCodec.Encode(batch);
        var items = TeamMessageBatchCodec.DecodeItems(payload);
        Assert.That(items.Count, Is.EqualTo(2));
        Assert.That((string)items[0]["message"], Is.EqualTo("\"first\""));
        Assert.That((string)items[1]["message"], Is.EqualTo("\"second\""));
        Assert.That(TeamMessageBatchCodec.MessageType, Is.EqualTo("team-message-batch-v1"));
    }

    [Test]
    public void BatchRequiresAtLeastOneNonNullPayload()
    {
        Assert.Throws<System.ArgumentException>(() => new TeamMessageBatch(System.Array.Empty<object>()));
        Assert.Throws<System.ArgumentException>(() => new TeamMessageBatch(new object[] { "ok", null }));
    }

    [Test]
    public void LogicalItemLimitAccepts128AndRejects129()
    {
        Assert.DoesNotThrow(() => IntentValidator.ValidateLogicalTeamMessageCount(128));
        Assert.Throws<BotException>(() => IntentValidator.ValidateLogicalTeamMessageCount(129));
    }
}
