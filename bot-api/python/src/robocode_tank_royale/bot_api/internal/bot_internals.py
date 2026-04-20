from typing import Callable, TYPE_CHECKING
import math

if TYPE_CHECKING:
    from ..bot import Bot

from .base_bot_internals import BaseBotInternals
from .stop_resume_listener_abs import StopResumeListenerABC
from .event_interruption import EventInterruption
from .thread_interrupted_exception import ThreadInterruptedException
from ..events import TickEvent, HitBotEvent, ScannedBotEvent
from ..constants import MAX_SPEED


class BotInternals(StopResumeListenerABC):
    def __init__(self, bot: "Bot", base_bot_internals: BaseBotInternals):
        self._base_bot_internals = base_bot_internals
        self._bot = bot
        self._base_bot_internals.stop_resume_listener = self

        self._clear_remaining()

        # Internal handlers are used for maintaining internal state only
        handlers = self._base_bot_internals.internal_event_handlers

        def _stop_thread(_: ...) -> None:
            self._base_bot_internals.stop_thread()

        # Priority 110 ensures _process_turn() runs BEFORE BaseBotInternals._on_next_turn (priority 100)
        # which calls notify_all() to wake up the bot thread. This prevents a race condition
        # where the bot thread wakes up before turn_remaining/distance_remaining are updated.
        handlers.on_next_turn.subscribe(self.on_next_turn, 110)

        # Priority 90 ensures BaseBotInternals.on_round_started (priority 100) resets state first,
        # then we pre-warm the bot thread so it is alive and waiting before turn 1 arrives.
        handlers.on_round_started.subscribe(lambda _: self._on_round_started_prewarm(), 90)

        handlers.on_game_aborted.subscribe(_stop_thread, 100)
        handlers.on_round_ended.subscribe(_stop_thread, 90)
        handlers.on_game_ended.subscribe(_stop_thread, 90)
        handlers.on_disconnected.subscribe(_stop_thread, 90)
        handlers.on_death.subscribe(_stop_thread, 90)
        handlers.on_hit_wall.subscribe(lambda _: self.on_hit_wall(), 90)
        handlers.on_hit_bot.subscribe(self.on_hit_bot, 90)

        # Did we go over desired distance to travel
        self._is_over_driving = False

        self._override_target_speed = False
        self._override_turn_rate = False
        self._override_gun_turn_rate = False
        self._override_radar_turn_rate = False

    def on_next_turn(self, e: TickEvent) -> None:
        """Handle the next turn event."""
        if e.turn_number == 1:
            self._on_first_turn()
        self._process_turn()

    def _on_round_started_prewarm(self) -> None:
        """Pre-warm bot thread at RoundStarted (P90) so thread is alive before turn 1 arrives."""
        self._base_bot_internals.stop_thread()
        self._clear_remaining()
        self._base_bot_internals.start_thread(self._bot)

    def _on_first_turn(self) -> None:
        """Handle the first turn — capture initial directions for delta tracking."""
        self._clear_remaining()

    def _clear_remaining(self) -> None:
        """Clear the remaining movement and turn values."""
        self.distance_remaining: float = 0.0
        self.turn_remaining: float = 0.0
        self.gun_turn_remaining: float = 0.0
        self.radar_turn_remaining: float = 0.0
        self._continuous_turn_rate: float = 0.0
        self._continuous_gun_turn_rate: float = 0.0
        self._continuous_radar_turn_rate: float = 0.0
        self._continuous_target_speed: float = 0.0
        try:
            self._previous_direction: float = self._bot.direction
            self._previous_gun_direction: float = self._bot.gun_direction
            self._previous_radar_direction: float = self._bot.radar_direction
        except Exception:
            self._previous_direction = 0.0
            self._previous_gun_direction = 0.0
            self._previous_radar_direction = 0.0

    def _process_turn(self) -> None:
        """Process the bot's turn, updating movement and turn values."""
        if self._bot.disabled:
            self._clear_remaining()
            return
        self._update_turn_remaining()
        self._update_gun_turn_remaining()
        self._update_radar_turn_remaining()
        self._update_movement()

    def on_hit_wall(self):
        self.distance_remaining = 0.0

    def on_hit_bot(self, e: HitBotEvent) -> None:
        if e.rammed:
            self.distance_remaining = 0.0

    @property
    def turn_rate(self) -> float:
        return self._base_bot_internals.turn_rate

    @turn_rate.setter
    def turn_rate(self, x: float) -> None:
        self._override_turn_rate = False
        self._continuous_turn_rate = x
        # Match Java semantics: remaining is based on the new requested turn rate
        self.turn_remaining = self._to_infinite_value(x)
        self._base_bot_internals.turn_rate = x

    @property
    def gun_turn_rate(self) -> float:
        return self._base_bot_internals.gun_turn_rate

    @gun_turn_rate.setter
    def gun_turn_rate(self, x: float) -> None:
        self._override_gun_turn_rate = False
        self._continuous_gun_turn_rate = x
        # Match Java semantics: remaining is based on the new requested gun turn rate
        self.gun_turn_remaining = self._to_infinite_value(x)
        self._base_bot_internals.gun_turn_rate = x

    @property
    def radar_turn_rate(self) -> float:
        return self._base_bot_internals.radar_turn_rate

    @radar_turn_rate.setter
    def radar_turn_rate(self, x: float) -> None:
        self._override_radar_turn_rate = False
        self._continuous_radar_turn_rate = x
        # Match Java semantics: remaining is based on the new requested radar turn rate
        self.radar_turn_remaining = self._to_infinite_value(x)
        self._base_bot_internals.radar_turn_rate = x

    def _to_infinite_value(self, x: float) -> float:
        """Convert a turn rate to an infinite value for remaining turns."""
        if x > 0:
            return float("inf")
        elif x < 0:
            return float("-inf")
        return 0.0

    @property
    def target_speed(self) -> float | None:
        return self._base_bot_internals.target_speed

    @target_speed.setter
    def target_speed(self, speed: float) -> None:
        self._override_target_speed = False
        self._continuous_target_speed = speed
        self.distance_remaining = self._to_infinite_value(speed)
        self._base_bot_internals.target_speed = speed

    def set_forward(self, distance: float) -> None:
        self._override_target_speed = True
        if math.isnan(distance):
            raise ValueError("'distance' cannot be NaN")
        self._get_and_set_new_target_speed(distance)
        self.distance_remaining = distance

    def forward(self, distance: float) -> None:
        if self._bot.stopped:
            self._bot.go()
        else:
            self.set_forward(distance)
            self.wait_for(
                lambda: self.distance_remaining == 0.0 and self._bot.speed == 0.0
            )

    def set_turn_left(self, degrees: float) -> None:
        self._override_turn_rate = True
        self.turn_remaining = degrees
        self._base_bot_internals.turn_rate = degrees

    def turn_left(self, degrees: float) -> None:
        if self._bot.stopped:
            self._bot.go()
        else:
            self.set_turn_left(degrees)
            self.wait_for(lambda: self.turn_remaining == 0)

    def set_turn_gun_left(self, degrees: float) -> None:
        self._override_gun_turn_rate = True
        self.gun_turn_remaining = degrees
        self._base_bot_internals.gun_turn_rate = degrees

    def turn_gun_left(self, degrees: float) -> None:
        if self._bot.stopped:
            self._bot.go()
        else:
            self.set_turn_gun_left(degrees)
            self.wait_for(lambda: self.gun_turn_remaining == 0)

    def set_turn_radar_left(self, degrees: float) -> None:
        self._override_radar_turn_rate = True
        self.radar_turn_remaining = degrees
        self._base_bot_internals.radar_turn_rate = degrees

    def turn_radar_left(self, degrees: float) -> None:
        if self._bot.stopped:
            self._bot.go()
        else:
            self.set_turn_radar_left(degrees)
            self.wait_for(lambda: self.radar_turn_remaining == 0)

    def fire(self, firepower: float) -> None:
        self._bot.set_fire(firepower)
        self._bot.go()

    def rescan(self) -> None:
        # Mark ScannedBotEvent as interruptible so a new scan can interrupt the current handler
        EventInterruption.set_interruptible(ScannedBotEvent, True)
        self._bot.set_rescan()
        # Align with Java: do not raise interruption here; event dispatch handles and swallows it
        self._bot.go()

    def wait_for(self, condition: Callable[[], bool]) -> None:
        self._bot.go()
        while self._base_bot_internals.is_running() and not condition():
            self._bot.go()

    def stop(self, overwrite: bool) -> None:
        self._bot.set_stop(overwrite)
        self._bot.go()

    def resume(self) -> None:
        self._base_bot_internals.set_resume()
        self._bot.go()

    def on_stop(self) -> None:
        self._saved_previous_direction = self._previous_direction
        self._saved_previous_gun_direction = self._previous_gun_direction
        self._saved_previous_radar_direction = self._previous_radar_direction

        self._saved_distance_remaining = self.distance_remaining
        self._saved_turn_remaining = self.turn_remaining
        self._saved_gun_turn_remaining = self.gun_turn_remaining
        self._saved_radar_turn_remaining = self.radar_turn_remaining

    def on_resume(self):
        self._previous_direction = self._saved_previous_direction
        self._previous_gun_direction = self._saved_previous_gun_direction
        self._previous_radar_direction = self._saved_previous_radar_direction

        self.distance_remaining = self._saved_distance_remaining
        self.turn_remaining = self._saved_turn_remaining
        self.gun_turn_remaining = self._saved_gun_turn_remaining
        self.radar_turn_remaining = self._saved_radar_turn_remaining

    def _update_turn_remaining(self) -> None:
        """Update the turn remaining value based on the bot's current direction."""
        delta = self._bot.calc_delta_angle(
            self._bot.direction, self._previous_direction
        )
        self._previous_direction = self._bot.direction

        if not self._override_turn_rate:
            self._base_bot_internals.turn_rate = self._continuous_turn_rate
            return

        if abs(self.turn_remaining) <= abs(delta):
            self.turn_remaining = 0
        else:
            self.turn_remaining -= delta
            if self._is_near_zero(self.turn_remaining):
                self.turn_remaining = 0

        self._base_bot_internals.turn_rate = self.turn_remaining

    def _update_gun_turn_remaining(self) -> None:
        """Update the gun turn remaining value based on the bot's current gun direction."""
        delta = self._bot.calc_delta_angle(
            self._bot.gun_direction, self._previous_gun_direction
        )
        self._previous_gun_direction = self._bot.gun_direction

        if not self._override_gun_turn_rate:
            self._base_bot_internals.gun_turn_rate = self._continuous_gun_turn_rate
            return

        if abs(self.gun_turn_remaining) <= abs(delta):
            self.gun_turn_remaining = 0
        else:
            self.gun_turn_remaining -= delta
            if self._is_near_zero(self.gun_turn_remaining):
                self.gun_turn_remaining = 0

        self._base_bot_internals.gun_turn_rate = self.gun_turn_remaining

    def _update_radar_turn_remaining(self) -> None:
        """Update the radar turn remaining value based on the bot's current radar direction."""
        delta = self._bot.calc_delta_angle(
            self._bot.radar_direction, self._previous_radar_direction
        )
        self._previous_radar_direction = self._bot.radar_direction

        if not self._override_radar_turn_rate:
            self._base_bot_internals.radar_turn_rate = self._continuous_radar_turn_rate
            return

        if abs(self.radar_turn_remaining) <= abs(delta):
            self.radar_turn_remaining = 0
        else:
            self.radar_turn_remaining -= delta
            if self._is_near_zero(self.radar_turn_remaining):
                self.radar_turn_remaining = 0

        self._base_bot_internals.radar_turn_rate = self.radar_turn_remaining

    def _update_movement(self):
        if not self._override_target_speed:
            self._base_bot_internals.target_speed = self._continuous_target_speed
            if abs(self.distance_remaining) < abs(self._bot.speed):
                self.distance_remaining = 0
            else:
                self.distance_remaining -= self._bot.speed
        elif math.isinf(self.distance_remaining):
            self._base_bot_internals.target_speed = (
                MAX_SPEED if self.distance_remaining > 0 else -MAX_SPEED
            )
        else:
            distance = self.distance_remaining
            # This is Nat Pavasant's method described here:
            # https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
            new_speed = self._get_and_set_new_target_speed(distance)
            # If we are over-driving our distance, and we are now at speed=0, then we stopped
            if self._is_near_zero(new_speed) and self._is_over_driving:
                distance = 0
                self._is_over_driving = False
            # the overdrive flag
            if distance * new_speed >= 0:
                self._is_over_driving = (
                    self._base_bot_internals.get_distance_traveled_until_stop(new_speed)
                    > abs(distance)
                )

            self.distance_remaining = distance - new_speed

    def _get_and_set_new_target_speed(self, distance: float) -> float:
        """Get and set the new target speed based on the remaining distance."""
        speed = self._base_bot_internals.get_new_target_speed(
            self._bot.speed, distance
        )
        self._base_bot_internals.target_speed = speed
        return speed

    def _is_near_zero(self, x: float) -> bool:
        return abs(x) < 1e-5
