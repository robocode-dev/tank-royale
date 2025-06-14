namespace Robocode.TankRoyale.BotApi.Graphics;

using System;
using JetBrains.Annotations;

/// <summary>
/// Represents an RGBA (red, green, blue, alpha) color for use in the Tank Royale game.
/// This struct provides methods for creating and manipulating colors.
/// </summary>
[PublicAPI]
public readonly struct Color : IEquatable<Color>
{
    private readonly uint _rgba;

    private Color(uint rgba)
    {
        _rgba = rgba;
    }

    /// <summary>
    /// Gets the red component value of this color.
    /// </summary>
    /// <value>The red component value between 0 and 255.</value>
    public byte R => (byte)((_rgba >> 24) & 0xFF);

    /// <summary>
    /// Gets the green component value of this color.
    /// </summary>
    /// <value>The green component value between 0 and 255.</value>
    public byte G => (byte)((_rgba >> 16) & 0xFF);

    /// <summary>
    /// Gets the blue component value of this color.
    /// </summary>
    /// <value>The blue component value between 0 and 255.</value>
    public byte B => (byte)((_rgba >> 8) & 0xFF);
    
    // ARGB properties
    /// <summary>
    /// Gets the alpha component value of this color.
    /// </summary>
    /// <value>The alpha component value between 0 and 255.</value>
    public byte A => (byte)(_rgba & 0xFF);

    // Factory methods
    /// <summary>
    /// Creates a color from a 32-bit RGBA value.
    /// </summary>
    /// <param name="rgba">A 32-bit value specifying the RGBA components.</param>
    /// <returns>A new Color structure initialized with the specified RGBA value.</returns>
    public static Color FromRgba(uint rgba) => new(rgba);

    /// <summary>
    /// Creates a color from the specified red, green, blue, and alpha values.
    /// </summary>
    /// <param name="r">The red component value (0-255).</param>
    /// <param name="g">The green component value (0-255).</param>
    /// <param name="b">The blue component value (0-255).</param>
    /// <param name="a">The alpha component value (0-255).</param>
    /// <returns>A new Color structure initialized with the specified RGBA values.</returns>
    public static Color FromRgba(uint r, uint g, uint b, uint a) =>
        new(r << 24 | g << 16 | b << 8 | a);

    /// <summary>
    /// Creates a color from the specified red, green, and blue values, with an alpha value of 255 (fully opaque).
    /// </summary>
    /// <param name="r">The red component value (0-255).</param>
    /// <param name="g">The green component value (0-255).</param>
    /// <param name="b">The blue component value (0-255).</param>
    /// <returns>A new Color structure initialized with the specified RGB values and an alpha value of 255.</returns>
    public static Color FromRgb(uint r, uint g, uint b) =>
        FromRgba(r, g, b, 255);

    /// <summary>
    /// Creates a color from the specified base color with a new alpha value.
    /// </summary>
    /// <param name="baseColor">The Color structure from which to derive the RGB values.</param>
    /// <param name="a">The alpha component value (0-255).</param>
    /// <returns>A new Color structure with the RGB values from the base color and the specified alpha value.</returns>
    public static Color FromRgba(Color baseColor, uint a) =>
        FromRgba(baseColor.R, baseColor.G, baseColor.B, a);

    // Convert to int (RGBA)
    /// <summary>
    /// Converts this Color structure to a 32-bit RGBA value.
    /// </summary>
    /// <returns>A 32-bit integer containing the RGBA representation of this color.</returns>
    public uint ToRgba() => _rgba;

    // Common colors
    public static Color Transparent => FromRgba(255, 255, 255, 0);
    public static Color AliceBlue => FromRgb(240, 248, 255);
    public static Color AntiqueWhite => FromRgb(250, 235, 215);
    public static Color Aqua => FromRgb(0, 255, 255);
    public static Color Aquamarine => FromRgb(127, 255, 212);
    public static Color Azure => FromRgb(240, 255, 255);
    public static Color Beige => FromRgb(245, 245, 220);
    public static Color Bisque => FromRgb(255, 228, 196);
    public static Color Black => FromRgb(0, 0, 0);
    public static Color BlanchedAlmond => FromRgb(255, 235, 205);
    public static Color Blue => FromRgb(0, 0, 255);
    public static Color BlueViolet => FromRgb(138, 43, 226);
    public static Color Brown => FromRgb(165, 42, 42);
    public static Color BurlyWood => FromRgb(222, 184, 135);
    public static Color CadetBlue => FromRgb(95, 158, 160);
    public static Color Chartreuse => FromRgb(127, 255, 0);
    public static Color Chocolate => FromRgb(210, 105, 30);
    public static Color Coral => FromRgb(255, 127, 80);
    public static Color CornflowerBlue => FromRgb(100, 149, 237);
    public static Color Cornsilk => FromRgb(255, 248, 220);
    public static Color Crimson => FromRgb(220, 20, 60);
    public static Color Cyan => FromRgb(0, 255, 255);
    public static Color DarkBlue => FromRgb(0, 0, 139);
    public static Color DarkCyan => FromRgb(0, 139, 139);
    public static Color DarkGoldenrod => FromRgb(184, 134, 11);
    public static Color DarkGray => FromRgb(169, 169, 169);
    public static Color DarkGreen => FromRgb(0, 100, 0);
    public static Color DarkKhaki => FromRgb(189, 183, 107);
    public static Color DarkMagenta => FromRgb(139, 0, 139);
    public static Color DarkOliveGreen => FromRgb(85, 107, 47);
    public static Color DarkOrange => FromRgb(255, 140, 0);
    public static Color DarkOrchid => FromRgb(153, 50, 204);
    public static Color DarkRed => FromRgb(139, 0, 0);
    public static Color DarkSalmon => FromRgb(233, 150, 122);
    public static Color DarkSeaGreen => FromRgb(143, 188, 139);
    public static Color DarkSlateBlue => FromRgb(72, 61, 139);
    public static Color DarkSlateGray => FromRgb(47, 79, 79);
    public static Color DarkTurquoise => FromRgb(0, 206, 209);
    public static Color DarkViolet => FromRgb(148, 0, 211);
    public static Color DeepPink => FromRgb(255, 20, 147);
    public static Color DeepSkyBlue => FromRgb(0, 191, 255);
    public static Color DimGray => FromRgb(105, 105, 105);
    public static Color DodgerBlue => FromRgb(30, 144, 255);
    public static Color Firebrick => FromRgb(178, 34, 34);
    public static Color FloralWhite => FromRgb(255, 250, 240);
    public static Color ForestGreen => FromRgb(34, 139, 34);
    public static Color Fuchsia => FromRgb(255, 0, 255);
    public static Color Gainsboro => FromRgb(220, 220, 220);
    public static Color GhostWhite => FromRgb(248, 248, 255);
    public static Color Gold => FromRgb(255, 215, 0);
    public static Color Goldenrod => FromRgb(218, 165, 32);
    public static Color Gray => FromRgb(128, 128, 128);
    public static Color Green => FromRgb(0, 128, 0);
    public static Color GreenYellow => FromRgb(173, 255, 47);
    public static Color Honeydew => FromRgb(240, 255, 240);
    public static Color HotPink => FromRgb(255, 105, 180);
    public static Color IndianRed => FromRgb(205, 92, 92);
    public static Color Indigo => FromRgb(75, 0, 130);
    public static Color Ivory => FromRgb(255, 255, 240);
    public static Color Khaki => FromRgb(240, 230, 140);
    public static Color Lavender => FromRgb(230, 230, 250);
    public static Color LavenderBlush => FromRgb(255, 240, 245);
    public static Color LawnGreen => FromRgb(124, 252, 0);
    public static Color LemonChiffon => FromRgb(255, 250, 205);
    public static Color LightBlue => FromRgb(173, 216, 230);
    public static Color LightCoral => FromRgb(240, 128, 128);
    public static Color LightCyan => FromRgb(224, 255, 255);
    public static Color LightGoldenrodYellow => FromRgb(250, 250, 210);
    public static Color LightGray => FromRgb(211, 211, 211);
    public static Color LightGreen => FromRgb(144, 238, 144);
    public static Color LightPink => FromRgb(255, 182, 193);
    public static Color LightSalmon => FromRgb(255, 160, 122);
    public static Color LightSeaGreen => FromRgb(32, 178, 170);
    public static Color LightSkyBlue => FromRgb(135, 206, 250);
    public static Color LightSlateGray => FromRgb(119, 136, 153);
    public static Color LightSteelBlue => FromRgb(176, 196, 222);
    public static Color LightYellow => FromRgb(255, 255, 224);
    public static Color Lime => FromRgb(0, 255, 0);
    public static Color LimeGreen => FromRgb(50, 205, 50);
    public static Color Linen => FromRgb(250, 240, 230);
    public static Color Magenta => FromRgb(255, 0, 255);
    public static Color Maroon => FromRgb(128, 0, 0);
    public static Color MediumAquamarine => FromRgb(102, 205, 170);
    public static Color MediumBlue => FromRgb(0, 0, 205);
    public static Color MediumOrchid => FromRgb(186, 85, 211);
    public static Color MediumPurple => FromRgb(147, 112, 219);
    public static Color MediumSeaGreen => FromRgb(60, 179, 113);
    public static Color MediumSlateBlue => FromRgb(123, 104, 238);
    public static Color MediumSpringGreen => FromRgb(0, 250, 154);
    public static Color MediumTurquoise => FromRgb(72, 209, 204);
    public static Color MediumVioletRed => FromRgb(199, 21, 133);
    public static Color MidnightBlue => FromRgb(25, 25, 112);
    public static Color MintCream => FromRgb(245, 255, 250);
    public static Color MistyRose => FromRgb(255, 228, 225);
    public static Color Moccasin => FromRgb(255, 228, 181);
    public static Color NavajoWhite => FromRgb(255, 222, 173);
    public static Color Navy => FromRgb(0, 0, 128);
    public static Color OldLace => FromRgb(253, 245, 230);
    public static Color Olive => FromRgb(128, 128, 0);
    public static Color OliveDrab => FromRgb(107, 142, 35);
    public static Color Orange => FromRgb(255, 165, 0);
    public static Color OrangeRed => FromRgb(255, 69, 0);
    public static Color Orchid => FromRgb(218, 112, 214);
    public static Color PaleGoldenrod => FromRgb(238, 232, 170);
    public static Color PaleGreen => FromRgb(152, 251, 152);
    public static Color PaleTurquoise => FromRgb(175, 238, 238);
    public static Color PaleVioletRed => FromRgb(219, 112, 147);
    public static Color PapayaWhip => FromRgb(255, 239, 213);
    public static Color PeachPuff => FromRgb(255, 218, 185);
    public static Color Peru => FromRgb(205, 133, 63);
    public static Color Pink => FromRgb(255, 192, 203);
    public static Color Plum => FromRgb(221, 160, 221);
    public static Color PowderBlue => FromRgb(176, 224, 230);
    public static Color Purple => FromRgb(128, 0, 128);
    public static Color Red => FromRgb(255, 0, 0);
    public static Color RosyBrown => FromRgb(188, 143, 143);
    public static Color RoyalBlue => FromRgb(65, 105, 225);
    public static Color SaddleBrown => FromRgb(139, 69, 19);
    public static Color Salmon => FromRgb(250, 128, 114);
    public static Color SandyBrown => FromRgb(244, 164, 96);
    public static Color SeaGreen => FromRgb(46, 139, 87);
    public static Color SeaShell => FromRgb(255, 245, 238);
    public static Color Sienna => FromRgb(160, 82, 45);
    public static Color Silver => FromRgb(192, 192, 192);
    public static Color SkyBlue => FromRgb(135, 206, 235);
    public static Color SlateBlue => FromRgb(106, 90, 205);
    public static Color SlateGray => FromRgb(112, 128, 144);
    public static Color Snow => FromRgb(255, 250, 250);
    public static Color SpringGreen => FromRgb(0, 255, 127);
    public static Color SteelBlue => FromRgb(70, 130, 180);
    public static Color Tan => FromRgb(210, 180, 140);
    public static Color Teal => FromRgb(0, 128, 128);
    public static Color Thistle => FromRgb(216, 191, 216);
    public static Color Tomato => FromRgb(255, 99, 71);
    public static Color Turquoise => FromRgb(64, 224, 208);
    public static Color Violet => FromRgb(238, 130, 238);
    public static Color Wheat => FromRgb(245, 222, 179);
    public static Color White => FromRgb(255, 255, 255);
    public static Color WhiteSmoke => FromRgb(245, 245, 245);
    public static Color Yellow => FromRgb(255, 255, 0);
    public static Color YellowGreen => FromRgb(154, 205, 50);

    // Equality and comparison
    /// <summary>
    /// Determines whether the specified Color is equal to the current Color.
    /// </summary>
    /// <param name="other">The Color to compare with the current Color.</param>
    /// <returns>true if the specified Color is equal to the current Color; otherwise, false.</returns>
    public bool Equals(Color other) => _rgba == other._rgba;

    /// <summary>
    /// Determines whether the specified object is equal to the current Color.
    /// </summary>
    /// <param name="obj">The object to compare with the current Color.</param>
    /// <returns>true if the specified object is a Color and equal to the current Color; otherwise, false.</returns>
    public override bool Equals(object obj) => obj is Color other && Equals(other);

    /// <summary>
    /// Returns the hash code for this Color structure.
    /// </summary>
    /// <returns>An integer hash code for this Color structure.</returns>
    public override int GetHashCode() => _rgba.GetHashCode();

    /// <summary>
    /// Determines whether two Color structures are equal.
    /// </summary>
    /// <param name="left">The first Color to compare.</param>
    /// <param name="right">The second Color to compare.</param>
    /// <returns>true if the two Colors are equal; otherwise, false.</returns>
    public static bool operator ==(Color left, Color right) => left.Equals(right);

    /// <summary>
    /// Determines whether two Color structures are different.
    /// </summary>
    /// <param name="left">The first Color to compare.</param>
    /// <param name="right">The second Color to compare.</param>
    /// <returns>true if the two Colors are different; otherwise, false.</returns>
    public static bool operator !=(Color left, Color right) => !left.Equals(right);

    // String representation
    /// <summary>
    /// Converts this Color structure to a human-readable string.
    /// </summary>
    /// <returns>
    /// A string that represents this Color in the format "Color [R=r, G=g, B=b]" when alpha is 255 (fully opaque),
    /// or "Color [A=a, R=r, G=g, B=b]" when alpha is not 255.
    /// </returns>
    public override string ToString()
    {
        if (A == 255)
            return $"RGB[{R}, {G}, {B}]";
        return $"RGBA[{A}, {R}, {G}, {B}]";
    }
    
    /// <summary>
    /// Converts the color to its hexadecimal representation.
    /// </summary>
    /// <returns>
    /// A string representing the color in hexadecimal format:
    /// - If alpha is 255 (fully opaque), returns #RRGGBB
    /// - If alpha is not 255, returns #RRGGBBAA
    /// </returns>
    /// <remarks>
    /// The method uses uppercase hexadecimal notation with two digits for each color component.
    /// R = Red, G = Green, B = Blue, A = Alpha
    /// </remarks>
    /// <example>
    /// // For a fully opaque red color
    /// Color red = Color.Red;
    /// string hexRed = red.ToHexColor(); // Returns "#FF0000"
    /// 
    /// // For a semi-transparent blue color
    /// Color semiTransparentBlue = Color.FromRgba(0, 0, 255, 128);
    /// string hexBlue = semiTransparentBlue.ToHexColor(); // Returns "#0000FF80"
    /// </example>
    public string ToHexColor()
    {
        if (A == 255)
            return $"#{R:X2}{G:X2}{B:X2}";
        return $"#{R:X2}{G:X2}{B:X2}{A:X2}";
    }

    // Implicit conversion to/from uint (RGBA)
    /// <summary>
    /// Defines an implicit conversion of a Color structure to a 32-bit integer.
    /// </summary>
    /// <param name="color">The Color structure to convert.</param>
    /// <returns>A 32-bit integer containing the RGBA values of the Color structure.</returns>
    public static implicit operator uint(Color color) => color._rgba;

    /// <summary>
    /// Defines an implicit conversion of a 32-bit integer to a Color structure.
    /// </summary>
    /// <param name="rgba">A 32-bit integer containing RGBA values.</param>
    /// <returns>A Color structure with the specified RGBA values.</returns>
    public static implicit operator Color(uint rgba) => FromRgba(rgba);
}