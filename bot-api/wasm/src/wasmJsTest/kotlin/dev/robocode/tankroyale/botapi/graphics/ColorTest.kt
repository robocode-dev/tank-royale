package dev.robocode.tankroyale.botapi.graphics

import dev.robocode.tankroyale.utils.TestAssertions
import kotlin.test.Test
import kotlin.test.assertTrue

class ColorTest {

    @Test
    fun testFromRgba_singleParameter() {
        // Test creation from a single RGBA integer
        val rgbaValue = 0x112233FF.toInt() // Red: 17, Green: 34, Blue: 51, Alpha: 255
        val color = Color.Companion.fromRgba(rgbaValue)

        TestAssertions.assertEquals(255, color.a)
        TestAssertions.assertEquals(17, color.r)
        TestAssertions.assertEquals(34, color.g)
        TestAssertions.assertEquals(51, color.b)
        TestAssertions.assertEquals(rgbaValue, color.toRgba())
    }

    @Test
    fun testFromRgba_rgbParameters() {
        // Test creation from RGB values (alpha should be 255)
        val color = Color.Companion.fromRgb(100, 150, 200)

        TestAssertions.assertEquals(255, color.a)
        TestAssertions.assertEquals(100, color.r)
        TestAssertions.assertEquals(150, color.g)
        TestAssertions.assertEquals(200, color.b)
        TestAssertions.assertEquals(0x6496C8FF.toInt(), color.toRgba())
    }

    @Test
    fun testFromRgba_rgbaParameters() {
        // Test creation from RGBA values
        val color = Color.Companion.fromRgba(64, 32, 16, 128)

        TestAssertions.assertEquals(128, color.a)
        TestAssertions.assertEquals(64, color.r)
        TestAssertions.assertEquals(32, color.g)
        TestAssertions.assertEquals(16, color.b)
        TestAssertions.assertEquals(0x40201080.toInt(), color.toRgba())
    }

    @Test
    fun testFromRgba_baseColor() {
        // Test creation with a base color and alpha
        val baseColor = Color.Companion.fromRgb(255, 200, 100)
        val color = Color.Companion.fromRgba(baseColor, 50)

        TestAssertions.assertEquals(50, color.a)
        TestAssertions.assertEquals(255, color.r)
        TestAssertions.assertEquals(200, color.g)
        TestAssertions.assertEquals(100, color.b)
    }

    @Test
    fun testToRgba() {
        // Create a color and test toRgba() returns the correct value
        val rgbaValue = 0x11223344.toInt()
        val color = Color.Companion.fromRgba(rgbaValue)

        TestAssertions.assertEquals(rgbaValue, color.toRgba())
    }

    @Test
    fun testRgbBoundsHandling() {
        // Test RGB values are properly bounded to 0-255
        val color = Color.Companion.fromRgba(300, 300, 300, 300)

        // Values should be truncated to 0xFF (255)
        TestAssertions.assertEquals(300 and 0xFF, color.a)
        TestAssertions.assertEquals(300 and 0xFF, color.r)
        TestAssertions.assertEquals(300 and 0xFF, color.g)
        TestAssertions.assertEquals(300 and 0xFF, color.b)
    }

    @Test
    fun testPredefinedColors() {
        // Test a few predefined colors
        TestAssertions.assertEquals(0x000000FF.toInt(), Color.Companion.BLACK.toRgba())
        TestAssertions.assertEquals(0xFFFFFFFF.toInt(), Color.Companion.WHITE.toRgba())
        TestAssertions.assertEquals(0x0000FFFF.toInt(), Color.Companion.BLUE.toRgba())
        TestAssertions.assertEquals(0x00FF00FF.toInt(), Color.Companion.LIME.toRgba()) // Note: LIME is RGB(0,255,0)
        TestAssertions.assertEquals(0xFF0000FF.toInt(), Color.Companion.RED.toRgba())

        // Test alpha value of TRANSPARENT
        TestAssertions.assertEquals(0, Color.Companion.TRANSPARENT.a)
    }

    @Test
    fun testEquality() {
        // Test equality of colors
        val color1 = Color.Companion.fromRgba(0x112233FF.toInt())
        val color2 = Color.Companion.fromRgba(0x112233FF.toInt())
        val color3 = Color.Companion.fromRgba(0x112244FF.toInt())

        TestAssertions.assertEquals(color1, color2)
        TestAssertions.assertNotEquals(color1, color3)
        TestAssertions.assertEquals(color1.hashCode(), color2.hashCode())
        TestAssertions.assertNotEquals(color1.hashCode(), color3.hashCode())
    }

    @Test
    fun testToString() {
        // Test toString() with opaque color
        val opaqueColor = Color.Companion.fromRgb(100, 150, 200)
        TestAssertions.assertEquals("Color(r=100, g=150, b=200)", opaqueColor.toString())

        // Test toString() with transparent color
        val transparentColor = Color.Companion.fromRgba(100, 150, 200, 128)
        TestAssertions.assertEquals("Color(r=100, g=150, b=200, a=128)", transparentColor.toString())
    }

    @Test
    fun testToHexColor() {
        // Test toHexColor() with opaque color
        val opaqueColor = Color.Companion.fromRgb(17, 34, 51)
        TestAssertions.assertEquals("#112233", opaqueColor.toHexColor())

        // Test toHexColor() with transparent color
        val transparentColor = Color.Companion.fromRgba(17, 34, 51, 128)
        TestAssertions.assertEquals("#11223380", transparentColor.toHexColor())
    }
}