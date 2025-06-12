package dev.robocode.tankroyale.botapi.graphics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of IGraphics that generates SVG markup.
 */
public class SvgGraphics implements IGraphics {

    private final List<String> elements = new ArrayList<>();
    private String strokeColor = "none";
    private String fillColor = "none";
    private double strokeWidth;
    private String fontFamily = "Arial";
    private double fontSize = 12;

    /**
     * Draws a line from point (x1,y1) to point (x2,y2).
     */
    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        elements.add("<line " +
                     "x1=\"" + format(x1) + "\" " +
                     "y1=\"" + format(y1) + "\" " +
                     "x2=\"" + format(x2) + "\" " +
                     "y2=\"" + format(y2) + "\" " +
                     "stroke=\"" + strokeColor + "\" " +
                     "stroke-width=\"" + format(strokeWidth) + "\" " +
                     "/>\n");
    }

    /**
     * Draws the outline of a rectangle.
     */
    @Override
    public void drawRectangle(double x, double y, double width, double height) {
        String strokeColor = this.strokeColor.equals("none") ? "#000000" : this.strokeColor;
        double strokeWidth = this.strokeWidth == 0 ? 1 : this.strokeWidth;

        elements.add("<rect " +
                     "x=\"" + format(x) + "\" " +
                     "y=\"" + format(y) + "\" " +
                     "width=\"" + format(width) + "\" " +
                     "height=\"" + format(height) + "\" " +
                     "fill=\"none\" " +
                     "stroke=\"" + strokeColor + "\" " +
                     "stroke-width=\"" + format(strokeWidth) + "\" " +
                     "/>\n");
    }

    /**
     * Fills a rectangle with the current fill color.
     */
    @Override
    public void fillRectangle(double x, double y, double width, double height) {
        elements.add("<rect " +
                     "x=\"" + format(x) + "\" " +
                     "y=\"" + format(y) + "\" " +
                     "width=\"" + format(width) + "\" " +
                     "height=\"" + format(height) + "\" " +
                     "fill=\"" + fillColor + "\" " +
                     "stroke=\"" + strokeColor + "\" " +
                     "stroke-width=\"" + format(strokeWidth) + "\" " +
                     "/>\n");
    }

    /**
     * Draws the outline of a circle.
     */
    @Override
    public void drawCircle(double x, double y, double radius) {
        String strokeColor = this.strokeColor.equals("none") ? "#000000" : this.strokeColor;
        double strokeWidth = this.strokeWidth == 0 ? 1 : this.strokeWidth;

        elements.add("<circle " +
                     "cx=\"" + format(x) + "\" " +
                     "cy=\"" + format(y) + "\" " +
                     "r=\"" + format(radius) + "\" " +
                     "fill=\"none\" " +
                     "stroke=\"" + strokeColor + "\" " +
                     "stroke-width=\"" + format(strokeWidth) + "\" " +
                     "/>\n");
    }

    /**
     * Fills a circle with the current fill color.
     */
    @Override
    public void fillCircle(double x, double y, double radius) {
        elements.add("<circle " +
                     "cx=\"" + format(x) + "\" " +
                     "cy=\"" + format(y) + "\" " +
                     "r=\"" + format(radius) + "\" " +
                     "fill=\"" + fillColor + "\" " +
                     "stroke=\"" + strokeColor + "\" " +
                     "stroke-width=\"" + format(strokeWidth) + "\" " +
                     "/>\n");
    }

    /**
     * Draws the outline of a polygon defined by a list of points.
     */
    @Override
    public void drawPolygon(List<Point> points) {
        if (points == null || points.size() < 3) {
            return;
        }

        StringBuilder pointsStr = new StringBuilder();
        for (Point point : points) {
            pointsStr.append(format(point.getX())).append(",").append(format(point.getY())).append(" ");
        }

        String strokeColor = this.strokeColor.equals("none") ? "#000000" : this.strokeColor;
        double strokeWidth = this.strokeWidth == 0 ? 1 : this.strokeWidth;

        elements.add("<polygon " +
                     "points=\"" + pointsStr.toString().trim() + "\" " +
                     "fill=\"none\" " +
                     "stroke=\"" + strokeColor + "\" " +
                     "stroke-width=\"" + format(strokeWidth) + "\" " +
                     "/>\n");
    }

    /**
     * Fills a polygon defined by a list of points with the current fill color.
     */
    @Override
    public void fillPolygon(List<Point> points) {
        if (points == null || points.size() < 3) {
            return;
        }

        StringBuilder pointsStr = new StringBuilder();
        for (Point point : points) {
            pointsStr.append(format(point.getX())).append(",").append(format(point.getY())).append(" ");
        }

        elements.add("<polygon " +
                     "points=\"" + pointsStr.toString().trim() + "\" " +
                     "fill=\"" + fillColor + "\" " +
                     "stroke=\"" + strokeColor + "\" " +
                     "stroke-width=\"" + format(strokeWidth) + "\" " +
                     "/>\n");
    }

    /**
     * Draws text at the specified position.
     */
    @Override
    public void drawText(String text, double x, double y) {
        elements.add("<text " +
                     "x=\"" + format(x) + "\" " +
                     "y=\"" + format(y) + "\" " +
                     "font-family=\"" + fontFamily + "\" " +
                     "font-size=\"" + format(fontSize) + "\" " +
                     "fill=\"" + strokeColor + "\" " +
                     ">" + text + "</text>\n");
    }

    /**
     * Sets the color used for drawing outlines.
     */
    @Override
    public void setStrokeColor(Color color) {
        this.strokeColor = color.toHexColor();
    }

    /**
     * Sets the color used for filling shapes.
     */
    @Override
    public void setFillColor(Color color) {
        this.fillColor = color.toHexColor();
    }

    /**
     * Sets the width of the stroke used for drawing outlines.
     */
    @Override
    public void setStrokeWidth(double width) {
        this.strokeWidth = width;
    }

    /**
     * Sets the font used for drawing text.
     */
    @Override
    public void setFont(String fontFamily, double fontSize) {
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
    }

    /**
     * Generates the SVG representation of all drawing operations.
     *
     * @return A string containing the SVG representation.
     */
    @Override
    public String toSvg() {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">\n");
        for (String element : elements) {
            svg.append(element);
        }

        svg.append("</svg>\n");
        return svg.toString();
    }

    /**
     * Clears all drawing operations.
     */
    @Override
    public void clear() {
        elements.clear();
    }

    /**
     * Formats a double value to a string with at most 3 decimal places.
     *
     * @param value The value to format.
     * @return The formatted string.
     */
    private static String format(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.###", symbols);
        return df.format(value);
    }
}
