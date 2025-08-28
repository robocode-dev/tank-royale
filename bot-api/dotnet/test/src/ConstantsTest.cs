using NUnit.Framework;
using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
public class ConstantsTest
{
    private const double Eps = 1e-10;

    [Test]
    public void GivenDefinedConstants_whenChecked_thenValuesMatchSpec()
    {
        Assert.That(Constants.BoundingCircleRadius, Is.EqualTo(18));
        Assert.That(Constants.ScanRadius, Is.EqualTo(1200));
        Assert.That(Constants.MaxTurnRate, Is.EqualTo(10));
        Assert.That(Constants.MaxGunTurnRate, Is.EqualTo(20));
        Assert.That(Constants.MaxRadarTurnRate, Is.EqualTo(45));
        Assert.That(Constants.MaxSpeed, Is.EqualTo(8));
        Assert.That(Constants.MaxForwardSpeed, Is.EqualTo(8));
        Assert.That(Constants.MaxBackwardSpeed, Is.EqualTo(-8));

        Assert.That(Constants.MinFirepower, Is.EqualTo(0.1).Within(Eps));
        Assert.That(Constants.MaxFirepower, Is.EqualTo(3.0).Within(Eps));

        Assert.That(Constants.MinBulletSpeed, Is.EqualTo(20 - 3 * Constants.MaxFirepower).Within(Eps));
        Assert.That(Constants.MinBulletSpeed, Is.EqualTo(11.0).Within(Eps));
        Assert.That(Constants.MaxBulletSpeed, Is.EqualTo(20 - 3 * Constants.MinFirepower).Within(Eps));
        Assert.That(Constants.MaxBulletSpeed, Is.EqualTo(19.7).Within(Eps));

        Assert.That(Constants.Acceleration, Is.EqualTo(1));
        Assert.That(Constants.Deceleration, Is.EqualTo(-2));
    }
}
