import math
import traceback
from typing import Any, Optional

from PIL import Image

from .base_bot_abc import BaseBotABC
from .bot_info import BotInfo
from ._base_bot_internals import _BaseBotInternals
from .color import Color
from .bot_exception import BotException
from .condition import Condition
from .bullet_state import BulletState
from .events import (
    BotEvent,
    ConnectedEvent,
    ConnectionErrorEvent,
    CustomEvent,
    DisconnectedEvent,
    GameEndedEvent,
    GameStartedEvent,
    RoundEndedEvent,
    RoundStartedEvent,
    TickEvent,
    BotDeathEvent,
    DeathEvent,
    HitBotEvent,
    HitWallEvent,
    BulletFiredEvent,
    HitByBulletEvent,
    BulletHitBotEvent,
    BulletHitBulletEvent,
    BulletHitWallEvent,
    ScannedBotEvent,
    SkippedTurnEvent,
    TeamMessageEvent,
    WonRoundEvent,
)
from .game_setup import GameSetup
from .initial_position import InitialPosition


class BaseBot(BaseBotABC):
    """
    BaseBot is a base class for creating Robocode Tank Royale bots.
    It provides common functionality and abstract methods that should be
    implemented by subclasses to define bot-specific behavior.
    """

    def __init__(
        self,
        bot_info: Optional[BotInfo] = None,
        server_url: Optional[str] = None,
        server_secret: Optional[str] = None,
    ):
        super().__init__()
        self._internals = _BaseBotInternals(self, bot_info, server_url, server_secret)

    def start(self) -> None:
        self._internals.start()

    async def go(self) -> None:
        await self._internals.go()

    def get_my_id(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.my_id

    def get_variant(self) -> str:
        return self._internals.variant

    def get_version(self) -> str:
        return self._internals.version

    def get_game_type(self) -> str:
        self._internals.get_current_tick_or_throw()
        return self._internals.game_setup.game_type

    def get_arena_width(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.game_setup.arena_width

    def get_arena_height(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.game_setup.arena_height

    def get_number_of_rounds(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.game_setup.number_of_rounds

    def get_gun_cooling_rate(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.game_setup.gun_cooling_rate

    def get_max_inactivity_turns(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.game_setup.max_inactivity_turns

    def get_turn_timeout(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.game_setup.turn_timeout

    def get_time_left(self) -> int:
        return self._internals.get_time_left()

    def get_round_number(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_round_number

    def get_turn_number(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_turn_number

    def get_enemy_count(self) -> int:
        self._internals.get_current_tick_or_throw()
        return self._internals.get_enemy_count()

    def get_energy(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.energy

    def is_disabled(self) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.energy == 0

    def get_x(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.x

    def get_y(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.y

    def get_direction(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.direction

    def get_gun_direction(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.gun_direction

    def get_radar_direction(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.radar_direction

    def get_speed(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.speed

    def get_gun_heat(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bot_state.gun_heat

    def get_bullet_states(self) -> list[BulletState]:
        self._internals.get_current_tick_or_throw()
        return self._internals.current_bullet_states()

    def get_events(self) -> list[BotEvent]:
        self._internals.get_current_tick_or_throw()
        return self._internals.events

    def clear_events(self) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.clear_events()

    def get_turn_rate(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.turn_rate

    def set_turn_rate(self, turn_rate: float) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.turn_rate = turn_rate

    def get_max_turn_rate(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.max_turn_rate

    def set_max_turn_rate(self, max_turn_rate: float) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.max_turn_rate = max_turn_rate

    def get_gun_turn_rate(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.gun_turn_rate

    def set_gun_turn_rate(self, gun_turn_rate: float) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.gun_turn_rate = gun_turn_rate

    def get_max_gun_turn_rate(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.max_gun_turn_rate

    def set_max_gun_turn_rate(self, max_gun_turn_rate: float) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.max_gun_turn_rate = max_gun_turn_rate

    def get_radar_turn_rate(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.radar_turn_rate

    def set_radar_turn_rate(self, radar_turn_rate: float) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.radar_turn_rate = radar_turn_rate

    def get_max_radar_turn_rate(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.max_radar_turn_rate

    def set_max_radar_turn_rate(self, max_radar_turn_rate: float) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.max_radar_turn_rate = max_radar_turn_rate

    def get_target_speed(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.target_speed

    def set_target_speed(self, target_speed: float) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.target_speed = target_speed

    def get_max_speed(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.max_speed

    def set_max_speed(self, max_speed: float) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.max_speed = max_speed

    def set_fire(self, firepower: float) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.set_fire(firepower)

    def get_firepower(self) -> float:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.firepower

    def set_rescan(self) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.set_rescan()

    def set_fire_assist(self, enable: bool) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.fire_assist = enable

    def set_interruptible(self, interruptible: bool) -> None:
        self._internals.set_interruptible(interruptible)

    def set_adjust_gun_for_body_turn(self, adjust: bool) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.adjust_gun_for_body_turn = adjust

    def is_adjust_gun_for_body_turn(self) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.adjust_gun_for_body_turn

    def set_adjust_radar_for_body_turn(self, adjust: bool) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.adjust_radar_for_body_turn = adjust

    def is_adjust_radar_for_body_turn(self) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.adjust_radar_for_body_turn

    def set_adjust_radar_for_gun_turn(self, adjust: bool) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.adjust_radar_for_gun_turn = adjust

    def is_adjust_radar_for_gun_turn(self) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.adjust_radar_for_gun_turn

    def add_custom_event(self, condition: Condition) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.add_custom_event(condition)

    def remove_custom_event(self, condition: Condition) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.remove_custom_event(condition)

    def set_stop(self, overwrite: bool = False) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.set_stop(overwrite)

    def set_resume(self) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.set_resume()

    def get_teammate_ids(self) -> set[int]:
        self._internals.get_current_tick_or_throw()
        return self._internals.get_teammate_ids()

    def is_teammate(self, bot_id: int) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.is_teammate(bot_id)

    def broadcast_team_message(self, message: Any) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.broadcast_team_message(message)

    def send_team_message(self, teammate_id: int, message: Any) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.send_team_message(teammate_id, message)

    def is_stopped(self) -> bool:
        self._internals.get_current_tick_or_throw()
        return self._internals.is_stopped()

    def get_body_color(self) -> Optional[Color]:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.body_color

    def set_body_color(self, color: Optional[Color]) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.body_color = color

    def get_turret_color(self) -> Optional[Color]:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.turret_color

    def set_turret_color(self, color: Optional[Color]) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.turret_color = color

    def get_radar_color(self) -> Optional[Color]:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.radar_color

    def set_radar_color(self, color: Optional[Color]) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.radar_color = color

    def get_bullet_color(self) -> Optional[Color]:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.bullet_color

    def set_bullet_color(self, color: Optional[Color]) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.bullet_color = color

    def get_scan_color(self) -> Optional[Color]:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.scan_color

    def set_scan_color(self, color: Optional[Color]) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.scan_color = color

    def get_tracks_color(self) -> Optional[Color]:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.tracks_color

    def set_tracks_color(self, color: Optional[Color]) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.tracks_color = color

    def get_gun_color(self) -> Optional[Color]:
        self._internals.get_current_tick_or_throw()
        return self._internals.bot_intent.gun_color

    def set_gun_color(self, color: Optional[Color]) -> None:
        self._internals.get_current_tick_or_throw()
        self._internals.bot_intent.gun_color = color

    def is_debugging_enabled(self) -> bool:
        # Debugging is always enabled for now in Python client
        return True

    def get_graphics(self) -> Image.Image:
        self._internals.get_current_tick_or_throw()
        return self._internals.get_graphics()

    # Event handlers - concrete implementations that can be overridden

    @staticmethod
    def on_connected(connected_event: ConnectedEvent) -> None:
        print(f"Connected to: {connected_event.server_uri}")

    @staticmethod
    def on_disconnected(disconnected_event: DisconnectedEvent) -> None:
        msg = f"Disconnected from: {disconnected_event.server_uri}"
        if disconnected_event.status_code is not None:
            msg += f", status code: {disconnected_event.status_code}"
        if disconnected_event.reason is not None:
            msg += f", reason: {disconnected_event.reason}"
        print(msg)

    @staticmethod
    def on_connection_error(connection_error_event: ConnectionErrorEvent) -> None:
        print(f"Connection error with {connection_error_event.server_uri}")
        if connection_error_event.error is not None:
            traceback.print_exception(type(connection_error_event.error),
                                      connection_error_event.error,
                                      connection_error_event.error.__traceback__)

    def on_game_started(self, game_started_event: GameStartedEvent) -> None:
        pass

    def on_game_ended(self, game_ended_event: GameEndedEvent) -> None:
        pass

    def on_round_started(self, round_started_event: RoundStartedEvent) -> None:
        pass

    def on_round_ended(self, round_ended_event: RoundEndedEvent) -> None:
        pass

    def on_tick(self, tick_event: TickEvent) -> None:
        pass

    def on_bot_death(self, bot_death_event: BotDeathEvent) -> None:
        pass

    def on_death(self, death_event: DeathEvent) -> None:
        pass

    def on_hit_bot(self, bot_hit_bot_event: HitBotEvent) -> None:
        pass

    def on_hit_wall(self, bot_hit_wall_event: HitWallEvent) -> None:
        pass

    def on_bullet_fired(self, bullet_fired_event: BulletFiredEvent) -> None:
        pass

    def on_hit_by_bullet(self, hit_by_bullet_event: HitByBulletEvent) -> None:
        pass

    def on_bullet_hit(self, bullet_hit_bot_event: BulletHitBotEvent) -> None:
        pass

    def on_bullet_hit_bullet(self, bullet_hit_bullet_event: BulletHitBulletEvent) -> None:
        pass

    def on_bullet_hit_wall(self, bullet_hit_wall_event: BulletHitWallEvent) -> None:
        pass

    def on_scanned_bot(self, scanned_bot_event: ScannedBotEvent) -> None:
        pass

    def on_skipped_turn(self, skipped_turn_event: SkippedTurnEvent) -> None:
        print(f"Skipped turn {skipped_turn_event.turn_number}")

    def on_won_round(self, won_round_event: WonRoundEvent) -> None:
        pass

    def on_custom_event(self, custom_event: CustomEvent) -> None:
        pass

    def on_team_message(self, team_message_event: TeamMessageEvent) -> None:
        pass

    # Utility methods

    def calc_max_turn_rate(self, speed: float) -> float:
        return self._internals.calc_max_turn_rate(speed)

    def calc_bullet_speed(self, firepower: float) -> float:
        return self._internals.calc_bullet_speed(firepower)

    def calc_gun_heat(self, firepower: float) -> float:
        return self._internals.calc_gun_heat(firepower)

    def get_event_priority(self, event_class: type) -> int:
        return self._internals.get_event_priority(event_class)

    def set_event_priority(self, event_class: type, priority: int) -> None:
        self._internals.set_event_priority(event_class, priority)

    def calc_bearing(self, direction: float) -> float:
        self._internals.get_current_tick_or_throw()
        return self.normalize_relative_angle(direction - self.get_direction())

    def calc_gun_bearing(self, direction: float) -> float:
        self._internals.get_current_tick_or_throw()
        return self.normalize_relative_angle(direction - self.get_gun_direction())

    def calc_radar_bearing(self, direction: float) -> float:
        self._internals.get_current_tick_or_throw()
        return self.normalize_relative_angle(direction - self.get_radar_direction())

    def direction_to(self, x: float, y: float) -> float:
        self._internals.get_current_tick_or_throw()
        dx = x - self.get_x()
        dy = y - self.get_y()
        return self.normalize_absolute_angle(math.degrees(math.atan2(dy, dx)))

    def bearing_to(self, x: float, y: float) -> float:
        self._internals.get_current_tick_or_throw()
        return self.normalize_relative_angle(self.direction_to(x, y) - self.get_direction())

    def gun_bearing_to(self, x: float, y: float) -> float:
        self._internals.get_current_tick_or_throw()
        return self.normalize_relative_angle(self.direction_to(x, y) - self.get_gun_direction())

    def radar_bearing_to(self, x: float, y: float) -> float:
        self._internals.get_current_tick_or_throw()
        return self.normalize_relative_angle(self.direction_to(x, y) - self.get_radar_direction())

    def distance_to(self, x: float, y: float) -> float:
        self._internals.get_current_tick_or_throw()
        return math.hypot(x - self.get_x(), y - self.get_y())

    def normalize_absolute_angle(self, angle: float) -> float:
        return self._internals.normalize_absolute_angle(angle)

    def normalize_relative_angle(self, angle: float) -> float:
        return self._internals.normalize_relative_angle(angle)

    def calc_delta_angle(self, target_angle: float, source_angle: float) -> float:
        return self._internals.calc_delta_angle(target_angle, source_angle)

    @property
    def initial_position(self) -> Optional[InitialPosition]:
        """
        Returns the initial starting position of the bot.
        This position can be used in `on_game_started` to determine the initial
        starting position of the bot.

        Returns:
            Optional[InitialPosition]: The initial position (x, y, direction) of the bot,
            or None if not set.
        """
        return self._internals.initial_position

    @initial_position.setter
    def initial_position(self, initial_position: Optional[InitialPosition]) -> None:
        """
        Sets the initial starting position of the bot.
        This method must be called before `start()` or `run()` to take effect.

        Args:
            initial_position (Optional[InitialPosition]): The initial position (x, y, direction) of the bot.
        """
        self._internals.initial_position = initial_position

    @property
    def country_codes(self) -> list[str]:
        return self._internals.bot_info.country_codes

    @country_codes.setter
    def country_codes(self, country_codes: list[str]) -> None:
        self._internals.bot_info.country_codes = country_codes
        self._internals.settings_updated()

    @property
    def game_variants(self) -> list[str]:
        return self._internals.bot_info.game_variants

    @game_variants.setter
    def game_variants(self, game_variants: list[str]) -> None:
        self._internals.bot_info.game_variants = game_variants
        self._internals.settings_updated()

    @property
    def programming_lang(self) -> Optional[str]:
        return self._internals.bot_info.programming_lang

    @programming_lang.setter
    def programming_lang(self, programming_lang: Optional[str]) -> None:
        self._internals.bot_info.programming_lang = programming_lang
        self._internals.settings_updated()

    # Add other properties from BotInfo if needed, like platform, etc.
    # For example:
    # @property
    # def platform(self) -> Optional[str]:
    #     return self._internals.bot_info.platform

    # @platform.setter
    # def platform(self, platform: Optional[str]) -> None:
    #     self._internals.bot_info.platform = platform
    #     self._internals.settings_updated()

    # @property
    # def author(self) -> Optional[str]:
    #     return self._internals.bot_info.author

    # @author.setter
    # def author(self, author: Optional[str]) -> None:
    #     self._internals.bot_info.author = author
    #     self._internals.settings_updated()

    # ... and so on for other BotInfo properties
