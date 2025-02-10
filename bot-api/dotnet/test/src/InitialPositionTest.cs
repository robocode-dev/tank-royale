using NUnit.Framework;

namespace Robocode.TankRoyale.BotApi.Tests;

public class InitialPositionTest
{
    private const double Tolerance = 0.000000000000005;

    [Test]
    [TestCase("50, 50, 90", 50.0, 50.0, 90.0)]
    [TestCase("12.23, -123.3, 45.5", 12.23, -123.3, 45.5)]
    [TestCase(" 50 ", 50.0, null, null)]
    [TestCase(" 50.1  70.2 ", 50.1, 70.2, null)]
    [TestCase("50.1 70.2, 678.3", 50.1, 70.2, 678.3)]
    [TestCase("50.1  , 70.2, 678.3", 50.1, 70.2, 678.3)]
    [TestCase("50.1 70.2, 678.3 789.1", 50.1, 70.2, 678.3)]
    [TestCase("50.1  , , 678.3", 50.1, null, 678.3)]
    [TestCase(", , 678.3", null, null, 678.3)]
    public void GivenValidInputString_whenCallingFromString_thenReturnedPositionFieldsMustBeSame(string str, double? x,
        double? y, double? angle)
    {
        var pos = InitialPosition.FromString(str);
        Assert.That(pos.X, Is.EqualTo(x).Within(Tolerance));
        Assert.That(pos.Y, Is.EqualTo(y).Within(Tolerance));
        Assert.That(pos.Direction, Is.EqualTo(angle).Within(Tolerance));
    }

    [Test]
    [TestCase("")]
    [TestCase(" \t")]
    [TestCase("  ")]
    [TestCase(",,,")]
    [TestCase(", ,")]
    public void GivenEmptyOrBlankInputString_whenCallingFromString_thenReturnNull(string str)
    {
        var pos = InitialPosition.FromString(str);
        Assert.That(pos, Is.Null);
    }

    [Test]
    [TestCase("50,50, 90", "50,50,90")]
    [TestCase("12.23, -123.3, 45.5", "12.23,-123.3,45.5")]
    [TestCase(" 50 ", "50,,")]
    [TestCase(" 50.1  70.2 ", "50.1,70.2,")]
    [TestCase("50.1 70.2, 678.3", "50.1,70.2,678.3")]
    [TestCase("50.1  , 70.2, 678.3", "50.1,70.2,678.3")]
    [TestCase("50.1 70.2, 678.3 789.1", "50.1,70.2,678.3")]
    [TestCase("50.1  , , 678.3", "50.1,,678.3")]
    [TestCase(", , 678.3", ",,678.3")]
    [TestCase("", "")]
    [TestCase(" \t", "")]
    [TestCase(" ,", "")]
    [TestCase(",,,", "")]
    [TestCase(", ,", "")]
    public void GivenValidInputString_whenCallingToString_thenReturnedStringIsFormattedAndMatchesInputString(
        string input, string expected)
    {
        var pos = InitialPosition.FromString(input);
        if (pos == null)
        {
            Assert.That(expected, Is.Empty);
        }
        else
        {
            Assert.That(pos.ToString(), Is.EqualTo(expected));
        }
    }
}