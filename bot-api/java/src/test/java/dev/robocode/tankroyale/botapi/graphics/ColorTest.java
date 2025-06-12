package dev.robocode.tankroyale.botapi.graphics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorTest {

    @Test
    public void testFromRgba_singleParameter() {
        // Test creation from single RGBA integer
        int rgbaValue = 0x112233FF; // Red: 17, Green: 34, Blue: 51, Alpha: 255
        Color color = Color.fromRgba(rgbaValue);

        assertEquals(255, color.getA());
        assertEquals(17, color.getR());
        assertEquals(34, color.getG());
        assertEquals(51, color.getB());
        assertEquals(rgbaValue, color.toRgba());
    }

    @Test
    public void testFromRgba_rgbParameters() {
        // Test creation from RGB values (alpha should be 255)
        Color color = Color.fromRgb(100, 150, 200);

        assertEquals(255, color.getA());
        assertEquals(100, color.getR());
        assertEquals(150, color.getG());
        assertEquals(200, color.getB());
        assertEquals(0x6496C8FF, color.toRgba());
    }

    @Test
    public void testFromRgba_rgbaParameters() {
        // Test creation from RGBA values
        Color color = Color.fromRgba(64, 32, 16, 128);

        assertEquals(128, color.getA());
        assertEquals(64, color.getR());
        assertEquals(32, color.getG());
        assertEquals(16, color.getB());
        assertEquals(0x40201080, color.toRgba());
    }

    @Test
    public void testFromRgba_baseColor() {
        // Test creation with a base color and alpha
        Color baseColor = Color.fromRgb(255, 200, 100);
        Color color = Color.fromRgba(baseColor, 50);

        assertEquals(50, color.getA());
        assertEquals(255, color.getR());
        assertEquals(200, color.getG());
        assertEquals(100, color.getB());
    }

    @Test
    public void testToRgba() {
        // Create a color and test toRgba() returns the correct value
        int rgbaValue = 0x11223344;
        Color color = Color.fromRgba(rgbaValue);

        assertEquals(rgbaValue, color.toRgba());
    }

    @Test
    public void testRgbBoundsHandling() {
        // Test RGB values are properly bounded to 0-255
        Color color = Color.fromRgba(300, 300, 300, 300);

        // Values should be truncated to 0xFF (255)
        assertEquals(300 & 0xFF, color.getA());
        assertEquals(300 & 0xFF, color.getR());
        assertEquals(300 & 0xFF, color.getG());
        assertEquals(300 & 0xFF, color.getB());
    }

    @Test
    public void testPredefinedColors() {
        // Test a few predefined colors
        assertEquals(0x000000FF, Color.BLACK.toRgba());
        assertEquals(0xFFFFFFFF, Color.WHITE.toRgba());
        assertEquals(0x0000FFFF, Color.BLUE.toRgba());
        assertEquals(0x00FF00FF, Color.LIME.toRgba()); // Note: LIME is RGB(0,255,0)
        assertEquals(0xFF0000FF, Color.RED.toRgba());

        // Test alpha value of TRANSPARENT
        assertEquals(0, Color.TRANSPARENT.getA());
    }

    @Test
    public void testEquality() {
        // Test equality of colors
        Color color1 = Color.fromRgba(0x112233FF);
        Color color2 = Color.fromRgba(0x112233FF);
        Color color3 = Color.fromRgba(0x112244FF);

        assertEquals(color1, color2);
        assertNotEquals(color1, color3);
        assertEquals(color1.hashCode(), color2.hashCode());
        assertNotEquals(color1.hashCode(), color3.hashCode());
    }

    @Test
    public void testToString() {
        // Test toString() with opaque color
        Color opaqueColor = Color.fromRgb(100, 150, 200);
        assertEquals("Color(r=100, g=150, b=200)", opaqueColor.toString());

        // Test toString() with transparent color
        Color transparentColor = Color.fromRgba(100, 150, 200, 128);
        assertEquals("Color(r=100, g=150, b=200, a=128)", transparentColor.toString());
    }

    @Test
    public void testToHexColor() {
        // Test toHexColor() with opaque color
        Color opaqueColor = Color.fromRgb(17, 34, 51);
        assertEquals("#112233", opaqueColor.toHexColor());

        // Test toHexColor() with transparent color
        Color transparentColor = Color.fromRgba(17, 34, 51, 128);
        assertEquals("#11223380", transparentColor.toHexColor());
    }
}
