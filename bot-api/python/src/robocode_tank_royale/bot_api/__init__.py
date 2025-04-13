# Bot API classes
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from .base_bot_abc import BaseBotABC
    from .bot_abc import BotABC
    from .bot_exception import BotException
    from .bot_info import BotInfo
    from .bot_results import BotResults
    from .bot_state import BotState
    from .bullet_state import BulletState
    from .color import Color
    from .constants import *
    from .default_event_priority import DefaultEventPriority
    from .droid_abc import DroidABC
    from .game_setup import GameSetup
    from .game_type import GameType
    from .initial_position import InitialPosition

__all__ = [
    # classes
    "BaseBotABC",
    "BotABC",
    "BotException",
    "BotInfo",
    "BotResults",
    "BotState",
    "BulletState",
    "Color",
    "DefaultEventPriority",
    "DroidABC",
    "GameSetup",
    "GameType",
    "InitialPosition",
    # constants
    "BOUNDING_CIRCLE_RADIUS",
    "SCAN_RADIUS",
    "MAX_TURN_RATE",
    "MAX_GUN_TURN_RATE",
    "MAX_RADAR_TURN_RATE",
    "MAX_SPEED",
    "MIN_FIREPOWER",
    "MAX_FIREPOWER",
    "MIN_BULLET_SPEED",
    "MAX_BULLET_SPEED",
    "ACCELERATION",
    "DECELERATION",
]
