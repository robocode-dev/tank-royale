package dev.robocode.tankroyale.botapi.graphics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ColorTest {

    @Test
    fun testFromRgba_singleParameter() {
        // Test creation from a single RGBA integer
        val rgbaValue = 0x112233FF // Red: 17, Green: 34, Blue: 51, Alpha: 255
        val color = Color.fromRgba(rgbaValue)

        assertEquals(255, color.a)
        assertEquals(17, color.r)
        assertEquals(34, color.g)
        assertEquals(51, color.b)
        assertEquals(rgbaValue, color.toRgba())
    }

    @Test
    fun testFromRgba_rgbParameters() {
        // Test creation from RGB values (alpha should be 255)
        val color = Color.fromRgb(100, 150, 200)

        assertEquals(255, color.a)
        assertEquals(100, color.r)
        assertEquals(150, color.g)
        assertEquals(200, color.b)
        assertEquals(0x6496C8FF, color.toRgba())
    }

    @Test
    fun testFromRgba_rgbaParameters() {
        // Test creation from RGBA values
        val color = Color.fromRgba(64, 32, 16, 128)

        assertEquals(128, color.a)
        assertEquals(64, color.r)
        assertEquals(32, color.g)
        assertEquals(16, color.b)
        assertEquals(0x40201080, color.toRgba())
    }

    @Test
    fun testFromRgba_baseColor() {
        // Test creation with a base color and alpha
        val baseColor = Color.fromRgb(255, 200, 100)
        val color = Color.fromRgba(baseColor, 50)

        assertEquals(50, color.a)
        assertEquals(255, color.r)
        assertEquals(200, color.g)
        assertEquals(100, color.b)
    }

    @Test
    fun testToRgba() {
        // Create a color and test toRgba() returns the correct value
        val rgbaValue = 0x11223344
        val color = Color.fromRgba(rgbaValue)

        assertEquals(rgbaValue, color.toRgba())
    }

    @Test
    fun testRgbBoundsHandling() {
        // Test RGB values are properly bounded to 0-255
        val color = Color.fromRgba(300, 300, 300, 300)

        // Values should be truncated to 0xFF (255)
        assertEquals(300 and 0xFF, color.a)
        assertEquals(300 and 0xFF, color.r)
        assertEquals(300 and 0xFF, color.g)
        assertEquals(300 and 0xFF, color.b)
    }

    @Test
    fun testPredefinedColors() {
        // Test a few predefined colors
        assertEquals(0x000000FF, Color.BLACK.toRgba())
        assertEquals(0xFFFFFFFF.toInt(), Color.WHITE.toRgba())
        assertEquals(0x0000FFFF, Color.BLUE.toRgba())
        assertEquals(0x00FF00FF, Color.LIME.toRgba()) // Note: LIME is RGB(0,255,0)
        assertEquals(0xFF0000FF.toInt(), Color.RED.toRgba())

        // Test alpha value of TRANSPARENT
        assertEquals(0, Color.TRANSPARENT.a)
    }

    @Test
    fun testEquality() {
        // Test equality of colors
        val color1 = Color.fromRgba(0x112233FF)
        val color2 = Color.fromRgba(0x112233FF)
        val color3 = Color.fromRgba(0x112244FF)

        assertEquals(color1, color2)
        assertNotEquals(color1, color3)
        assertEquals(color1.hashCode(), color2.hashCode())
        assertNotEquals(color1.hashCode(), color3.hashCode())
    }

    @Test
    fun testToString() {
        // Test toString() with opaque color
        val opaqueColor = Color.fromRgb(100, 150, 200)
        assertEquals("Color(r=100, g=150, b=200)", opaqueColor.toString())

        // Test toString() with transparent color
        val transparentColor = Color.fromRgba(100, 150, 200, 128)
        assertEquals("Color(r=100, g=150, b=200, a=128)", transparentColor.toString())
    }

    @Test
    fun testToHexColor() {
        // Test toHexColor() with opaque color
        val opaqueColor = Color.fromRgb(17, 34, 51)
        assertEquals("#112233", opaqueColor.toHexColor())

        // Test toHexColor() with transparent color
        val transparentColor = Color.fromRgba(17, 34, 51, 128)
        assertEquals("#11223380", transparentColor.toHexColor())
    }
}