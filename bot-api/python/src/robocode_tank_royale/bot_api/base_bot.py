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
                print(f'Failed to read bot info json file: {bot_info_file}.')
        self._internals = BaseBotInternals(self, bot_info, server_url, server_secret)

    def start(self) -> None:
        """
        The method used to start running the bot. You should call this method from the main method or
        similar.

        Example:
            if __name__ == "__main__":
                # create my_bot
                ...
                my_bot.start()
        """
        self._internals.start()

    def go(self) -> None:
        """
        Commits the current commands (actions), which finalizes the current turn for the bot.

        This method must be called once per turn to send the bot actions to the server and must be
        called before the turn timeout occurs. A turn timer is started when the GameStartedEvent
        and TickEvent occurs. If the go() method is called too late, a turn timeout will
        occur and the SkippedTurnEvent will occur, which means that the bot has skipped all
        actions for the last turn. In this case, the server will continue executing the last actions
        received. This could be fatal for the bot due to loss of control over the bot. So make sure that
        go() is called before the turn ends.

        The commands executed when go() is called are set by calling the various setter
        methods prior to calling the go() method: turn_rate, gun_turn_rate,
        radar_turn_rate, target_speed, and set_fire().

        See Also:
            turn_timeout
        """
        # Process all events before executing the turn commands to mimic classic Robocode behavior
        current_tick = self._internals.get_current_tick_or_null()
        if current_tick is not None:
            # Align with Java: only dispatch events here; staging happens when the tick is received
            self._internals.dispatch_events(current_tick.turn_number)
        self._internals.execute()

    @property
    def my_id(self) -> int:
        """Unique id of this bot, which is available when the game has started.

        Returns:
            The unique id of this bot.
        """
        return self._internals.my_id

    @property
    def variant(self) -> str:
        """The game variant, which is "Tank Royale".

        Returns:
            The game variant of Robocode.
        """
        return self._internals.variant

    @property
    def version(self) -> str:
        """Game version, e.g. "1.0.0".

        Returns:
            The game version.
        """
        return self._internals.version

    @property
    def game_type(self) -> str:
        """Game type, e.g. "melee" or "1v1".

        First available when the game has started.

        Returns:
            The game type.
        """
        return self._internals.game_setup.game_type

    @property
    def arena_width(self) -> int:
        """Width of the arena measured in units.

        First available when the game has started.

        Returns:
            The arena width measured in units.
        """
        return self._internals.game_setup.arena_width

    @property
    def arena_height(self) -> int:
        """Height of the arena measured in units.

        First available when the game has started.

        Returns:
            The arena height measured in units.
        """
        return self._internals.game_setup.arena_height

    @property
    def number_of_rounds(self) -> int:
        """The number of rounds in a battle.

        First available when the game has started.

        Returns:
            The number of rounds in a battle.
        """
        return self._internals.game_setup.number_of_rounds

    @property
    def gun_cooling_rate(self) -> float:
        """Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun can fire.
        The gun cooling rate determines how fast the gun cools down. That is, the gun cooling rate is
        subtracted from the gun heat each turn until the gun heat reaches zero.

        First available when the game has started.

        Returns:
            The gun cooling rate.

        See Also:
            gun_heat
        """
        return self._internals.game_setup.gun_cooling_rate

    @property
    def max_inactivity_turns(self) -> int:
        """The maximum number of inactive turns allowed the bot will become zapped by the game for being
        inactive. Inactive means that the bot has taken no action in several turns in a row.

        First available when the game has started.

        Returns:
            The maximum number of allowed inactive turns.
        """
        return self._internals.game_setup.max_inactivity_turns

    @property
    def turn_timeout(self) -> int:
        """The turn timeout is important as the bot needs to take action by calling go() before
        the turn timeout occurs. As soon as the TickEvent is triggered, i.e. when on_tick() is called,
        you need to call go() to take action before the turn timeout occurs. Otherwise, your bot will
        skip a turn and receive a on_skipped_turn() for each turn where go() is called too late.

        First available when the game has started.

        Returns:
            The turn timeout in microseconds (1 / 1,000,000 second).

        See Also:
            time_left, go
        """
        return self._internals.game_setup.turn_timeout

    @property
    def time_left(self) -> int:
        """The number of microseconds left of this turn before the bot will skip the turn.
        Make sure to call go() before the time runs out.

        Returns:
            The amount of time left in microseconds.

        See Also:
            turn_timeout, go
        """
        return self._internals.get_time_left()

    @property
    def round_number(self) -> int:
        """Current round number.

        Returns:
            The current round number.
        """
        return self._internals.get_current_tick_or_throw().round_number

    @property
    def turn_number(self) -> int:
        """Current turn number.

        Returns:
            The current turn number.
        """
        return self._internals.get_current_tick_or_throw().turn_number

    @property
    def enemy_count(self) -> int:
        """Number of enemies left in the round.

        Returns:
            The number of enemies left in the round.
        """
        bot_state = self._internals.get_current_tick_or_throw().bot_state
        assert bot_state is not None
        return bot_state.enemy_count

    @property
    def energy(self) -> float:
        """Current energy level. When the energy level is positive, the bot is alive and active.
        When the energy level is 0, the bot is still alive but disabled. If the bot becomes disabled
        it will not be able to move or take any action. If negative, the bot has been defeated.

        Returns:
            The current energy level.
        """
        bot_state = self._internals.get_current_tick_or_throw().bot_state
        assert bot_state is not None
        return bot_state.energy

    @property
    def disabled(self) -> bool:
        """Specifies if the bot is disabled, i.e., when the energy is zero. When the bot is disabled,
        it is not able to take any action like movement, turning, and firing.

        Returns:
            True if the bot is disabled; False otherwise.
        """
        return self.energy == 0

    @property
    def x(self) -> float:
        """Current X coordinate of the center of the bot.

        Returns:
            The current X coordinate of the bot.
        """
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            x = self._internals.initial_position.x
            assert x is not None
            return x
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.x

    @property
    def y(self) -> float:
        """Current Y coordinate of the center of the bot.

        Returns:
            The current Y coordinate of the bot.
        """
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            y = self._internals.initial_position.y
            assert y is not None
            return y
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.y

    @property
    def direction(self) -> float:
        """Current driving direction of the bot in degrees.

        Returns:
            The current driving direction of the bot.
        """
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            direction = self._internals.initial_position.direction
            assert direction is not None
            return direction
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.direction

    @property
    def gun_direction(self) -> float:
        """Current direction of the gun in degrees.

        Returns:
            The current gun direction of the bot.
        """
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            gun_direction = self._internals.initial_position.direction
            assert gun_direction is not None
            return gun_direction
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.gun_direction

    @property
    def radar_direction(self) -> float:
        """Current direction of the radar in degrees.

        Returns:
            The current radar direction of the bot.
        """
        tick = self._internals.get_current_tick_or_null()
        if not tick:
            assert self._internals.initial_position is not None
            radar_direction = self._internals.initial_position.direction
            assert radar_direction is not None
            return radar_direction
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.radar_direction

    @property
    def speed(self) -> float:
        """The current speed measured in units per turn. If the speed is positive, the bot moves forward.
        If negative, the bot moves backward. Zero speed means that the bot is not moving from its
        current position.

        Returns:
            The current speed.
        """
        return self._internals.get_speed()

    @property
    def gun_heat(self) -> float:
        """Current gun heat. When the gun is fired it gets heated and will not be able to fire before it has
        been cooled down. The gun is cooled down when the gun heat is zero.

        When the gun has fired the gun heat is set to 1 + (firepower / 5) and will be cooled down by
        the gun cooling rate.

        Returns:
            The current gun heat.

        See Also:
            gun_cooling_rate
        """
        return self._internals.get_gun_heat()

    @property
    def bullet_states(self) -> Sequence[BulletState | None] | None:
        """Current bullet states. Keeps track of all the bullets fired by the bot, which are still active
        on the arena.

        Returns:
            The current bullet states.
        """
        return self._internals.get_bullet_states()

    @property
    def events(self) -> Sequence[BotEvent | None] | None:
        """Returns an ordered list containing all events currently in the bot's event queue.
        You might, for example, call this while processing another event.

        Returns:
            An ordered list containing all events currently in the bot's event queue.

        See Also:
            clear_events
        """
        return self._internals.get_events()

    def clear_events(self) -> None:
        """Clears out any pending events in the bot's event queue immediately.

        See Also:
            events
        """
        self._internals.clear_events()

    @property
    def turn_rate(self) -> float:
        """Returns the turn rate of the bot in degrees per turn.

        Returns:
            The turn rate of the bot.
        """
        return self._internals.turn_rate

    @turn_rate.setter
    def turn_rate(self, turn_rate: float) -> None:
        """Sets the turn rate of the bot, which can be positive and negative.

        Args:
            turn_rate: The new turn rate of the bot in degrees per turn.
        """
        self._internals.turn_rate = turn_rate

    @property
    def max_turn_rate(self) -> float:
        """Returns the maximum turn rate of the bot in degrees per turn.

        Returns:
            The maximum turn rate of the bot.
        """
        return self._internals.get_max_turn_rate()

    @max_turn_rate.setter
    def max_turn_rate(self, max_turn_rate: float) -> None:
        """Sets the maximum turn rate the bot can turn to the left or right.

        Args:
            max_turn_rate: The new maximum turn rate of the bot.
        """
        self._internals.set_max_turn_rate(max_turn_rate)

    @property
    def gun_turn_rate(self) -> float:
        """Returns the gun turn rate in degrees per turn.

        Returns:
            The gun turn rate.
        """
        return self._internals.gun_turn_rate

    @gun_turn_rate.setter
    def gun_turn_rate(self, gun_turn_rate: float) -> None:
        """Sets the turn rate of the gun, which can be positive and negative.

        Args:
            gun_turn_rate: The new gun turn rate in degrees per turn.
        """
        self._internals.gun_turn_rate = gun_turn_rate

    @property
    def max_gun_turn_rate(self) -> float:
        """Returns the maximum gun turn rate in degrees per turn.

        Returns:
            The maximum gun turn rate.
        """
        return self._internals.get_max_gun_turn_rate()

    @max_gun_turn_rate.setter
    def max_gun_turn_rate(self, max_gun_turn_rate: float) -> None:
        """Sets the maximum turn rate the gun can turn to the left or right.

        Args:
            max_gun_turn_rate: The new maximum gun turn rate.
        """
        self._internals.set_max_gun_turn_rate(max_gun_turn_rate)

    @property
    def radar_turn_rate(self) -> float:
        """Returns the radar turn rate in degrees per turn.

        Returns:
            The radar turn rate.
        """
        return self._internals.radar_turn_rate

    @radar_turn_rate.setter
    def radar_turn_rate(self, radar_turn_rate: float) -> None:
        """Sets the turn rate of the radar, which can be positive and negative.

        Args:
            radar_turn_rate: The new radar turn rate in degrees per turn.
        """
        self._internals.radar_turn_rate = radar_turn_rate

    @property
    def max_radar_turn_rate(self) -> float:
        """Returns the maximum radar turn rate in degrees per turn.

        Returns:
            The maximum radar turn rate.
        """
        return self._internals.get_max_radar_turn_rate()

    @max_radar_turn_rate.setter
    def max_radar_turn_rate(self, max_radar_turn_rate: float) -> None:
        """Sets the maximum turn rate the radar can turn to the left or right.

        Args:
            max_radar_turn_rate: The new maximum radar turn rate.
        """
        self._internals.set_max_radar_turn_rate(max_radar_turn_rate)

    @property
    def target_speed(self) -> float:
        """Returns the target speed in units per turn.

        Returns:
            The target speed.
        """
        # Match Java semantics: return 0 if not set
        ts = self._internals.target_speed
        return 0.0 if ts is None else ts

    @target_speed.setter
    def target_speed(self, target_speed: float) -> None:
        """Sets the target speed for the bot in units per turn.

        Args:
            target_speed: The new target speed.
        """
        # Delegate to internals to ensure clamping/validation
        self._internals.target_speed = target_speed

    @property
    def max_speed(self) -> float:
        """Returns the maximum speed in units per turn.

        Returns:
            The maximum speed.
        """
        return self._internals.get_max_speed()

    @max_speed.setter
    def max_speed(self, max_speed: float) -> None:
        """Sets the maximum speed the bot can move.

        Args:
            max_speed: The new maximum speed.
        """
        self._internals.set_max_speed(max_speed)

    def set_fire(self, firepower: float) -> bool:
        """Sets the gun to fire in the direction the gun is pointing with the specified firepower.

        Args:
            firepower: The firepower to use for firing.

        Returns:
            True if the gun will fire; False otherwise.
        """
        return self._internals.set_fire(firepower)

    @property
    def firepower(self) -> float:
        """Returns the firepower set for firing the gun.

        Returns:
            The firepower.
        """
        firepower = self._internals.data.bot_intent.firepower
        return 0.0 if firepower is None else firepower

    def set_rescan(self) -> None:
        """Sets the radar to rescan with the radar."""
        self._internals.data.bot_intent.rescan = True

    def set_fire_assist(self, enable: bool) -> None:
        """Enables or disables fire assistance.

        Args:
            enable: True to enable fire assist; False to disable.
        """
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

    @property
    def adjust_gun_for_body_turn(self) -> bool:
        """Returns whether the gun adjusts for the bot's body turn."""
        assert self._internals.data.bot_intent.adjust_gun_for_body_turn is not None, (
            "Adjust gun for body turn must be set before accessing it."
        )
        return self._internals.data.bot_intent.adjust_gun_for_body_turn

    @adjust_gun_for_body_turn.setter
    def adjust_gun_for_body_turn(self, adjust: bool) -> None:
        """
        Sets whether the gun's direction should adjust for the bot's body turn.

        When set to True, the gun will maintain its direction relative to the body as the bot turns.
        When set to False, the gun will turn with the body.

        Args:
            adjust (bool): If True, gun direction is adjusted for body turn.
        """
        self._internals.data.bot_intent.adjust_gun_for_body_turn = adjust

    @property
    def adjust_radar_for_body_turn(self) -> bool:
        """Returns whether the radar adjusts for the bot's body turn."""
        assert self._internals.data.bot_intent.adjust_radar_for_body_turn is not None, (
            "Adjust radar for body turn must be set before accessing it."
        )
        return self._internals.data.bot_intent.adjust_radar_for_body_turn

    @adjust_radar_for_body_turn.setter
    def adjust_radar_for_body_turn(self, adjust: bool) -> None:
        """Sets whether the radar adjusts for the bot's body turn."""
        self._internals.data.bot_intent.adjust_radar_for_body_turn = adjust

    @property
    def adjust_radar_for_gun_turn(self) -> bool:
        """Returns whether the radar adjusts for the gun's turn."""
        assert self._internals.data.bot_intent.adjust_radar_for_gun_turn is not None, (
            "Adjust radar for gun turn must be set before accessing it."
        )
        return self._internals.data.bot_intent.adjust_radar_for_gun_turn

    @adjust_radar_for_gun_turn.setter
    def adjust_radar_for_gun_turn(self, adjust: bool) -> None:
        """
        Sets whether the radar's direction should adjust for the gun's turn.

        When set to True, the radar will maintain its direction relative to the gun as the gun turns.
        When set to False, the radar will turn with the gun.

        Args:
            adjust (bool): If True, radar direction is adjusted for gun turn.
        """
        self._internals.data.bot_intent.adjust_radar_for_gun_turn = adjust
        self._internals.data.bot_intent.fire_assist = not adjust


    def add_custom_event(self, condition: Condition) -> bool:
        """Adds a custom event based on a condition. When the condition is met, the on_custom_event()
        handler is triggered with a CustomEvent containing the condition.

        Args:
            condition: The condition that must be met to trigger the custom event.

        Returns:
            True if the condition was added; False if it already exists.
        """
        return self._internals.add_condition(condition)

    def remove_custom_event(self, condition: Condition) -> bool:
        """Removes a custom event that was previously added with add_custom_event().

        Args:
            condition: The condition to remove.

        Returns:
            True if the condition was removed; False if it was not found.
        """
        return self._internals.remove_condition(condition)

    def set_stop(self, overwrite: bool = False) -> None:
        """Sets the bot to stop all movement including turning the gun and radar.
        The remaining movement is saved for a call to set_resume() or resume().

        Args:
            overwrite: True to override a previously saved movement from stop() or set_stop().
        """
        self._internals.set_stop(overwrite)

    def set_resume(self) -> None:
        """Sets the bot to resume the movement prior to calling set_stop() or stop()."""
        self._internals.set_resume()

    @property
    def teammate_ids(self) -> set[int]:
        """Returns the IDs of all teammates.

        Returns:
            A set of teammate IDs.
        """
        return self._internals.teammate_ids

    def is_teammate(self, bot_id: int) -> bool:
        """Checks if the specified bot ID is a teammate.

        Args:
            bot_id: The bot ID to check.

        Returns:
            True if the bot is a teammate; False otherwise.
        """
        self._internals.get_current_tick_or_throw()
        return self._internals.is_teammate(bot_id)

    def broadcast_team_message(self, message: Any) -> None:
        """Broadcasts a message to all teammates.

        Args:
            message: The message to broadcast.
        """
        self._internals.get_current_tick_or_throw()
        self._internals.broadcast_team_message(message)

    def send_team_message(self, teammate_id: int, message: Any) -> None:
        """Sends a message to a specific teammate.

        Args:
            teammate_id: The ID of the teammate to send the message to.
            message: The message to send.
        """
        self._internals.get_current_tick_or_throw()
        self._internals.send_team_message(teammate_id, message)

    @property
    def stopped(self) -> bool:
        """Checks if the bot is currently stopped.

        Returns:
            True if the bot is stopped; False otherwise.
        """
        return self._internals.data.is_stopped

    @property
    def body_color(self) -> Optional[Color]:
        """Returns the color of the body.

        Returns:
            The body color or None if not set.
        """
        return self._internals.body_color

    @body_color.setter
    def body_color(self, color: Optional[Color]) -> None:
        """Sets the color of the body.

        Args:
            color: The new body color or None to use the default.
        """
        self._internals.body_color = color

    @property
    def turret_color(self) -> Optional[Color]:
        """Returns the color of the turret.

        Returns:
            The turret color or None if not set.
        """
        return self._internals.turret_color

    @turret_color.setter
    def turret_color(self, color: Optional[Color]) -> None:
        """Sets the color of the turret.

        Args:
            color: The new turret color or None to use the default.
        """
        self._internals.turret_color = color

    @property
    def radar_color(self) -> Optional[Color]:
        """Returns the color of the radar.

        Returns:
            The radar color or None if not set.
        """
        return self._internals.radar_color

    @radar_color.setter
    def radar_color(self, color: Optional[Color]) -> None:
        """Sets the color of the radar.

        Args:
            color: The new radar color or None to use the default.
        """
        self._internals.radar_color = color

    @property
    def bullet_color(self) -> Optional[Color]:
        """Returns the color of the bullets.

        Returns:
            The bullet color or None if not set.
        """
        return self._internals.bullet_color

    @bullet_color.setter
    def bullet_color(self, color: Optional[Color]) -> None:
        """Sets the color of the bullets.

        Args:
            color: The new bullet color or None to use the default.
        """
        self._internals.bullet_color = color

    @property
    def scan_color(self) -> Optional[Color]:
        """Returns the color of the scan arc.

        Returns:
            The scan color or None if not set.
        """
        return self._internals.scan_color

    @scan_color.setter
    def scan_color(self, color: Optional[Color]) -> None:
        """Sets the color of the scan arc.

        Args:
            color: The new scan color or None to use the default.
        """
        self._internals.scan_color = color

    @property
    def tracks_color(self) -> Optional[Color]:
        """Returns the color of the tracks.

        Returns:
            The tracks color or None if not set.
        """
        return self._internals.tracks_color

    @tracks_color.setter
    def tracks_color(self, color: Optional[Color]) -> None:
        """Sets the color of the tracks.

        Args:
            color: The new tracks color or None to use the default.
        """
        self._internals.tracks_color = color

    @property
    def gun_color(self) -> Optional[Color]:
        """Returns the color of the gun.

        Returns:
            The gun color or None if not set.
        """
        return self._internals.gun_color

    @gun_color.setter
    def gun_color(self, color: Optional[Color]) -> None:
        """Sets the color of the gun.

        Args:
            color: The new gun color or None to use the default.
        """
        self._internals.gun_color = color

    @property
    def debugging_enabled(self) -> bool:
        """Checks if debugging is enabled for this bot.

        Returns:
            True if debugging is enabled; False otherwise.
        """
        tick = self._internals.get_current_tick_or_throw()
        bot_state = tick.bot_state
        assert bot_state is not None
        return bot_state.debugging_enabled

    @property
    def graphics(self) -> GraphicsABC:
        """Returns the graphics context for drawing debug graphics.

        Returns:
            The graphics context.
        """
        return self._internals.get_graphics()

    # Utility methods
    def calc_max_turn_rate(self, speed: float) -> float:
        """Calculates the maximum turn rate for a given speed.

        Args:
            speed: The speed.

        Returns:
            The maximum turn rate at the given speed.
        """
        return MAX_TURN_RATE - 0.75 * math.fabs(
            MathUtil.clamp(speed, -MAX_SPEED, MAX_SPEED)
        )

    def calc_bullet_speed(self, firepower: float) -> float:
        """Calculates the bullet speed for a given firepower.

        Args:
            firepower: The firepower.

        Returns:
            The bullet speed.
        """
        return 20 - 3 * MathUtil.clamp(firepower, MIN_FIREPOWER, MAX_FIREPOWER)

    def calc_gun_heat(self, firepower: float) -> float:
        """Calculates the gun heat generated by firing with a given firepower.

        Args:
            firepower: The firepower.

        Returns:
            The gun heat generated.
        """
        return 1 + (MathUtil.clamp(firepower, MIN_FIREPOWER, MAX_FIREPOWER) / 5)

    def get_event_priority(self, event_class: type) -> int:
        """Returns the priority of an event class.

        Args:
            event_class: The event class.

        Returns:
            The priority of the event class.
        """
        return EventPriorities.get_priority(event_class)

    def set_event_priority(self, event_class: type, priority: int) -> None:
        """Sets the priority of an event class.

        Args:
            event_class: The event class.
            priority: The new priority.
        """
        EventPriorities.set_priority(event_class, priority)
