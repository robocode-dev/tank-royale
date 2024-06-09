package dev.robocode.tankroyale.botapi;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Color represented in RGB format.
 *
 * @see <a href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</a>.
 */
public class Color {

    private static final Pattern NUMERIC_RGB = Pattern.compile("^#[0-9a-fA-F]{3,6}$");

    private static final Pattern THREE_HEX_DIGITS = Pattern.compile("^[0-9a-fA-F]{3}$");
    private static final Pattern SIX_HEX_DIGITS = Pattern.compile("^[0-9a-fA-F]{6}$");

    public static final Color WHITE = fromHex("FFFFFF");
    public static final Color SILVER = fromHex("C0C0C0");
    public static final Color GRAY = fromHex("808080");
    public static final Color BLACK = fromHex("000000");
    public static final Color RED = fromHex("FF0000");
    public static final Color MAROON = fromHex("800000");
    public static final Color YELLOW = fromHex("FFFF00");
    public static final Color OLIVE = fromHex("808000");
    public static final Color LIME = fromHex("00FF00");
    public static final Color GREEN = fromHex("008000");
    public static final Color CYAN = fromHex("00FFFF");
    public static final Color TEAL = fromHex("008080");
    public static final Color BLUE = fromHex("0000FF");
    public static final Color NAVY = fromHex("000080");
    public static final Color FUCHSIA = fromHex("FF00FF");
    public static final Color PURPLE = fromHex("800080");
    public static final Color ORANGE = fromHex("FF8000");

    private final int redValue; // [0-255]
    private final int greenValue; // [0-255]
    private final int blueValue; // [0-255]

    /**
     * Creates a Color from RGB values.
     *
     * @param red   is the red color component of the RGB color in the range [0 - 255]
     * @param green is the green color component of the RGB color in the range [0 - 255]
     * @param blue  is the red blue component of the RGB color in the range [0 - 255]
     * @see <a href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</a>.
     */
    public Color(int red, int green, int blue) {
        if (red < 0 || red > 255) {
            throw new IllegalArgumentException("The 'red' color component must be in the range 0 - 255");
        }
        if (green < 0 || green > 255) {
            throw new IllegalArgumentException("The 'green' color component must be in the range 0 - 255");
        }
        if (blue < 0 || blue > 255) {
            throw new IllegalArgumentException("The 'blue' color component must be in the range 0 - 255");
        }
        this.redValue = red;
        this.greenValue = green;
        this.blueValue = blue;
    }

    /**
     * Returns the red color component of the color.
     *
     * @return the red color component of the RGB color in the range [0 - 255]
     */
    public int getRed() {
        return redValue;
    }

    /**
     * Returns the green color component of the color.
     *
     * @return the green color component of the RGB color in the range [0 - 255]
     */
    public int getGreen() {
        return greenValue;
    }

    /**
     * Returns the blue color component of the color.
     *
     * @return the blue color component of the RGB color in the range [0 - 255]
     */
    public int getBlue() {
        return blueValue;
    }

    /**
     * Returns the color as a hex triplet of six hexadecimal digits representing an RGB color, e.g. "0099CC".
     *
     * @return the color as a hex triplet of six hexadecimal digits representing an RGB color, e.g. "0099CC".
     */
    public String toHex() {
        return toHex(redValue) + toHex(greenValue) + toHex(blueValue);
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
        if (THREE_HEX_DIGITS.matcher(hexTriplet).matches()) {
            return fromThreeHexDigits(hexTriplet);
        }
        if (SIX_HEX_DIGITS.matcher(hexTriplet).matches()) {
            return fromSixHexDigits(hexTriplet);
        }
        throw new IllegalArgumentException("You must supply 3 or 6 hex digits [0-9a-fA-F], e.g. \"09C\" or \"0099CC\"");
    }

    private static Color fromThreeHexDigits(String threeHexDigits) {
        int r = Integer.valueOf(threeHexDigits.substring(0, 1), 16);
        int g = Integer.valueOf(threeHexDigits.substring(1, 2), 16);
        int b = Integer.valueOf(threeHexDigits.substring(2, 3), 16);
        r = r << 4 | r;
        g = g << 4 | g;
        b = b << 4 | b;
        return new Color(r, g, b);
    }

    private static Color fromSixHexDigits(String sixHexDigits) {
        int r = Integer.valueOf(sixHexDigits.substring(0, 2), 16);
        int g = Integer.valueOf(sixHexDigits.substring(2, 4), 16);
        int b = Integer.valueOf(sixHexDigits.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    protected boolean equals(Color other) {
        return redValue == other.redValue && greenValue == other.greenValue && blueValue == other.blueValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equals((Color) o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redValue, greenValue, blueValue);
    }

    @Override
    public String toString() {
        return toHex();
    }
}