package dev.robocode.tankroyale.botapi.graphics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the [SvgGraphics] class.
 */
class SvgGraphicsTest {

    private fun countOccurrences(text: String, token: String): Int =
        Regex(Regex.escape(token)).findAll(text).count()

    @Test
    fun givenNewGraphics_whenToSvg_thenContainsRootSvgAndClosed() {
        val graphics = SvgGraphics()
        val svg = graphics.toSvg()
        assertTrue(svg.contains("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">"))
        assertTrue(svg.trim().endsWith("</svg>"))
    }

    @Test
    fun givenStrokeSet_whenDrawLine_thenLineElementWithAttributesPresent() {
        val g = SvgGraphics()
        g.setStrokeColor(Color.RED)
        g.setStrokeWidth(2.0)
        g.drawLine(10.0, 20.0, 30.0, 40.0)

        val svg = g.toSvg()
        assertTrue(svg.contains("<line "))
        assertTrue(svg.contains("x1=\"10\" "))
        assertTrue(svg.contains("y1=\"20\" "))
        assertTrue(svg.contains("x2=\"30\" "))
        assertTrue(svg.contains("y2=\"40\" "))
        assertTrue(svg.contains("stroke=\"#FF0000\" "))
        assertTrue(svg.contains("stroke-width=\"2\" "))
    }

    @Test
    fun givenStrokeSet_whenDrawRectangle_thenRectElementWithAttributesPresent() {
        val g = SvgGraphics()
        g.setStrokeColor(Color.BLUE)
        g.setStrokeWidth(3.0)
        g.drawRectangle(10.0, 20.0, 100.0, 50.0)

        val svg = g.toSvg()
        assertTrue(svg.contains("<rect "))
        assertTrue(svg.contains("x=\"10\" "))
        assertTrue(svg.contains("y=\"20\" "))
        assertTrue(svg.contains("width=\"100\" "))
        assertTrue(svg.contains("height=\"50\" "))
        assertTrue(svg.contains("fill=\"none\" "))
        assertTrue(svg.contains("stroke=\"#0000FF\" "))
        assertTrue(svg.contains("stroke-width=\"3\" "))
    }

    @Test
    fun givenFillAndStrokeSet_whenFillRectangle_thenRectElementWithAttributesPresent() {
        val g = SvgGraphics()
        g.setFillColor(Color.GREEN)
        g.setStrokeColor(Color.RED)
        g.setStrokeWidth(1.0)
        g.fillRectangle(10.0, 20.0, 100.0, 50.0)

        val svg = g.toSvg()
        assertTrue(svg.contains("<rect "))
        assertTrue(svg.contains("fill=\"#008000\" "))
        assertTrue(svg.contains("stroke=\"#FF0000\" "))
        assertTrue(svg.contains("stroke-width=\"1\" "))
    }

    @Test
    fun givenStrokeSet_whenDrawCircle_thenCircleElementWithAttributesPresent() {
        val g = SvgGraphics()
        g.setStrokeColor(Color.PURPLE)
        g.setStrokeWidth(2.0)
        g.drawCircle(100.0, 100.0, 50.0)

        val svg = g.toSvg()
        assertTrue(svg.contains("<circle "))
        assertTrue(svg.contains("cx=\"100\" "))
        assertTrue(svg.contains("cy=\"100\" "))
        assertTrue(svg.contains("r=\"50\" "))
        assertTrue(svg.contains("fill=\"none\" "))
        assertTrue(svg.contains("stroke=\"#800080\" "))
        assertTrue(svg.contains("stroke-width=\"2\" "))
    }

    @Test
    fun givenFillAndStrokeSet_whenFillCircle_thenCircleElementWithAttributesPresent() {
        val g = SvgGraphics()
        g.setFillColor(Color.YELLOW)
        g.setStrokeColor(Color.ORANGE)
        g.setStrokeWidth(1.0)
        g.fillCircle(100.0, 100.0, 50.0)

        val svg = g.toSvg()
        assertTrue(svg.contains("fill=\"#FFFF00\" "))
        assertTrue(svg.contains("stroke=\"#FFA500\" "))
    }

    @Test
    fun givenStrokeSet_whenDrawPolygon_thenPolygonElementWithAttributesPresent() {
        val g = SvgGraphics()
        g.setStrokeColor(Color.BLACK)
        g.setStrokeWidth(2.0)
        val pts = listOf(Point(10.0, 10.0), Point(50.0, 10.0), Point(30.0, 40.0))
        g.drawPolygon(pts)

        val svg = g.toSvg()
        assertTrue(svg.contains("<polygon "))
        assertTrue(svg.contains("points=\"10,10 50,10 30,40\" "))
        assertTrue(svg.contains("fill=\"none\" "))
        assertTrue(svg.contains("stroke=\"#000000\" "))
        assertTrue(svg.contains("stroke-width=\"2\" "))
    }

    @Test
    fun givenFillAndStrokeSet_whenFillPolygon_thenPolygonElementWithAttributesPresent() {
        val g = SvgGraphics()
        g.setFillColor(Color.BLUE)
        g.setStrokeColor(Color.BLACK)
        g.setStrokeWidth(1.0)
        val pts = listOf(Point(10.0, 10.0), Point(50.0, 10.0), Point(30.0, 40.0))
        g.fillPolygon(pts)

        val svg = g.toSvg()
        assertTrue(svg.contains("fill=\"#0000FF\" "))
        assertTrue(svg.contains("stroke=\"#000000\" "))
    }

    @Test
    fun givenTooFewPoints_whenDrawOrFillPolygon_thenNoPolygonIsAdded() {
        val g = SvgGraphics()
        val pts = listOf(Point(10.0, 10.0), Point(50.0, 10.0))
        g.drawPolygon(pts)
        g.fillPolygon(pts)

        val svg = g.toSvg()
        assertFalse(svg.contains("<polygon "))
    }

    @Test
    fun givenStrokeAndFontSet_whenDrawText_thenTextElementWithAttributesPresent() {
        val g = SvgGraphics()
        g.setStrokeColor(Color.BLUE)
        g.setFont("Verdana", 24.0)
        g.drawText("Hello World", 100.0, 200.0)

        val svg = g.toSvg()
        assertTrue(svg.contains("<text "))
        assertTrue(svg.contains("x=\"100\" "))
        assertTrue(svg.contains("y=\"200\" "))
        assertTrue(svg.contains("font-family=\"Verdana\" "))
        assertTrue(svg.contains("font-size=\"24\" "))
        assertTrue(svg.contains("fill=\"#0000FF\""))
        assertTrue(svg.contains(">Hello World</text>"))
    }

    @Test
    fun givenVariousElementsDrawn_whenToSvg_thenCountsMatch() {
        val g = SvgGraphics()
        g.setStrokeColor(Color.RED)
        g.drawLine(10.0, 10.0, 20.0, 20.0)
        g.setFillColor(Color.BLUE)
        g.fillCircle(100.0, 100.0, 50.0)

        val svg = g.toSvg()
        assertTrue(svg.contains("<line "))
        assertTrue(svg.contains("<circle "))

        assertEquals(1, countOccurrences(svg, "<line "), "Expected exactly one <line>")
        assertEquals(1, countOccurrences(svg, "<circle "), "Expected exactly one <circle>")
    }

    @Test
    fun givenGraphicsWithElements_whenClear_thenElementsAreRemovedFromSvg() {
        val g = SvgGraphics()
        g.setStrokeColor(Color.RED)
        g.drawLine(10.0, 10.0, 20.0, 20.0)
        g.setFillColor(Color.BLUE)
        g.fillCircle(100.0, 100.0, 50.0)

        assertTrue(g.toSvg().contains("<line "))
        assertTrue(g.toSvg().contains("<circle "))

        g.clear()
        val svgAfter = g.toSvg()
        assertFalse(svgAfter.contains("<line "))
        assertFalse(svgAfter.contains("<circle "))
    }

    @Test
    fun givenNoStrokeSet_whenDrawShapes_thenDefaultBlackStrokeAndWidth1Used() {
        val g = SvgGraphics()
        g.drawRectangle(10.0, 20.0, 100.0, 50.0)
        var svg = g.toSvg()
        assertTrue(svg.contains("stroke=\"#000000\" "))
        assertTrue(svg.contains("stroke-width=\"1\" "))

        g.clear()
        g.drawCircle(100.0, 100.0, 50.0)
        svg = g.toSvg()
        assertTrue(svg.contains("stroke=\"#000000\" "))
        assertTrue(svg.contains("stroke-width=\"1\" "))
    }

    @Test
    fun givenLineDrawn_whenToSvg_thenContainsRootSvgAndClosed() {
        val g = SvgGraphics()
        g.drawLine(10.0, 10.0, 20.0, 20.0)
        val svg = g.toSvg().trim()
        assertTrue(svg.startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">"))
        assertTrue(svg.endsWith("</svg>"))
    }

    @Test
    fun givenPreciseNumbers_whenToSvg_thenFormattedToThreeDecimals() {
        val g = SvgGraphics()
        g.drawLine(10.123, 20.456, 30.789, 40.987)
        var svg = g.toSvg()
        assertTrue(svg.contains("x1=\"10.123\" "))
        assertTrue(svg.contains("y1=\"20.456\" "))
        assertTrue(svg.contains("x2=\"30.789\" "))
        assertTrue(svg.contains("y2=\"40.987\" "))

        g.clear()
        g.drawLine(10.12345, 20.45678, 30.78912, 40.98765)
        svg = g.toSvg()
        assertTrue(svg.contains("x1=\"10.123\" "))
        assertTrue(svg.contains("y1=\"20.457\" "))
        assertTrue(svg.contains("x2=\"30.789\" "))
        assertTrue(svg.contains("y2=\"40.988\" "))
    }
}

