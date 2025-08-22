from dataclasses import dataclass
from typing import Optional

@dataclass
class BotState:
    from .graphics.color import Color

    """Represents the current bot state."""

    is_droid: bool
    """Flag specifying if the bot is a droid."""

    energy: float
    """Energy level. Typically starts at 100."""

    x: float
    """X coordinate."""

    y: float
    """Y coordinate."""

    direction: float
    """Driving direction in degrees."""

    gun_direction: float
    """Gun direction in degrees."""

    radar_direction: float
    """Radar direction in degrees."""

    radar_sweep: float
    """Radar sweep angle in degrees."""

    speed: float
    """Speed measured in units per turn."""

    turn_rate: float
    """Turn rate of the body in degrees per turn."""

    gun_turn_rate: float
    """Turn rate of the gun in degrees per turn."""

    radar_turn_rate: float
    """Turn rate of the radar in degrees per turn."""

    gun_heat: float
    """Gun heat."""

    enemy_count: int
    """Number of enemies left."""

    body_color: Optional[Color]  # Color can be optional
    """Body color."""

    turret_color: Optional[Color]
    """Gun turret color."""

    radar_color: Optional[Color]
    """Radar color."""

    bullet_color: Optional[Color]
    """Bullet color."""

    scan_color: Optional[Color]
    """Scan arc color."""

    tracks_color: Optional[Color]
    """Tracks color."""

    gun_color: Optional[Color]
    """Gun color."""

    is_debugging_enabled: bool
    """Flag indicating if graphical debugging is enabled."""
