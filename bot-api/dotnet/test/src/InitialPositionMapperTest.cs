using NUnit.Framework;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Mapper;

namespace Robocode.TankRoyale.BotApi.Tests;

public class InitialPositionMapperTest
{
    [Test]
    [Category("VAL")]
    [Property("ID", "TR-API-VAL-004")]
    public void GivenInitialPosition_whenMapped_thenSchemaValuesArePreserved()
    {
        // Arrange
        var ip = InitialPosition.FromString("11, 22, 33");

        // Act
        var schemaIp = InitialPositionMapper.Map(ip);

        // Assert
        Assert.That(schemaIp, Is.Not.Null);
        Assert.That(schemaIp.X, Is.EqualTo(11.0));
        Assert.That(schemaIp.Y, Is.EqualTo(22.0));
        Assert.That(schemaIp.Direction, Is.EqualTo(33.0));

        // Round-trip back via values
        var ip2 = InitialPosition.FromString($"{schemaIp.X}, {schemaIp.Y}, {schemaIp.Direction}");
        Assert.That(ip2, Is.Not.Null);
        Assert.That(ip2.X, Is.EqualTo(ip.X));
        Assert.That(ip2.Y, Is.EqualTo(ip.Y));
        Assert.That(ip2.Direction, Is.EqualTo(ip.Direction));
    }

    [Test]
    [Category("VAL")]
    [Property("ID", "TR-API-VAL-004")]
    public void GivenNullOrPartialInitialPosition_whenMapped_thenNullOrPartialSchemaIsReturned()
    {
        // Null source -> null schema
        var schemaNull = InitialPositionMapper.Map(null);
        Assert.That(schemaNull, Is.Null);

        // Partial values preserved (x only)
        var ipXOnly = InitialPosition.FromString("50");
        var schemaXOnly = InitialPositionMapper.Map(ipXOnly);
        Assert.That(schemaXOnly, Is.Not.Null);
        Assert.That(schemaXOnly.X, Is.EqualTo(50.0));
        Assert.That(schemaXOnly.Y, Is.Null);
        Assert.That(schemaXOnly.Direction, Is.Null);
    }
}
