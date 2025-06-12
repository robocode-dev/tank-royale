package dev.robocode.tankroyale.botapi.graphics;

/**
 * Interface for graphics context that provides methods for drawing graphics primitives.
 */
public interface IGraphics {
    /**
     * Draws a line from point (x1,y1) to point (x2,y2).
     *
     * @param x1 The x coordinate of the first point.
     * @param y1 The y coordinate of the first point.
     * @param x2 The x coordinate of the second point.
     * @param y2 The y coordinate of the second point.
     */
    void drawLine(double x1, double y1, double x2, double y2);

    /**
     * Draws the outline of a rectangle.
     *
     * @param x The x coordinate of the upper-left corner of the rectangle.
     * @param y The y coordinate of the upper-left corner of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    void drawRectangle(double x, double y, double width, double height);

    /**
     * Fills a rectangle with the current fill color.
     *
     * @param x The x coordinate of the upper-left corner of the rectangle.
     * @param y The y coordinate of the upper-left corner of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    void fillRectangle(double x, double y, double width, double height);

    /**
     * Draws the outline of a circle.
     *
     * @param x The x coordinate of the center of the circle.
     * @param y The y coordinate of the center of the circle.
     * @param radius The radius of the circle.
     */
    void drawCircle(double x, double y, double radius);

    /**
     * Fills a circle with the current fill color.
     *
     * @param x The x coordinate of the center of the circle.
     * @param y The y coordinate of the center of the circle.
     * @param radius The radius of the circle.
     */
    void fillCircle(double x, double y, double radius);

    /**
     * Draws the outline of a polygon defined by a list of points.
     *
     * @param points List of points defining the polygon.
     */
    void drawPolygon(java.util.List<Point> points);

    /**
     * Fills a polygon defined by a list of points with the current fill color.
     *
     * @param points List of points defining the polygon.
     */
    void fillPolygon(java.util.List<Point> points);

    /**
     * Draws text at the specified position.
     *
     * @param text The text to draw.
     * @param x The x coordinate where to draw the text.
     * @param y The y coordinate where to draw the text.
     */
    void drawText(String text, double x, double y);

    /**
     * Sets the color used for drawing outlines.
     *
     * @param color The color to use for drawing outlines.
     */
    void setStrokeColor(Color color);

    /**
     * Sets the color used for filling shapes.
     *
     * @param color The color to use for filling shapes.
     */
    void setFillColor(Color color);

    /**
     * Sets the width of the stroke used for drawing outlines.
     *
     * @param width The width of the stroke.
     */
    void setStrokeWidth(double width);

    /**
     * Sets the font used for drawing text.
     *
     * @param fontFamily The font family name.
     * @param fontSize The font size.
     */
    void setFont(String fontFamily, double fontSize);

    /**
     * Generates the SVG representation of all drawing operations.
     *
     * @return A string containing the SVG representation.
     */
    String toSvg();

    /**
     * Clears all drawing operations.
     */
    void clear();
}
