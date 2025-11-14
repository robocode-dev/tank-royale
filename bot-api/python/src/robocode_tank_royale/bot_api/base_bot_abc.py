import math
import traceback
from abc import ABC, abstractmethod
from typing import Any, Sequence

from .graphics import GraphicsABC

from .events import *
from .bullet_state import BulletState
from .graphics import Color


class BaseBotABC(ABC):
    """
    Interface containing the core API for a bot.
    """

    TEAM_MESSAGE_MAX_SIZE: int = 32768  # bytes
    """Maximum size of a team message, which is 32 KB."""

    MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN: int = 10
    """The maximum number of team messages that can be sent per turn, which is 10 messages."""

    @abstractmethod
    async def start(self) -> None:
        """
        The method used to start running the bot. You should call this method from the main function or a similar entry point.

        Example::

            def main():
                # create my_bot
                ...
                my_bot.start()
        """
        pass

    @abstractmethod
    async def go(self) -> None:
        """
        Commits the current commands (actions), which finalizes the current turn for the bot.

        This method must be called once per turn to send the bot's actions to the server and must be called before the turn timeout occurs. A turn timer starts when the `GameStartedEvent` and `TickEvent` occur. If the `go()` method is called too late, a turn timeout will occur, and the `SkippedTurnEvent` will be triggered, meaning the bot has skipped all actions for the previous turn. In such a case, the server will continue executing the last actions received. This could be fatal for the bot due to the loss of control over it. Ensure that `go()` is called before the turn ends.

        The commands executed when `go()` is called are set via the appropriate setter methods prior to calling `go()`: `setTurnRate`, `setGunTurnRate`, `setRadarTurnRate`, `setTargetSpeed`, and `setFire`.

        See also:
            `getTurnTimeout`: For additional information on the turn timeout.
        """
        pass

    @abstractmethod
    def get_my_id(self) -> int:
        """
        Returns the bot's unique identifier.
        """
        pass

    @abstractmethod
    def get_variant(self) -> str:
        """
        The game variant, which is "Tank Royale".

        Returns:
            str: The game variant of Robocode.
        """
        pass

    @abstractmethod
    def get_version(self) -> str:
        """
        Game version, e.g. "1.0.0".

        Returns:
            str: The game version.
        """
        pass

    @abstractmethod
    def get_game_type(self) -> str:
        """
        Game type, e.g. "melee" or "1v1".

        First available when the game has started.

        Returns:
            str: The game type.
        """
        pass

    @abstractmethod
    def get_arena_width(self) -> int:
        """
        Width of the arena measured in units.

        First available when the game has started.

        Returns:
            int: The arena width measured in units
        """
        pass

    @abstractmethod
    def get_arena_height(self) -> int:
        """
        Height of the arena measured in units.

        First available when the game has started.

        Returns:
            int: The arena height measured in units
        """
        pass

    @abstractmethod
    def get_number_of_rounds(self) -> int:
        """
        The number of rounds in a battle.

        First available when the game has started.

        Returns:
            int: The number of rounds in a battle.
        """
        pass

    @abstractmethod
    def get_gun_cooling_rate(self) -> float:
        """
        Gun cooling rate.

        The gun needs to cool down to a gun heat of zero before the gun can fire.
        The gun cooling rate determines how fast the gun cools down. That is, the gun cooling rate is
        subtracted from the gun heat each turn until the gun heat reaches zero.

        First available when the game has started.

        Returns:
            float: The gun cooling rate.
        """
        pass

    @abstractmethod
    def get_max_inactivity_turns(self) -> int:
        """
        The maximum number of inactive turns allowed.

        The bot will become zapped by the game for being inactive. Inactive means
        that the bot has taken no action in several turns in a row.

        First available when the game has started.

        Returns:
            int: The maximum number of allowed inactive turns.
        """
        pass

    @abstractmethod
    def get_turn_timeout(self) -> int:
        """
        The turn timeout in microseconds.

        The turn timeout is important as the bot needs to take action by calling go() before
        the turn timeout occurs. As soon as the TickEvent() is triggered, i.e. when onTick() is called,
        you need to call go() to take action before the turn timeout occurs.
        Otherwise, your bot will skip a turn and receive a onSkippedTurn for each turn where
        go() is called too late.

        First available when the game has started.

        Returns:
            int: The turn timeout in microseconds.
        """
        pass

    @abstractmethod
    def get_time_left(self) -> int:
        """
        The number of microseconds left of this turn before the bot will skip the turn.

        Make sure to call go() before the time runs out.

        Returns:
            int: The amount of time left in microseconds.
        """
        pass

    @abstractmethod
    def get_round_number(self) -> int:
        """
        Current round number.

        Returns:
            int: The current round number.
        """
        pass

    @abstractmethod
    def get_turn_number(self) -> int:
        """
        Current turn number.

        Returns:
            int: The current turn number.
        """
        pass

    @abstractmethod
    def get_enemy_count(self) -> int:
        """
        Number of enemies left in the round.

        Returns:
            int: The number of enemies left in the round.
        """
        pass

    @abstractmethod
    def get_energy(self) -> float:
        """
        Current energy level.

        When the energy level is positive, the bot is alive and active. When the
        energy level is 0, the bot is still alive but disabled. If the bot becomes disabled it will not
        be able to move or take any action. If negative, the bot has been defeated.

        Returns:
            float: The current energy level.
        """
        pass

    @abstractmethod
    def is_disabled(self) -> bool:
        """
        Specifies if the bot is disabled, i.e., when the energy is zero.

        When the bot is disabled, it is not able to take any action like movement, turning, and firing.

        Returns:
            bool: True if the bot is disabled; False otherwise.
        """
        pass

    @abstractmethod
    def get_x(self) -> float:
        """
        Current X coordinate of the center of the bot.

        Returns:
            float: The current X coordinate of the bot.
        """
        pass

    @abstractmethod
    def get_y(self) -> float:
        """
        Current Y coordinate of the center of the bot.

        Returns:
            float: The current Y coordinate of the bot.
        """
        pass

    @abstractmethod
    def get_direction(self) -> float:
        """
        Current driving direction of the bot in degrees.

        Returns:
            float: The current driving direction of the bot.
        """
        pass

    @abstractmethod
    def get_gun_direction(self) -> float:
        """
        Current direction of the gun in degrees.

        Returns:
            float: The current gun direction of the bot.
        """
        pass

    @abstractmethod
    def get_radar_direction(self) -> float:
        """
        Current direction of the radar in degrees.

        Returns:
            float: The current radar direction of the bot.
        """
        pass

    @abstractmethod
    def get_speed(self) -> float:
        """
        The current speed measured in units per turn.

        If the speed is positive, the bot moves forward. If negative, the bot moves backward.
        Zero speed means that the bot is not moving from its current position.

        Returns:
            float: The current speed.
        """
        pass

    @abstractmethod
    def get_gun_heat(self) -> float:
        """
        Current gun heat.

        When the gun is fired it gets heated and will not be able to fire before it has
        been cooled down. The gun is cooled down when the gun heat is zero.
        When the gun has fired the gun heat is set to 1 + (firepower / 5) and will be cooled down by
        the gun cooling rate.

        Returns:
            float: The current gun heat.
        """
        pass

    @abstractmethod
    def get_bullet_states(self) -> Sequence[BulletState | None] | None:
        """
        Current bullet states.

        Keeps track of all the bullets fired by the bot, which are still active on the arena.

        Returns:
            list[BulletState]: The current bullet states.
        """
        pass

    @abstractmethod
    def get_events(self) -> Sequence[BotEvent | None] | None:
        """
        Returns an ordered list containing all events currently in the bot's event queue.

        You might, for example, call this while processing another event.

        Returns:
            list[BotEvent]: An ordered list containing all events currently in the bot's event queue.
        """
        pass

    @abstractmethod
    def clear_events(self) -> None:
        """
        Clears out any pending events in the bot's event queue immediately.
        """
        pass

    @property
    @abstractmethod
    def turn_rate(self) -> float:
        """
        Returns the turn rate of the bot in degrees per turn.

        Returns:
            float: The turn rate of the bot.
        """
        pass

    @turn_rate.setter
    @abstractmethod
    def turn_rate(self, turn_rate: float) -> None:
        """
        Sets the turn rate of the bot, which can be positive or negative. The turn rate is measured in
        degrees per turn. The turn rate is added to the current direction of the bot, as well as the
        current directions of the gun and radar. This is because the gun is mounted on the bot's body
        and turns with it, and the radar is mounted on the gun and moves with it.

        You can compensate for the bot's turn rate by subtracting the bot's turn rate from the turn
        rates of the gun and radar. However, be aware that the turn limits defined for the gun and
        radar cannot be exceeded.

        The turn rate is truncated to `MAX_TURN_RATE` if the turn rate exceeds this value.

        If this property is set multiple times, the last value set before `go()` is called will be used.

        Args:
            turn_rate (float): The new turn rate of the bot in degrees per turn.
        """
        pass

    @property
    @abstractmethod
    def max_turn_rate(self) -> float:
        """
        Returns the maximum turn rate of the bot in degrees per turn.

        Returns:
            float: The maximum turn rate of the bot.
        """
        pass

    @max_turn_rate.setter
    @abstractmethod
    def max_turn_rate(self, max_turn_rate: float) -> None:
        """
        Sets the maximum turn rate, which applies to turning the bot to the left or right.

        The maximum turn rate must be an absolute value between 0 and `Constants.MAX_TURN_RATE`, inclusive.
        If the input turn rate is negative, the maximum turn rate will be set to zero. If the input turn
        rate exceeds `Constants.MAX_TURN_RATE`, the maximum turn rate will be set to `Constants.MAX_TURN_RATE`.

        For example, if the maximum turn rate is set to 5, the bot will be able to:
        - Turn right with a rate down to -5 degrees per turn.
        - Turn left with a rate up to 5 degrees per turn.

        This method will take effect only when the `go()` method is called, allowing other setter methods
        to be invoked beforehand. This makes it possible to set multiple actions (e.g., move, turn the body,
        radar, gun, and fire) in parallel within a single turn when calling `go()`.

        Note that calling this method multiple times will result in the last call before `go()` taking precedence.

        Args:
            max_turn_rate (float): The new maximum turn rate.

        See Also:
            set_turn_rate: For setting the turn rate directly.
        """
        pass

    @property
    @abstractmethod
    def gun_turn_rate(self) -> float:
        """
        Returns the gun turn rate in degrees per turn.

        Returns:
            float: The turn rate of the gun.
        """
        pass

    @gun_turn_rate.setter
    @abstractmethod
    def gun_turn_rate(self, gun_turn_rate: float) -> None:
        """
        Sets the turn rate of the gun, which can be positive or negative. The gun turn rate is
        measured in degrees per turn. The turn rate is added to the current turn direction of the gun.
        However, the turn rate also influences the radar direction, as the radar is mounted on the gun
        and moves with it.

        To compensate for the gun's turn rate, you can subtract the gun turn rate from the radar turn
        rate, but note that the radar turn limits cannot be exceeded.

        The gun turn rate is truncated to the maximum allowed turn rate if it exceeds that value.

        If this property is set multiple times, only the last value set prior to calling `go()` is used.

        Args:
            gun_turn_rate (float): The new turn rate of the gun, in degrees per turn.
        """
        pass

    @property
    @abstractmethod
    def max_gun_turn_rate(self) -> float:
        """
        Returns the maximum gun turn rate in degrees per turn.

        Returns:
            float: The maximum turn rate of the gun.
        """
        pass

    @max_gun_turn_rate.setter
    @abstractmethod
    def max_gun_turn_rate(self, max_gun_turn_rate: float) -> None:
        """
        Sets the maximum turn rate for rotating the gun to the left or right. The maximum turn rate
        must be an absolute value between 0 and `Constants.MAX_GUN_TURN_RATE`, inclusive. If the input
        turn rate is negative, it will be set to 0. If the input turn rate exceeds
        `Constants.MAX_GUN_TURN_RATE`, it will be set to `Constants.MAX_GUN_TURN_RATE`.

        For example, if the maximum gun turn rate is set to 5, the gun will be able to turn left
        or right with a turn rate ranging from -5 degrees per turn (when turning right) to
        5 degrees per turn (when turning left).

        This method will take effect when the `go()` method is called, making it possible to set
        multiple attributes such as movement, body rotation, radar rotation, gun rotation, and firing
        actions in parallel within a single turn. Note that this is only achievable by calling
        setter methods before invoking `go()`.

        If this method is called multiple times before `go()` is executed, only the last call will
        take effect.

        Args:
            max_gun_turn_rate (float): The new maximum gun turn rate.

        See Also:
            set_gun_turn_rate: To adjust the current gun turn rate.
        """
        pass

    @property
    @abstractmethod
    def radar_turn_rate(self) -> float:
        """
        Returns the radar turn rate in degrees per turn.

        Returns:
            float: The turn rate of the radar.
        """
        pass

    @radar_turn_rate.setter
    @abstractmethod
    def radar_turn_rate(self, radar_turn_rate: float) -> None:
        """
        Sets the turn rate of the radar, which can be positive or negative. The radar turn rate is
        measured in degrees per turn. The turn rate is added to the current direction of the radar.

        Note:
            - Besides the turn rate of the radar, the turn rates of the bot and gun are also added
              to the radar direction because the radar moves with the gun, which is mounted on the gun
              that moves with the body.
            - You can compensate for the turn rate of the gun by subtracting the turn rates of the bot
              and gun from the radar turn rate. However, be aware that the turn limits defined for the
              radar cannot be exceeded.
            - The radar turn rate is truncated to `Constants.MAX_RADAR_TURN_RATE` if it exceeds this value.
            - If this method is called multiple times, the last value set before the `go()` method is called
              will take effect.

        Args:
            radar_turn_rate (float): The new turn rate of the radar in degrees per turn.
        """
        pass

    @property
    @abstractmethod
    def max_radar_turn_rate(self) -> float:
        """
        Returns the maximum radar turn rate in degrees per turn.

        Returns:
            float: The maximum turn rate of the radar.
        """
        pass

    @max_radar_turn_rate.setter
    @abstractmethod
    def max_radar_turn_rate(self, max_radar_turn_rate: float) -> None:
        """
        Sets the maximum turn rate for turning the radar to the left or right. The maximum turn rate must
        be an absolute value between 0 and `Constants.MAX_RADAR_TURN_RATE`, inclusive. If the input turn
        rate is negative, the maximum turn rate will be set to zero. If the input turn rate exceeds
        `Constants.MAX_RADAR_TURN_RATE`, it will be capped at `Constants.MAX_RADAR_TURN_RATE`.

        For example, if the maximum radar turn rate is set to 5, the radar can turn up to 5 degrees to
        the left or down to -5 degrees to the right per turn.

        This method will only take effect when the `go()` method is called. This allows the bot to
        configure multiple settings for movement, body turns, radar turns, gun turns, and firing actions
        in a single turn before executing them with `go()`. Note that executing multiple methods in
        parallel is only achievable by calling setter methods prior to invoking `go()`.

        If this method is called multiple times, the last invocation before `go()` will determine the
        effective radar turn rate.

        Args:
            max_radar_turn_rate (float): The new maximum radar turn rate.

        See Also:
            set_radar_turn_rate()
        """
        pass

    @property
    @abstractmethod
    def target_speed(self) -> float:
        """
        Returns the target speed in units per turn.

        Returns:
            float: The target speed.
        """
        pass

    @target_speed.setter
    @abstractmethod
    def target_speed(self, target_speed: float) -> None:
        """
        Sets the new target speed for the bot in units per turn. The target speed is the speed you want
        to achieve eventually, which could take one to several turns depending on the current speed.
        For example, if the bot is moving forward with max speed, and then must change to move backward
        at full speed, the bot will have to first decelerate/brake its positive speed (moving forward).
        After passing a speed of zero, it will then need to accelerate to achieve max negative speed.

        Note:
            - Acceleration is 1 unit per turn.
            - Deceleration/braking is faster than acceleration, as it is -2 units per turn.
            - Deceleration is applied as a negative value since it is added to the speed, reducing it
              when slowing down.

        The target speed is truncated to the maximum allowable speed if it exceeds the defined limit.

        If this property is set multiple times before execution (e.g., in a single turn), only the
        last set value will be applied once the method `go()` is invoked.

        Args:
            target_speed (float): The new target speed in units per turn.
        """
        pass

    @property
    @abstractmethod
    def max_speed(self) -> float:
        """
        Returns the maximum speed in units per turn.

        Returns:
            float: The maximum speed.
        """
        pass

    @max_speed.setter
    @abstractmethod
    def max_speed(self, max_speed: float) -> None:
        """
        Sets the maximum speed for the bot.

        The value must be between 0 and Constants.MAX_SPEED. Negative values
        default to 0, and values above Constants.MAX_SPEED are truncated.

        Example:
            set_max_speed(5)  # Sets max speed to 5 units per turn.

        Args:
            max_speed (float): The desired maximum speed.
        """
        pass

    @abstractmethod
    def set_fire(self, firepower: float) -> bool:
        """
        Sets the gun to fire in the direction that the gun is pointing with the specified firepower.

        Firepower is the amount of energy your bot will spend on firing the gun. This means that the
        bot will lose power on firing the gun, where the energy loss is equal to the firepower. You
        cannot spend more energy than is available from your bot.

        The bullet power must be greater than the minimum firepower and gun heat must be zero
        before the gun can fire.

        If the bullet hits an opponent bot, you will gain energy from the bullet hit.
        When hitting another bot, your bot will be rewarded and retrieve an energy boost of 3x
        the firepower.

        The gun will only fire when the firepower is at or above the minimum firepower. If the firepower
        is greater than the maximum firepower, the power will be truncated to the maximum firepower.

        Whenever the gun is fired, the gun becomes heated and needs to cool down before it can fire
        again. The gun heat must be zero before the gun can fire again. The gun heat generated by firing
        the gun is calculated as 1 + (firepower / 5). Hence, the more firepower used, the longer it
        takes to cool down the gun. The gun cooling rate can be retrieved using the `get_gun_cooling_rate()` function.

        The amount of energy used for firing the gun is subtracted from the bot's total energy. The
        amount of damage dealt by a bullet hitting another bot is 4x firepower. If the firepower is
        greater than 1, it will deal an additional 2 x (firepower - 1) damage.

        Note that the gun will automatically keep firing each turn as soon as the gun heat reaches
        zero. It is possible to disable the gun firing by setting the firepower to zero.

        The firepower is truncated between 0 and the maximum firepower if the provided value exceeds
        this range.

        If this property is set multiple times, the last value set before calling `go()` is used.

        Args:
            firepower (float): The new firepower.

        Returns:
            bool: True if the cannon can fire (i.e., if there is no gun heat), False otherwise.

        See Also:
            - `on_bullet_fired()`
            - `get_firepower()`
            - `get_gun_heat()`
            - `get_gun_cooling_rate()`
        """
        pass

    @abstractmethod
    def get_firepower(self) -> float:
        """
        Returns the firepower.

        Returns:
            float: The firepower.
        """
        pass

    @abstractmethod
    def set_rescan(self) -> None:
        """
        Sets the bot to rescan with the radar.

        This method is useful if the radar has not turned, and hence will not automatically scan bots.
        The last radar direction and sweep angle will be used for scanning for bots.
        """
        pass

    @abstractmethod
    def set_fire_assist(self, enable: bool) -> None:
        """
        Enables or disables fire assistance explicitly. Fire assistance is useful for bots with
        limited aiming capabilities as it helps the bot by firing directly at a scanned bot
        when the gun is fired, which is a very simple aiming strategy.

        When fire assistance is enabled, the gun will fire towards the center of the scanned bot
        when all these conditions are met:

        1. The gun is fired (via `set_fire` or `fire()`).
        2. The radar is scanning a bot *when* firing the gun (e.g., in the `on_scanned_bot()` event,
           after calling `set_rescan()` or `rescan()`).
        3. The gun and radar are pointing in the exact same direction. You can disable radar
           and gun movement alignment using `set_adjust_radar_for_gun_turn(False)` to ensure
           the gun and radar stay aligned while avoiding radar turning independently of the gun.

        The fire assistance feature is provided for backwards compatibility with the original Robocode,
        where bots that were not considered `AdvancedRobot` had fire assistance enabled by default,
        as their gun and radar could not move independently of each other. In contrast, `AdvancedRobot`
        allows the body, gun, and radar to move independently.

        Args:
            enable (bool): Enables fire assistance when set to True, and disables it otherwise.
        """
        pass

    @abstractmethod
    def set_interruptible(self, interruptible: bool) -> None:
        """
        Sets whether the bot's event handlers are interruptible.

        When set to True, event handlers can be interrupted by higher-priority events.
        When set to False, handlers run to completion before other events are processed.

        Args:
            interruptible (bool): If True, event handlers are interruptible; otherwise, they are not.
        """
        pass

    @abstractmethod
    def set_adjust_gun_for_body_turn(self, adjust: bool) -> None:
        """
        Sets whether the gun's direction should adjust for the bot's body turn.

        When set to True, the gun maintains its direction relative to the body as the bot turns.
        When set to False, the gun turns with the body.

        Args:
            adjust (bool): If True, gun direction is adjusted for body turn.
        """
        pass

    @abstractmethod
    def set_adjust_radar_for_gun_turn(self, adjust: bool) -> None:
        """
        Sets whether the radar's direction should adjust for the gun's turn.

        When set to True, the radar maintains its direction relative to the gun as the gun turns.
        When set to False, the radar turns with the gun.

        Args:
            adjust (bool): If True, radar direction is adjusted for gun turn.
        """
        pass

    @abstractmethod
    def is_teammate(self, bot_id: int) -> bool:
        """
        Checks if the specified bot ID is a teammate.

        Args:
            bot_id (int): The bot ID to check.

        Returns:
            bool: True if the bot is a teammate; False otherwise.
        """
        pass

    @abstractmethod
    def get_teammate_ids(self) -> set[int]:
        """
        Returns the IDs of all teammates.

        Returns:
            Set[int]: A set of IDs of all teammates if the bot is participating in a team,
            or an empty set if the bot is not in a team.

        See Also:
            is_teammate: Checks if a bot is a teammate.
            send_team_message: Sends a message to the team.
        """
        pass

    @abstractmethod
    def broadcast_team_message(self, message: Any) -> None:
        """
        Broadcasts a message to all teammates.

        When the message is sent, it is serialized into a JSON representation. This means that all public
        fields, and only public fields, are serialized into a JSON representation as a data transfer object (DTO).

        The maximum team message size limit is defined by `TEAM_MESSAGE_MAX_SIZE`, which is set to 32,768 bytes.
        This size is calculated after serializing the message into a JSON representation.

        The maximum number of messages that can be broadcast per turn is limited to `MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN`,
        which is set to 10.

        Args:
            message: The message to broadcast.

        Raises:
            ValueError: If the size of the message exceeds the size limit.

        See Also:
            send_team_message: Method to send a message to teammates.
            get_teammate_ids: Method to retrieve IDs of all teammates.
        """
        pass

    @abstractmethod
    def send_team_message(self, teammate_id: int, message: Any) -> None:
        """
        Sends a message to a specific teammate.

        When the message is sent, it is serialized into a JSON representation,
        meaning that all public fields, and only public fields, are being
        serialized into a JSON representation as a DTO (data transfer object).

        The maximum team message size limit is defined by
        `TEAM_MESSAGE_MAX_SIZE`, which is set to
        `TEAM_MESSAGE_MAX_SIZE` bytes. This size is the size of the message
        when it is serialized into a JSON representation.

        The maximum number of messages that can be sent/broadcast per turn is
        limited to `MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN`.

        Args:
            teammate_id: The id of the teammate to send the message to.
            message: The message to send.

        Raises:
            ValueError: If the size of the message exceeds the size limit.
        """
        pass

    @abstractmethod
    def is_stopped(self) -> bool:
        """
        Checks if the movement has been stopped.

        Returns:
            bool: True if the movement has been stopped by `set_stop()`, False otherwise.

        See Also:
            set_resume: Resumes the movement.
            set_stop(): Stops the movement.
            set_stop(flag: bool): Stops the movement, with a flag to specify additional behavior.
        """
        pass

    @property
    @abstractmethod
    def body_color(self) -> Color | None:
        """
        Returns the color of the body.

        Returns:
            Color: The color of the body, or `None` if no color has been set yet.
                   In that case, the default color will be used.
        """
        pass

    @body_color.setter
    @abstractmethod
    def body_color(self, color: Color | None) -> None:
        """
        Sets the color of the body. Colors can (only) be changed each turn.

        Example:
            set_body_color(Color.RED)         # the red color
            set_body_color(Color(255, 0, 0))  # also the red color

        Args:
            color: The color of the body or `None` if the bot must use the default color instead.
        """
        pass

    @property
    @abstractmethod
    def turret_color(self) -> Color|None:
        """
        Returns the color of the gun turret.

        Returns:
            The color of the turret or `None` if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @turret_color.setter
    @abstractmethod
    def turret_color(self, color: Color|None) -> None:
        """
        Sets the color of the gun turret.

        Colors can (only) be changed each turn.

        Example:
            set_turret_color(Color.RED)         # the red color
            set_turret_color(Color(255, 0, 0))  # also the red color

        Args:
            color: The color of the gun turret or `None` if the bot must use the
                default color instead.
        """
        pass

    @property
    @abstractmethod
    def radar_color(self) -> Color|None:
        """
        Returns the color of the radar.

        Returns:
            Color: The color of the radar. If no color has been set yet, returns `None`,
            which indicates that the default radar color will be used.
        """
        pass

    @radar_color.setter
    @abstractmethod
    def radar_color(self, color: Color|None) -> None:
        """
        Sets the color of the radar.

        Colors can (only) be changed each turn.

        Example:
            set_radar_color(Color.RED)         # the red color
            set_radar_color(Color(255, 0, 0))  # also the red color

        Args:
            color: The color of the radar or `None` if the bot must use the default color instead.
        """
        pass

    @property
    @abstractmethod
    def bullet_color(self) -> Color|None:
        """
        Returns the color of the fired bullets.

        Returns:
            Color: The color of the bullets, or `None` if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @bullet_color.setter
    @abstractmethod
    def bullet_color(self, color: Color|None) -> None:
        """
        Sets the color of the fired bullets. Colors can only be changed each turn.

        Note:
            A fired bullet will not change its color once it has been fired. However, new bullets fired
            after setting the bullet color will use the new color.

        Example:
            set_bullet_color(Color.RED)         # The red color
            set_bullet_color(Color(255, 0, 0))  # Also the red color

        Args:
            color (Color or None):
                The color of the fired bullets. Pass `None` to use the default color instead.
        """
        pass

    @property
    @abstractmethod
    def scan_color(self) -> Color|None:
        """
        Returns the color of the scan arc.

        Returns:
            Color: The color of the scan arc, or `None` if no color has been set, meaning the default color will be used.
        """
        pass

    @scan_color.setter
    @abstractmethod
    def scan_color(self, color: Color|None) -> None:
        """
        Sets the color of the scan arc. Colors can (only) be changed each turn.

        Example:
            set_scan_color(Color.RED)         # the red color
            set_scan_color(Color(255, 0, 0))  # also the red color

        Args:
            color: The color of the scan arc. Pass `None` if the bot should use the default color instead.
        """
        pass

    @property
    @abstractmethod
    def tracks_color(self) -> Color|None:
        """
        Returns the color of the tank tracks.

        Returns:
            Color: The color of the tank tracks, or `None` if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @tracks_color.setter
    @abstractmethod
    def tracks_color(self, color: Color|None) -> None:
        """
        Sets the color of the tracks. Colors can only be changed once per turn.

        Example:
            set_tracks_color(Color.RED)         # The red color
            set_tracks_color(Color(255, 0, 0))  # Also the red color

        Args:
            color (Color or None): The color of the tracks. Use `None` to reset to the default color.
        """
        pass

    @property
    @abstractmethod
    def gun_color(self) -> Color|None:
        """
        Returns the color of the gun.

        Returns:
            Color: The color of the gun, or `None` if no color has been set yet. If `None`, the default color
            will be used.
        """
        pass

    @gun_color.setter
    @abstractmethod
    def gun_color(self, color: Color|None) -> None:
        """
        Sets the color of the gun.

        Colors can (only) be changed each turn.

        Example:
            set_gun_color(Color.RED)         # the red color
            set_gun_color(Color(255, 0, 0))  # also the red color

        Args:
            color: The color of the gun or `None` if the bot must use the
                default color instead.
        """
        pass

    @abstractmethod
    def is_debugging_enabled(self) -> bool:
        """
        Indicates whether graphical debugging is enabled. If enabled, the `get_graphics`
        method can be used for debug painting.

        Returns:
            bool: True if graphical debugging is enabled, False otherwise.
        """
        pass

    @abstractmethod
    def get_graphics(self) -> GraphicsABC:
        """
        Gets a graphics object for debug painting.

        Example:
            g = get_graphics()
            g.set_fill_color(Color.from_rgb(0, 0, 255))
            g.fill_rectangle(50, 50, 100, 100)  # A blue filled rect

        Returns:
            A graphics canvas to use for painting graphical objects, making debugging easier.
        """
        pass

    @staticmethod
    async def on_connected(connected_event: ConnectedEvent) -> None:
        """
        The event handler triggered when connected to the server.

        Args:
            connected_event: The event details from the game.
        """
        print(f"Connected to: {connected_event.server_uri}")  # Use f-string

    @staticmethod
    async def on_disconnected(disconnected_event: DisconnectedEvent) -> None:
        """
        The event handler triggered when disconnected from the server.

        Args:
            disconnected_event: The event details from the game.
        """
        msg = f"Disconnected from: {disconnected_event.server_uri}"  # Use f-string
        if disconnected_event.status_code is not None:
            msg += f", status code: {disconnected_event.status_code}"
        if disconnected_event.reason is not None:
            msg += f", reason: {disconnected_event.reason}"

        print(msg)

    @staticmethod
    async def on_connection_error(connection_error_event: ConnectionErrorEvent) -> None:
        """
        The event handler triggered when a connection error occurs.

        Parameters:
            connection_error_event: Object containing details about the connection error from the game.
        """
        print(f"Connection error with {connection_error_event.server_uri}")

        if connection_error_event.error is not None:
            traceback.print_exception(
                connection_error_event.error
            )  # Print the exception

    async def on_game_started(self, game_started_event: GameStartedEvent) -> None:
        """
        The event handler triggered when a game has started.

        Args:
            game_started_event: The event details from the game.
        """
        pass

    async def on_game_ended(self, game_ended_event: GameEndedEvent) -> None:
        """
        The event handler triggered when a game has ended.

        Args:
            game_ended_event: The event details from the game.
        """
        pass

    async def on_round_started(self, round_started_event: RoundStartedEvent) -> None:
        """
        The event handler triggered when a new round has started.

        Args:
            round_started_event: The event details from the game.
        """
        pass

    async def on_round_ended(self, round_ended_event: RoundEndedEvent) -> None:
        """
        The event handler triggered when a round has ended.

        Args:
            round_ended_event: The event details from the game.
        """
        pass

    async def on_tick(self, tick_event: TickEvent) -> None:
        """
        The event handler triggered when a game tick event occurs, i.e., when a new turn in a round has started.

        Args:
            tick_event: The event details from the game.
        """
        pass

    async def on_bot_death(self, bot_death_event: BotDeathEvent) -> None:
        """
        The event handler triggered when another bot has died.

        Args:
            bot_death_event: The event details from the game.
        """
        pass

    async def on_death(self, death_event: DeathEvent) -> None:
        """
        The event handler triggered when this bot has died.

        Args:
            death_event: The event details from the game.
        """
        pass

    async def on_hit_bot(self, bot_hit_bot_event: HitBotEvent) -> None:
        """
        The event handler triggered when the bot has collided with another bot.

        Args:
            bot_hit_bot_event: The event details from the game.
        """
        pass

    async def on_hit_wall(self, bot_hit_wall_event: HitWallEvent) -> None:
        """
        The event handler triggered when the bot has hit a wall.

        Args:
            bot_hit_wall_event: The event details from the game.
        """
        pass

    async def on_bullet_fired(self, bullet_fired_event: BulletFiredEvent) -> None:
        """
        The event handler triggered when the bot has fired a bullet.

        Args:
            bullet_fired_event: The event details from the game.
        """
        pass

    async def on_hit_by_bullet(self, hit_by_bullet_event: HitByBulletEvent) -> None:
        """
        The event handler triggered when the bot has been hit by a bullet.

        Args:
            hit_by_bullet_event: The event details from the game.
        """
        pass

    async def on_bullet_hit(self, bullet_hit_bot_event: BulletHitBotEvent) -> None:
        """
        The event handler triggered when the bot has hit another bot with a bullet.

        Args:
            bullet_hit_bot_event: The event details from the game.
        """
        pass

    async def on_bullet_hit_bullet(
        self, bullet_hit_bullet_event: BulletHitBulletEvent
    ) -> None:
        """
        The event handler triggered when a bullet fired from the bot has collided with another bullet.

        Args:
            bullet_hit_bullet_event: The event details from the game.
        """
        pass

    async def on_bullet_hit_wall(self, bullet_hit_wall_event: BulletHitWallEvent) -> None:
        """
        The event handler triggered when a bullet has hit a wall.

        Args:
            bullet_hit_wall_event: The event details from the game.
        """
        pass

    async def on_scanned_bot(self, scanned_bot_event: ScannedBotEvent) -> None:
        """
        Event handler triggered when the bot's radar scans another bot.

        This event occurs when the bot's radar detects another bot within its scanning range.
        The event provides information about the scanned bot such as its position, energy level,
        and velocity.

        The information provided by this event is essential for targeting, tracking, and making
        strategic decisions based on the positions and states of other bots on the battlefield.

        Args:
            scanned_bot_event: The event details about the scanned bot from the game.
        """
        pass

    async def on_skipped_turn(self, skipped_turn_event: SkippedTurnEvent) -> None:
        """
        Handles the event when the bot skips a turn.

        A turn is skipped if the bot does not send any instructions to the server (via the `go()` method)
        before the turn timeout occurs. When this happens, the server continues using the last received
        set of actions, such as movement, turning rates, or firing commands.

        Reasons for skipped turns may include:
        - Excessive processing or delays in the bot's logic, leading to timeout.
        - Failure to invoke the `go()` method in the current turn.
        - Misaligned or unintended logic in the bot's turn-handling code.

        This method can be overridden to define custom behavior for handling skipped turns, such as
        logging the event, debugging performance issues, or modifying the bot's logic to avoid future skips.

        Args:
            skipped_turn_event (SkippedTurnEvent): An event containing details about the skipped turn.
        """
        pass

    async def on_won_round(self, won_round_event: WonRoundEvent) -> None:
        """
        The event handler triggered when the bot has won a round.

        This event indicates that the bot successfully won a single round in the match.

        Args:
            won_round_event: The event details from the game.
        """
        pass

    async def on_custom_event(self, custom_event: CustomEvent) -> None:
        """
        The event handler triggered when some specific custom condition has been met.

        You can differentiate between various types of conditions by using the `getName()` method
        of the condition object associated with the event.

        Args:
            custom_event: The event details from the game.
        """
        pass

    async def on_team_message(self, team_message_event: TeamMessageEvent) -> None:
        """
        The event handler triggered when the bot has received a message from a teammate.

        This event provides the message and its associated details when a teammate sends communication
        during the game.

        Args:
            team_message_event: The event details from the game.
        """
        pass

    @abstractmethod
    def calc_max_turn_rate(self, speed: float) -> float:
        """
        Calculates the maximum turn rate for a specific speed.

        Args:
            speed (float): The speed.

        Returns:
            float: The maximum turn rate determined by the given speed.
        """
        pass

    @abstractmethod
    def calc_bullet_speed(self, firepower: float) -> float:
        """
        Calculates the bullet speed given a firepower.

        Args:
            firepower (float): The firepower.

        Returns:
            float: The bullet speed determined by the given firepower.
        """
        pass

    @abstractmethod
    def calc_gun_heat(self, firepower: float) -> float:
        """
        Calculates gun heat after having fired the gun.

        Args:
            firepower (float): The firepower used when firing the gun.

        Returns:
            float: The gun heat produced when firing the gun with the given firepower.
        """
        pass

    @abstractmethod
    def get_event_priority(self, event_class: type) -> int:
        """
        Returns the event priority for a specific event class.

        Example:
            scanned_bot_event_priority = get_priority(ScannedBotEvent)

        Args:
            event_class: The event class to get the event priority for.

        Returns:
            int: The event priority for a specific event class.

        See Also:
            DefaultEventPriority
            set_event_priority
        """
        pass

    @abstractmethod
    def set_event_priority(self, event_class: type, priority: int) -> None:
        """
        Changes the event priority for an event class. The event priority determines
        which event types (classes) must be fired and handled before others. Events
        with higher priorities will be handled before events with lower priorities.

        Note:
            You should normally not need to change the event priority.

        Args:
            event_class (Type[BotEvent]): The event class to change the priority for.
            priority (int): The new priority, typically a positive number from 1 to 150.
                The higher the value, the higher the priority.

        See Also:
            DefaultEventPriority, get_event_priority.
        """
        pass

    def calc_bearing(self, direction: float) -> float:
        """
        Calculates the bearing (delta angle) between the input direction and the bot's direction.

        Example:
            bearing = calc_bearing(direction) = normalize_relative_degrees(direction - get_direction())

        Args:
            direction (float): The input direction to calculate the bearing from.

        Returns:
            float: A normalized bearing (delta angle) in the range [-180, 180).

        See Also:
            get_direction, normalize_relative_angle.
        """
        return self.normalize_relative_angle(direction - self.get_direction())

    def calc_gun_bearing(self, direction: float) -> float:
        """
        Calculates the bearing (delta angle) between the input direction and the gun's direction.

        Example:
            bearing = calc_gun_bearing(direction) =
                      normalize_relative_degrees(direction - get_gun_direction())

        Args:
            direction (float): The input direction to calculate the bearing from.

        Returns:
            float: A normalized bearing (delta angle) in the range [-180, 180).

        See Also:
            get_gun_direction, normalize_relative_angle.
        """
        return self.normalize_relative_angle(direction - self.get_gun_direction())

    def calc_radar_bearing(self, direction: float) -> float:
        """
        Calculates the bearing (delta angle) between the input direction and the radar's direction.

        Example:
            bearing = calc_radar_bearing(direction) =
                      normalize_relative_degrees(direction - get_radar_direction())

        Args:
            direction (float): The input direction to calculate the bearing from.

        Returns:
            float: A normalized bearing (delta angle) in the range [-180, 180).

        See Also:
            get_radar_direction, normalize_relative_angle.
        """
        return self.normalize_relative_angle(direction - self.get_radar_direction())

    def direction_to(self, x: float, y: float) -> float:
        """
        Calculates the direction (angle) from the bot's coordinates to a point (x, y).

        Args:
            x (float): The x-coordinate of the point.
            y (float): The y-coordinate of the point.

        Returns:
            float: The direction to the point (x, y) in the range [0, 360).
        """
        return self.normalize_absolute_angle(
            math.degrees(math.atan2(y - self.get_y(), x - self.get_x()))
        )

    def bearing_to(self, x: float, y: float) -> float:
        """
        Calculates the bearing (delta angle) between the bot's current direction and
        the direction to a point (x, y).

        Args:
            x (float): The x-coordinate of the point.
            y (float): The y-coordinate of the point.

        Returns:
            float: A bearing to the point (x, y) in the range [-180, 180).
        """
        return self.normalize_relative_angle(
            self.direction_to(x, y) - self.get_direction()
        )

    def gun_bearing_to(self, x: float, y: float) -> float:
        """
        Calculates the bearing (delta angle) between the gun's current direction and
        the direction to a point (x, y).

        Args:
            x (float): The x-coordinate of the point.
            y (float): The y-coordinate of the point.

        Returns:
            float: A bearing to the point (x, y) in the range [-180, 180).
        """
        return self.normalize_relative_angle(
            self.direction_to(x, y) - self.get_gun_direction()
        )

    def radar_bearing_to(self, x: float, y: float) -> float:
        """
        Calculates the bearing (delta angle) between the radar's current direction and
        the direction to a point (x, y).

        Args:
            x (float): The x-coordinate of the point.
            y (float): The y-coordinate of the point.

        Returns:
            float: A bearing to the point (x, y) in the range [-180, 180).
        """
        return self.normalize_relative_angle(
            self.direction_to(x, y) - self.get_radar_direction()
        )

    def distance_to(self, x: float, y: float) -> float:
        """
        Calculates the distance from the bot's coordinates to a point (x, y).

        Args:
            x (float): The x-coordinate of the point.
            y (float): The y-coordinate of the point.

        Returns:
            float: The distance to the point (x, y).
        """
        return math.hypot(x - self.get_x(), y - self.get_y())

    def normalize_absolute_angle(self, angle: float) -> float:
        """
        Normalizes an angle to an absolute angle in the range [0, 360).

        Args:
            angle (float): The angle to normalize.

        Returns:
            float: The normalized absolute angle.
        """
        angle %= 360
        return angle if angle >= 0 else angle + 360

    def normalize_relative_angle(self, angle: float) -> float:
        """
        Normalizes the given angle to the range [-180, 180] degrees.

        Args:
            angle (float): The angle to normalize.

        Returns:
            float: The normalized angle in degrees.
        """

        angle %= 360
        if angle >= 0:
            return angle if angle < 180 else angle - 360
        else:
            return angle if angle >= -180 else angle + 360

    def calc_delta_angle(self, target_angle: float, source_angle: float) -> float:
        """
        Calculates the difference between two angles, i.e., the number of degrees
        from a source angle to a target angle.

        Args:
            target_angle (float): The target angle.
            source_angle (float): The source angle.

        Returns:
            float: The delta angle in the range [-180, 180].
        """
        angle = target_angle - source_angle
        if angle > 180:
            angle -= 360
        elif angle < -180:
            angle += 360
        return angle
