import asyncio
import urllib.parse
import traceback
import time
import math
import os
import sys
import threading
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
from ..graphics import Color, GraphicsABC, SvgGraphics

from .bot_event_handlers import BotEventHandlers
from .event_queue import EventQueue
from .env_vars import EnvVars
from .intent_validator import IntentValidator
from .internal_event_handlers import InternalEventHandlers
from .json_util import to_json
from .recording_text_writer import RecordingTextWriter
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

from robocode_tank_royale.schema import Message, BotIntent, ServerHandshake, TeamMessage

DEFAULT_SERVER_URL = "ws://localhost:7654"

GAME_NOT_RUNNING_MSG = (
    "Game is not running. Make sure onGameStarted() event handler has been called first"
)
TICK_NOT_AVAILABLE_MSG = "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first"
NOT_CONNECTED_TO_SERVER_MSG = "Not connected to a game server. Make sure onConnected() event handler has been called first"


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

        self.bot_intent: BotIntent = BotIntent(
            type=Message.Type.BOT_INTENT, team_messages=[]
        )
        self._my_id: Optional[int] = None
        self._teammate_ids: Set[int] = set()
        self._game_setup: Optional[GameSetup] = None
        self._initial_position: Optional[InitialPosition] = None
        self._tick_event: Optional[TickEvent] = None
        self._tick_start_nano_time: int = 0
        self._server_handshake: Optional[ServerHandshake] = None
        self._conditions: Set[Condition] = set()
        self._is_running_atomic: bool = False
        self._event_handling_disabled_turn: int = 0
        self.graphics_state: GraphicsABC = SvgGraphics()
        # Fields for set_stop / set_resume
        self.is_stopped: bool = False
        self.saved_target_speed: Optional[float] = None
        self.saved_turn_rate: Optional[float] = None
        self.saved_gun_turn_rate: Optional[float] = None
        self.saved_radar_turn_rate: Optional[float] = None
        # Flag set when the current event handler was interrupted by a new event
        self.was_current_event_interrupted: bool = False
        # Recording writers for capturing stdout/stderr
        self.recording_stdout: Optional[object] = None
        self.recording_stderr: Optional[object] = None


        self.bot_event_handlers: BotEventHandlers = BotEventHandlers(base_bot)
        self.internal_event_handlers: InternalEventHandlers = InternalEventHandlers()
        self.event_queue: EventQueue = EventQueue(self, self.bot_event_handlers)

        self.closed_event: threading.Event = threading.Event()
        self.socket: Optional[Any] = None  # To store the WebSocket connection
        self.web_socket_handler: Optional[WebSocketHandler] = None

        self._next_turn_condition: threading.Condition = threading.Condition()

        # WebSocket background thread with async event loop
        self._ws_thread: Optional[threading.Thread] = None
        self._ws_loop: Optional[asyncio.AbstractEventLoop] = None
        self._ws_loop_ready_event: threading.Event = threading.Event()

        # Bot thread (runs bot.run() and bot.go())
        self.thread: Optional[threading.Thread] = None
        self.stop_resume_listener: Optional[StopResumeListenerABC] = None

        self.max_speed: float = MAX_SPEED
        self.max_turn_rate: float = MAX_TURN_RATE
        self.max_gun_turn_rate: float = MAX_GUN_TURN_RATE
        self.max_radar_turn_rate: float = MAX_RADAR_TURN_RATE

        self.last_execute_turn_number: int = -1

        # Movement reset deferral flag (mirrors Java/.NET movementResetPending)
        self._movement_reset_pending: bool = False

        # Recording text writers for capturing stdout/stderr
        self._recording_stdout: Optional[RecordingTextWriter] = None
        self._recording_stderr: Optional[RecordingTextWriter] = None

        self._init()

    def _get_server_url_from_setting(self) -> str:
        url = os.getenv("SERVER_URL", os.getenv("SERVER_URL"))
        if url is None:
            url = DEFAULT_SERVER_URL
        return url

    def _get_server_secret_from_setting(self) -> Optional[str]:
        return os.getenv("SERVER_SECRET", os.getenv("SERVER_SECRET"))

    def _init(self) -> None:
        self._redirect_stdout_and_stderr()
        self._subscribe_to_events()

    def _redirect_stdout_and_stderr(self) -> None:
        """Redirect stdout and stderr to recording writers for sending to server."""
        self._recording_stdout = RecordingTextWriter(sys.stdout)
        self._recording_stderr = RecordingTextWriter(sys.stderr)

        # Store in data so WebSocketHandler can access them
        self.recording_stdout = self._recording_stdout
        self.recording_stderr = self._recording_stderr

        sys.stdout = self._recording_stdout
        sys.stderr = self._recording_stderr

    def _subscribe_to_events(self) -> None:
        self.internal_event_handlers.on_round_started.subscribe(
            self._on_round_started, 100
        )
        self.internal_event_handlers.on_next_turn.subscribe(self._on_next_turn, 100)
        self.internal_event_handlers.on_bullet_fired.subscribe(
            self._on_bullet_fired, 100
        )

    def _on_round_started(self, event: RoundStartedEvent) -> None:
        """Handle round started event (matches Java's onRoundStarted)"""
        self.tick_event = None
        # Defer movement reset until after first intent has been sent, mirroring Java/.NET behavior
        if not hasattr(self, "_movement_reset_pending"):
            self._movement_reset_pending = False
        self._movement_reset_pending = True
        self.event_queue.clear()  # Clears conditions in self.conditions
        self.is_stopped = False
        self.event_handling_disabled_turn = 0
        self.last_execute_turn_number = -1

    def _on_next_turn(self, event: TickEvent) -> None:
        """Handle next turn event - unblock waiting threads (matches Java's onNextTurn)"""
        with self._next_turn_condition:
            # Unblock methods waiting for the next turn
            self._next_turn_condition.notify_all()

    def _on_bullet_fired(self, event: BulletFiredEvent) -> None:
        """Handle bullet fired event (matches Java's onBulletFired)"""
        if self.bot_intent:
            # Reset firepower so the bot stops firing continuously
            self.bot_intent.firepower = 0.0

    def _reset_movement(self) -> None:
        if self.bot_intent:
            self.bot_intent.turn_rate = None
            self.bot_intent.gun_turn_rate = None
            self.bot_intent.radar_turn_rate = None
            self.bot_intent.target_speed = None
            self.bot_intent.firepower = None

    @property
    def my_id(self) -> int:
        if self._my_id is None:
            raise BotException(GAME_NOT_RUNNING_MSG)
        return self._my_id

    @my_id.setter
    def my_id(self, value: int):
        self._my_id = value

    @property
    def teammate_ids(self) -> Set[int]:
        if self._my_id is None:
            raise BotException(GAME_NOT_RUNNING_MSG)
        return self._teammate_ids

    @teammate_ids.setter
    def teammate_ids(self, value: Set[int]):
        self._teammate_ids = value

    @property
    def game_setup(self) -> GameSetup:
        if self._game_setup is None:
            raise BotException(GAME_NOT_RUNNING_MSG)
        return self._game_setup

    @game_setup.setter
    def game_setup(self, value: GameSetup):
        self._game_setup = value

    @property
    def initial_position(self) -> Optional[InitialPosition]:
        return self._initial_position

    @initial_position.setter
    def initial_position(self, value: Optional[InitialPosition]):
        self._initial_position = value

    def get_bot_intent(self) -> BotIntent:
        return self.bot_intent

    @property
    def tick_event(self) -> Optional[TickEvent]:
        return self._tick_event

    @tick_event.setter
    def tick_event(self, value: Optional[TickEvent]):
        self._tick_event = value

    @property
    def current_tick_or_throw(self) -> TickEvent:
        if self._tick_event is None:
            raise BotException(TICK_NOT_AVAILABLE_MSG)
        return self._tick_event

    def get_current_tick_or_throw(self) -> TickEvent:
        return self.current_tick_or_throw

    def set_tick_event(self, tick_event: TickEvent) -> None:
        self.tick_event = tick_event

    @property
    def current_tick_or_null(self) -> Optional[TickEvent]:
        return self._tick_event

    def get_current_tick_or_null(self) -> Optional[TickEvent]:
        return self.current_tick_or_null

    @property
    def tick_start_nano_time(self) -> int:
        return self._tick_start_nano_time

    @tick_start_nano_time.setter
    def tick_start_nano_time(self, value: int):
        self._tick_start_nano_time = value

    def get_tick_start_nano_time(self) -> int:
        return self.tick_start_nano_time

    def set_tick_start_nano_time(self, tick_start_nano_time: int) -> None:
        self.tick_start_nano_time = tick_start_nano_time

    def get_time_left(self) -> int:
        if self.current_tick_or_null is None:
            return self.game_setup.turn_timeout

        passed_microseconds = (
            time.monotonic_ns() - self.tick_start_nano_time
        ) // 1000
        return max(0, self.game_setup.turn_timeout - passed_microseconds)

    @property
    def event_handling_disabled_turn(self) -> int:
        return self._event_handling_disabled_turn

    @event_handling_disabled_turn.setter
    def event_handling_disabled_turn(self, value: int):
        self._event_handling_disabled_turn = value

    def enable_event_handling(self, enable: bool) -> None:
        if enable:
            self.event_handling_disabled_turn = 0
        else:
            # Ensure tick event is available before accessing turn_number
            current_tick = self.current_tick_or_null
            if current_tick:
                self.event_handling_disabled_turn = current_tick.turn_number
            else:
                # If called before any tick, disabling implies from turn 0.
                self.event_handling_disabled_turn = 0

    def is_event_handling_disabled(self) -> bool:
        # Important! Allow an additional turn so events like RoundStarted can be handled
        current_tick = self.current_tick_or_null
        if not current_tick:
            return (
                self.event_handling_disabled_turn != 0
            )  # If no tick, rely purely on the flag

        return (
            self.event_handling_disabled_turn != 0
            and self.event_handling_disabled_turn < (current_tick.turn_number - 1)
        )

    def get_events(self) -> Sequence[BotEvent]:
        turn_number = self.current_tick_or_throw.turn_number
        return self.event_queue.get_events(turn_number)

    def clear_events(self) -> None:
        self.event_queue.clear_events()

    # Public wrapper to enqueue events from a tick, aligning with Java BaseBotInternals
    def add_events_from_tick(self, event: TickEvent) -> None:
        self.event_queue.add_events_from_tick(event)

    def add_event(self, event: BotEvent) -> None:
        self.event_queue.add_event(event)

    def set_interruptible(self, interruptible: bool) -> None:
        self.event_queue.set_current_event_interruptible(interruptible)

    def dispatch_events(self, turn_number: int) -> None:
        try:
            self.event_queue.dispatch_events(turn_number)
        except BotException:
            # Suppress tick-unavailable errors during round transitions (bot is shutting down)
            if self.is_running():
                traceback.print_exc()
        except Exception:
            # Align with Java: do not propagate interruptions from event handling
            traceback.print_exc()

    def set_running(self, is_running: bool) -> None:
        self._is_running_atomic = is_running

    def is_running(self) -> bool:
        return self._is_running_atomic

    def _wait_until_first_tick_arrived(self) -> None:
        """Block the pre-warmed bot thread until the first tick of the round arrives.
        The thread is started at round-started (before any tick), so it must wait here
        before run() can safely read bot state.
        Notified by _on_next_turn() (priority 100) after BotInternals._on_first_turn()
        (priority 110) has already captured initial directions via _clear_remaining().
        """
        with self._next_turn_condition:
            while self.is_running() and self.current_tick_or_null is None:
                self._next_turn_condition.wait()
        # Dispatch tick 1 events before run() starts, so the first run() iteration reads state
        # that already has events fired — matching Classic Robocode semantics.
        first_tick = self.current_tick_or_null
        if first_tick is not None:
            self.dispatch_events(first_tick.turn_number)

    def _create_runnable(self, bot: BotABC):
        """Create runnable function for bot thread (matches Java's createRunnable)"""
        def runnable():
            self.set_running(True)
            try:
                self._wait_until_first_tick_arrived()
                bot.run()
            except ThreadInterruptedException:
                pass

            self._dispatch_final_turn_events()

            # Skip every turn after the run method has exited
            while self.is_running():
                try:
                    bot.go()
                except ThreadInterruptedException:
                    break

            self._dispatch_final_turn_events()
        return runnable

    def _dispatch_final_turn_events(self) -> None:
        """Dispatch any remaining events from the current tick before the thread exits."""
        tick = self.current_tick_or_null
        if tick is not None:
            self.dispatch_events(tick.turn_number)

    def start_thread(self, bot: BotABC) -> None:
        """Start bot thread (matches Java's startThread)"""
        self.enable_event_handling(True)  # reset on WebSocket thread — before new bot thread starts
        self.thread = threading.Thread(target=self._create_runnable(bot))
        self.thread.start()

    def stop_thread(self) -> None:
        """Stop bot thread (matches Java's stopThread)"""
        if not self.is_running():
            return

        self.set_running(False)
        self.enable_event_handling(False)  # disable on WebSocket thread — prevents new ticks from queuing after bot stops

        # Wake up any threads waiting on the next turn condition so they can see is_running=False
        with self._next_turn_condition:
            self._next_turn_condition.notify_all()

        thread = self.thread
        self.thread = None
        if thread is not None and thread is not threading.current_thread():
            # Wait for the bot thread to finish so that handle_round_ended's
            # dispatch_events call does not race with _dispatch_final_turn_events.
            thread.join(timeout=5.0)

    def _sanitize_url(self, uri: str) -> None:
        parsed_url = urllib.parse.urlparse(uri)
        if parsed_url.scheme not in ("ws", "wss"):
            raise BotException(f"Wrong scheme used with server URL: {uri}")

    async def _connect(self) -> None:
        self._sanitize_url(self.server_url)
        try:
            self.web_socket_handler = WebSocketHandler(  # Store the handler instance
                self,  # Pass BaseBotInternals instance
                self.server_url,
                self.server_secret,
                self.base_bot,  # The bot instance itself (BaseBotABC)
                self.bot_info,  # BotInfo needed by WebSocketHandler
                self.bot_event_handlers,  # Event handlers needed by WebSocketHandler
                self.internal_event_handlers,  # Event handlers needed by WebSocketHandler
                self.closed_event,  # Threading event for signaling connection close
                self.event_queue,  # Provide access to the shared EventQueue for staging events
            )
            self.socket = await self.web_socket_handler.connect()

        except Exception as ex:
            raise BotException(
                f"Could not create web socket for URL: {self.server_url}", ex
            ) from ex

    def start(self) -> None:
        """Start bot and block until game ends (matches Java's start)"""
        self._start_websocket_thread()
        self._connect_sync()
        self.closed_event.wait()
        # CRITICAL: Stop the WebSocket event loop when bot finishes
        self._stop_websocket_thread()

    def _start_websocket_thread(self) -> None:
        """Start WebSocket background thread with async event loop"""
        def ws_thread_target():
            self._ws_loop = asyncio.new_event_loop()
            asyncio.set_event_loop(self._ws_loop)
            self._ws_loop_ready_event.set()
            try:
                self._ws_loop.run_forever()
            finally:
                # Standard asyncio shutdown: cancel all tasks, shutdown executor, then close loop
                try:
                    # Cancel all tasks
                    pending = asyncio.all_tasks(self._ws_loop)
                    for task in pending:
                        task.cancel()
                    # Wait for cancellation to complete
                    if pending:
                        self._ws_loop.run_until_complete(
                            asyncio.gather(*pending, return_exceptions=True)
                        )
                    # CRITICAL: Shutdown the default executor to prevent hanging
                    self._ws_loop.run_until_complete(self._ws_loop.shutdown_default_executor())
                except Exception:
                    pass  # Ignore errors during cleanup
                finally:
                    self._ws_loop.close()

        self._ws_loop_ready_event.clear()
        self._ws_thread = threading.Thread(target=ws_thread_target, daemon=False)
        self._ws_thread.start()
        if not self._ws_loop_ready_event.wait(timeout=2.0):
            raise BotException("WebSocket event loop not started")

    def _stop_websocket_thread(self) -> None:
        """Stop WebSocket event loop and thread using proper asyncio shutdown"""
        if not self._ws_loop or self._ws_loop.is_closed():
            return

        # Simply stop the loop - cleanup happens in the finally block
        try:
            self._ws_loop.call_soon_threadsafe(self._ws_loop.stop)
        except RuntimeError:
            pass

        # Wait for thread to finish (it will clean up all tasks)
        if self._ws_thread and self._ws_thread.is_alive():
            self._ws_thread.join(timeout=3.0)

    def _connect_sync(self) -> None:
        """Connect to WebSocket server (synchronous wrapper)"""
        if self._ws_loop is None:
            raise BotException("WebSocket event loop not started")

        future = asyncio.run_coroutine_threadsafe(self._connect(), self._ws_loop)
        future.result()  # Block until connection is established

        # Start receiving messages in the WebSocket thread
        if self.web_socket_handler:
            asyncio.run_coroutine_threadsafe(
                self.web_socket_handler.receive_messages(),
                self._ws_loop
            )

    def execute(self, captured_turn_number: int) -> None:
        """Execute bot intent and wait for next turn.

        Args:
            captured_turn_number: The turn number captured by go() at the time events were
                dispatched, or -1 if no tick was available.
        """
        # If no tick has been received yet, send current intent once to allow the server to progress
        if captured_turn_number < 0:
            self._send_intent()
            return

        if captured_turn_number != self.last_execute_turn_number:
            self.last_execute_turn_number = captured_turn_number
            # Events are dispatched from BaseBot.go(); staging happens on tick reception
            self._send_intent()

            if self._movement_reset_pending:
                self._reset_movement()
                self._movement_reset_pending = False

        self._wait_for_next_turn(captured_turn_number)

        # Dispatch events for the new turn *after* waiting, so that run() always reads state
        # that matches the events that just fired — matching Classic Robocode semantics.
        new_tick = self.current_tick_or_null
        if new_tick is not None:
            self.dispatch_events(new_tick.turn_number)

    def _send_intent(self) -> None:
        """Send bot intent to server (synchronous)"""
        self._render_graphics_to_bot_intent()
        self._transfer_std_out_to_bot_intent()

        if self.socket and self._ws_loop:
            try:
                json_intent = to_json(self.bot_intent)
                # Send via WebSocket in the WebSocket thread
                asyncio.run_coroutine_threadsafe(
                    self.socket.send(json_intent),
                    self._ws_loop
                )
                # Clear rescan flag after serializing — consumed by this intent
                if self.bot_intent.rescan:
                    self.bot_intent.rescan = False
                # Clear team messages after sending intent (matches Java implementation)
                if self.bot_intent.team_messages:
                    self.bot_intent.team_messages.clear()
            except Exception as e:
                print(f"Error sending bot intent: {e}")


    def _transfer_std_out_to_bot_intent(self) -> None:
        """Transfer captured stdout/stderr to bot intent for sending to server."""
        if self._recording_stdout:
            output = self._recording_stdout.read_next()
            if output:
                self.bot_intent.std_out = output
            else:
                self.bot_intent.std_out = None

        if self._recording_stderr:
            error = self._recording_stderr.read_next()
            if error:
                self.bot_intent.std_err = error
            else:
                self.bot_intent.std_err = None

    def _render_graphics_to_bot_intent(self) -> None:
        current_tick = self.current_tick_or_null
        # Check if debugging is enabled for the bot in the current tick
        if (
            current_tick
            and hasattr(current_tick, "bot_state")
            and current_tick.bot_state
            and hasattr(current_tick.bot_state, "debugging_enabled")
            and current_tick.bot_state.debugging_enabled
        ):
            svg_output = self.graphics_state.to_svg()
            self.bot_intent.debug_graphics = svg_output
            self.graphics_state.clear()
        else:
            # Ensure it's not set if debugging is off or tick not available
            self.bot_intent.debug_graphics = None

    def _wait_for_next_turn(self, turn_number: int) -> None:
        """Wait for next turn (matches Java's waitForNextTurn)"""
        # Check if we're being called from the designated bot thread
        # If self.thread is None (test mode with no run() loop) or current thread doesn't match,
        # exit immediately - the intent was already sent in execute() before this call
        if self.thread is None or threading.current_thread() != self.thread:
            # In test mode or when called from wrong thread, just return without waiting
            # This allows tests to call go() from test threads without hanging
            return

        # Only wait if we're in the correct bot thread and bot is running
        with self._next_turn_condition:
            while self.is_running():
                current_tick = self.current_tick_or_null
                if current_tick is None or current_tick.turn_number != turn_number:
                    break
                try:
                    # Use timeout to allow periodic check of is_running() flag
                    # This makes the bot thread interruptible for clean shutdown
                    self._next_turn_condition.wait(timeout=0.1)
                except InterruptedError:
                    raise ThreadInterruptedException()

    def _stop_rogue_thread(self) -> None:
        """Stop rogue thread (matches Java's stopRogueThread)"""
        # This method is no longer called from _wait_for_next_turn
        # Kept for compatibility but effectively disabled
        pass

    def set_fire(self, firepower: float) -> bool:
        """Set fire with given firepower. Matches Java's setFire() semantics exactly."""
        IntentValidator.validate_firepower(firepower)

        # Match Java: use base_bot.energy and base_bot.gun_heat (which return 0 when no tick received)
        if self.base_bot.energy < firepower or self.base_bot.gun_heat > 0:
            return False  # cannot fire yet

        self.bot_intent.firepower = firepower
        return True

    def get_gun_heat(self) -> float:
        tick = self.current_tick_or_null
        return tick.bot_state.gun_heat if tick and tick.bot_state else 0.0

    def get_speed(self) -> float:
        tick = self.current_tick_or_null
        return tick.bot_state.speed if tick and tick.bot_state else 0.0

    @property
    def turn_rate(self) -> float:
        if self.bot_intent.turn_rate is not None:
            return self.bot_intent.turn_rate
        tick = self.current_tick_or_null
        return tick.bot_state.turn_rate if tick and tick.bot_state else 0.0

    @turn_rate.setter
    def turn_rate(self, turn_rate: float) -> None:
        self.bot_intent.turn_rate = IntentValidator.validate_turn_rate(
            turn_rate, self.max_turn_rate
        )

    @property
    def gun_turn_rate(self) -> float:
        if self.bot_intent.gun_turn_rate is not None:
            return self.bot_intent.gun_turn_rate
        tick = self.current_tick_or_null
        return tick.bot_state.gun_turn_rate if tick and tick.bot_state else 0.0

    @gun_turn_rate.setter
    def gun_turn_rate(self, gun_turn_rate: float) -> None:
        self.bot_intent.gun_turn_rate = IntentValidator.validate_gun_turn_rate(
            gun_turn_rate, self.max_gun_turn_rate
        )

    @property
    def radar_turn_rate(self) -> float:
        if self.bot_intent.radar_turn_rate is not None:
            return self.bot_intent.radar_turn_rate
        tick = self.current_tick_or_null
        return tick.bot_state.radar_turn_rate if tick and tick.bot_state else 0.0

    @radar_turn_rate.setter
    def radar_turn_rate(self, radar_turn_rate: float) -> None:
        self.bot_intent.radar_turn_rate = IntentValidator.validate_radar_turn_rate(
            radar_turn_rate, self.max_radar_turn_rate
        )

    @property
    def target_speed(self) -> float | None:
        return self.bot_intent.target_speed

    @target_speed.setter
    def target_speed(self, target_speed: float) -> None:
        self.bot_intent.target_speed = IntentValidator.validate_target_speed(
            target_speed, self.max_speed
        )

    def get_max_speed(self) -> float:
        # Max speed is part of bot's own limits, not server state
        return self.max_speed

    def set_max_speed(self, max_speed: float) -> None:
        # Max speed is part of bot's own limits
        self.max_speed = IntentValidator.validate_max_speed(max_speed)

    def get_max_turn_rate(self) -> float:
        # Max turn rate is part of bot's own limits
        return self.max_turn_rate

    def set_max_turn_rate(self, max_turn_rate: float) -> None:
        # Max turn rate is part of bot's own limits
        self.max_turn_rate = IntentValidator.validate_max_turn_rate(max_turn_rate)

    def get_max_gun_turn_rate(self) -> float:
        # Max gun turn rate is part of bot's own limits
        return self.max_gun_turn_rate

    def set_max_gun_turn_rate(self, max_gun_turn_rate: float) -> None:
        # Max gun turn rate is part of bot's own limits
        self.max_gun_turn_rate = IntentValidator.validate_max_gun_turn_rate(max_gun_turn_rate)

    def get_max_radar_turn_rate(self) -> float:
        # Max radar turn rate is part of bot's own limits
        return self.max_radar_turn_rate

    def set_max_radar_turn_rate(self, max_radar_turn_rate: float) -> None:
        # Max radar turn rate is part of bot's own limits
        self.max_radar_turn_rate = IntentValidator.validate_max_radar_turn_rate(max_radar_turn_rate)

    def get_new_target_speed(self, speed: float, distance: float) -> float:
        return IntentValidator.get_new_target_speed(speed, distance, self.max_speed)

    def get_distance_traveled_until_stop(self, speed: float) -> float:
        return IntentValidator.get_distance_traveled_until_stop(speed, self.max_speed)

    @property
    def conditions(self) -> Set[Condition]:
        return self._conditions

    # Conditions
    def add_condition(self, condition: Condition) -> bool:
        # Set add method does not return a boolean indicating if the add was successful
        # We need to check length before and after.
        prev_len = len(self.conditions)
        self.conditions.add(condition)
        return len(self.conditions) > prev_len

    def remove_condition(self, condition: Condition) -> bool:
        try:
            self.conditions.remove(condition)
            return True  # remove() raises KeyError if not found
        except KeyError:
            return False  # Condition not found

    def set_stop(self, overwrite: bool) -> None:
        if not self.is_stopped or overwrite:
            self.is_stopped = True

            # Save current intent values
            self.saved_target_speed = self.bot_intent.target_speed
            self.saved_turn_rate = self.bot_intent.turn_rate
            self.saved_gun_turn_rate = self.bot_intent.gun_turn_rate
            self.saved_radar_turn_rate = self.bot_intent.radar_turn_rate

            # Stop all movement/turning immediately
            self.bot_intent.target_speed = 0.0
            self.bot_intent.turn_rate = 0.0
            self.bot_intent.gun_turn_rate = 0.0
            self.bot_intent.radar_turn_rate = 0.0

            if self.stop_resume_listener is not None:
                self.stop_resume_listener.on_stop()

    def set_resume(self) -> None:
        if self.is_stopped:
            # Restore saved intent values
            self.bot_intent.target_speed = self.saved_target_speed
            self.bot_intent.turn_rate = self.saved_turn_rate
            self.bot_intent.gun_turn_rate = self.saved_gun_turn_rate
            self.bot_intent.radar_turn_rate = self.saved_radar_turn_rate

            if self.stop_resume_listener is not None:
                self.stop_resume_listener.on_resume()

            self.is_stopped = False  # Must be the last step

    def is_teammate(self, bot_id: int) -> bool:
        return bot_id in self.teammate_ids  # Uses property getter

    def broadcast_team_message(self, message: "Any") -> None:
        self.send_team_message(None, message)

    def send_team_message(self, teammate_id: Optional[int], message: Any) -> None:
        from ..team_message import serialize_team_message

        IntentValidator.validate_teammate_id(teammate_id, self.teammate_ids)

        team_messages_list = self.bot_intent.team_messages
        if team_messages_list is None:
            team_messages_list = []
            self.bot_intent.team_messages = team_messages_list

        IntentValidator.validate_team_message(message, len(team_messages_list))

        # Serialize the message using team_message module which handles Color objects
        json_message_str = serialize_team_message(message)
        IntentValidator.validate_team_message_size(json_message_str)

        team_message = TeamMessage(
            message_type=type(message).__name__,
            receiver_id=teammate_id,
            message=json_message_str,
        )
        team_messages_list.append(team_message)

    # Color and Graphics - Delegated
    @property
    def body_color(self) -> Optional[Color]:
        tick = self.current_tick_or_null
        return tick.bot_state.body_color if tick and tick.bot_state else None

    @body_color.setter
    def body_color(self, color: Optional[Color]) -> None:
        self.bot_intent.body_color = IntentValidator.color_to_schema(color)

    @property
    def turret_color(self) -> Optional[Color]:
        tick = self.current_tick_or_null
        return tick.bot_state.turret_color if tick and tick.bot_state else None

    @turret_color.setter
    def turret_color(self, color: Optional[Color]) -> None:
        self.bot_intent.turret_color = IntentValidator.color_to_schema(color)

    @property
    def radar_color(self) -> Optional[Color]:
        tick = self.current_tick_or_null
        return tick.bot_state.radar_color if tick and tick.bot_state else None

    @radar_color.setter
    def radar_color(self, color: Optional[Color]) -> None:
        self.bot_intent.radar_color = IntentValidator.color_to_schema(color)

    @property
    def bullet_color(self) -> Optional[Color]:
        tick = self.current_tick_or_null
        return tick.bot_state.bullet_color if tick and tick.bot_state else None

    @bullet_color.setter
    def bullet_color(self, color: Optional[Color]) -> None:
        self.bot_intent.bullet_color = IntentValidator.color_to_schema(color)

    @property
    def scan_color(self) -> Optional[Color]:
        tick = self.current_tick_or_null
        return tick.bot_state.scan_color if tick and tick.bot_state else None

    @scan_color.setter
    def scan_color(self, color: Optional[Color]) -> None:
        self.bot_intent.scan_color = IntentValidator.color_to_schema(color)

    @property
    def tracks_color(self) -> Optional[Color]:
        tick = self.current_tick_or_null
        return tick.bot_state.tracks_color if tick and tick.bot_state else None

    @tracks_color.setter
    def tracks_color(self, color: Optional[Color]) -> None:
        self.bot_intent.tracks_color = IntentValidator.color_to_schema(color)

    @property
    def gun_color(self) -> Optional[Color]:
        tick = self.current_tick_or_null
        return tick.bot_state.gun_color if tick and tick.bot_state else None

    @gun_color.setter
    def gun_color(self, color: Optional[Color]) -> None:
        self.bot_intent.gun_color = IntentValidator.color_to_schema(color)

    def get_graphics(self) -> GraphicsABC:
        return self.graphics_state

    # Bullet States - Delegated
    def get_bullet_states(self) -> Sequence[BulletState | None]:
        tick = self.current_tick_or_null
        if tick and tick.bullet_states:
            return list(tick.bullet_states)
        return []

    # Server Handshake
    @property
    def server_handshake(self) -> ServerHandshake:
        if self._server_handshake is None:
            raise BotException(NOT_CONNECTED_TO_SERVER_MSG)
        return self._server_handshake

    @server_handshake.setter
    def server_handshake(self, value: ServerHandshake):
        self._server_handshake = value

    @property
    def variant(self) -> str:
        return self.server_handshake.variant

    @variant.setter
    def variant(self, variant: str) -> None:
        self.server_handshake.variant = variant

    @property
    def version(self) -> str:
        return self.server_handshake.version

    @version.setter
    def version(self, version: str) -> None:
        self.server_handshake.version = version
