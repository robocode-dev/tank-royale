# Backwards-compatible import path for Color
# Historically, Color was located at robocode_tank_royale.bot_api.color
# It has been moved under graphics module. This shim preserves the old import path.
from .graphics.color import Color  # re-export

__all__ = ["Color"]
