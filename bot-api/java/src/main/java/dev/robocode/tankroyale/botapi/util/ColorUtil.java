package dev.robocode.tankroyale.botapi.util;

import java.awt.Color;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern NUMERIC_RGB = Pattern.compile("^#[0-9a-fA-F]{3,6}$");

    private static final Pattern HEX_DIGITS = Pattern.compile("^(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");


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
        return (color == null) ? null : toHex(color.getRed()) + toHex(color.getGreen()) + toHex(color.getBlue());
    }

    private static String toHex(int value) {
        return Integer.toHexString(value >> 4) + Integer.toHexString(value & 0xF);
    }

    /**
     * Creates a color from a string. Currently, only numeric RGB values are supported.
     * This method works the same was as {@link #fromHex} except that is required as hash sign before the hex value.
     * An example of a numeric RGB value is "#09C" or "#0099CC", which both represents the same color.
     *
     * @param str is a string containing either a three or six hexadecimal RGB values like "#09C" or "#0099CC".
     * @return the created Color; {@code null} if the input parameter is {@code null}.
     * @see <a href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</a>
     * @see <a href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</a>
     */
    public static Color fromString(String str) {
        if (str == null) return null;
        str = str.trim();
        if (NUMERIC_RGB.matcher(str).matches()) {
            return fromHex(str.substring(1));
        }
        throw new IllegalArgumentException("You must supply the string in numeric RGB format #[0-9a-fA-F], e.g. \"#09C\" or \"#0099CC\"");
    }

    /**
     * Creates a color from a hex triplet. A hex triplet is either three or six hexadecimal digits that represents an
     * RGB Color.<br>
     * An example of a hex triplet is "09C" or "0099CC", which both represents the same color.
     *
     * @param hexTriplet is a string containing either a three or six hexadecimal numbers like "09C" or "0099CC".
     * @return the created Color.
     * @see <a href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</a>
     * @see <a href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</a>
     */
    public static Color fromHex(String hexTriplet) {
        hexTriplet = hexTriplet.trim();
        if (!HEX_DIGITS.matcher(hexTriplet).matches()) {
            throw new IllegalArgumentException("You must supply 3 or 6 hex digits [0-9a-fA-F], e.g. \"09C\" or \"0099CC\"");
        }

        boolean isThreeDigits = hexTriplet.length() == 3;
        int componentLength = isThreeDigits ? 1 : 2;

        int r = Integer.valueOf(hexTriplet.substring(0, componentLength), 16);
        int g = Integer.valueOf(hexTriplet.substring(componentLength, componentLength * 2), 16);
        int b = Integer.valueOf(hexTriplet.substring(componentLength * 2, componentLength * 3), 16);

        if (isThreeDigits) {
            r = r << 4 | r;
            g = g << 4 | g;
            b = b << 4 | b;
        }
        return new Color(r, g, b);
    }
}
