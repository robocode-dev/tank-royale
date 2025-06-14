namespace Robocode.TankRoyale.BotApi.Tests.Graphics;

using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Graphics;

[TestFixture]
public class PointTest
{
    private const double Epsilon = 1e-10; // Same epsilon used in Point.cs

    [Test]
    public void TestPointConstruction()
    {
        var point = new Point(10.5, 20.5);
        Assert.That(point.X, Is.EqualTo(10.5));
        Assert.That(point.Y, Is.EqualTo(20.5));

        // Test default values for struct
        var defaultPoint = default(Point);
        Assert.That(defaultPoint.X, Is.EqualTo(0.0));
        Assert.That(defaultPoint.Y, Is.EqualTo(0.0));
    }

    [Test]
    public void TestEquality()
    {
        var point1 = new Point(10.5, 20.5);
        var point2 = new Point(10.5, 20.5);
        var point3 = new Point(10.5, 20.6);
        var point4 = new Point(10.6, 20.5);

        // Test equality
        Assert.That(point1, Is.EqualTo(point2));
        Assert.That(point1 == point2, Is.True);
        Assert.That(point1 != point3, Is.True);
        Assert.That(point1 != point4, Is.True);
        Assert.That(point1.Equals((object)point2), Is.True);
        Assert.That(point1.Equals("not a point"), Is.False);
        Assert.That(point1.Equals(null), Is.False);

        // Test floating-point comparison tolerance
        var point5 = new Point(10.5 + Epsilon / 2, 20.5);
        Assert.That(point1, Is.EqualTo(point5), "Points should be equal within epsilon");

        var point6 = new Point(10.5 + Epsilon * 2, 20.5);
        Assert.That(point1, Is.Not.EqualTo(point6), "Points should not be equal outside epsilon");
    }

    [Test]
    public void TestHashCode()
    {
        var point1 = new Point(10.5, 20.5);
        var point2 = new Point(10.5, 20.5);
        var point3 = new Point(20.5, 10.5);

        // Same points should have same hash code
        Assert.That(point1.GetHashCode(), Is.EqualTo(point2.GetHashCode()));

        // Different points should have different hash codes
        Assert.That(point1.GetHashCode(), Is.Not.EqualTo(point3.GetHashCode()));
    }

    [Test]
    public void TestToString()
    {
        var point = new Point(10.5, 20.5);
        Assert.That(point.ToString(), Is.EqualTo("(10.5, 20.5)"));

        var negativePoint = new Point(-10.5, -20.5);
        Assert.That(negativePoint.ToString(), Is.EqualTo("(-10.5, -20.5)"));
    }

    [Test]
    public void TestOperatorEquality()
    {
        var point1 = new Point(10.5, 20.5);
        var point2 = new Point(10.5, 20.5);
        var point3 = new Point(10.5, 20.6);

        Assert.That(point1 == point2, Is.True);
        Assert.That(point1 != point3, Is.True);
        Assert.That(point1 == point3, Is.False);
        Assert.That(point1 != point2, Is.False);
    }

    [Test]
    public void TestWithDifferentEpsilons()
    {
        // This test verifies the epsilon comparison behavior
        var basePoint = new Point(10.0, 20.0);

        // Should be equal (difference < epsilon)
        var slightlyDifferentPoint = new Point(10.0 + Epsilon / 2, 20.0 + Epsilon / 2);
        Assert.That(basePoint.Equals(slightlyDifferentPoint), Is.True);

        // Should be different (difference > epsilon)
        var moreDifferentPoint = new Point(10.0 + Epsilon * 2, 20.0);
        Assert.That(basePoint.Equals(moreDifferentPoint), Is.False);

        var evenMoreDifferentPoint = new Point(10.0, 20.0 + Epsilon * 2);
        Assert.That(basePoint.Equals(evenMoreDifferentPoint), Is.False);
    }
}
