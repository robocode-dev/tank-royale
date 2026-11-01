using System;
using System.Text.RegularExpressions;
using Robocode.TankRoyale.BotApi.Graphics;

namespace Robocode.TankRoyale.BotApi.Util;

/// <summary>
/// Color represented in RGB format.
/// </summary>
/// <see href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</see>
static class ColorUtil
{
    private const string NumericRgb = "^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$";

    private const string HexDigits = "^(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$";

    /// <summary>
    /// Converts the specified <c>Color</c> object to a hex triplet string representation.
    /// The hex triplet consists of six hexadecimal digits representing an RGB color, e.g., "0099CC".
    /// </summary>
    /// <param name="color">The <c>Color</c> object to convert to a hex triplet</param>
    /// <returns>A string representing the color as a hex triplet of six hexadecimal digits</returns>
    internal static string ToHex(Color? color)
    {
        return color == null ? null : ToHex(color.Value.R) + ToHex(color.Value.G) + ToHex(color.Value.B);
    }

    private static string ToHex(uint value)
    {
        return "" + (value >> 4).ToString("X") + (value & 0xF).ToString("X");
    }

    /// <summary>
    /// Creates a color from a hex color, which is a hex string with a leading hash sign. The same four forms are
    /// accepted as by <see cref="FromHex"/>: "#RGB", "#RGBA", "#RRGGBB" and "#RRGGBBAA", in capital or small letters.
    ///
    /// An example is "#09C" or "#0099CC", which both represent the same, fully opaque color.
    /// </summary>
    /// <param name="str">A string like "#09C", "#09C8", "#0099CC" or "#0099CC88". Surrounding spaces are ignored.
    /// </param>
    /// <returns>The created Color; <c>null</c> if the input parameter is <c>null</c>.</returns>
    /// <exception cref="ArgumentException"/>
    /// <see href="https://www.w3schools.com/colors/colors_rgb.asp">Colors RGB</see>
    /// <see href="https://en.wikipedia.org/wiki/Web_colors">Web Colors</see>
    internal static Color? FromHexColor(string str)
    {
        if (str == null) return null;
        str = str.Trim();
        if (Regex.Match(str, NumericRgb).Success)
        {
            return FromHex(str[1..]);
        }

        throw new ArgumentException(
            "You must supply the string in the format #RGB, #RGBA, #RRGGBB or #RRGGBBAA, e.g. \"#09C\" or \"#0099CC\"");
    }

    /// <summary>
    /// Creates a color from a hex string without a leading hash sign. Four forms are accepted, in capital or small
    /// letters: three digits (RGB), four digits (RGBA), six digits (RRGGBB) and eight digits (RRGGBBAA). In the short
    /// forms each digit is doubled, so "09C" and "0099CC" represent the same color. Without alpha digits the color
    /// is fully opaque.
    /// </summary>
    /// <param name="hexTriplet">A string containing 3, 4, 6 or 8 hexadecimal digits like "09C" or "0099CC88".</param>
    /// <returns>The created <see cref="Color"/>.</returns>
    /// <exception cref="ArgumentException">Thrown when the input string does not match the required hex triplet format.</exception>
    internal static Color FromHex(string hexTriplet)
    {
        hexTriplet = hexTriplet.Trim();
        if (!Regex.IsMatch(hexTriplet, HexDigits))
        {
            throw new ArgumentException("You must supply 3, 4, 6 or 8 hex digits [0-9a-fA-F], e.g., \"09C\" or \"0099CC\"");
        }

        int length = hexTriplet.Length;
        bool isShort = length is 3 or 4;
        bool hasAlpha = length is 4 or 8;
        int componentLength = isShort ? 1 : 2;

        uint r = (uint)Convert.ToInt32(hexTriplet.Substring(0, componentLength), 16);
        uint g = (uint)Convert.ToInt32(hexTriplet.Substring(componentLength, componentLength), 16);
        uint b = (uint)Convert.ToInt32(hexTriplet.Substring(componentLength * 2, componentLength), 16);
        uint a = hasAlpha ? (uint)Convert.ToInt32(hexTriplet.Substring(componentLength * 3, componentLength), 16) : 255u;

        if (isShort)
        {
            r = (r << 4) | r;
            g = (g << 4) | g;
            b = (b << 4) | b;
            if (hasAlpha) a = (a << 4) | a;
        }

        return Color.FromRgba(r, g, b, a);
    }
}