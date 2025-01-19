using System;
using System.Drawing;
using System.Text.RegularExpressions;

namespace Robocode.TankRoyale.BotApi.Util;

/// <summary>
/// Color represented in RGB format.
/// </summary>
/// <see href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</see>
public static class ColorUtil
{
    private const string NumericRgb = "^#[0-9a-fA-F]{3,6}$";

    private const string HexDigits = "^[0-9a-fA-F]{3}$|^[0-9a-fA-F]{6}$";

    /// <summary>
    /// Converts the specified <c>Color</c> object to a hex triplet string representation.
    /// The hex triplet consists of six hexadecimal digits representing an RGB color, e.g., "0099CC".
    /// </summary>
    /// <param name="color">The <c>Color</c> object to convert to a hex triplet</param>
    /// <returns>A string representing the color as a hex triplet of six hexadecimal digits</returns>
    public static string ToHex(Color? color)
    {
        return color == null ? null : ToHex(color.Value.R) + ToHex(color.Value.G) + ToHex(color.Value.B);
    }

    private static string ToHex(int value)
    {
        return "" + (value >> 4).ToString("X") + (value & 0xF).ToString("X");
    }

    /// <summary>
    /// Creates a color from a string. Currently, only numeric RGB values are supported.
    /// This method works the same was as <see cref="FromHex"/> except that is required as hash sign before the hex value.
    ///
    /// An example of a numeric RGB value is "#09C" or "#0099CC", which both represents the same color.
    /// </summary>
    /// <param name="str">A string containing either a three or six hexadecimal RGB values like "#09C" or "#0099CC".
    /// </param>
    /// <returns>The created Color; <c>null</c> if the input parameter is <c>null</c>.</returns>
    /// <exception cref="ArgumentException"/>
    /// <see href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</see>
    /// <see href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</see>
    public static Color? FromString(string str)
    {
        if (str == null) return null;
        str = str.Trim();
        if (Regex.Match(str, NumericRgb).Success)
        {
            return FromHex(str[1..]);
        }

        throw new ArgumentException(
            "You must supply the string in numeric RGB format #[0-9a-fA-F], e.g. \"#09C\" or \"#0099CC\"");
    }

    /// <summary>
    /// Creates a color from a hex triplet. A hex triplet is either three or six hexadecimal digits representing an RGB color.
    /// An example of a hex triplet is "09C" or "0099CC", which both represent the same color.
    /// </summary>
    /// <param name="hexTriplet">A string containing either three or six hexadecimal numbers like "09C" or "0099CC".</param>
    /// <returns>The created <see cref="Color"/>.</returns>
    /// <exception cref="ArgumentException">Thrown when the input string does not match the required hex triplet format.</exception>
    public static Color FromHex(string hexTriplet)
    {
        hexTriplet = hexTriplet.Trim();
        if (!Regex.IsMatch(hexTriplet, HexDigits))
        {
            throw new ArgumentException("You must supply 3 or 6 hex digits [0-9a-fA-F], e.g., \"09C\" or \"0099CC\"");
        }

        bool isThreeDigits = hexTriplet.Length == 3;
        int componentLength = isThreeDigits ? 1 : 2;

        int r = Convert.ToInt32(hexTriplet.Substring(0, componentLength), 16);
        int g = Convert.ToInt32(hexTriplet.Substring(componentLength, componentLength), 16);
        int b = Convert.ToInt32(hexTriplet.Substring(componentLength * 2, componentLength), 16);

        if (isThreeDigits)
        {
            r = (r << 4) | r;
            g = (g << 4) | g;
            b = (b << 4) | b;
        }

        return Color.FromArgb(r, g, b);
    }
}