from dataclasses import dataclass

@dataclass(frozen=True)  # frozen=True makes Color immutable
class Color:
    """Represents a color with red, green, and blue components."""
    red: int
    green: int
    blue: int
    alpha: int = 255  # Defaults to fully opaque

    def __post_init__(self):
        """Validate color component values."""
        for component, name in [(self.red, "red"), (self.green, "green"), (self.blue, "blue"), (self.alpha, "alpha")]:
            if component is not None:  # Alpha can be None
                if not 0 <= component <= 255:
                    raise ValueError(f"{name} component must be between 0 and 255, got {component}")


    @classmethod
    def from_rgb(cls, red: int, green: int, blue: int) -> "Color":
        """Creates a Color instance from RGB values."""
        return cls(red, green, blue)

    @classmethod
    def from_rgba(cls, red: int, green: int, blue: int, alpha: int) -> "Color":
      """Creates a Color instance from RGBA values."""
      return cls(red, green, blue, alpha)


    def to_tuple(self) -> tuple[int, int, int, int]:
        """Returns the color as a tuple (r, g, b, a)."""
        return (self.red, self.green, self.blue, self.alpha)

    def to_rgb_tuple(self) -> tuple[int, int, int]:
        """Return the color as a tuple (r, g, b)"""
        return (self.red, self.green, self.blue)
