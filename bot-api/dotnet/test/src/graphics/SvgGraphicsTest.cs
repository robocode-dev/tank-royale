using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi.Tests.Graphics;

using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Graphics;
using System.Text.RegularExpressions;

[TestFixture]
public class SvgGraphicsTest
{
    private SvgGraphics _graphics;

    [SetUp]
    public void Setup()
    {
        _graphics = new SvgGraphics();
    }

    [Test]
    public void GivenNewGraphics_whenToSvg_thenContainsRootSvgAndClosed()
    {
        // Initial SVG should just contain the basic structure
        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">"));
        Assert.That(svg.Trim(), Does.EndWith("</svg>"));
    }

    [Test]
    public void GivenStrokeSet_whenDrawLine_thenLineElementWithAttributesPresent()
    {
        _graphics.SetStrokeColor(Color.Red);
        _graphics.SetStrokeWidth(2);
        _graphics.DrawLine(10, 20, 30, 40);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<line "));
        Assert.That(svg, Does.Contain("x1=\"10\" "));
        Assert.That(svg, Does.Contain("y1=\"20\" "));
        Assert.That(svg, Does.Contain("x2=\"30\" "));
        Assert.That(svg, Does.Contain("y2=\"40\" "));
        Assert.That(svg, Does.Contain("stroke=\"#FF0000\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"2\" "));
    }

    [Test]
    public void GivenStrokeSet_whenDrawRectangle_thenRectElementWithAttributesPresent()
    {
        _graphics.SetStrokeColor(Color.Blue);
        _graphics.SetStrokeWidth(3);
        _graphics.DrawRectangle(10, 20, 100, 50);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<rect "));
        Assert.That(svg, Does.Contain("x=\"10\" "));
        Assert.That(svg, Does.Contain("y=\"20\" "));
        Assert.That(svg, Does.Contain("width=\"100\" "));
        Assert.That(svg, Does.Contain("height=\"50\" "));
        Assert.That(svg, Does.Contain("fill=\"none\" "));
        Assert.That(svg, Does.Contain("stroke=\"#0000FF\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"3\" "));
    }

    [Test]
    public void GivenFillAndStrokeSet_whenFillRectangle_thenRectElementWithAttributesPresent()
    {
        _graphics.SetFillColor(Color.Green);
        _graphics.SetStrokeColor(Color.Red);
        _graphics.SetStrokeWidth(1);
        _graphics.FillRectangle(10, 20, 100, 50);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<rect "));
        Assert.That(svg, Does.Contain("x=\"10\" "));
        Assert.That(svg, Does.Contain("y=\"20\" "));
        Assert.That(svg, Does.Contain("width=\"100\" "));
        Assert.That(svg, Does.Contain("height=\"50\" "));
        Assert.That(svg, Does.Contain("fill=\"#008000\" "));
        Assert.That(svg, Does.Contain("stroke=\"#FF0000\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"1\" "));
    }

    [Test]
    public void GivenStrokeSet_whenDrawCircle_thenCircleElementWithAttributesPresent()
    {
        _graphics.SetStrokeColor(Color.Purple);
        _graphics.SetStrokeWidth(2);
        _graphics.DrawCircle(100, 100, 50);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<circle "));
        Assert.That(svg, Does.Contain("cx=\"100\" "));
        Assert.That(svg, Does.Contain("cy=\"100\" "));
        Assert.That(svg, Does.Contain("r=\"50\" "));
        Assert.That(svg, Does.Contain("fill=\"none\" "));
        Assert.That(svg, Does.Contain("stroke=\"#800080\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"2\" "));
    }

    [Test]
    public void GivenFillAndStrokeSet_whenFillCircle_thenCircleElementWithAttributesPresent()
    {
        _graphics.SetFillColor(Color.Yellow);
        _graphics.SetStrokeColor(Color.Orange);
        _graphics.SetStrokeWidth(1);
        _graphics.FillCircle(100, 100, 50);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<circle "));
        Assert.That(svg, Does.Contain("cx=\"100\" "));
        Assert.That(svg, Does.Contain("cy=\"100\" "));
        Assert.That(svg, Does.Contain("r=\"50\" "));
        Assert.That(svg, Does.Contain("fill=\"#FFFF00\" "));
        Assert.That(svg, Does.Contain("stroke=\"#FFA500\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"1\" "));
    }

    [Test]
    public void GivenStrokeSet_whenDrawPolygon_thenPolygonElementWithAttributesPresent()
    {
        _graphics.SetStrokeColor(Color.Black);
        _graphics.SetStrokeWidth(2);
        List<Point> points = new List<Point>
        {
            new Point(10, 10),
            new Point(50, 10),
            new Point(30, 40)
        };
        _graphics.DrawPolygon(points);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<polygon "));
        Assert.That(svg, Does.Contain("points=\"10,10 50,10 30,40\" "));
        Assert.That(svg, Does.Contain("fill=\"none\" "));
        Assert.That(svg, Does.Contain("stroke=\"#000000\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"2\" "));
    }

    [Test]
    public void GivenFillAndStrokeSet_whenFillPolygon_thenPolygonElementWithAttributesPresent()
    {
        _graphics.SetFillColor(Color.Blue);
        _graphics.SetStrokeColor(Color.Black);
        _graphics.SetStrokeWidth(1);
        List<Point> points = new List<Point>
        {
            new Point(10, 10),
            new Point(50, 10),
            new Point(30, 40)
        };
        _graphics.FillPolygon(points);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<polygon "));
        Assert.That(svg, Does.Contain("points=\"10,10 50,10 30,40\" "));
        Assert.That(svg, Does.Contain("fill=\"#0000FF\" "));
        Assert.That(svg, Does.Contain("stroke=\"#000000\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"1\" "));
    }

    [Test]
    public void GivenTooFewPoints_whenDrawOrFillPolygon_thenNoPolygonIsAdded()
    {
        _graphics.SetStrokeColor(Color.Black);
        List<Point> points = new List<Point>
        {
            new Point(10, 10),
            new Point(50, 10)
        };
        _graphics.DrawPolygon(points);
        _graphics.FillPolygon(points);

        // Should not add any polygons
        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Not.Contain("<polygon "));
    }

    [Test]
    public void GivenStrokeAndFontSet_whenDrawText_thenTextElementWithAttributesPresent()
    {
        _graphics.SetStrokeColor(Color.Blue);
        _graphics.SetFont("Verdana", 24);
        _graphics.DrawText("Hello World", 100, 200);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<text "));
        Assert.That(svg, Does.Contain("x=\"100\" "));
        Assert.That(svg, Does.Contain("y=\"200\" "));
        Assert.That(svg, Does.Contain("font-family=\"Verdana\" "));
        Assert.That(svg, Does.Contain("font-size=\"24\" "));
        Assert.That(svg, Does.Contain("fill=\"#0000FF\" "));
        Assert.That(svg, Does.Contain(">Hello World</text>"));
    }

    [Test]
    public void GivenVariousElementsDrawn_whenToSvg_thenCountsMatch()
    {
        _graphics.SetStrokeColor(Color.Red);
        _graphics.DrawLine(10, 10, 20, 20);
        _graphics.SetFillColor(Color.Blue);
        _graphics.FillCircle(100, 100, 50);

        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("<line "));
        Assert.That(svg, Does.Contain("<circle "));

        // Count the number of elements
        int lineCount = Regex.Matches(svg, "<line ").Count;
        int circleCount = Regex.Matches(svg, "<circle ").Count;
        Assert.That(lineCount, Is.EqualTo(1));
        Assert.That(circleCount, Is.EqualTo(1));
    }

    [Test]
    public void GivenGraphicsWithElements_whenClear_thenElementsAreRemovedFromSvg()
    {
        _graphics.SetStrokeColor(Color.Red);
        _graphics.DrawLine(10, 10, 20, 20);
        _graphics.SetFillColor(Color.Blue);
        _graphics.FillCircle(100, 100, 50);

        // Verify elements exist before clearing
        string svgBefore = _graphics.ToSvg();
        Assert.That(svgBefore, Does.Contain("<line "));
        Assert.That(svgBefore, Does.Contain("<circle "));

        // Clear and verify elements are removed
        _graphics.Clear();
        string svgAfter = _graphics.ToSvg();
        Assert.That(svgAfter, Does.Not.Contain("<line "));
        Assert.That(svgAfter, Does.Not.Contain("<circle "));
    }

    [Test]
    public void GivenNoStrokeSet_whenDrawShapes_thenDefaultBlackStrokeAndWidth1Used()
    {
        // DrawRectangle and DrawCircle should use default black stroke if none is set
        _graphics.DrawRectangle(10, 20, 100, 50);
        string svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("stroke=\"#000000\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"1\" "));

        _graphics.Clear();
        _graphics.DrawCircle(100, 100, 50);
        svg = _graphics.ToSvg();
        Assert.That(svg, Does.Contain("stroke=\"#000000\" "));
        Assert.That(svg, Does.Contain("stroke-width=\"1\" "));
    }

    [Test]
    public void GivenLineDrawn_whenToSvg_thenContainsRootSvgAndClosed()
    {
        _graphics.DrawLine(10, 10, 20, 20);
        string svg = _graphics.ToSvg();

        // Check overall structure
        Assert.That(svg.Trim(), Does.StartWith("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">"));
        Assert.That(svg.Trim(), Does.EndWith("</svg>"));
    }

    [Test]
    public void GivenPreciseNumbers_whenToSvg_thenFormattedToThreeDecimals()
    {
        // Test that decimal numbers are formatted correctly
        _graphics.DrawLine(10.123, 20.456, 30.789, 40.987);
        string svg = _graphics.ToSvg();

        Assert.That(svg, Does.Contain("x1=\"10.123\" "));
        Assert.That(svg, Does.Contain("y1=\"20.456\" "));
        Assert.That(svg, Does.Contain("x2=\"30.789\" "));
        Assert.That(svg, Does.Contain("y2=\"40.987\" "));

        // Test that very precise numbers are truncated to 3 decimal places
        _graphics.Clear();
        _graphics.DrawLine(10.12345, 20.45678, 30.78912, 40.98765);
        svg = _graphics.ToSvg();

        Assert.That(svg, Does.Contain("x1=\"10.123\" "));
        Assert.That(svg, Does.Contain("y1=\"20.457\" "));
        Assert.That(svg, Does.Contain("x2=\"30.789\" "));
        Assert.That(svg, Does.Contain("y2=\"40.988\" "));
    }
}
