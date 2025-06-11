namespace Robocode.TankRoyale.BotApi.Tests;

using NUnit.Framework;

[TestFixture]
public class ColorTest
{
    [Test]
    public void TestColorConstruction()
    {
        // Test FromArgb with uint argb parameter
        var color1 = Color.FromArgb(0xFFAABBCC);
        Assert.That(color1.A, Is.EqualTo(255));
        Assert.That(color1.R, Is.EqualTo(170));
        Assert.That(color1.G, Is.EqualTo(187));
        Assert.That(color1.B, Is.EqualTo(204));

        // Test FromArgb with separate components
        var color2 = Color.FromArgb(128, 255, 0, 0); // Semi-transparent red
        Assert.That(color2.A, Is.EqualTo(128));
        Assert.That(color2.R, Is.EqualTo(255));
        Assert.That(color2.G, Is.EqualTo(0));
        Assert.That(color2.B, Is.EqualTo(0));

        // Test FromArgb with RGB (fully opaque)
        var color3 = Color.FromArgb(100, 150, 200);
        Assert.That(color3.A, Is.EqualTo(255)); // Default alpha
        Assert.That(color3.R, Is.EqualTo(100));
        Assert.That(color3.G, Is.EqualTo(150));
        Assert.That(color3.B, Is.EqualTo(200));

        // Test FromArgb with alpha and base color
        var baseColor = Color.Blue;
        var color4 = Color.FromArgb(64, baseColor);
        Assert.That(color4.A, Is.EqualTo(64));
        Assert.That(color4.R, Is.EqualTo(0));
        Assert.That(color4.G, Is.EqualTo(0));
        Assert.That(color4.B, Is.EqualTo(255));
    }

    [Test]
    public void TestToArgb()
    {
        var color = Color.FromArgb(128, 255, 0, 0); // Semi-transparent red
        int argb = color.ToArgb();
        Assert.That(argb, Is.EqualTo(unchecked((int)0x80FF0000)));
    }

    [Test]
    public void TestPredefinedColors()
    {
        // Test a few predefined colors
        Assert.That(Color.Red.R, Is.EqualTo(255));
        Assert.That(Color.Red.G, Is.EqualTo(0));
        Assert.That(Color.Red.B, Is.EqualTo(0));
        Assert.That(Color.Red.A, Is.EqualTo(255));

        Assert.That(Color.Transparent.A, Is.EqualTo(0));

        Assert.That(Color.Blue.B, Is.EqualTo(255));
        Assert.That(Color.Green.G, Is.EqualTo(128)); // Note: Green is (0,128,0) not (0,255,0)
        Assert.That(Color.Lime.G, Is.EqualTo(255)); // Lime is (0,255,0)
    }

    [Test]
    public void TestEquality()
    {
        var color1 = Color.FromArgb(255, 100, 150, 200);
        var color2 = Color.FromArgb(255, 100, 150, 200);
        var color3 = Color.FromArgb(128, 100, 150, 200);

        Assert.That(color1, Is.EqualTo(color2));
        Assert.That(color1 == color2, Is.True);
        Assert.That(color1 != color3, Is.True);
        Assert.That(color1.Equals((object)color2), Is.True);

        // Test hash code
        Assert.That(color1.GetHashCode(), Is.EqualTo(color2.GetHashCode()));
        Assert.That(color1.GetHashCode(), Is.Not.EqualTo(color3.GetHashCode()));
    }

    [Test]
    public void TestToString()
    {
        var opaqueColor = Color.FromArgb(255, 100, 150, 200);
        var transparentColor = Color.FromArgb(128, 100, 150, 200);

        Assert.That(opaqueColor.ToString(), Is.EqualTo("Color [R=100, G=150, B=200]"));
        Assert.That(transparentColor.ToString(), Is.EqualTo("Color [A=128, R=100, G=150, B=200]"));
    }

    [Test]
    public void TestToHexColor()
    {
        var opaqueColor = Color.FromArgb(255, 100, 150, 200);
        var transparentColor = Color.FromArgb(128, 100, 150, 200);

        Assert.That(opaqueColor.ToHexColor(), Is.EqualTo("#6496C8"));
        Assert.That(transparentColor.ToHexColor(), Is.EqualTo("#6496C880"));
    }

    [Test]
    public void TestImplicitConversions()
    {
        // Test uint to Color conversion
        uint argb = 0xFF112233;
        Color color = argb;
        Assert.That(color.A, Is.EqualTo(255));
        Assert.That(color.R, Is.EqualTo(17));
        Assert.That(color.G, Is.EqualTo(34));
        Assert.That(color.B, Is.EqualTo(51));

        // Test Color to int conversion
        Color testColor = Color.FromArgb(128, 255, 0, 0);
        int colorInt = testColor;
        Assert.That(colorInt, Is.EqualTo(unchecked((int)0x80FF0000)));
    }
}
