package dev.robocode.tankroyale.botapi.graphics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ColorTest {

    @Test
    fun givenRgbaInt_whenFromRgba_thenChannelsAndRoundtripMatch() {
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
    fun givenRgb_whenFromRgb_thenAlphaIs255AndRoundtripRgbaMatches() {
        // Test creation from RGB values (alpha should be 255)
        val color = Color.fromRgb(100, 150, 200)

        assertEquals(255, color.a)
        assertEquals(100, color.r)
        assertEquals(150, color.g)
        assertEquals(200, color.b)
        assertEquals(0x6496C8FF, color.toRgba())
    }

    @Test
    fun givenRgbaComponents_whenFromRgba_thenChannelsAndRoundtripMatch() {
        // Test creation from RGBA values
        val color = Color.fromRgba(64, 32, 16, 128)

        assertEquals(128, color.a)
        assertEquals(64, color.r)
        assertEquals(32, color.g)
        assertEquals(16, color.b)
        assertEquals(0x40201080, color.toRgba())
    }

    @Test
    fun givenBaseColorAndAlpha_whenFromRgba_thenReturnedColorHasBaseRgbAndAlpha() {
        // Test creation with a base color and alpha
        val baseColor = Color.fromRgb(255, 200, 100)
        val color = Color.fromRgba(baseColor, 50)

        assertEquals(50, color.a)
        assertEquals(255, color.r)
        assertEquals(200, color.g)
        assertEquals(100, color.b)
    }

    @Test
    fun givenRgbaInt_whenToRgba_thenReturnsSameInt() {
        // Create a color and test toRgba() returns the correct value
        val rgbaValue = 0x11223344
        val color = Color.fromRgba(rgbaValue)

        assertEquals(rgbaValue, color.toRgba())
    }

    @Test
    fun givenOutOfRangeRgba_whenFromRgba_thenChannelsAreClampedToByte() {
        // Test RGB values are properly bounded to 0-255
        val color = Color.fromRgba(300, 300, 300, 300)

        // Values should be truncated to 0xFF (255)
        assertEquals(300 and 0xFF, color.a)
        assertEquals(300 and 0xFF, color.r)
        assertEquals(300 and 0xFF, color.g)
        assertEquals(300 and 0xFF, color.b)
    }

    @Test
    fun givenPredefinedColors_whenToRgba_thenValuesMatchAndTransparentAlphaIsZero() {
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
    fun givenColors_whenEqualsAndHashCode_thenBehaveAsExpected() {
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
    fun givenOpaqueAndTransparentColors_whenToString_thenFormattedStringMatches() {
        // Test toString() with opaque color
        val opaqueColor = Color.fromRgb(100, 150, 200)
        assertEquals("Color(r=100, g=150, b=200)", opaqueColor.toString())

        // Test toString() with transparent color
        val transparentColor = Color.fromRgba(100, 150, 200, 128)
        assertEquals("Color(r=100, g=150, b=200, a=128)", transparentColor.toString())
    }

    @Test
    fun givenOpaqueAndTransparentColors_whenToHexColor_thenHexStringMatches() {
        // Test toHexColor() with opaque color
        val opaqueColor = Color.fromRgb(17, 34, 51)
        assertEquals("#112233", opaqueColor.toHexColor())

        // Test toHexColor() with transparent color
        val transparentColor = Color.fromRgba(17, 34, 51, 128)
        assertEquals("#11223380", transparentColor.toHexColor())
    }

    // --- Merged from ColorConstantsTest ---

    @Test
    fun givenTransparentAndWhiteColors_whenToRgbaAndToHex_thenExpectedValues() {
        // TRANSPARENT should have alpha 0 with white RGB per Java API
        val transparent = Color.TRANSPARENT
        assertEquals(0, transparent.a)
        assertEquals(0xFFFFFF00.toInt(), transparent.toRgba())
        assertEquals("#FFFFFF00", transparent.toHexColor())

        val white = Color.WHITE
        assertEquals(0xFFFFFFFF.toInt(), white.toRgba())
        assertEquals("#FFFFFF", white.toHexColor())
    }

    @Test
    fun givenSelectedNamedColors_whenToRgba_thenMatchJavaValues() {
        // Spot-check a selection of named colors, values taken from Java API
        val expected = mapOf(
            "ALICE_BLUE" to 0xF0F8FFFF.toInt(),
            "HOT_PINK" to 0xFF69B4FF.toInt(),
            "ROYAL_BLUE" to 0x4169E1FF,
            "SPRING_GREEN" to 0x00FF7FFF,
            "SLATE_GRAY" to 0x708090FF,
            "ORANGE_RED" to 0xFF4500FF.toInt(),
            "DARK_TURQUOISE" to 0x00CED1FF,
            "LIME_GREEN" to 0x32CD32FF
        )
        val actual = mapOf(
            "ALICE_BLUE" to Color.ALICE_BLUE.toRgba(),
            "HOT_PINK" to Color.HOT_PINK.toRgba(),
            "ROYAL_BLUE" to Color.ROYAL_BLUE.toRgba(),
            "SPRING_GREEN" to Color.SPRING_GREEN.toRgba(),
            "SLATE_GRAY" to Color.SLATE_GRAY.toRgba(),
            "ORANGE_RED" to Color.ORANGE_RED.toRgba(),
            "DARK_TURQUOISE" to Color.DARK_TURQUOISE.toRgba(),
            "LIME_GREEN" to Color.LIME_GREEN.toRgba()
        )
        for ((name, exp) in expected) {
            assertEquals(exp, actual[name], "Color constant $name mismatch")
        }
    }

    @Test
    fun givenSvgGraphics_whenCheckingType_thenIsIGraphics() {
        val g = SvgGraphics()
        assertTrue(g is IGraphics)
    }
}
