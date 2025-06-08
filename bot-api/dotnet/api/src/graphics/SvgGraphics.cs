using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;
using JetBrains.Annotations;

namespace Robocode.TankRoyale.BotApi.Graphics;

/// <summary>
/// Implementation of IGraphicsContext that generates SVG markup.
/// </summary>
[PublicAPI]
public class SvgGraphics : IGraphics
{
    private readonly List<string> _elements = new();
    private string _strokeColor = "#000000";
    private string _fillColor = "none";
    private double _strokeWidth = 1.0;
    private string _fontFamily = "Arial";
    private double _fontSize = 12;

    /// <summary>
    /// Draws a line from point (x1,y1) to point (x2,y2).
    /// </summary>
    public void DrawLine(double x1, double y1, double x2, double y2)
    {
        _elements.Add($"<line " +
                      $"x1=\"{Format(x1)}\" " +
                      $"y1=\"{Format(y1)}\" " +
                      $"x2=\"{Format(x2)}\" " +
                      $"y2=\"{Format(y2)}\" " +
                      $"stroke=\"{_strokeColor}\" " +
                      $"stroke-width=\"{Format(_strokeWidth)}\" " +
                      "/>\n");
    }

    /// <summary>
    /// Draws the outline of a rectangle.
    /// </summary>
    public void DrawRectangle(double x, double y, double width, double height)
    {
        _elements.Add($"<rect " +
                      $"x=\"{Format(x)}\" " +
                      $"y=\"{Format(y)}\" " +
                      $"width=\"{Format(width)}\" " +
                      $"height=\"{Format(height)}\" " +
                      $"stroke=\"{_strokeColor}\" " +
                      $"stroke-width=\"{Format(_strokeWidth)}\" " +
                      $"fill=\"none\" " +
                      "/>\n");
    }

    /// <summary>
    /// Fills a rectangle with the current fill color.
    /// </summary>
    public void FillRectangle(double x, double y, double width, double height)
    {
        _elements.Add($"<rect " +
                      $"x=\"{Format(x)}\" " +
                      $"y=\"{Format(y)}\" " +
                      $"width=\"{Format(width)}\" " +
                      $"height=\"{Format(height)}\" " +
                      $"fill=\"{_fillColor}\" " +
                      $"stroke=\"{_strokeColor}\" " +
                      $"stroke-width=\"{Format(_strokeWidth)}\" " +
                      $"/>\n");
    }

    /// <summary>
    /// Draws the outline of a circle.
    /// </summary>
    public void DrawCircle(double x, double y, double radius)
    {
        _elements.Add($"<circle " +
                      $"cx=\"{Format(x)}\" " +
                      $"cy=\"{Format(y)}\" " +
                      $"r=\"{Format(radius)}\" " +
                      $"fill=\"none\" " +
                      $"stroke=\"{_strokeColor}\" " +
                      $"stroke-width=\"{Format(_strokeWidth)}\" " +
                      $"/>\n");
    }

    /// <summary>
    /// Fills a circle with the current fill color.
    /// </summary>
    public void FillCircle(double x, double y, double radius)
    {
        _elements.Add($"<circle " +
                      $"cx=\"{Format(x)}\" " +
                      $"cy=\"{Format(y)}\" " +
                      $"r=\"{Format(radius)}\" " +
                      $"fill=\"{_fillColor}\" " +
                      $"stroke=\"{_strokeColor}\" " +
                      $"stroke-width=\"{Format(_strokeWidth)}\" " +
                      $"/>\n");
    }

    /// <summary>
    /// Draws the outline of a polygon defined by an array of points.
    /// </summary>
    public void DrawPolygon(Point[] points)
    {
        if (points == null || points.Length < 3)
            return;

        var pointsStr = new StringBuilder();
        foreach (var point in points)
        {
            pointsStr.Append($"{Format(point.X)},{Format(point.Y)} ");
        }

        _elements.Add($"<polygon " +
                      $"points=\"{pointsStr.ToString().Trim()}\" " +
                      $"fill=\"none\" " +
                      $"stroke=\"{_strokeColor}\" " +
                      $"stroke-width=\"{Format(_strokeWidth)}\" " +
                      $"/>\n");
    }

    /// <summary>
    /// Fills a polygon defined by an array of points with the current fill color.
    /// </summary>
    public void FillPolygon(Point[] points)
    {
        if (points == null || points.Length < 3)
            return;

        var pointsStr = new StringBuilder();
        foreach (var point in points)
        {
            pointsStr.Append($"{Format(point.X)},{Format(point.Y)} ");
        }

        _elements.Add($"<polygon " +
                      $"points=\"{pointsStr.ToString().Trim()}\" " +
                      $"fill=\"{_fillColor}\" " +
                      $"stroke=\"{_strokeColor}\" " +
                      $"stroke-width=\"{Format(_strokeWidth)}\" " +
                      $"/>\n");
    }

    /// <summary>
    /// Draws text at the specified position.
    /// </summary>
    public void DrawText(double x, double y, string text)
    {
        _elements.Add($"<text " +
                      $"x=\"{Format(x)}\" " +
                      $"y=\"{Format(y)}\" " +
                      $"font-family=\"{_fontFamily}\" " +
                      $"font-size=\"{Format(_fontSize)}\" " +
                      $"fill=\"{_strokeColor}\" " +
                      $">{text}</text>\n");
    }

    /// <summary>
    /// Sets the color used for drawing outlines.
    /// </summary>
    public void SetStrokeColor(Color color)
    {
        _strokeColor = ColorToHex(color);
    }

    /// <summary>
    /// Sets the color used for filling shapes.
    /// </summary>
    public void SetFillColor(Color color)
    {
        _fillColor = ColorToHex(color);
    }

    /// <summary>
    /// Sets the width of the stroke used for drawing outlines.
    /// </summary>
    public void SetStrokeWidth(double width)
    {
        _strokeWidth = width;
    }

    /// <summary>
    /// Sets the font used for drawing text.
    /// </summary>
    public void SetFont(string fontFamily, double fontSize)
    {
        _fontFamily = fontFamily;
        _fontSize = fontSize;
    }

    /// <summary>
    /// Generates the SVG representation of all drawing operations.
    /// </summary>
    public string ToSvg()
    {
        var svg = new StringBuilder();
        svg.AppendLine("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">");
        foreach (var element in _elements)
        {
            svg.Append(element);
        }

        svg.AppendLine("</svg>");
        return svg.ToString();
    }

    /// <summary>
    /// Clears all drawing operations.
    /// </summary>
    public void Clear()
    {
        _elements.Clear();
    }

    private static string ColorToHex(Color color)
    {
        return $"#{color.R:X2}{color.G:X2}{color.B:X2}";
    }

    private static string Format(double value)
    {
        return value.ToString("0.###", CultureInfo.InvariantCulture);
    }
}