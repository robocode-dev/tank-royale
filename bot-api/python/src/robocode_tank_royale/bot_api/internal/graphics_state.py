from typing import List, Optional

class GraphicsState:
    def __init__(self) -> None:
        self.graphics_calls: List[str] = []

    def clear(self) -> None:
        self.graphics_calls = []

    def get_svg_output(self) -> Optional[str]:
        if not self.graphics_calls:
            return None
        # For now, just joining them. A real implementation would create an SVG string.
        # Example: "<svg width='100' height='100'><line x1='0' y1='0' x2='100' y2='100' stroke='black'/></svg>"
        # Placeholder for actual SVG generation based on collected calls.
        # The Java version builds an SVG document. Here we are simplifying.
        return ";".join(self.graphics_calls)


    # Example drawing methods - these would be expanded in a full implementation
    def draw_line(self, x1: float, y1: float, x2: float, y2: float, color_hex: Optional[str]) -> None:
        # In a real implementation, color_hex might be processed or validated.
        # The string format is arbitrary for this placeholder; actual SVG elements would be constructed.
        self.graphics_calls.append(f"LINE;{x1};{y1};{x2};{y2};{color_hex if color_hex else '000000'}")

    def draw_circle(self, x: float, y: float, radius: float, color_hex: Optional[str]) -> None:
        self.graphics_calls.append(f"CIRCLE;{x};{y};{radius};{color_hex if color_hex else '000000'}")

    def draw_rectangle(self, x: float, y: float, width: float, height: float, color_hex: Optional[str]) -> None:
        self.graphics_calls.append(f"RECT;{x};{y};{width};{height};{color_hex if color_hex else '000000'}")

    def add_text(self, text: str, x: float, y: float, color_hex: Optional[str]) -> None:
        self.graphics_calls.append(f"TEXT;{text};{x};{y};{color_hex if color_hex else '000000'}")

    # get_graphics() in BaseBotInternals will return this instance,
    # allowing calls like: bot_internals.get_graphics().draw_line(...)
    # This class itself acts as the "graphics context" for now.
    # A more complex version might return a different object that wraps GraphicsState.
