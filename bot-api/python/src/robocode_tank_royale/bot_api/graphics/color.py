from __future__ import annotations

from dataclasses import dataclass

from robocode_tank_royale.schema import Color as ColorSchema


@dataclass(frozen=True)
class Color:
    """Represents an RGBA (red, green, blue, alpha) color used in the Tank Royale game.

    This graphics Color implementation contains:
    - Internal 32-bit RGBA value
    - Factory methods from_rgb(), from_rgba(r,g,b,a), and from_rgba_value(rgba)
    - Read-only channel properties: red, green, blue, alpha (mapped to R,G,B,A)
    - to_rgba() for round-trip, to_hex_color() for hex string
    - Equality/hash based on RGBA value
    - Many predefined common colors

    Note: For compatibility with existing Python code, to_color_schema() is preserved
    and uses lowercase hex values as before.
    """

    # Single internal 32-bit RGBA value, channels stored as: R (bits 31-24), G (23-16), B (15-8), A (7-0)
    _value: int

    # --- Factory methods ---
    @classmethod
    def from_rgba_value(cls, rgba: int) -> Color:
        """Creates a color from a 32-bit RGBA value.

        Args:
            rgba: A 32-bit value specifying the RGBA components.

        Returns:
            A new Color initialized with the specified RGBA value.
        """
        return cls(rgba & 0xFFFFFFFF)

    @classmethod
    def from_rgba(cls, r: int, g: int, b: int, a: int) -> Color:
        """Creates a color from the specified red, green, blue, and alpha values.

        Args:
            r: The red component value (0-255).
            g: The green component value (0-255).
            b: The blue component value (0-255).
            a: The alpha component value (0-255).

        Returns:
            A new Color initialized with the specified RGBA values.
        """
        return cls(((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF))

    @classmethod
    def from_rgb(cls, r: int, g: int, b: int) -> Color:
        """Creates a color from the specified red, green, and blue values with alpha 255 (fully opaque).

        Args:
            r: The red component value (0-255).
            g: The green component value (0-255).
            b: The blue component value (0-255).

        Returns:
            A new Color initialized with the specified RGB values and an alpha value of 255.
        """
        return cls.from_rgba(r, g, b, 255)

    # Convenience to mirror Java's fromRgba(baseColor, a)
    @classmethod
    def from_color_with_alpha(cls, base_color: Color, a: int) -> Color:
        """Creates a color from the specified base color with a new alpha value.

        Args:
            base_color: The Color from which to derive the RGB values.
            a: The alpha component value (0-255).

        Returns:
            A new Color with the RGB values from the base color and the specified alpha value.
        """
        return cls.from_rgba(base_color.red, base_color.green, base_color.blue, a)

    # --- Channel properties ---
    @property
    def red(self) -> int:
        """Gets the red component value of this color.

        Returns:
            The red component value between 0 and 255.
        """
        return (self._value >> 24) & 0xFF

    @property
    def green(self) -> int:
        """Gets the green component value of this color.

        Returns:
            The green component value between 0 and 255.
        """
        return (self._value >> 16) & 0xFF

    @property
    def blue(self) -> int:
        """Gets the blue component value of this color.

        Returns:
            The blue component value between 0 and 255.
        """
        return (self._value >> 8) & 0xFF

    @property
    def alpha(self) -> int:
        """Gets the alpha component value of this color.

        Returns:
            The alpha component value between 0 and 255.
        """
        return self._value & 0xFF

    # Aliases for short names, if needed
    @property
    def r(self) -> int:
        return self.red

    @property
    def g(self) -> int:
        return self.green

    @property
    def b(self) -> int:
        return self.blue

    @property
    def a(self) -> int:
        return self.alpha

    # --- Conversions ---
    def to_rgba(self) -> int:
        """Converts this Color to a 32-bit RGBA value.

        Returns:
            A 32-bit integer containing the RGBA representation of this color.
        """
        return self._value & 0xFFFFFFFF

    def to_hex_color(self) -> str:
        """Converts the color to its hexadecimal representation.

        Returns:
            A string representing the color in hexadecimal format:
            - If alpha is 255 (fully opaque), returns "#RRGGBB".
            - If alpha is not 255, returns "#RRGGBBAA".
        """
        # Uppercase hex like Java version
        if self.alpha == 255:
            return f"#{self.red:02X}{self.green:02X}{self.blue:02X}"
        return f"#{self.red:02X}{self.green:02X}{self.blue:02X}{self.alpha:02X}"

    # Kept for Python API compatibility (lowercase hex like before)
    def to_color_schema(self) -> ColorSchema:
        """Converts this color to the schema Color representation used by the API.

        Note:
            The returned hex string uses lowercase letters for compatibility with the
            Python schema representation.

        Returns:
            A Color schema object with the hex value set.
        """
        if self.alpha == 255:
            return ColorSchema(value=f"#{self.red:02x}{self.green:02x}{self.blue:02x}")
        else:
            return ColorSchema(value=f"#{self.red:02x}{self.green:02x}{self.blue:02x}{self.alpha:02x}")

    # --- Equality / hashing / string ---
    def __eq__(self, other: object) -> bool:
        if self is other:
            return True
        if not isinstance(other, Color):
            return False
        return self._value == other._value

    def __hash__(self) -> int:
        return self._value

    def __str__(self) -> str:
        if self.alpha == 255:
            return f"Color(r={self.red}, g={self.green}, b={self.blue})"
        return f"Color(r={self.red}, g={self.green}, b={self.blue}, a={self.alpha})"


# --- Predefined colors (mirroring Java) ---
Color.TRANSPARENT = Color.from_rgba(255, 255, 255, 0)
Color.ALICE_BLUE = Color.from_rgb(240, 248, 255)
Color.ANTIQUE_WHITE = Color.from_rgb(250, 235, 215)
Color.AQUA = Color.from_rgb(0, 255, 255)
Color.AQUAMARINE = Color.from_rgb(127, 255, 212)
Color.AZURE = Color.from_rgb(240, 255, 255)
Color.BEIGE = Color.from_rgb(245, 245, 220)
Color.BISQUE = Color.from_rgb(255, 228, 196)
Color.BLACK = Color.from_rgb(0, 0, 0)
Color.BLANCHED_ALMOND = Color.from_rgb(255, 235, 205)
Color.BLUE = Color.from_rgb(0, 0, 255)
Color.BLUE_VIOLET = Color.from_rgb(138, 43, 226)
Color.BROWN = Color.from_rgb(165, 42, 42)
Color.BURLY_WOOD = Color.from_rgb(222, 184, 135)
Color.CADET_BLUE = Color.from_rgb(95, 158, 160)
Color.CHARTREUSE = Color.from_rgb(127, 255, 0)
Color.CHOCOLATE = Color.from_rgb(210, 105, 30)
Color.CORAL = Color.from_rgb(255, 127, 80)
Color.CORNFLOWER_BLUE = Color.from_rgb(100, 149, 237)
Color.CORNSILK = Color.from_rgb(255, 248, 220)
Color.CRIMSON = Color.from_rgb(220, 20, 60)
Color.CYAN = Color.from_rgb(0, 255, 255)
Color.DARK_BLUE = Color.from_rgb(0, 0, 139)
Color.DARK_CYAN = Color.from_rgb(0, 139, 139)
Color.DARK_GOLDENROD = Color.from_rgb(184, 134, 11)
Color.DARK_GRAY = Color.from_rgb(169, 169, 169)
Color.DARK_GREEN = Color.from_rgb(0, 100, 0)
Color.DARK_KHAKI = Color.from_rgb(189, 183, 107)
Color.DARK_MAGENTA = Color.from_rgb(139, 0, 139)
Color.DARK_OLIVE_GREEN = Color.from_rgb(85, 107, 47)
Color.DARK_ORANGE = Color.from_rgb(255, 140, 0)
Color.DARK_ORCHID = Color.from_rgb(153, 50, 204)
Color.DARK_RED = Color.from_rgb(139, 0, 0)
Color.DARK_SALMON = Color.from_rgb(233, 150, 122)
Color.DARK_SEA_GREEN = Color.from_rgb(143, 188, 139)
Color.DARK_SLATE_BLUE = Color.from_rgb(72, 61, 139)
Color.DARK_SLATE_GRAY = Color.from_rgb(47, 79, 79)
Color.DARK_TURQUOISE = Color.from_rgb(0, 206, 209)
Color.DARK_VIOLET = Color.from_rgb(148, 0, 211)
Color.DEEP_PINK = Color.from_rgb(255, 20, 147)
Color.DEEP_SKY_BLUE = Color.from_rgb(0, 191, 255)
Color.DIM_GRAY = Color.from_rgb(105, 105, 105)
Color.DODGER_BLUE = Color.from_rgb(30, 144, 255)
Color.FIREBRICK = Color.from_rgb(178, 34, 34)
Color.FLORAL_WHITE = Color.from_rgb(255, 250, 240)
Color.FOREST_GREEN = Color.from_rgb(34, 139, 34)
Color.FUCHSIA = Color.from_rgb(255, 0, 255)
Color.GAINSBORO = Color.from_rgb(220, 220, 220)
Color.GHOST_WHITE = Color.from_rgb(248, 248, 255)
Color.GOLD = Color.from_rgb(255, 215, 0)
Color.GOLDENROD = Color.from_rgb(218, 165, 32)
Color.GRAY = Color.from_rgb(128, 128, 128)
Color.GREEN = Color.from_rgb(0, 128, 0)
Color.GREEN_YELLOW = Color.from_rgb(173, 255, 47)
Color.HONEYDEW = Color.from_rgb(240, 255, 240)
Color.HOT_PINK = Color.from_rgb(255, 105, 180)
Color.INDIAN_RED = Color.from_rgb(205, 92, 92)
Color.INDIGO = Color.from_rgb(75, 0, 130)
Color.IVORY = Color.from_rgb(255, 255, 240)
Color.KHAKI = Color.from_rgb(240, 230, 140)
Color.LAVENDER = Color.from_rgb(230, 230, 250)
Color.LAVENDER_BLUSH = Color.from_rgb(255, 240, 245)
Color.LAWN_GREEN = Color.from_rgb(124, 252, 0)
Color.LEMON_CHIFFON = Color.from_rgb(255, 250, 205)
Color.LIGHT_BLUE = Color.from_rgb(173, 216, 230)
Color.LIGHT_CORAL = Color.from_rgb(240, 128, 128)
Color.LIGHT_CYAN = Color.from_rgb(224, 255, 255)
Color.LIGHT_GOLDENROD_YELLOW = Color.from_rgb(250, 250, 210)
Color.LIGHT_GRAY = Color.from_rgb(211, 211, 211)
Color.LIGHT_GREEN = Color.from_rgb(144, 238, 144)
Color.LIGHT_PINK = Color.from_rgb(255, 182, 193)
Color.LIGHT_SALMON = Color.from_rgb(255, 160, 122)
Color.LIGHT_SEA_GREEN = Color.from_rgb(32, 178, 170)
Color.LIGHT_SKY_BLUE = Color.from_rgb(135, 206, 250)
Color.LIGHT_SLATE_GRAY = Color.from_rgb(119, 136, 153)
Color.LIGHT_STEEL_BLUE = Color.from_rgb(176, 196, 222)
Color.LIGHT_YELLOW = Color.from_rgb(255, 255, 224)
Color.LIME = Color.from_rgb(0, 255, 0)
Color.LIME_GREEN = Color.from_rgb(50, 205, 50)
Color.LINEN = Color.from_rgb(250, 240, 230)
Color.MAGENTA = Color.from_rgb(255, 0, 255)
Color.MAROON = Color.from_rgb(128, 0, 0)
Color.MEDIUM_AQUAMARINE = Color.from_rgb(102, 205, 170)
Color.MEDIUM_BLUE = Color.from_rgb(0, 0, 205)
Color.MEDIUM_ORCHID = Color.from_rgb(186, 85, 211)
Color.MEDIUM_PURPLE = Color.from_rgb(147, 112, 219)
Color.MEDIUM_SEA_GREEN = Color.from_rgb(60, 179, 113)
Color.MEDIUM_SLATE_BLUE = Color.from_rgb(123, 104, 238)
Color.MEDIUM_SPRING_GREEN = Color.from_rgb(0, 250, 154)
Color.MEDIUM_TURQUOISE = Color.from_rgb(72, 209, 204)
Color.MEDIUM_VIOLET_RED = Color.from_rgb(199, 21, 133)
Color.MIDNIGHT_BLUE = Color.from_rgb(25, 25, 112)
Color.MINT_CREAM = Color.from_rgb(245, 255, 250)
Color.MISTY_ROSE = Color.from_rgb(255, 228, 225)
Color.MOCCASIN = Color.from_rgb(255, 228, 181)
Color.NAVAJO_WHITE = Color.from_rgb(255, 222, 173)
Color.NAVY = Color.from_rgb(0, 0, 128)
Color.OLD_LACE = Color.from_rgb(253, 245, 230)
Color.OLIVE = Color.from_rgb(128, 128, 0)
Color.OLIVE_DRAB = Color.from_rgb(107, 142, 35)
Color.ORANGE = Color.from_rgb(255, 165, 0)
Color.ORANGE_RED = Color.from_rgb(255, 69, 0)
Color.ORCHID = Color.from_rgb(218, 112, 214)
Color.PALE_GOLDENROD = Color.from_rgb(238, 232, 170)
Color.PALE_GREEN = Color.from_rgb(152, 251, 152)
Color.PALE_TURQUOISE = Color.from_rgb(175, 238, 238)
Color.PALE_VIOLET_RED = Color.from_rgb(219, 112, 147)
Color.PAPAYA_WHIP = Color.from_rgb(255, 239, 213)
Color.PEACH_PUFF = Color.from_rgb(255, 218, 185)
Color.PERU = Color.from_rgb(205, 133, 63)
Color.PINK = Color.from_rgb(255, 192, 203)
Color.PLUM = Color.from_rgb(221, 160, 221)
Color.POWDER_BLUE = Color.from_rgb(176, 224, 230)
Color.PURPLE = Color.from_rgb(128, 0, 128)
Color.RED = Color.from_rgb(255, 0, 0)
Color.ROSY_BROWN = Color.from_rgb(188, 143, 143)
Color.ROYAL_BLUE = Color.from_rgb(65, 105, 225)
Color.SADDLE_BROWN = Color.from_rgb(139, 69, 19)
Color.SALMON = Color.from_rgb(250, 128, 114)
Color.SANDY_BROWN = Color.from_rgb(244, 164, 96)
Color.SEA_GREEN = Color.from_rgb(46, 139, 87)
Color.SEA_SHELL = Color.from_rgb(255, 245, 238)
Color.SIENNA = Color.from_rgb(160, 82, 45)
Color.SILVER = Color.from_rgb(192, 192, 192)
Color.SKY_BLUE = Color.from_rgb(135, 206, 235)
Color.SLATE_BLUE = Color.from_rgb(106, 90, 205)
Color.SLATE_GRAY = Color.from_rgb(112, 128, 144)
Color.SNOW = Color.from_rgb(255, 250, 250)
Color.SPRING_GREEN = Color.from_rgb(0, 255, 127)
Color.STEEL_BLUE = Color.from_rgb(70, 130, 180)
Color.TAN = Color.from_rgb(210, 180, 140)
Color.TEAL = Color.from_rgb(0, 128, 128)
Color.THISTLE = Color.from_rgb(216, 191, 216)
Color.TOMATO = Color.from_rgb(255, 99, 71)
Color.TURQUOISE = Color.from_rgb(64, 224, 208)
Color.VIOLET = Color.from_rgb(238, 130, 238)
Color.WHEAT = Color.from_rgb(245, 222, 179)
Color.WHITE = Color.from_rgb(255, 255, 255)
Color.WHITE_SMOKE = Color.from_rgb(245, 245, 245)
Color.YELLOW = Color.from_rgb(255, 255, 0)
Color.YELLOW_GREEN = Color.from_rgb(154, 205, 50)
