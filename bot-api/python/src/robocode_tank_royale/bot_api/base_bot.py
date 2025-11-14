import math
from typing import Any, Optional, Sequence

from .base_bot_abc import BaseBotABC
from .bot_info import BotInfo
from .constants import *
from .internal.base_bot_internals import BaseBotInternals
from .graphics import Color, GraphicsABC
from .events.condition import Condition
from .util.math_util import MathUtil
from .bullet_state import BulletState
from .internal.event_priorities import EventPriorities
from .events import BotEvent


class BaseBot(BaseBotABC):
    """
    BaseBot is a base class for creating Robocode Tank Royale bots.

    Configuration and defaults:

    - By default, the constructor attempts to load a bot config JSON named <ClassName>.json located next to your bot class. If not found or incomplete, environment variables are used instead.
    - SERVER_URL can be set to the WebSocket URL of the server. If not set, ws://localhost:7654 is used.
    - SERVER_SECRET is optional. Set it only if the server requires a secret for connecting bots. If the server enforces secrets and none is provided, the server will disconnect the bot.
    - When no config file is used, these BotInfo environment variables must be provided: BOT_NAME, BOT_VERSION, BOT_AUTHORS. Optional vars include: BOT_DESCRIPTION, BOT_HOMEPAGE, BOT_COUNTRY_CODES, BOT_GAME_TYPES, BOT_PLATFORM, BOT_PROG_LANG, BOT_INITIAL_POS.

    You can also pass bot_info, server_url, and server_secret explicitly via the constructor.
    """

    def __init__(
        self,
        bot_info: Optional[BotInfo] = None,
        server_url: Optional[str] = None,
        server_secret: Optional[str] = None,
    ):
        super().__init__()
        # try to automatically read the bot config file
        if bot_info is None:
            bot_info_file = f"{self.__class__.__name__}.json"
            try:
                bot_info = BotInfo.from_file(bot_info_file)
            except:
                print('Failed to read bot info json file: {bot_info_file}.')
        self._internals = BaseBotInternals(self, bot_info, server_url, server_secret)

    async def start(self) -> None:
        await self._internals.start()

    async def go(self) -> None:
        # Process all events before executing the turn commands to mimic classic Robocode behavior
        current_tick = self._internals.get_current_tick_or_null()
        if current_tick is not None:
            # Align with Java: only dispatch events here; staging happens when the tick is received
            await self._internals.dispatch_events(current_tick.turn_number)
        await self._internals.execute()

    def get_my_id(self) -> int:
        return self._internals.my_id

    def get_variant(self) -> str:
        return self._internals.variant

    def get_version(self) -> str:
        return self._internals.version

    def get_game_type(self) -> str:
        return self._internals.game_setup.game_type

    def get_arena_width(self) -> int:
        return self._internals.game_setup.arena_width

    def get_arena_height(self) -> int:
        return self._internals.game_setup.arena_height

    def get_number_of_rounds(self) -> int:
        return self._internals.game_setup.number_of_rounds

    def get_gun_cooling_rate(self) -> float:
        return self._internals.game_setup.gun_cooling_rate

    def get_max_inactivity_turns(self) -> int:
        return self._internals.game_setup.max_inactivity_turns

    def get_turn_timeout(self) -> int:
        return self._internals.game_setup.turn_timeout

    def get_time_left(self) -> int:
        return self._internals.get_time_left()

    def get_round_number(self) -> int:
        return self._internals.get_current_tick_or_throw().round_number

    def get_turn_number(self) -> int:
        return self._internals.get_current_tick_or_throw().turn_number

    def get_enemy_count(self) -> int:
        bot_state = self._internals.get_current_tick_or_throw().bot_state
        assert bot_state is not None
        return bot_state.enemy_count

    def get_energy(self) -> float:
        bot_state = self._internals.get_current_tick_or_throw().bot_state
        assert bot_state is not None
        return bot_state.energy

    def is_disabled(self) -> bool:
        return self.get_energy() == 0

    def get_x(self) -> float:
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            x = self._internals.initial_position.x
            assert x is not None
            return x
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.x

    def get_y(self) -> float:
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            y = self._internals.initial_position.y
            assert y is not None
            return y
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.y

    def get_direction(self) -> float:
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            direction = self._internals.initial_position.direction
            assert direction is not None
            return direction
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.direction

    def get_gun_direction(self) -> float:
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            gun_direction = self._internals.initial_position.direction
            assert gun_direction is not None
            return gun_direction
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.gun_direction

    def get_radar_direction(self) -> float:
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            radar_direction = self._internals.initial_position.direction
            assert radar_direction is not None
            return radar_direction
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.radar_direction

    def get_speed(self) -> float:
        return self._internals.get_speed()

    def get_gun_heat(self) -> float:
        return self._internals.get_gun_heat()

    def get_bullet_states(self) -> Sequence[BulletState | None] | None:
        return self._internals.get_bullet_states()

    def get_events(self) -> Sequence[BotEvent | None] | None:
        return self._internals.get_events()

    def clear_events(self) -> None:
        self._internals.clear_events()

    @property
    def turn_rate(self) -> float:
        return self._internals.turn_rate

    @turn_rate.setter
    def turn_rate(self, turn_rate: float) -> None:
        self._internals.turn_rate = turn_rate

    @property
    def max_turn_rate(self) -> float:
        return self._internals.get_max_turn_rate()

    @max_turn_rate.setter
    def max_turn_rate(self, max_turn_rate: float) -> None:
        self._internals.set_max_turn_rate(max_turn_rate)

    @property
    def gun_turn_rate(self) -> float:
        return self._internals.gun_turn_rate

    @gun_turn_rate.setter
    def gun_turn_rate(self, gun_turn_rate: float) -> None:
        self._internals.gun_turn_rate = gun_turn_rate

    @property
    def max_gun_turn_rate(self) -> float:
        return self._internals.get_max_gun_turn_rate()

    @max_gun_turn_rate.setter
    def max_gun_turn_rate(self, max_gun_turn_rate: float) -> None:
        self._internals.set_max_gun_turn_rate(max_gun_turn_rate)

    @property
    def radar_turn_rate(self) -> float:
        return self._internals.radar_turn_rate

    @radar_turn_rate.setter
    def radar_turn_rate(self, radar_turn_rate: float) -> None:
        self._internals.radar_turn_rate = radar_turn_rate

    @property
    def max_radar_turn_rate(self) -> float:
        return self._internals.get_max_radar_turn_rate()

    @max_radar_turn_rate.setter
    def max_radar_turn_rate(self, max_radar_turn_rate: float) -> None:
        self._internals.set_max_radar_turn_rate(max_radar_turn_rate)

    @property
    def target_speed(self) -> float:
        # Match Java semantics: return 0 if not set
        ts = self._internals.target_speed
        return 0.0 if ts is None else ts

    @target_speed.setter
    def target_speed(self, target_speed: float) -> None:
        # Delegate to internals to ensure clamping/validation
        self._internals.target_speed = target_speed

    @property
    def max_speed(self) -> float:
        return self._internals.get_max_speed()

    @max_speed.setter
    def max_speed(self, max_speed: float) -> None:
        self._internals.set_max_speed(max_speed)

    def set_fire(self, firepower: float) -> bool:
        return self._internals.set_fire(firepower)

    def get_firepower(self) -> float:
        firepower = self._internals.data.bot_intent.firepower
        return 0.0 if firepower is None else firepower

    def set_rescan(self) -> None:
        self._internals.data.bot_intent.rescan = True

    def set_fire_assist(self, enable: bool) -> None:
        self._internals.data.bot_intent.fire_assist = enable

    def set_interruptible(self, interruptible: bool) -> None:
        """
        Sets whether the bot's event handlers are interruptible.

        When set to True, the bot's event handlers can be interrupted by higher-priority events.
        When set to False, event handlers will run to completion before other events are processed.

        Args:
            interruptible (bool): If True, event handlers are interruptible; otherwise, they are not.
        """
        self._internals.set_interruptible(interruptible)

    def set_adjust_gun_for_body_turn(self, adjust: bool) -> None:
        """
        Sets whether the gun's direction should adjust for the bot's body turn.

        When set to True, the gun will maintain its direction relative to the body as the bot turns.
        When set to False, the gun will turn with the body.

        Args:
            adjust (bool): If True, gun direction is adjusted for body turn.
        """
        self._internals.data.bot_intent.adjust_gun_for_body_turn = adjust

    def set_adjust_radar_for_body_turn(self, adjust: bool) -> None:
        self._internals.data.bot_intent.adjust_radar_for_body_turn = adjust

    def is_adjust_radar_for_body_turn(self) -> bool:
        assert self._internals.data.bot_intent.adjust_radar_for_body_turn is not None, (
            "Adjust radar for body turn must be set before accessing it."
        )
        return self._internals.data.bot_intent.adjust_radar_for_body_turn

    def set_adjust_radar_for_gun_turn(self, adjust: bool) -> None:
        """
        Sets whether the radar's direction should adjust for the gun's turn.

        When set to True, the radar will maintain its direction relative to the gun as the gun turns.
        When set to False, the radar will turn with the gun.

        Args:
            adjust (bool): If True, radar direction is adjusted for gun turn.
        """
        self._internals.data.bot_intent.adjust_radar_for_gun_turn = adjust
        self._internals.data.bot_intent.fire_assist = not adjust

    def is_adjust_radar_for_gun_turn(self) -> bool:
        assert self._internals.data.bot_intent.adjust_radar_for_gun_turn is not None, (
            "Adjust radar for gun turn must be set before accessing it."
        )
        return self._internals.data.bot_intent.adjust_radar_for_gun_turn

    def add_custom_event(self, condition: Condition) -> bool:
        return self._internals.add_condition(condition)

    def remove_custom_event(self, condition: Condition) -> bool:
        return self._internals.remove_condition(condition)

    def set_stop(self, overwrite: bool = False) -> None:
        self._internals.set_stop(overwrite)

    def set_resume(self) -> None:
        self._internals.set_resume()

    def get_teammate_ids(self) -> set[int]:
        return self._internals.teammate_ids

    def is_teammate(self, bot_id: int) -> bool:
        """
        Checks if the specified bot ID is a teammate.

        Args:
            bot_id (int): The bot ID to check.

        Returns:
            bool: True if the bot is a teammate; False otherwise.
        """
        self._internals.get_current_tick_or_throw()
        return self._internals.is_teammate(bot_id)

    def broadcast_team_message(self, message: Any) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.broadcast_team_message(message)

    def send_team_message(self, teammate_id: int, message: Any) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.send_team_message(teammate_id, message)

    def is_stopped(self) -> bool:
        return self._internals.data.is_stopped

    @property
    def body_color(self) -> Optional[Color]:
        return self._internals.body_color

    @body_color.setter
    def body_color(self, color: Optional[Color]) -> None:
        self._internals.body_color = color

    @property
    def turret_color(self) -> Optional[Color]:
        return self._internals.turret_color

    @turret_color.setter
    def turret_color(self, color: Optional[Color]) -> None:
        self._internals.turret_color = color

    @property
    def radar_color(self) -> Optional[Color]:
        return self._internals.radar_color

    @radar_color.setter
    def radar_color(self, color: Optional[Color]) -> None:
        self._internals.radar_color = color

    @property
    def bullet_color(self) -> Optional[Color]:
        return self._internals.bullet_color

    @bullet_color.setter
    def bullet_color(self, color: Optional[Color]) -> None:
        self._internals.bullet_color = color

    @property
    def scan_color(self) -> Optional[Color]:
        return self._internals.scan_color

    @scan_color.setter
    def scan_color(self, color: Optional[Color]) -> None:
        self._internals.scan_color = color

    @property
    def tracks_color(self) -> Optional[Color]:
        return self._internals.tracks_color

    @tracks_color.setter
    def tracks_color(self, color: Optional[Color]) -> None:
        self._internals.tracks_color = color

    @property
    def gun_color(self) -> Optional[Color]:
        return self._internals.gun_color

    @gun_color.setter
    def gun_color(self, color: Optional[Color]) -> None:
        self._internals.gun_color = color

    def is_debugging_enabled(self) -> bool:
        tick = self._internals.get_current_tick_or_throw()
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.is_debugging_enabled

    def get_graphics(self) -> GraphicsABC:
        return self._internals.get_graphics()

    # Utility methods
    def calc_max_turn_rate(self, speed: float) -> float:
        return MAX_TURN_RATE - 0.75 * math.fabs(
            MathUtil.clamp(speed, -MAX_SPEED, MAX_SPEED)
        )

    def calc_bullet_speed(self, firepower: float) -> float:
        return 20 - 3 * MathUtil.clamp(firepower, MIN_FIREPOWER, MAX_FIREPOWER)

    def calc_gun_heat(self, firepower: float) -> float:
        return 1 + (MathUtil.clamp(firepower, MIN_FIREPOWER, MAX_FIREPOWER) / 5)

    def get_event_priority(self, event_class: type) -> int:
        return EventPriorities.get_priority(event_class)

    def set_event_priority(self, event_class: type, priority: int) -> None:
        EventPriorities.set_priority(event_class, priority)
