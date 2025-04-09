from dataclasses import dataclass


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

    def __post_init__(self):
        """
        Validates the color component values after the object is initialized.

        Ensures that all color components (red, green, blue, alpha) are within the range [0, 255].

        Raises:
            ValueError: If any component is outside the valid range.
        """
        for component, name in [(self.red, "red"), (self.green, "green"), (self.blue, "blue"), (self.alpha, "alpha")]:
            if component is not None:
                if not 0 <= component <= 255:
                    raise ValueError(f"{name} component must be between 0 and 255, got {component}")

    @classmethod
    def from_rgb(cls, red: int, green: int, blue: int):
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
    def from_rgba(cls, red: int, green: int, blue: int, alpha: int):
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

    def to_tuple(self):
        """
        Converts the color into a tuple representation.

        Returns:
            tuple: A tuple of the form (red, green, blue, alpha).
        """
        return self.red, self.green, self.blue, self.alpha

    def to_rgb_tuple(self):
        """
        Converts the color into an RGB tuple representation, excluding the alpha value.

        Returns:
            tuple: A tuple of the form (red, green, blue).
        """
        return self.red, self.green, self.blue