"""Shared team message type definitions for tests.

This module defines the Point and RobotColors classes that are used across
multiple test files to avoid registration conflicts in the team message registry.
"""
from dataclasses import dataclass
from typing import Optional

from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.team_message import team_message_type


@team_message_type
@dataclass
class Point:
    """Point (x,y) message type for team communication."""
    x: float
    y: float


@team_message_type
@dataclass
class RobotColors:
    """Robot colors message type for team communication."""
    body_color: Optional[Color] = None
    tracks_color: Optional[Color] = None
    turret_color: Optional[Color] = None
    gun_color: Optional[Color] = None
    radar_color: Optional[Color] = None
    scan_color: Optional[Color] = None
    bullet_color: Optional[Color] = None
