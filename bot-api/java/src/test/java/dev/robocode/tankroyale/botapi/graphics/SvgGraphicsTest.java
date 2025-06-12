package dev.robocode.tankroyale.botapi.graphics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link SvgGraphics} class.
 */
public class SvgGraphicsTest {
    private SvgGraphics graphics;

    @BeforeEach
    public void setUp() {
        graphics = new SvgGraphics();
    }

    @Test
    public void testInitialState() {
        // Initial SVG should just contain the basic structure
        String svg = graphics.toSvg();
        assertTrue(svg.contains("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">"));
        assertTrue(svg.trim().endsWith("</svg>"));
    }

    @Test
    public void testDrawLine() {
        graphics.setStrokeColor(Color.RED);
        graphics.setStrokeWidth(2);
        graphics.drawLine(10, 20, 30, 40);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<line "));
        assertTrue(svg.contains("x1=\"10\" "));
        assertTrue(svg.contains("y1=\"20\" "));
        assertTrue(svg.contains("x2=\"30\" "));
        assertTrue(svg.contains("y2=\"40\" "));
        assertTrue(svg.contains("stroke=\"#FF0000\" "));
        assertTrue(svg.contains("stroke-width=\"2\" "));
    }

    @Test
    public void testDrawRectangle() {
        graphics.setStrokeColor(Color.BLUE);
        graphics.setStrokeWidth(3);
        graphics.drawRectangle(10, 20, 100, 50);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<rect "));
        assertTrue(svg.contains("x=\"10\" "));
        assertTrue(svg.contains("y=\"20\" "));
        assertTrue(svg.contains("width=\"100\" "));
        assertTrue(svg.contains("height=\"50\" "));
        assertTrue(svg.contains("fill=\"none\" "));
        assertTrue(svg.contains("stroke=\"#0000FF\" "));
        assertTrue(svg.contains("stroke-width=\"3\" "));
    }

    @Test
    public void testFillRectangle() {
        graphics.setFillColor(Color.GREEN);
        graphics.setStrokeColor(Color.RED);
        graphics.setStrokeWidth(1);
        graphics.fillRectangle(10, 20, 100, 50);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<rect "));
        assertTrue(svg.contains("x=\"10\" "));
        assertTrue(svg.contains("y=\"20\" "));
        assertTrue(svg.contains("width=\"100\" "));
        assertTrue(svg.contains("height=\"50\" "));
        assertTrue(svg.contains("fill=\"#008000\" "));
        assertTrue(svg.contains("stroke=\"#FF0000\" "));
        assertTrue(svg.contains("stroke-width=\"1\" "));
    }

    @Test
    public void testDrawCircle() {
        graphics.setStrokeColor(Color.PURPLE);
        graphics.setStrokeWidth(2);
        graphics.drawCircle(100, 100, 50);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<circle "));
        assertTrue(svg.contains("cx=\"100\" "));
        assertTrue(svg.contains("cy=\"100\" "));
        assertTrue(svg.contains("r=\"50\" "));
        assertTrue(svg.contains("fill=\"none\" "));
        assertTrue(svg.contains("stroke=\"#800080\" "));
        assertTrue(svg.contains("stroke-width=\"2\" "));
    }

    @Test
    public void testFillCircle() {
        graphics.setFillColor(Color.YELLOW);
        graphics.setStrokeColor(Color.ORANGE);
        graphics.setStrokeWidth(1);
        graphics.fillCircle(100, 100, 50);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<circle "));
        assertTrue(svg.contains("cx=\"100\" "));
        assertTrue(svg.contains("cy=\"100\" "));
        assertTrue(svg.contains("r=\"50\" "));
        assertTrue(svg.contains("fill=\"#FFFF00\" "));
        assertTrue(svg.contains("stroke=\"#FFA500\" "));
        assertTrue(svg.contains("stroke-width=\"1\" "));
    }

    @Test
    public void testDrawPolygon() {
        graphics.setStrokeColor(Color.BLACK);
        graphics.setStrokeWidth(2);
        List<Point> points = new ArrayList<>();
        points.add(new Point(10, 10));
        points.add(new Point(50, 10));
        points.add(new Point(30, 40));
        graphics.drawPolygon(points);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<polygon "));
        assertTrue(svg.contains("points=\"10,10 50,10 30,40\" "));
        assertTrue(svg.contains("fill=\"none\" "));
        assertTrue(svg.contains("stroke=\"#000000\" "));
        assertTrue(svg.contains("stroke-width=\"2\" "));
    }

    @Test
    public void testFillPolygon() {
        graphics.setFillColor(Color.BLUE);
        graphics.setStrokeColor(Color.BLACK);
        graphics.setStrokeWidth(1);
        List<Point> points = new ArrayList<>();
        points.add(new Point(10, 10));
        points.add(new Point(50, 10));
        points.add(new Point(30, 40));
        graphics.fillPolygon(points);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<polygon "));
        assertTrue(svg.contains("points=\"10,10 50,10 30,40\" "));
        assertTrue(svg.contains("fill=\"#0000FF\" "));
        assertTrue(svg.contains("stroke=\"#000000\" "));
        assertTrue(svg.contains("stroke-width=\"1\" "));
    }

    @Test
    public void testPolygonWithTooFewPoints() {
        graphics.setStrokeColor(Color.BLACK);
        List<Point> points = new ArrayList<>();
        points.add(new Point(10, 10));
        points.add(new Point(50, 10));
        graphics.drawPolygon(points);
        graphics.fillPolygon(points);

        // Should not add any polygons
        String svg = graphics.toSvg();
        assertFalse(svg.contains("<polygon "));
    }

    @Test
    public void testDrawText() {
        graphics.setStrokeColor(Color.BLUE);
        graphics.setFont("Verdana", 24);
        graphics.drawText("Hello World", 100, 200);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<text "));
        assertTrue(svg.contains("x=\"100\" "));
        assertTrue(svg.contains("y=\"200\" "));
        assertTrue(svg.contains("font-family=\"Verdana\" "));
        assertTrue(svg.contains("font-size=\"24\" "));
        assertTrue(svg.contains("fill=\"#0000FF\" "));
        assertTrue(svg.contains(">Hello World</text>"));
    }

    @Test
    public void testMultipleElements() {
        graphics.setStrokeColor(Color.RED);
        graphics.drawLine(10, 10, 20, 20);
        graphics.setFillColor(Color.BLUE);
        graphics.fillCircle(100, 100, 50);

        String svg = graphics.toSvg();
        assertTrue(svg.contains("<line "));
        assertTrue(svg.contains("<circle "));

        // Count the number of elements
        int lineCount = countOccurrences(svg, "<line ");
        int circleCount = countOccurrences(svg, "<circle ");
        assertEquals(1, lineCount);
        assertEquals(1, circleCount);
    }

    @Test
    public void testClear() {
        graphics.setStrokeColor(Color.RED);
        graphics.drawLine(10, 10, 20, 20);
        graphics.setFillColor(Color.BLUE);
        graphics.fillCircle(100, 100, 50);

        // Verify elements exist before clearing
        String svgBefore = graphics.toSvg();
        assertTrue(svgBefore.contains("<line "));
        assertTrue(svgBefore.contains("<circle "));

        // Clear and verify elements are removed
        graphics.clear();
        String svgAfter = graphics.toSvg();
        assertFalse(svgAfter.contains("<line "));
        assertFalse(svgAfter.contains("<circle "));
    }

    @Test
    public void testDefaultStrokeValues() {
        // DrawRectangle and DrawCircle should use default black stroke if none is set
        graphics.drawRectangle(10, 20, 100, 50);
        String svg = graphics.toSvg();
        assertTrue(svg.contains("stroke=\"#000000\" "));
        assertTrue(svg.contains("stroke-width=\"1\" "));

        graphics.clear();
        graphics.drawCircle(100, 100, 50);
        svg = graphics.toSvg();
        assertTrue(svg.contains("stroke=\"#000000\" "));
        assertTrue(svg.contains("stroke-width=\"1\" "));
    }

    @Test
    public void testToSvgFormatting() {
        graphics.drawLine(10, 10, 20, 20);
        String svg = graphics.toSvg();

        // Check overall structure
        assertTrue(svg.trim().startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">"));
        assertTrue(svg.trim().endsWith("</svg>"));
    }

    @Test
    public void testNumberFormatting() {
        // Test that decimal numbers are formatted correctly
        graphics.drawLine(10.123, 20.456, 30.789, 40.987);
        String svg = graphics.toSvg();

        assertTrue(svg.contains("x1=\"10.123\" "));
        assertTrue(svg.contains("y1=\"20.456\" "));
        assertTrue(svg.contains("x2=\"30.789\" "));
        assertTrue(svg.contains("y2=\"40.987\" "));

        // Test that very precise numbers are truncated to 3 decimal places
        graphics.clear();
        graphics.drawLine(10.12345, 20.45678, 30.78912, 40.98765);
        svg = graphics.toSvg();

        assertTrue(svg.contains("x1=\"10.123\" "));
        assertTrue(svg.contains("y1=\"20.457\" "));
        assertTrue(svg.contains("x2=\"30.789\" "));
        assertTrue(svg.contains("y2=\"40.988\" "));
    }

    private int countOccurrences(String text, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }
}
