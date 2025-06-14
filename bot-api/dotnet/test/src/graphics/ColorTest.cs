using Robocode.TankRoyale.BotApi.Graphics;

namespace Robocode.TankRoyale.BotApi.Tests.Graphics;

using NUnit.Framework;

[TestFixture]
public class ColorTest
{
    [Test]
    public void TestColorConstruction()
    {
        // Test FromRgba with uint rgba parameter
        var color1 = Color.FromRgba(0xAABBCCFF);
        Assert.That(color1.R, Is.EqualTo(170));
        Assert.That(color1.G, Is.EqualTo(187));
        Assert.That(color1.B, Is.EqualTo(204));
        Assert.That(color1.A, Is.EqualTo(255));

        // Test FromRgba with separate components
        var color2 = Color.FromRgba(255, 0, 0, 128); // Semi-transparent red
        Assert.That(color2.R, Is.EqualTo(255));
        Assert.That(color2.G, Is.EqualTo(0));
        Assert.That(color2.B, Is.EqualTo(0));
        Assert.That(color2.A, Is.EqualTo(128));

        // Test FromRgba with RGB (fully opaque)
        var color3 = Color.FromRgb(100, 150, 200);
        Assert.That(color3.R, Is.EqualTo(100));
        Assert.That(color3.G, Is.EqualTo(150));
        Assert.That(color3.B, Is.EqualTo(200));
        Assert.That(color3.A, Is.EqualTo(255)); // Default alpha

        // Test FromRgba with base color and alpha
        var baseColor = Color.Blue;
        var color4 = Color.FromRgba(baseColor, 64);
        Assert.That(color4.R, Is.EqualTo(0));
        Assert.That(color4.G, Is.EqualTo(0));
        Assert.That(color4.B, Is.EqualTo(255));
        Assert.That(color4.A, Is.EqualTo(64));
    }

    [Test]
    public void TestToRgba()
    {
        var color = Color.FromRgba(255, 0, 0, 128); // Semi-transparent red
        uint rgba = color.ToRgba();
        Assert.That(rgba, Is.EqualTo(0xFF000080));
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
        var color1 = Color.FromRgb(100, 150, 200);
        var color2 = Color.FromRgb(100, 150, 200);
        var color3 = Color.FromRgba(100, 150, 200, 128);

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
        var opaqueColor = Color.FromRgb(100, 150, 200);
        var transparentColor = Color.FromRgba(100, 150, 200, 128);

        Assert.That(opaqueColor.ToString(), Is.EqualTo("RGB[100, 150, 200]"));
        Assert.That(transparentColor.ToString(), Is.EqualTo("RGBA[128, 100, 150, 200]"));
    }

    [Test]
    public void TestToHexColor()
    {
        var opaqueColor = Color.FromRgb(100, 150, 200);
        var transparentColor = Color.FromRgba(100, 150, 200, 128);

        Assert.That(opaqueColor.ToHexColor(), Is.EqualTo("#6496C8"));
        Assert.That(transparentColor.ToHexColor(), Is.EqualTo("#6496C880"));
    }

    [Test]
    public void TestImplicitConversions()
    {
        // Test uint to Color conversion
        uint rgba = 0x112233FF;
        Color color = rgba;
        Assert.That(color.R, Is.EqualTo(17));
        Assert.That(color.G, Is.EqualTo(34));
        Assert.That(color.B, Is.EqualTo(51));
        Assert.That(color.A, Is.EqualTo(255));

        // Test Color to int conversion
        Color testColor = Color.FromRgba(255, 0, 0, 128);
        uint colorInt = testColor;
        Assert.That(colorInt, Is.EqualTo(0xFF000080));
    }
}
