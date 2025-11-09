import asyncio
import urllib.parse
import traceback
import time
import math
import os
from typing import Any, Optional, Set, Sequence

from ..base_bot_abc import BaseBotABC
from ..bot_abc import BotABC
from ..bot_exception import BotException
from ..bot_info import BotInfo
from ..bullet_state import BulletState
from ..events import TickEvent, BulletFiredEvent, RoundStartedEvent, BotEvent
from ..events.condition import Condition
from ..game_setup import GameSetup
from ..initial_position import InitialPosition
from ..util.math_util import MathUtil
from ..graphics import Color, GraphicsABC

from .base_bot_internal_data import BaseBotInternalData
from .bot_event_handlers import BotEventHandlers
from .event_queue import EventQueue
from .env_vars import EnvVars
from .internal_event_handlers import InternalEventHandlers
from .json_util import to_json
from .stop_resume_listener_abs import StopResumeListenerABC
from .thread_interrupted_exception import ThreadInterruptedException
from .websocket_handler import WebSocketHandler

from ..constants import (
    MAX_SPEED,
    MAX_TURN_RATE,
    MAX_GUN_TURN_RATE,
    MAX_RADAR_TURN_RATE,
    DECELERATION,
    ACCELERATION,
    MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN,
    TEAM_MESSAGE_MAX_SIZE,
)

from robocode_tank_royale.schema import BotIntent, ServerHandshake, TeamMessage

DEFAULT_SERVER_URL = "ws://localhost:7654"

GAME_NOT_RUNNING_MSG = (
    "Game is not running. Make sure onGameStarted() event handler has been called first"
)
TICK_NOT_AVAILABLE_MSG = "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first"
NOT_CONNECTED_TO_SERVER_MSG = "Not connected to a game server. Make sure onConnected() event handler has been called first"


# TODO: This class does not work yet - esp., figure out the asyncio integration and how to handle the bot's main loop.


class BaseBotInternals:
    def __init__(
        self,
        base_bot: BaseBotABC,
        bot_info: Optional[BotInfo],
        server_url: Optional[str],
        server_secret: Optional[str],
    ):
        self.base_bot = base_bot
        if bot_info is None:
            self.bot_info = EnvVars.get_bot_info()
        else:
            self.bot_info = bot_info

        self.server_url = (
            server_url
            if server_url is not None
            else self._get_server_url_from_setting()
        )
        self.server_secret = (
            server_secret
            if server_secret is not None
            else self._get_server_secret_from_setting()
        )

        self.data: BaseBotInternalData = BaseBotInternalData(self.bot_info)

        self.bot_event_handlers: BotEventHandlers = BotEventHandlers(base_bot)
        self.internal_event_handlers: InternalEventHandlers = InternalEventHandlers()
        self.event_queue: EventQueue = EventQueue(self.data, self.bot_event_handlers)

        self.closed_event: asyncio.Event = asyncio.Event()
        self.socket: Optional[Any] = None  # To store the WebSocket connection
        self.web_socket_handler: Optional[WebSocketHandler] = None

        self.next_turn_monitor: asyncio.Condition = asyncio.Condition()

        self.thread: Optional[asyncio.Task[Any]] = None  # For asyncio task
        self.stop_resume_listener: Optional[StopResumeListenerABC] = None

        self.max_speed: float = MAX_SPEED
        self.max_turn_rate: float = MAX_TURN_RATE
        self.max_gun_turn_rate: float = MAX_GUN_TURN_RATE
        self.max_radar_turn_rate: float = MAX_RADAR_TURN_RATE

        self.abs_deceleration: float = abs(DECELERATION)
        self.last_execute_turn_number: int = -1

        # Movement reset deferral flag (mirrors Java/.NET movementResetPending)
        self._movement_reset_pending: bool = False

        self._init()

    def _get_server_url_from_setting(self) -> str:
        url = os.getenv("SERVER_URL", os.getenv("SERVER_URL"))
        if url is None:
            url = DEFAULT_SERVER_URL
        return url

    def _get_server_secret_from_setting(self) -> Optional[str]:
        return os.getenv("SERVER_SECRET", os.getenv("SERVER_SECRET"))

    def _init(self) -> None:
        # Skipping redirectStdOutAndStdErr for now
        self._subscribe_to_events()

    def _subscribe_to_events(self) -> None:
        self.internal_event_handlers.on_round_started.subscribe(
            self._on_round_started, 100
        )
        self.internal_event_handlers.on_next_turn.subscribe(self._on_next_turn, 100)
        self.internal_event_handlers.on_bullet_fired.subscribe(
            self._on_bullet_fired, 100
        )

    async def _on_round_started(self, event: RoundStartedEvent) -> None:
        # Defer movement reset until after first intent has been sent, mirroring Java/.NET behavior
        if not hasattr(self, "_movement_reset_pending"):
            self._movement_reset_pending = False
        self._movement_reset_pending = True
        self.event_queue.clear()  # Clears conditions in self.data.conditions
        self.data.is_stopped = False
        self.data.event_handling_disabled_turn = 0
        self.last_execute_turn_number = -1

    async def _on_next_turn(self, event: TickEvent) -> None:
        async with self.next_turn_monitor:
            self.next_turn_monitor.notify_all()
        # Only reset movement after first intent following round start
        if self._movement_reset_pending:
            self._reset_movement()
            self._movement_reset_pending = False

    async def _on_bullet_fired(self, event: BulletFiredEvent) -> None:
        if self.data.bot_intent:
            self.data.bot_intent.firepower = 0.0
        pass

    def _reset_movement(self) -> None:
        if self.data.bot_intent:
            self.data.bot_intent.turn_rate = None
            self.data.bot_intent.gun_turn_rate = None
            self.data.bot_intent.radar_turn_rate = None
            self.data.bot_intent.target_speed = None
            self.data.bot_intent.firepower = None

    @property
    def my_id(self) -> int:
        return self.data.my_id

    @my_id.setter
    def my_id(self, my_id: int) -> None:
        self.data.my_id = my_id

    @property
    def teammate_ids(self) -> Set[int]:
        return self.data.teammate_ids

    @teammate_ids.setter
    def teammate_ids(self, teammate_ids: Set[int]) -> None:
        self.data.teammate_ids = teammate_ids

    @property
    def game_setup(self) -> GameSetup:
        return self.data.game_setup

    @game_setup.setter
    def game_setup(
        self, game_setup: GameSetup
    ) -> None:  # Typically called by WebSocketHandler
        self.data.game_setup = game_setup

    @property
    def initial_position(self) -> Optional[InitialPosition]:
        return self.data.initial_position

    @initial_position.setter
    def initial_position(
        self, initial_position: Optional[InitialPosition]
    ) -> None:  # Typically called by WebSocketHandler
        self.data.initial_position = initial_position

    def get_bot_intent(self) -> BotIntent:
        return self.data.bot_intent

    def get_current_tick_or_throw(self) -> TickEvent:
        return self.data.current_tick_or_throw

    def set_tick_event(
        self, tick_event: TickEvent
    ) -> None:  # Typically called by WebSocketHandler
        self.data.tick_event = tick_event

    def get_current_tick_or_null(self) -> Optional[TickEvent]:
        return self.data.current_tick_or_null

    def get_tick_start_nano_time(self) -> int:
        return self.data.tick_start_nano_time

    def set_tick_start_nano_time(
        self, tick_start_nano_time: int
    ) -> None:  # Typically called by WebSocketHandler
        self.data.tick_start_nano_time = tick_start_nano_time

    def get_time_left(self) -> int:
        passed_microseconds = (
            time.monotonic_ns() - self.data.tick_start_nano_time
        ) // 1000
        game_setup = self.data.game_setup
        return game_setup.turn_timeout - passed_microseconds

    def enable_event_handling(self, enable: bool) -> None:
        if enable:
            self.data.event_handling_disabled_turn = 0
        else:
            # Ensure tick event is available before accessing turn_number
            current_tick = self.data.current_tick_or_null
            if current_tick:
                self.data.event_handling_disabled_turn = current_tick.turn_number
            else:
                # If called before any tick, disabling implies from turn 0.
                # Or consider raising an error if disabling without a current turn context is invalid.
                self.data.event_handling_disabled_turn = (
                    0  # Or some other default like -1 or 1
                )

    def get_event_handling_disabled_turn(self) -> bool:
        # Important! Allow an additional turn so events like RoundStarted can be handled
        current_tick = self.data.current_tick_or_null
        if not current_tick:
            return (
                self.data.event_handling_disabled_turn != 0
            )  # If no tick, rely purely on the flag

        return (
            self.data.event_handling_disabled_turn != 0
            and self.data.event_handling_disabled_turn < (current_tick.turn_number - 1)
        )

    def get_events(self) -> Sequence[BotEvent]:
        turn_number = self.data.current_tick_or_throw.turn_number
        return self.event_queue.get_events(turn_number)

    def clear_events(self) -> None:
        self.event_queue.clear_events()

    # Public wrapper to enqueue events from a tick, aligning with Java BaseBotInternals
    def add_events_from_tick(self, event: TickEvent) -> None:
        self.event_queue.add_events_from_tick(event)

    def set_interruptible(self, interruptible: bool) -> None:
        self.event_queue.set_current_event_interruptible(interruptible)

    async def dispatch_events(self, turn_number: int) -> None:
        try:
            await self.event_queue.dispatch_events(turn_number)
        except Exception:
            # Align with Java: do not propagate interruptions from event handling
            traceback.print_exc()

    def set_running(self, is_running: bool) -> None:
        self.data.is_running = is_running

    def is_running(self) -> bool:
        return self.data.is_running

    async def _runnable(self, bot: BotABC) -> None:
        self.data.is_running = True
        self.enable_event_handling(True)
        try:
            await bot.run()

            # Skip every turn after the run method has exited
            while self.data.is_running:
                try:
                    await bot.go()
                except ThreadInterruptedException:
                    return  # Thread was interrupted deliberately - exit silently
                except asyncio.CancelledError:
                    return  # Task was cancelled
                except ThreadInterruptedException:
                    return  # Rogue thread detected, stop execution
                except Exception as e:  # Catch other exceptions during bot.go()
                    # Potentially log the error or handle specific bot exceptions
                    print(f"Exception in bot.go(): {e}")  # Basic error logging
                    # Depending on desired behavior, may need to stop or continue
                    self.data.is_running = False  # Example: stop on error
                    return
        except ThreadInterruptedException:
            return  # Thread was interrupted deliberately - exit silently
        except asyncio.CancelledError:
            # This handles cancellation if bot.run() or the loop itself is cancelled
            pass  # Task was cancelled
        except ThreadInterruptedException:
            # Rogue thread detected during bot.run()
            pass
        except Exception as e:
            # Potentially log the error or handle specific bot exceptions
            print(f"Exception in bot.run() or main loop: {e}")  # Basic error logging
            self.data.is_running = False  # Stop if unhandled exception in run()
        finally:
            self.enable_event_handling(False)

    def start_thread(self, bot: BotABC) -> None:
        if self.thread is not None:
            # Potentially handle if a thread/task is already running
            pass
        self.thread = asyncio.create_task(self._runnable(bot))

    def stop_thread(self) -> None:
        if not self.data.is_running:
            return
        self.data.is_running = False
        if self.thread is not None:
            self.thread.cancel()
            # It's good practice to await the task to ensure it finishes,
            # but this might block if the task doesn't handle cancellation promptly.
            # Consider if `await self.thread` is needed here or if fire-and-forget cancel is okay.
            self.thread = None

    def _sanitize_url(self, uri: str) -> None:
        parsed_url = urllib.parse.urlparse(uri)
        if parsed_url.scheme not in ("ws", "wss"):
            raise BotException(f"Wrong scheme used with server URL: {uri}")

    async def _connect(self) -> None:
        self._sanitize_url(self.server_url)
        try:
            self.web_socket_handler = WebSocketHandler(  # Store the handler instance
                self.data,  # Pass BaseBotInternalData instance
                self.server_url,
                self.server_secret,
                self.base_bot,  # The bot instance itself (BaseBotABC)
                self.bot_info,  # BotInfo needed by WebSocketHandler
                self.bot_event_handlers,  # Event handlers needed by WebSocketHandler
                self.internal_event_handlers,  # Event handlers needed by WebSocketHandler
                self.closed_event,  # Async event needed by WebSocketHandler
                self.event_queue,  # Provide access to the shared EventQueue for staging events
            )
            self.socket = await self.web_socket_handler.connect()

        except Exception as ex:
            raise BotException(
                f"Could not create web socket for URL: {self.server_url}", ex
            ) from ex

    async def start(self) -> None:
        await self._connect()
        if self.web_socket_handler:  # Check if handler was created
            asyncio.create_task(self.web_socket_handler.receive_messages())
        await self.closed_event.wait()

    async def execute(self) -> None:
        current_tick = self.data.current_tick_or_null
        # If no tick has been received yet, send current intent once to allow the server to progress
        if current_tick is None:
            await self._send_intent()
            return

        turn_number = current_tick.turn_number
        if turn_number != self.last_execute_turn_number:
            self.last_execute_turn_number = turn_number
            # Events are dispatched from BaseBot.go(); staging happens on tick reception
            await self._send_intent()
        await self._wait_for_next_turn(turn_number)

    async def _send_intent(self) -> None:
        self._render_graphics_to_bot_intent()  # Operates on self.data.graphics_state and self.data.bot_intent
        self._transfer_std_out_to_bot_intent()  # Placeholder

        if self.socket:
            try:
                json_intent = to_json(self.data.bot_intent)
                await self.socket.send(json_intent)
                if self.data.bot_intent.team_messages:
                    self.data.bot_intent.team_messages = []  # Clear after sending
            except Exception as e:
                print(f"Error sending bot intent: {e}")
                # Consider if self.socket needs to be invalidated or reconnected
                # For now, just printing error.

    def _transfer_std_out_to_bot_intent(self) -> None:
        # Stdout/stderr redirection and inclusion in intent might be handled differently in Python
        # For now, this is a placeholder.
        pass

    def _render_graphics_to_bot_intent(self) -> None:
        current_tick = self.data.current_tick_or_null
        # Check if debugging is enabled for the bot in the current tick
        if (
            current_tick
            and hasattr(current_tick, "bot_state")
            and current_tick.bot_state
            and hasattr(current_tick.bot_state, "is_debugging_enabled")
            and current_tick.bot_state.is_debugging_enabled
        ):
            svg_output = self.data.graphics_state.to_svg()
            self.data.bot_intent.debug_graphics = svg_output
            self.data.graphics_state.clear()
        else:
            # Ensure it's not set if debugging is off or tick not available
            self.data.bot_intent.debug_graphics = None

    async def _wait_for_next_turn(self, turn_number: int) -> None:
        self._stop_rogue_thread()
        try:
            async with self.next_turn_monitor:
                while (
                    self.data.is_running
                    and turn_number == self.data.current_tick_or_throw.turn_number
                    and self.thread
                    == asyncio.current_task()  # Ensure this is the bot's main task\
                    and not asyncio.current_task().cancelled()  # type: ignore
                ):
                    await self.next_turn_monitor.wait()
        except asyncio.CancelledError:
            # We get a CancelledError if the server stops the game.
            return None

    def _stop_rogue_thread(self) -> None:
        # In asyncio, tasks don't have a direct 'thread' equivalent accessible this way for comparison.
        # self.thread is the asyncio.Task object for the bot's main execution.
        # We check if the current task is the one we stored.
        if asyncio.current_task() != self.thread:
            raise ThreadInterruptedException()

    def set_fire(self, firepower: float) -> bool:
        if math.isnan(firepower):
            raise ValueError("'firepower' cannot be NaN")

        current_tick = self.data.current_tick_or_null
        if not current_tick or not current_tick.bot_state:
            return False  # Cannot determine gun heat or energy

        # Assuming self.base_bot.energy is updated by the BaseBot instance from its on_tick handler
        current_energy = self.base_bot.get_energy()
        gun_heat = current_tick.bot_state.gun_heat

        if current_energy < firepower or gun_heat > 0:
            return False

        self.data.bot_intent.firepower = firepower
        return True

    def get_gun_heat(self) -> float:
        tick = self.data.current_tick_or_null
        return tick.bot_state.gun_heat if tick and tick.bot_state else 0.0

    def get_speed(self) -> float:
        tick = self.data.current_tick_or_null
        return tick.bot_state.speed if tick and tick.bot_state else 0.0

    @property
    def turn_rate(self) -> float:
        if self.data.bot_intent.turn_rate is not None:
            return self.data.bot_intent.turn_rate
        tick = self.data.current_tick_or_null
        return tick.bot_state.turn_rate if tick and tick.bot_state else 0.0

    @turn_rate.setter
    def turn_rate(self, turn_rate: float) -> None:
        if math.isnan(turn_rate):
            raise ValueError("'turn_rate' cannot be NaN")
        self.data.bot_intent.turn_rate = MathUtil.clamp(
            turn_rate, -self.max_turn_rate, self.max_turn_rate
        )

    @property
    def gun_turn_rate(self) -> float:
        if self.data.bot_intent.gun_turn_rate is not None:
            return self.data.bot_intent.gun_turn_rate
        tick = self.data.current_tick_or_null
        return tick.bot_state.gun_turn_rate if tick and tick.bot_state else 0.0

    @gun_turn_rate.setter
    def gun_turn_rate(self, gun_turn_rate: float) -> None:
        if math.isnan(gun_turn_rate):
            raise ValueError("'gun_turn_rate' cannot be NaN")
        self.data.bot_intent.gun_turn_rate = MathUtil.clamp(
            gun_turn_rate, -self.max_gun_turn_rate, self.max_gun_turn_rate
        )

    @property
    def radar_turn_rate(self) -> float:
        if self.data.bot_intent.radar_turn_rate is not None:
            return self.data.bot_intent.radar_turn_rate
        tick = self.data.current_tick_or_null
        return tick.bot_state.radar_turn_rate if tick and tick.bot_state else 0.0

    @radar_turn_rate.setter
    def radar_turn_rate(self, radar_turn_rate: float) -> None:
        if math.isnan(radar_turn_rate):
            raise ValueError("'radar_turn_rate' cannot be NaN")
        self.data.bot_intent.radar_turn_rate = MathUtil.clamp(
            radar_turn_rate, -self.max_radar_turn_rate, self.max_radar_turn_rate
        )

    @property
    def target_speed(self) -> float | None:
        return self.data.bot_intent.target_speed

    @target_speed.setter
    def target_speed(self, target_speed: float) -> None:
        if math.isnan(target_speed):
            raise ValueError("'target_speed' cannot be NaN")
        self.data.bot_intent.target_speed = MathUtil.clamp(
            target_speed, -self.max_speed, self.max_speed
        )

    def get_max_speed(self) -> float:
        # Max speed is part of bot's own limits, not server state
        return self.max_speed

    def set_max_speed(self, max_speed: float) -> None:
        # Max speed is part of bot's own limits
        self.max_speed = MathUtil.clamp(max_speed, 0, MAX_SPEED)

    def get_max_turn_rate(self) -> float:
        # Max turn rate is part of bot's own limits
        return self.max_turn_rate

    def set_max_turn_rate(self, max_turn_rate: float) -> None:
        # Max turn rate is part of bot's own limits
        self.max_turn_rate = MathUtil.clamp(max_turn_rate, 0, MAX_TURN_RATE)

    def get_max_gun_turn_rate(self) -> float:
        # Max gun turn rate is part of bot's own limits
        return self.max_gun_turn_rate

    def set_max_gun_turn_rate(self, max_gun_turn_rate: float) -> None:
        # Max gun turn rate is part of bot's own limits
        self.max_gun_turn_rate = MathUtil.clamp(max_gun_turn_rate, 0, MAX_GUN_TURN_RATE)

    def get_max_radar_turn_rate(self) -> float:
        # Max radar turn rate is part of bot's own limits
        return self.max_radar_turn_rate

    def set_max_radar_turn_rate(self, max_radar_turn_rate: float) -> None:
        # Max radar turn rate is part of bot's own limits
        self.max_radar_turn_rate = MathUtil.clamp(
            max_radar_turn_rate, 0, MAX_RADAR_TURN_RATE
        )

    def get_new_target_speed(self, speed: float, distance: float) -> float:
        if distance < 0:
            return -self.get_new_target_speed(-speed, -distance)

        target_speed: float
        if math.isinf(distance):
            target_speed = self.max_speed
        else:
            target_speed = min(
                self.max_speed, self._get_max_speed_for_distance(distance)
            )

        if speed >= 0:
            return MathUtil.clamp(
                target_speed, speed - self.abs_deceleration, speed + ACCELERATION
            )
        else:  # speed < 0
            return MathUtil.clamp(
                target_speed,
                speed - ACCELERATION,
                speed + self._get_max_deceleration(-speed),
            )

    def _get_max_speed_for_distance(
        self, distance: float
    ) -> float:  # Corresponds to Java's getMaxSpeed(distance)
        deceleration_time = max(
            1,
            math.ceil(
                (math.sqrt((4 * 2 / self.abs_deceleration) * distance + 1) - 1) / 2
            ),
        )
        if math.isinf(deceleration_time):
            return MAX_SPEED

        deceleration_distance = (
            (deceleration_time / 2) * (deceleration_time - 1) * self.abs_deceleration
        )
        return ((deceleration_time - 1) * self.abs_deceleration) + (
            (distance - deceleration_distance) / deceleration_time
        )

    def _get_max_deceleration(
        self, speed: float
    ) -> float:  # Corresponds to Java's getMaxDeceleration(speed)
        deceleration_time = speed / self.abs_deceleration
        acceleration_time = 1 - deceleration_time
        return (
            min(1, deceleration_time) * self.abs_deceleration
            + max(0, acceleration_time) * ACCELERATION
        )

    def get_distance_traveled_until_stop(self, speed: float) -> float:
        speed = math.fabs(speed)
        distance = 0.0
        while speed > 0:
            # This uses the current bot's max_speed and deceleration properties.
            # It simulates deceleration to zero under ideal conditions.
            speed = self.get_new_target_speed(speed, 0)
            distance += speed
        return distance

    # Conditions - Delegated to self.data.conditions
    def add_condition(self, condition: Condition) -> bool:
        # Set add method does not return a boolean indicating if the add was successful
        # We need to check length before and after.
        prev_len = len(self.data.conditions)
        self.data.conditions.add(condition)
        return len(self.data.conditions) > prev_len

    def remove_condition(self, condition: Condition) -> bool:
        try:
            self.data.conditions.remove(condition)
            return True  # remove() raises KeyError if not found
        except KeyError:
            return False  # Condition not found

    def set_stop(self, overwrite: bool) -> None:
        if not self.data.is_stopped or overwrite:
            self.data.is_stopped = True

            # Save current intent values
            self.data.saved_target_speed = self.data.bot_intent.target_speed
            self.data.saved_turn_rate = self.data.bot_intent.turn_rate
            self.data.saved_gun_turn_rate = self.data.bot_intent.gun_turn_rate
            self.data.saved_radar_turn_rate = self.data.bot_intent.radar_turn_rate

            # Stop all movement/turning immediately
            self.data.bot_intent.target_speed = 0.0
            self.data.bot_intent.turn_rate = 0.0
            self.data.bot_intent.gun_turn_rate = 0.0
            self.data.bot_intent.radar_turn_rate = 0.0

            if self.stop_resume_listener is not None:
                self.stop_resume_listener.on_stop()

    def set_resume(self) -> None:
        if self.data.is_stopped:
            # Restore saved intent values
            self.data.bot_intent.target_speed = self.data.saved_target_speed
            self.data.bot_intent.turn_rate = self.data.saved_turn_rate
            self.data.bot_intent.gun_turn_rate = self.data.saved_gun_turn_rate
            self.data.bot_intent.radar_turn_rate = self.data.saved_radar_turn_rate

            if self.stop_resume_listener is not None:
                self.stop_resume_listener.on_resume()

            self.data.is_stopped = False  # Must be the last step

    def is_teammate(self, bot_id: int) -> bool:
        return bot_id in self.data.teammate_ids  # Uses property getter

    def broadcast_team_message(self, message: "Any") -> None:
        self.send_team_message(None, message)

    def send_team_message(self, teammate_id: Optional[int], message: Any) -> None:
        if (
            teammate_id is not None and teammate_id not in self.data.teammate_ids
        ):  # Uses property getter
            raise ValueError("No teammate was found with the specified 'teammate_id'")

        team_messages_list = self.data.bot_intent.team_messages
        if team_messages_list is None:
            team_messages_list = []
            self.data.bot_intent.team_messages = team_messages_list

        if len(team_messages_list) >= MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN:
            raise BotException(
                f"The maximum number team messages has already been reached: {MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN}"
            )

        if message is None:
            raise ValueError("The 'message' of a team message cannot be null")

        # TODO: Consider how to handle message serialization if 'message' is not directly JSON serializable.
        # The current approach relies on to_json(message) which might fail for complex types.
        # For now, assuming 'message' is a simple dict or a type with a __dict__ attribute suitable for JSON.
        json_message_str = to_json(message)
        if len(json_message_str.encode("utf-8")) > TEAM_MESSAGE_MAX_SIZE:
            raise ValueError(
                f"The team message is larger than the limit of {TEAM_MESSAGE_MAX_SIZE} bytes (compact JSON format)"
            )

        team_message = TeamMessage(
            message_type=type(message).__name__,
            receiver_id=teammate_id,
            message=json_message_str,
        )
        team_messages_list.append(team_message)

    # Color and Graphics - Delegated
    @property
    def body_color(self) -> Optional[Color]:
        tick = self.data.current_tick_or_null
        return tick.bot_state.body_color if tick and tick.bot_state else None

    @body_color.setter
    def body_color(self, color: Optional[Color]) -> None:
        self.data.bot_intent.body_color = color.to_color_schema() if color else None

    @property
    def turret_color(self) -> Optional[Color]:
        tick = self.data.current_tick_or_null
        return tick.bot_state.turret_color if tick and tick.bot_state else None

    @turret_color.setter
    def turret_color(self, color: Optional[Color]) -> None:
        self.data.bot_intent.turret_color = color.to_color_schema() if color else None

    @property
    def radar_color(self) -> Optional[Color]:
        tick = self.data.current_tick_or_null
        return tick.bot_state.radar_color if tick and tick.bot_state else None

    @radar_color.setter
    def radar_color(self, color: Optional[Color]) -> None:
        self.data.bot_intent.radar_color = color.to_color_schema() if color else None

    @property
    def bullet_color(self) -> Optional[Color]:
        tick = self.data.current_tick_or_null
        return tick.bot_state.bullet_color if tick and tick.bot_state else None

    @bullet_color.setter
    def bullet_color(self, color: Optional[Color]) -> None:
        self.data.bot_intent.bullet_color = color.to_color_schema() if color else None

    @property
    def scan_color(self) -> Optional[Color]:
        tick = self.data.current_tick_or_null
        return tick.bot_state.scan_color if tick and tick.bot_state else None

    @scan_color.setter
    def scan_color(self, color: Optional[Color]) -> None:
        self.data.bot_intent.scan_color = color.to_color_schema() if color else None

    @property
    def tracks_color(self) -> Optional[Color]:
        tick = self.data.current_tick_or_null
        return tick.bot_state.tracks_color if tick and tick.bot_state else None

    @tracks_color.setter
    def tracks_color(self, color: Optional[Color]) -> None:
        self.data.bot_intent.tracks_color = color.to_color_schema() if color else None

    @property
    def gun_color(self) -> Optional[Color]:
        tick = self.data.current_tick_or_null
        return tick.bot_state.gun_color if tick and tick.bot_state else None

    @gun_color.setter
    def gun_color(self, color: Optional[Color]) -> None:
        self.data.bot_intent.gun_color = color.to_color_schema() if color else None

    def get_graphics(self) -> GraphicsABC:
        return self.data.graphics_state

    # Bullet States - Delegated
    def get_bullet_states(self) -> Sequence[BulletState | None]:
        tick = self.data.current_tick_or_null
        if tick and tick.bullet_states:
            return list(tick.bullet_states)
        return []

    # Server Handshake - Delegated
    @property
    def server_handshake(self) -> ServerHandshake:
        return self.data.server_handshake  # Uses property getter which throws if None

    @server_handshake.setter
    def server_handshake(
        self, server_handshake: ServerHandshake
    ) -> None:  # Typically called by WebSocketHandler
        self.data.server_handshake = server_handshake

    @property
    def variant(self) -> str:
        return self.data.server_handshake.variant

    @variant.setter
    def variant(self, variant: str) -> None:
        self.data.server_handshake.variant = variant

    @property
    def version(self) -> str:
        return self.data.server_handshake.version

    @version.setter
    def version(self, version: str) -> None:
        self.data.server_handshake.version = version
