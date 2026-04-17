from typing import Callable
import math

from .base_bot import BaseBot
from .bot_abc import BotABC
from .bot_info import BotInfo
from .events import Condition, TickEvent, HitBotEvent, ScannedBotEvent
from .internal.base_bot_internals import BaseBotInternals
from .internal.bot_internals import BotInternals
from .internal.stop_resume_listener_abs import StopResumeListenerABC
from .internal.event_interruption import EventInterruption
from .internal.thread_interrupted_exception import ThreadInterruptedException
from .constants import *


class Bot(BaseBot, BotABC):
    """Base class for a bot in Robocode Tank Royale.

    This class provides the basic structure and methods that all bots must implement.
    It inherits from BaseBot and BotABC, which define the core functionality and
    abstract methods that must be overridden by any specific bot implementation.
    """

    def __init__(
        self,
        bot_info: None | BotInfo = None,
        server_url: None | str = None,
        server_secret: None | str = None,
    ):
        super().__init__(bot_info, server_url, server_secret)
        self._bot_internals = BotInternals(
            bot=self, base_bot_internals=self._internals
        )

    def run(self) -> None:
        """Runs the bot program.

        Example:
            def run(self) -> None:
                while self.running:
                    self.forward(100)
                    self.turn_gun_left(360)
                    self.back(100)
                    self.turn_gun_right(360)

        Note:
            When running a loop that could potentially run forever, the best practice is to check if
            the bot is still running to stop and exit the loop. This gives the game a chance to stop
            the thread running the loop. If the thread is not stopped correctly, the bot may behave
            strangely in new rounds.

        See Also:
            running
        """
        pass

    def on_event(self, event: Condition) -> None:
        """Handle events that occur during the bot's execution."""
        pass

    @property
    def turn_rate(self) -> float:
        """Get the turn rate of the bot."""
        return self._bot_internals.turn_rate

    @turn_rate.setter
    def turn_rate(self, turn_rate: float) -> None:
        """Set the turn rate of the bot."""
        self._bot_internals.turn_rate = turn_rate

    @property
    def gun_turn_rate(self) -> float:
        return self._bot_internals.gun_turn_rate

    @gun_turn_rate.setter
    def gun_turn_rate(self, gun_turn_rate: float) -> None:
        self._bot_internals.gun_turn_rate = gun_turn_rate

    @property
    def radar_turn_rate(self) -> float:
        return self._bot_internals.radar_turn_rate

    @radar_turn_rate.setter
    def radar_turn_rate(self, radar_turn_rate: float) -> None:
        self._bot_internals.radar_turn_rate = radar_turn_rate

    @property
    def target_speed(self) -> float:
        ts = self._bot_internals.target_speed
        return 0.0 if ts is None else ts

    @target_speed.setter
    def target_speed(self, speed: float) -> None:
        # Ensure Java parity: setting target speed should clear overrideTargetSpeed and set distance_remaining to +/-inf
        self._bot_internals.target_speed = speed

    @property
    def running(self) -> bool:
        """Check if this bot is running.

        Returns:
            True when the bot is running, False otherwise.
        """
        return self._internals.is_running()

    def set_forward(self, distance: float) -> None:
        """Set the bot to move forward until it has traveled a specific distance from its current position.

        The speed is limited by max_speed. While moving forward, ACCELERATION determines the
        acceleration (+1 unit per turn), and DECELERATION determines braking (-2 units per turn).

        This method is executed when go() is called, making it possible to call other setter
        methods and run them in parallel in the same turn. If this method is called multiple times,
        the last call before go() is executed counts.

        This method cancels the effect of prior calls to target_speed, as set_forward and
        set_back call target_speed each turn until distance_remaining reaches 0.

        Args:
            distance: Distance to move forward. If negative, the bot moves backward. If positive
                or negative infinity, the bot moves infinitely in that direction.
        """
        self._bot_internals.set_forward(distance)

    def forward(self, distance: float) -> None:
        """Move the bot forward until it has traveled a specific distance from its current position.

        The speed is limited by max_speed. While moving forward, ACCELERATION determines the
        acceleration (+1 unit per turn), and DECELERATION determines braking (-2 units per turn).

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to target_speed, set_forward, and set_back.

        Args:
            distance: Distance to move forward. If negative, the bot moves backward. If positive
                or negative infinity, the bot moves infinitely in that direction.
        """
        self._bot_internals.forward(distance)

    def set_back(self, distance: float) -> None:
        """Set the bot to move backward until it has traveled a specific distance from its current position.

        The speed is limited by max_speed. While moving forward, ACCELERATION determines the
        acceleration (+1 unit per turn), and DECELERATION determines braking (-2 units per turn).

        This method is executed when go() is called, making it possible to call other setter
        methods and run them in parallel in the same turn. If this method is called multiple times,
        the last call before go() is executed counts.

        This method cancels the effect of prior calls to target_speed, as set_forward and
        set_back call target_speed each turn until distance_remaining reaches 0.

        Args:
            distance: Distance to move backward. If negative, the bot moves forward. If positive
                or negative infinity, the bot moves infinitely in that direction.
        """
        self.set_forward(-distance)

    def back(self, distance: float) -> None:
        """Move the bot backward until it has traveled a specific distance from its current position.

        The speed is limited by max_speed. While moving forward, ACCELERATION determines the
        acceleration (+1 unit per turn), and DECELERATION determines braking (-2 units per turn).

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to target_speed, set_forward, and set_back.

        Args:
            distance: Distance to move backward. If negative, the bot moves forward. If positive
                or negative infinity, the bot moves infinitely in that direction.
        """
        self.forward(-distance)

    @property
    def distance_remaining(self) -> float:
        """Return the remaining distance before the current movement is completed.

        When the remaining distance is positive, the bot is moving forward. When negative, the
        bot is moving backward. Positive/negative infinity means infinite movement in that
        direction.
        """
        return self._bot_internals.distance_remaining

    def set_turn_left(self, degrees: float) -> None:
        """Set the bot to turn left (following the increasing degrees of the unit circle).

        The bot turns until turn_remaining is 0. The amount of degrees to turn each turn is limited
        by max_turn_rate.

        This method is executed when go() is called, making it possible to call other setter
        methods and run them in parallel in the same turn. If this method is called multiple times,
        the last call before go() is executed counts.

        This method cancels the effect of prior calls to set_turn_right.

        Args:
            degrees: Degrees to turn left. If negative, the bot turns right. If positive or negative
                infinity, the bot turns infinitely in that direction.
        """
        self._bot_internals.set_turn_left(degrees)

    def turn_left(self, degrees: float) -> None:
        """Turn the bot left (following the increasing degrees of the unit circle).

        The bot turns until turn_remaining is 0. The amount of degrees to turn each turn is limited
        by max_turn_rate.

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to set_turn_left and set_turn_right.

        Args:
            degrees: Degrees to turn left. If negative, the bot turns right. If positive or negative
                infinity, the bot turns infinitely in that direction.
        """
        self._bot_internals.turn_left(degrees)

    def set_turn_right(self, degrees: float) -> None:
        """Set the bot to turn right (following the decreasing degrees of the unit circle).

        The bot turns until turn_remaining is 0. The amount of degrees to turn each turn is limited
        by max_turn_rate.

        This method is executed when go() is called, making it possible to call other setter
        methods and run them in parallel in the same turn. If this method is called multiple times,
        the last call before go() is executed counts.

        This method cancels the effect of prior calls to set_turn_left.

        Args:
            degrees: Degrees to turn right. If negative, the bot turns left. If positive or negative
                infinity, the bot turns infinitely in that direction.
        """
        self.set_turn_left(-degrees)

    def turn_right(self, degrees: float) -> None:
        """Turn the bot right (following the increasing degrees of the unit circle).

        The bot turns until turn_remaining is 0. The amount of degrees to turn each turn is limited
        by max_turn_rate.

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to set_turn_left and set_turn_right.

        Args:
            degrees: Degrees to turn right. If negative, the bot turns left. If positive or negative
                infinity, the bot turns infinitely in that direction.
        """
        self.turn_left(-degrees)

    @property
    def turn_remaining(self) -> float:
        """Return the remaining degrees before the current turn is completed.

        Positive means turning left along the unit circle; negative means turning right. Positive
        or negative infinity means infinite turning in that direction.
        """
        return self._bot_internals.turn_remaining

    def set_turn_gun_left(self, degrees: float) -> None:
        """Set the gun to turn left (following the increasing degrees of the unit circle).

        The gun turns until gun_turn_remaining is 0. The amount of degrees to turn each turn is
        limited by max_gun_turn_rate.

        This method is executed when go() is called, making it possible to call other setter
        methods and run them in parallel in the same turn. If this method is called multiple times,
        the last call before go() is executed counts.

        This method cancels the effect of prior calls to set_turn_gun_right.

        Args:
            degrees: Degrees to turn left. If negative, the gun turns right. If positive or negative
                infinity, the gun turns infinitely in that direction.
        """
        self._bot_internals.set_turn_gun_left(degrees)

    def turn_gun_left(self, degrees: float) -> None:
        """Turn the gun left (following the increasing degrees of the unit circle).

        The gun turns until gun_turn_remaining is 0. The amount of degrees to turn each turn is
        limited by max_gun_turn_rate.

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to set_turn_gun_left and set_turn_gun_right.

        Args:
            degrees: Degrees to turn left. If negative, the gun turns right. If positive or negative
                infinity, the gun turns infinitely in that direction.
        """
        self._bot_internals.turn_gun_left(degrees)

    def set_turn_gun_right(self, degrees: float) -> None:
        """Set the gun to turn right (following the decreasing degrees of the unit circle).

        The gun turns until gun_turn_remaining is 0. The amount of degrees to turn each turn is
        limited by max_gun_turn_rate.

        This method is executed when go() is called, making it possible to call other setter
        methods and run them in parallel in the same turn. If this method is called multiple times,
        the last call before go() is executed counts.

        This method cancels the effect of prior calls to set_turn_gun_left.

        Args:
            degrees: Degrees to turn right. If negative, the gun turns left. If positive or negative
                infinity, the gun turns infinitely in that direction.
        """
        self.set_turn_gun_left(-degrees)

    def turn_gun_right(self, degrees: float) -> None:
        """Turn the gun right (following the decreasing degrees of the unit circle).

        The gun turns until gun_turn_remaining is 0. The amount of degrees to turn each turn is
        limited by max_gun_turn_rate.

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to set_turn_gun_left and set_turn_gun_right.

        Args:
            degrees: Degrees to turn right. If negative, the gun turns left. If positive or negative
                infinity, the gun turns infinitely in that direction.
        """
        self.turn_gun_left(-degrees)

    @property
    def gun_turn_remaining(self) -> float:
        """Return the remaining degrees before the current gun turn is completed.

        Positive means turning left along the unit circle; negative means turning right. Positive
        or negative infinity means infinite turning in that direction.
        """
        return self._bot_internals.gun_turn_remaining

    def set_turn_radar_left(self, degrees: float) -> None:
        """Set the radar to turn left (following the increasing degrees of the unit circle).

        The radar turns until radar_turn_remaining is 0. The amount of degrees to turn each turn is
        limited by max_radar_turn_rate.

        This method is executed when go() is called, making it possible to call other setter
        methods and run them in parallel in the same turn. If this method is called multiple times,
        the last call before go() is executed counts.

        This method cancels the effect of prior calls to set_turn_radar_right.

        Args:
            degrees: Degrees to turn left. If negative, the radar turns right. If positive or negative
                infinity, the radar turns infinitely in that direction.
        """
        self._bot_internals.set_turn_radar_left(degrees)

    def turn_radar_left(self, degrees: float) -> None:
        """Turn the radar left (following the increasing degrees of the unit circle).

        The radar turns until radar_turn_remaining is 0. The amount of degrees to turn each turn is
        limited by max_radar_turn_rate.

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to set_turn_radar_left and set_turn_radar_right.

        Args:
            degrees: Degrees to turn left. If negative, the radar turns right. If positive or negative
                infinity, the radar turns infinitely in that direction.
        """
        self._bot_internals.turn_radar_left(degrees)

    def set_turn_radar_right(self, degrees: float) -> None:
        """Set the radar to turn right (following the decreasing degrees of the unit circle).

        The radar turns until radar_turn_remaining is 0. The amount of degrees to turn each turn is
        limited by max_radar_turn_rate.

        This method is executed when go() is called, making it possible to call other setter
        methods and run them in parallel in the same turn. If this method is called multiple times,
        the last call before go() is executed counts.

        This method cancels the effect of prior calls to set_turn_radar_left.

        Args:
            degrees: Degrees to turn right. If negative, the radar turns left. If positive or negative
                infinity, the radar turns infinitely in that direction.
        """
        self.set_turn_radar_left(-degrees)

    def turn_radar_right(self, degrees: float) -> None:
        """Turn the radar right (following the increasing degrees of the unit circle).

        The radar turns until radar_turn_remaining is 0. The amount of degrees to turn each turn is
        limited by max_radar_turn_rate.

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to set_turn_radar_left and set_turn_radar_right.

        Args:
            degrees: Degrees to turn right. If negative, the radar turns left. If positive or negative
                infinity, the radar turns infinitely in that direction.
        """
        self.turn_radar_left(-degrees)

    @property
    def radar_turn_remaining(self) -> float:
        """Return the remaining degrees before the current radar turn is completed.

        Positive means turning left along the unit circle; negative means turning right. Positive
        or negative infinity means infinite turning in that direction.
        """
        return self._bot_internals.radar_turn_remaining

    def fire(self, firepower: float) -> None:
        """Fire the gun in the direction the gun is pointing.

        Firing spends energy from the bot. The energy loss equals firepower, and hitting another bot
        rewards energy equal to 3x firepower. The gun can fire only when firepower is at least
        MIN_FIREPOWER. If firepower exceeds MAX_FIREPOWER, it is truncated to MAX_FIREPOWER.

        The gun heats when firing and must cool down before it can fire again. Gun heat generated by
        firing is 1 + (firepower / 5). The gun cooling rate is read from gun_cooling_rate.

        Damage dealt is 4x firepower, and if firepower > 1, it adds 2 * (firepower - 1).

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes. To run
        multiple commands in parallel, use setter methods instead of this blocking method.

        This method cancels the effect of prior calls to set_fire.

        Args:
            firepower: Energy to spend on firing. Must be at least MIN_FIREPOWER.
        """
        self._bot_internals.fire(firepower)

    def stop(self, overwrite: bool = False) -> None:
        """Stop all movement including turning the gun and radar.

        Remaining movement is saved for a call to set_resume or resume. This call executes
        immediately by calling go() internally and blocks until completed, which can take one to
        several turns. New commands take place after this method completes.

        Args:
            overwrite: True to override a previously saved movement from stop or set_stop. If False,
                this method is identical to set_stop.
        """
        self._bot_internals.stop(overwrite)

    def resume(self) -> None:
        """Resume the movement prior to calling set_stop or stop.

        This call executes immediately by calling go() internally and blocks until completed, which
        can take one to several turns. New commands take place after this method completes.
        """
        self._bot_internals.resume()

    def rescan(self) -> None:
        """Scan again with the radar.

        This is useful when the radar is not turning and cannot automatically scan bots, e.g. after
        stop has been called. The last radar direction and sweep angle are used for rescanning.
        """
        self._bot_internals.rescan()

    def wait_for(self, condition: Callable[[], bool]) -> None:
        """Block until a condition is met (condition returns True).

        Args:
            condition: Callable that returns True when the condition is met.
        """
        self._bot_internals.wait_for(condition)
