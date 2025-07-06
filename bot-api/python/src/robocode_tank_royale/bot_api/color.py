from dataclasses import dataclass

from robocode_tank_royale.schema import Color as ColorSchema


@dataclass(frozen=True)
class Color:
    """
    Represents a color with red, green, and blue components, as well as an optional alpha (transparency) value.

    Attributes:
        red (int): The red component of the color, ranging from 0 to 255.
        green (int): The green component of the color, ranging from 0 to 255.
        blue (int): The blue component of the color, ranging from 0 to 255.
        alpha (int): The alpha (transparency) component of the color, ranging from 0 (fully transparent)
                     to 255 (fully opaque). Defaults to 255 (fully opaque).
    """

    red: int
    green: int
    blue: int
    alpha: int = 255  # Defaults to fully opaque

    def __post_init__(self) -> None:
        """
        Validates the color component values after the object is initialized.

        Ensures that all color components (red, green, blue, alpha) are within the range [0, 255].

        Raises:
            ValueError: If any component is outside the valid range.
        """
        for component, name in [
            (self.red, "red"),
            (self.green, "green"),
            (self.blue, "blue"),
            (self.alpha, "alpha"),
        ]:
            if not 0 <= component <= 255:
                raise ValueError(
                    f"{name} component must be between 0 and 255, got {component}"
                )

    @classmethod
    def from_rgb(cls, red: int, green: int, blue: int) -> "Color":
        """
        Creates a Color instance using RGB values.

        Args:
            red (int): The red component of the color (0–255).
            green (int): The green component of the color (0–255).
            blue (int): The blue component of the color (0–255).

        Returns:
            Color: A new Color instance with the specified RGB values.
        """
        return cls(red, green, blue)

    @classmethod
    def from_rgba(cls, red: int, green: int, blue: int, alpha: int) -> "Color":
        """
        Creates a Color instance using RGBA values.

        Args:
            red (int): The red component of the color (0–255).
            green (int): The green component of the color (0–255).
            blue (int): The blue component of the color (0–255).
            alpha (int): The alpha (transparency) component of the color (0–255).

        Returns:
            Color: A new Color instance with the specified RGBA values.
        """
        return cls(red, green, blue, alpha)

    @classmethod
    def from_hex(cls, hex_string: str) -> "Color":
        """
        Creates a Color instance from a hex color string.

        Args:
            hex_string (str): The hex color string, e.g., "#RRGGBB" or "#RRGGBBAA".

        Returns:
            Color: A new Color instance.
        """
        hex_string = hex_string.lstrip("#")
        if len(hex_string) == 6:
            r = int(hex_string[0:2], 16)
            g = int(hex_string[2:4], 16)
            b = int(hex_string[4:6], 16)
            return cls(r, g, b)
        elif len(hex_string) == 8:
            r = int(hex_string[0:2], 16)
            g = int(hex_string[2:4], 16)
            b = int(hex_string[4:6], 16)
            a = int(hex_string[6:8], 16)
            return cls(r, g, b, a)
        else:
            raise ValueError("Invalid hex color string format")

    def to_tuple(self) -> tuple[int, int, int, int]:
        """
        Converts the color into a tuple representation.

        Returns:
            tuple: A tuple of the form (red, green, blue, alpha).
        """
        return self.red, self.green, self.blue, self.alpha

    def to_rgb_tuple(self) -> tuple[int, int, int]:
        """
        Converts the color into an RGB tuple representation, excluding the alpha value.

        Returns:
            tuple: A tuple of the form (red, green, blue).
        """
        return self.red, self.green, self.blue

    def to_color_schema(self) -> ColorSchema:
        """
        Converts the Color instance to a schema.Color instance.

        Returns:
            schema.Color: A schema.Color instance with the same RGB and alpha values.
        """
        return ColorSchema(
            value=f"#{self.red:02x}{self.green:02x}{self.blue:02x}{self.alpha:02x}"
        )
