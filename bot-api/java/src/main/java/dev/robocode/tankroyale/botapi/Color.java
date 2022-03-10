package dev.robocode.tankroyale.botapi;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Color represented in RGB format.
 *
 * @see <a href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</a>.
 */
public class Color {

    private static final Pattern threeHexDigits = Pattern.compile("^[0-9a-fA-F]{3}$");
    private static final Pattern sixHexDigits = Pattern.compile("^[0-9a-fA-F]{6}$");

    public static final Color WHITE = fromHexTriplet("FFFFFF");
    public static final Color SILVER = fromHexTriplet("C0C0C0");
    public static final Color GRAY = fromHexTriplet("808080");
    public static final Color BLACK = fromHexTriplet("000000");
    public static final Color RED = fromHexTriplet("FF0000");
    public static final Color MAROON = fromHexTriplet("800000");
    public static final Color YELLOW = fromHexTriplet("FFFF00");
    public static final Color OLIVE = fromHexTriplet("808000");
    public static final Color LIME = fromHexTriplet("00FF00");
    public static final Color GREEN = fromHexTriplet("008000");
    public static final Color CYAN = fromHexTriplet("00FFFF");
    public static final Color TEAL = fromHexTriplet("008080");
    public static final Color BLUE = fromHexTriplet("0000FF");
    public static final Color NAVY = fromHexTriplet("000080");
    public static final Color FUCHSIA = fromHexTriplet("FF00FF");
    public static final Color PURPLE = fromHexTriplet("800080");
    public static final Color ORANGE = fromHexTriplet("FF8000");

    private final int red; // [0-255];
    private final int green; // [0-255];
    private final int blue; // [0-255];

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
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Returns the red color component of the color.
     *
     * @return the red color component of the RGB color in the range [0 - 255]
     */
    public int getRed() {
        return red;
    }

    /**
     * Returns the green color component of the color.
     *
     * @return the green color component of the RGB color in the range [0 - 255]
     */
    public int getGreen() {
        return green;
    }

    /**
     * Returns the blue color component of the color.
     *
     * @return the blue color component of the RGB color in the range [0 - 255]
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Returns the color as a hex triplet of six hexadecimal digits representing an RGB color, e.g. "0099CC".
     *
     * @return the color as a hex triplet of six hexadecimal digits representing an RGB color, e.g. "0099CC".
     */
    public String toHexTriplet() {
        return toHex(red) + toHex(green) + toHex(blue);
    }

    private static String toHex(int value) {
        return "" + Integer.toHexString(value >> 4) + Integer.toHexString(value & 0xF);
    }

    /**
     * Creates a Color from an RGB integer value.
     *
     * @param rgb RGB value, where bit 0-7 is blue, bit 8-15 is green, and bit 16-23 is red.
     * @return the created Color or <code>null</code> if the input value is <code>null</code>.
     */
    public static Color fromRgbInt(Integer rgb) {
        if (rgb == null) {
            return null;
        }
        int r = (rgb & 0xFF0000) >> 16;
        int g = (rgb & 0x00FF00) >> 8;
        int b = rgb & 0x0000FF;
        return new Color(r, g, b);
    }

    /**
     * Creates a color from a hex triplet. A hex triplet is either three or six hexadecimal digits that represents an
     * RGB Color.<br>
     * An example of a hex triplet is "09C" or "0099CC", which both represents the same color.<br>
     *
     * @param hexTriplet is a string containing either a three or six hexadecimal numbers like "09C" or "0099CC".
     * @return the created Color.
     * @see <a href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</a>
     * @see <a href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</a>
     */
    public static Color fromHexTriplet(String hexTriplet) {
        if (threeHexDigits.matcher(hexTriplet).matches()) {
            return fromThreeHexDigits(hexTriplet);
        }
        if (sixHexDigits.matcher(hexTriplet).matches()) {
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
        return red == other.red && green == other.green && blue == other.blue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equals((Color) o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }

    @Override
    public String toString() {
        return toHexTriplet();
    }
}