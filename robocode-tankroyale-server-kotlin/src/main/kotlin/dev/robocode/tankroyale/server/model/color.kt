package dev.robocode.tankroyale.server.model

import java.util.regex.Pattern

private val NUMERIC_RGB: Pattern = Pattern.compile("^#[0-9A-F]{3,6}$", Pattern.CASE_INSENSITIVE)

/**
 * Converts a string represented in a numeric format #<red><green><blue> into an integer presentation of the RGB color
 * value in 24-bit. Currently, only numeric representations of colors is supported. Later on, more formats might be
 * supported.
 * <p>
 * Two formats are currently supported, where is RGB color value is either 24-bit (3 x 8-bit color channels) or
 * 12-bit (3 x 4-bit color channels).
 * For example, the saddlebrown color can be represented as the 24-bit version "#8B4513", where red, green, blue
 * are the hex values 8B, 45, and 13. The returned integer value will be 0x8B4513 (24-bit format).
 * The same color can also be represented with a 12-bit version in lower color resolution "#941", where red, green,
 * blue are the hex values 9, 4, and 1. The returned integer value will be 0x994411 (24-bit format).
 *
 * @param colorStr is the string representation of a RGB color, e.g. "#8B4513" (24-bit format) or "#941" (12-bit format).
 * @return is an integer value, representing the RGB value in 24-format, e.g. 0x8B4513 or 0x994411.
 */
fun colorStringToRGB(colorStr: String?): Int? {
    if (colorStr == null) {
        return null
    }
    val str = colorStr.trim()
    if (!NUMERIC_RGB.matcher(colorStr).matches()) {
        return null
    }
    if (str.length == 7) {
        return hexToRgb24bit(str)
    }
    return if (str.length == 4) hexToRgb12bit(str) else null
}

/**
 * Converts a string representation of a 24-bit RGB value into an integer representation of the RGB value in 24-bit.
 * E.g. "#8B4513" is converted into the integer 0x8B4513.
 *
 * @param colorStr is the string representation of a 24-bit RGB color, e.g. "#8B4513".
 * @return is an integer value, representing the RGB value in 24-format, e.g. 0x8B4513.
 */
fun hexToRgb24bit(colorStr: String): Int {
    val r = Integer.valueOf(colorStr.substring(1, 3), 16)
    val g = Integer.valueOf(colorStr.substring(3, 5), 16)
    val b = Integer.valueOf(colorStr.substring(5, 7), 16)
    return r shl 16 or (g shl 8) or b
}

/**
 * Converts a string representation of a 12-bit RGB value into an integer representation of the RGB value in 24-bit.
 * E.g. "#941" is converted into the integer 0x994411.
 *
 * @param colorStr is the string representation of a 12-bit RGB color, e.g. "#941".
 * @return is an integer value, representing the RGB value in 24-format, e.g. 0x994411.
 */
fun hexToRgb12bit(colorStr: String): Int {
    var r = Integer.valueOf(colorStr.substring(1, 2), 16)
    var g = Integer.valueOf(colorStr.substring(2, 3), 16)
    var b = Integer.valueOf(colorStr.substring(3, 4), 16)
    r = r shl 4 or r
    g = g shl 4 or g
    b = b shl 4 or b
    return r shl 16 or (g shl 8) or b
}