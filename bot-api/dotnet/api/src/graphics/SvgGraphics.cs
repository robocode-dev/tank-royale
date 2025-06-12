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
    private string _strokeColor = "none";
    private string _fillColor = "none";
    private double _strokeWidth;
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
        var strokeColor = _strokeColor == "none" ? "#000000" : _strokeColor;
        var strokeWidth = _strokeWidth == 0 ? 1 : _strokeWidth;
        
        _elements.Add($"<rect " +
                      $"x=\"{Format(x)}\" " +
                      $"y=\"{Format(y)}\" " +
                      $"width=\"{Format(width)}\" " +
                      $"height=\"{Format(height)}\" " +
                      $"fill=\"none\" " +
                      $"stroke=\"{strokeColor}\" " +
                      $"stroke-width=\"{Format(strokeWidth)}\" " +
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
        var strokeColor = _strokeColor == "none" ? "#000000" : _strokeColor;
        var strokeWidth = _strokeWidth == 0 ? 1 : _strokeWidth;

        _elements.Add($"<circle " +
                      $"cx=\"{Format(x)}\" " +
                      $"cy=\"{Format(y)}\" " +
                      $"r=\"{Format(radius)}\" " +
                      $"fill=\"none\" " +
                      $"stroke=\"{strokeColor}\" " +
                      $"stroke-width=\"{Format(strokeWidth)}\" " +
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
    /// Draws the outline of a polygon defined by a list of points.
    /// </summary>
    public void DrawPolygon(List<Point> points)
    {
        if (points == null || points.Count < 3)
            return;

        var pointsStr = new StringBuilder();
        foreach (var point in points)
        {
            pointsStr.Append($"{Format(point.X)},{Format(point.Y)} ");
        }

        var strokeColor = _strokeColor == "none" ? "#000000" : _strokeColor;
        var strokeWidth = _strokeWidth == 0 ? 1 : _strokeWidth;

        _elements.Add($"<polygon " +
                      $"points=\"{pointsStr.ToString().Trim()}\" " +
                      $"fill=\"none\" " +
                      $"stroke=\"{strokeColor}\" " +
                      $"stroke-width=\"{Format(strokeWidth)}\" " +
                      $"/>\n");
    }

    /// <summary>
    /// Fills a polygon defined by a list of points with the current fill color.
    /// </summary>
    public void FillPolygon(List<Point> points)
    {
        if (points == null || points.Count < 3)
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
    public void DrawText(string text, double x, double y)
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
        _strokeColor = color.ToHexColor();
    }

    /// <summary>
    /// Sets the color used for filling shapes.
    /// </summary>
    public void SetFillColor(Color color)
    {
        _fillColor = color.ToHexColor();
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

    private static string Format(double value)
    {
        return value.ToString("0.###", CultureInfo.InvariantCulture);
    }
}