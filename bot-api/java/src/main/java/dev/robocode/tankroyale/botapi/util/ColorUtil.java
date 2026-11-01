package dev.robocode.tankroyale.botapi.util;

import dev.robocode.tankroyale.botapi.graphics.Color;

import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern NUMERIC_RGB = Pattern.compile("^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");

    private static final Pattern HEX_DIGITS = Pattern.compile("^(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");


    // Hide constructor to prevent instantiation
    private ColorUtil() {
    }

    /**
     * Converts the specified {@code Color} object to a hex triplet string representation.
     * The hex triplet consists of six hexadecimal digits representing an RGB color, e.g., "0099CC".
     *
     * @param color the {@code Color} object to convert to a hex triplet
     * @return a string representing the color as a hex triplet of six hexadecimal digits
     */
    public static String toHex(Color color) {
        return (color == null) ? null : toHex(color.getR()) + toHex(color.getG()) + toHex(color.getB());
    }

    private static String toHex(int value) {
        return Integer.toHexString(value >> 4) + Integer.toHexString(value & 0xF);
    }

    /**
     * Creates a color from a hex color, which is a hex string with a leading hash sign. The same four forms are
     * accepted as by {@link #fromHex}: "#RGB", "#RGBA", "#RRGGBB" and "#RRGGBBAA", in capital or small letters.
     * An example is "#09C" or "#0099CC", which both represent the same, fully opaque color.
     *
     * @param str is a string like "#09C", "#09C8", "#0099CC" or "#0099CC88". Surrounding spaces are ignored.
     * @return the created Color; {@code null} if the input parameter is {@code null}.
     * @throws IllegalArgumentException if the string is not one of the four forms.
     * @see <a href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</a>
     * @see <a href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</a>
     */
    public static Color fromHexColor(String str) {
        if (str == null) return null;
        str = str.trim();
        if (NUMERIC_RGB.matcher(str).matches()) {
            return fromHex(str.substring(1));
        }
        throw new IllegalArgumentException("You must supply the string in the format #RGB, #RGBA, #RRGGBB or #RRGGBBAA, e.g. \"#09C\" or \"#0099CC\"");
    }

    /**
     * Creates a color from a hex string without a leading hash sign. Four forms are accepted, in capital or small
     * letters: three digits (RGB), four digits (RGBA), six digits (RRGGBB) and eight digits (RRGGBBAA). In the short
     * forms each digit is doubled, so "09C" and "0099CC" represent the same color. Without alpha digits the color
     * is fully opaque.
     *
     * @param hexTriplet is a string containing 3, 4, 6 or 8 hexadecimal digits like "09C" or "0099CC88".
     * @return the created Color.
     * @throws IllegalArgumentException if the string is not one of the four forms.
     * @see <a href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</a>
     * @see <a href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</a>
     */
    public static Color fromHex(String hexTriplet) {
        hexTriplet = hexTriplet.trim();
        if (!HEX_DIGITS.matcher(hexTriplet).matches()) {
            throw new IllegalArgumentException("You must supply 3, 4, 6 or 8 hex digits [0-9a-fA-F], e.g. \"09C\" or \"0099CC\"");
        }

        int length = hexTriplet.length();
        boolean isShort = length == 3 || length == 4;
        boolean hasAlpha = length == 4 || length == 8;
        int componentLength = isShort ? 1 : 2;

        int r = Integer.parseInt(hexTriplet.substring(0, componentLength), 16);
        int g = Integer.parseInt(hexTriplet.substring(componentLength, componentLength * 2), 16);
        int b = Integer.parseInt(hexTriplet.substring(componentLength * 2, componentLength * 3), 16);
        int a = hasAlpha ? Integer.parseInt(hexTriplet.substring(componentLength * 3, componentLength * 4), 16) : 255;

        if (isShort) {
            r = r << 4 | r;
            g = g << 4 | g;
            b = b << 4 | b;
            if (hasAlpha) {
                a = a << 4 | a;
            }
        }
        return Color.fromRgba(r, g, b, a);
    }
}
