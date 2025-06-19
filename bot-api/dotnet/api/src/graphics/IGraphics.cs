using System.Collections.Generic;
using JetBrains.Annotations;

namespace Robocode.TankRoyale.BotApi.Graphics;

/// <summary>
/// Interface for graphics context that provides methods for drawing graphics primitives.
/// </summary>
[PublicAPI]
public interface IGraphics
{
    /// <summary>
    /// Draws a line from point (x1,y1) to point (x2,y2).
    /// </summary>
    /// <param name="x1">The x coordinate of the first point.</param>
    /// <param name="y1">The y coordinate of the first point.</param>
    /// <param name="x2">The x coordinate of the second point.</param>
    /// <param name="y2">The y coordinate of the second point.</param>
    void DrawLine(double x1, double y1, double x2, double y2);

    /// <summary>
    /// Draws the outline of a rectangle.
    /// </summary>
    /// <param name="x">The x coordinate of the upper-left corner of the rectangle.</param>
    /// <param name="y">The y coordinate of the upper-left corner of the rectangle.</param>
    /// <param name="width">The width of the rectangle.</param>
    /// <param name="height">The height of the rectangle.</param>
    void DrawRectangle(double x, double y, double width, double height);

    /// <summary>
    /// Fills a rectangle with the current fill color.
    /// </summary>
    /// <param name="x">The x coordinate of the upper-left corner of the rectangle.</param>
    /// <param name="y">The y coordinate of the upper-left corner of the rectangle.</param>
    /// <param name="width">The width of the rectangle.</param>
    /// <param name="height">The height of the rectangle.</param>
    void FillRectangle(double x, double y, double width, double height);

    /// <summary>
    /// Draws the outline of a circle.
    /// </summary>
    /// <param name="x">The x coordinate of the center of the circle.</param>
    /// <param name="y">The y coordinate of the center of the circle.</param>
    /// <param name="radius">The radius of the circle.</param>
    void DrawCircle(double x, double y, double radius);

    /// <summary>
    /// Fills a circle with the current fill color.
    /// </summary>
    /// <param name="x">The x coordinate of the center of the circle.</param>
    /// <param name="y">The y coordinate of the center of the circle.</param>
    /// <param name="radius">The radius of the circle.</param>
    void FillCircle(double x, double y, double radius);

    /// <summary>
    /// Draws the outline of a polygon defined by a list of points.
    /// </summary>
    /// <param name="points">List of points defining the polygon.</param>
    void DrawPolygon(List<Point> points);

    /// <summary>
    /// Fills a polygon defined by a list of points with the current fill color.
    /// </summary>
    /// <param name="points">List of points defining the polygon.</param>
    void FillPolygon(List<Point> points);

    /// <summary>
    /// Draws text at the specified position.
    /// </summary>
    /// <param name="text">The text to draw.</param>
    /// <param name="x">The x coordinate where to draw the text.</param>
    /// <param name="y">The y coordinate where to draw the text.</param>
    void DrawText(string text, double x, double y);

    /// <summary>
    /// Sets the color used for drawing outlines.
    /// </summary>
    /// <param name="color">The color to use for drawing outlines.</param>
    void SetStrokeColor(Color color);

    /// <summary>
    /// Sets the color used for filling shapes.
    /// </summary>
    /// <param name="color">The color to use for filling shapes.</param>
    void SetFillColor(Color color);

    /// <summary>
    /// Sets the width of the stroke used for drawing outlines.
    /// </summary>
    /// <param name="width">The width of the stroke.</param>
    void SetStrokeWidth(double width);

    /// <summary>
    /// Sets the font used for drawing text.
    /// </summary>
    /// <param name="fontFamily">The font family name.</param>
    /// <param name="fontSize">The font size.</param>
    void SetFont(string fontFamily, double fontSize);

    /// <summary>
    /// Generates the SVG representation of all drawing operations.
    /// </summary>
    /// <returns>A string containing the SVG representation.</returns>
    string ToSvg();

    /// <summary>
    /// Clears all drawing operations.
    /// </summary>
    void Clear();
}
