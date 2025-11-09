using System;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Internal.Json;
using Robocode.TankRoyale.BotApi.Util;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Tests.Util;

[Description("TR-API-UTL-002 JsonUtil serialization + schema compliance")]
public class JsonConverterTest
{
    [Test]
    [Category("UTL")] 
    [Property("ID", "TR-API-UTL-002")]
    public void GivenScannedBotEvent_whenSerializedAndDeserialized_thenFieldsArePreserved()
    {
        // Arrange
        var evt = new ScannedBotEvent
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.ScannedBotEvent),
            TurnNumber = 1,
            ScannedByBotId = 2,
            ScannedBotId = 3,
            Energy = 100,
            X = 10,
            Y = 20,
            Direction = 90,
            Speed = 5,
        };

        // Act
        var json = JsonConverter.ToJson(evt);
        var deserialized = JsonConverter.FromJson(json, typeof(ScannedBotEvent)) as ScannedBotEvent;

        // Assert
        Assert.That(deserialized, Is.Not.Null);
        Assert.Multiple(() =>
        {
            Assert.That(deserialized.Type, Is.EqualTo(evt.Type));
            Assert.That(deserialized.TurnNumber, Is.EqualTo(evt.TurnNumber));
            Assert.That(deserialized.ScannedByBotId, Is.EqualTo(evt.ScannedByBotId));
            Assert.That(deserialized.ScannedBotId, Is.EqualTo(evt.ScannedBotId));
            Assert.That(deserialized.Energy, Is.EqualTo(evt.Energy));
            Assert.That(deserialized.X, Is.EqualTo(evt.X));
            Assert.That(deserialized.Y, Is.EqualTo(evt.Y));
            Assert.That(deserialized.Direction, Is.EqualTo(evt.Direction));
            Assert.That(deserialized.Speed, Is.EqualTo(evt.Speed));
        });
    }

    [Test]
    [Category("UTL")] 
    [Property("ID", "TR-API-UTL-002")]
    public void GivenBulletFiredEventWithNestedBulletState_whenSerializedAndDeserialized_thenNestedFieldsArePreserved()
    {
        // Arrange
        var bullet = new global::Robocode.TankRoyale.Schema.BulletState
        {
            BulletId = 1,
            OwnerId = 2,
            Power = 2.5,
            X = 100,
            Y = 200,
            Direction = 45,
            Color = "#FF0000",
        };
        var evt = new BulletFiredEvent
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BulletFiredEvent),
            TurnNumber = 7,
            Bullet = bullet,
        };

        // Act
        var json = JsonConverter.ToJson(evt);
        var deserialized = JsonConverter.FromJson(json, typeof(BulletFiredEvent)) as BulletFiredEvent;

        // Assert
        Assert.That(deserialized, Is.Not.Null);
        Assert.That(deserialized.Bullet, Is.Not.Null);
        Assert.Multiple(() =>
        {
            Assert.That(deserialized.Type, Is.EqualTo(evt.Type));
            Assert.That(deserialized.TurnNumber, Is.EqualTo(evt.TurnNumber));
            Assert.That(deserialized.Bullet.BulletId, Is.EqualTo(bullet.BulletId));
            Assert.That(deserialized.Bullet.OwnerId, Is.EqualTo(bullet.OwnerId));
            Assert.That(deserialized.Bullet.Power, Is.EqualTo(bullet.Power));
            Assert.That(deserialized.Bullet.X, Is.EqualTo(bullet.X));
            Assert.That(deserialized.Bullet.Y, Is.EqualTo(bullet.Y));
            Assert.That(deserialized.Bullet.Direction, Is.EqualTo(bullet.Direction));
            Assert.That(deserialized.Bullet.Color, Is.EqualTo(bullet.Color));
        });
    }
}
