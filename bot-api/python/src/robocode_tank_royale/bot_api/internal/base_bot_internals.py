import asyncio
import urllib.parse
import traceback
import time
import json
import math
import os # Added import
from types import SimpleNamespace # Keep for now, may be removed if get_current_tick_or_throw is fully stable
from typing import Any, Optional, Set, Dict, List

from robocode_tank_royale.bot_api.base_bot_abc import BaseBotABC
from robocode_tank_royale.bot_api.bot_abc import BotABC
from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.internal.bot_event_handlers import BotEventHandlers
from robocode_tank_royale.bot_api.internal.internal_event_handlers import InternalEventHandlers
from robocode_tank_royale.bot_api.internal.event_queue import EventQueue
from robocode_tank_royale.bot_api.game_setup import GameSetup
from robocode_tank_royale.bot_api.events import TickEvent, BulletFiredEvent, RoundStartedEvent, BotEvent
from robocode_tank_royale.bot_api.bullet_state import BulletState # Updated import
from robocode_tank_royale.bot_api.events.condition import Condition
from robocode_tank_royale.bot_api.initial_position import InitialPosition
from robocode_tank_royale.bot_api.bot_exception import BotException
from robocode_tank_royale.bot_api.internal.thread_interrupted_exception import ThreadInterruptedException
from robocode_tank_royale.bot_api.internal.stop_resume_listener_abs import StopResumeListenerABC
from robocode_tank_royale.bot_api.internal.websocket_handler import WebSocketHandler
from robocode_tank_royale.bot_api.util.math_util import MathUtil
from .graphics_state import GraphicsState
from robocode_tank_royale.schema import ServerHandshake # Using this path

from robocode_tank_royale.bot_api.constants import (
    MAX_SPEED,
    MAX_TURN_RATE,
    MAX_GUN_TURN_RATE,
    MAX_RADAR_TURN_RATE,
    DECELERATION,
    ACCELERATION, # Make sure this is defined in constants
    MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN, # Added import
    TEAM_MESSAGE_MAX_SIZE, # Added import
)
# Assuming schema_messages would provide BotIntent, but we are using a dictionary for now.
# from robocode_tank_royale.schema_messages import BotIntent # Not used yet
from robocode_tank_royale.bot_api.internal.env_vars import EnvVars

DEFAULT_SERVER_URL = "ws://localhost:7654"

GAME_NOT_RUNNING_MSG = "Game is not running. Make sure onGameStarted() event handler has been called first"
TICK_NOT_AVAILABLE_MSG = \
    "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first"
NOT_CONNECTED_TO_SERVER_MSG = \
    "Not connected to a game server. Make sure onConnected() event handler has been called first"


class BaseBotInternals:
    def __init__(self, base_bot: BaseBotABC, bot_info: Optional[BotInfo], server_url: Optional[str], server_secret: Optional[str]):
        self.base_bot = base_bot
        if bot_info is None:
            self.bot_info = EnvVars.get_bot_info()
        else:
            self.bot_info = bot_info

        self.server_url = server_url if server_url is not None else self._get_server_url_from_setting()
        self.server_secret = server_secret if server_secret is not None else self._get_server_secret_from_setting()

        self.bot_intent: Dict[str, Any] = self._new_bot_intent()

        self.bot_event_handlers: BotEventHandlers = BotEventHandlers(base_bot)
        self.internal_event_handlers: InternalEventHandlers = InternalEventHandlers()
        self.event_queue: EventQueue = EventQueue(self, self.bot_event_handlers)

        self.conditions: Set[Any] = set()  # Using a standard set for now

        self.closed_event: asyncio.Event = asyncio.Event()
        self.socket: Optional[Any] = None # To store the WebSocket connection

        self.my_id: Optional[int] = None
        self.teammate_ids: Set[int] = set()
        self.game_setup: Optional[GameSetup] = None
        self.initial_position: Optional[InitialPosition] = None

        self.tick_event: Optional[TickEvent] = None
        self.tick_start_nano_time: Optional[int] = None # Representing Long as int

        self.next_turn_monitor: asyncio.Condition = asyncio.Condition() # Changed from Lock to Condition

        self.thread: Optional[asyncio.Task[Any]] = None # For asyncio task
        self.is_running_atomic: bool = False # Simple boolean, manage access if threaded
        self.is_stopped: bool = False
        self.stop_resume_listener: Optional[StopResumeListenerABC] = None

        self.max_speed: float = MAX_SPEED
        self.max_turn_rate: float = MAX_TURN_RATE
        self.max_gun_turn_rate: float = MAX_GUN_TURN_RATE
        self.max_radar_turn_rate: float = MAX_RADAR_TURN_RATE

        self.saved_target_speed: Optional[float] = None
        self.saved_turn_rate: Optional[float] = None
        self.saved_gun_turn_rate: Optional[float] = None
        self.saved_radar_turn_rate: Optional[float] = None

        self.abs_deceleration: float = abs(DECELERATION)

        self.event_handling_disabled_turn: int = 0
        self.last_execute_turn_number: int = -1

        self.graphics_state: GraphicsState = GraphicsState() # Uncommented and typed

        self.server_handshake: Optional[ServerHandshake] = None # Added attribute

        self._init()

    def _get_server_url_from_setting(self) -> str:
        url = os.getenv('ROBOCODE_SERVER_URL', os.getenv('SERVER_URL'))
        if url is None:
            url = DEFAULT_SERVER_URL
        return url

    def _get_server_secret_from_setting(self) -> Optional[str]:
        return os.getenv('ROBOCODE_SERVER_SECRET', os.getenv('SERVER_SECRET'))

    def _new_bot_intent(self) -> Dict[str, Any]:
        return {
            'type': 'BotIntent', # Must be set!
            'targetSpeed': None,
            'turnRate': None,
            'gunTurnRate': None,
            'radarTurnRate': None,
            'firepower': None,
            'adjustGunForBodyTurn': None,
            'adjustRadarForGunTurn': None,
            'scan': None,
            'bodyColor': None,
            'turretColor': None,
            'radarColor': None,
            'bulletColor': None,
            'scanColor': None,
            'tracksColor': None,
            'gunColor': None,
            'teamMessages': [],
            'stdOut': None,
            'stdErr': None,
            'debugGraphics': None, # For SVG output from graphics_state
        }

    def _init(self) -> None:
        # Skipping redirectStdOutAndStdErr for now
        self._subscribe_to_events()

    def _subscribe_to_events(self) -> None:
        self.internal_event_handlers.on_round_started.subscribe(self._on_round_started, 100)
        self.internal_event_handlers.on_next_turn.subscribe(self._on_next_turn, 100)
        self.internal_event_handlers.on_bullet_fired.subscribe(self._on_bullet_fired, 100)

    def _on_round_started(self, event: RoundStartedEvent) -> None:
        self._reset_movement()
        self.event_queue.clear()
        self.is_stopped = False
        self.event_handling_disabled_turn = 0
        self.last_execute_turn_number = -1

    def _on_next_turn(self, event: TickEvent) -> None:
        # TODO: figure out how the next_turn thing works.

    def _on_bullet_fired(self, event: BulletFiredEvent) -> None:
        if self.bot_intent:
            self.bot_intent['firepower'] = 0.0
        pass

    def _reset_movement(self) -> None:
        if self.bot_intent:
            self.bot_intent['turnRate'] = None
            self.bot_intent['gunTurnRate'] = None
            self.bot_intent['radarTurnRate'] = None
            self.bot_intent['targetSpeed'] = None
            self.bot_intent['firepower'] = None

    # Bot State Management
    def get_my_id(self) -> int:
        if self.my_id is None:
            raise BotException(GAME_NOT_RUNNING_MSG)
        return self.my_id

    def set_my_id(self, my_id: int) -> None:
        self.my_id = my_id

    def get_teammate_ids(self) -> Set[int]:
        if self.my_id is None: # Check if game has started via my_id
            raise BotException(GAME_NOT_RUNNING_MSG)
        return self.teammate_ids

    def set_teammate_ids(self, teammate_ids: Set[int]) -> None:
        self.teammate_ids = teammate_ids

    def get_game_setup(self) -> GameSetup:
        if self.game_setup is None:
            raise BotException(GAME_NOT_RUNNING_MSG)
        return self.game_setup

    def set_game_setup(self, game_setup: GameSetup) -> None:
        self.game_setup = game_setup

    def get_initial_position(self) -> Optional[InitialPosition]:
        return self.initial_position

    def set_initial_position(self, initial_position: Optional[InitialPosition]) -> None:
        self.initial_position = initial_position

    def get_bot_intent(self) -> Dict[str, Any]:
        return self.bot_intent

    def get_current_tick_or_throw(self) -> TickEvent:
        if self.tick_event is None:
            raise BotException(TICK_NOT_AVAILABLE_MSG)
        return self.tick_event

    def set_tick_event(self, tick_event: TickEvent) -> None:
        self.tick_event = tick_event

    def get_current_tick_or_null(self) -> Optional[TickEvent]:
        return self.tick_event

    def get_tick_start_nano_time(self) -> int:
        if self.tick_start_nano_time is None:
            raise BotException(TICK_NOT_AVAILABLE_MSG)
        return self.tick_start_nano_time

    def set_tick_start_nano_time(self, tick_start_nano_time: int) -> None:
        self.tick_start_nano_time = tick_start_nano_time

    def add_events_from_tick(self, tick_event: TickEvent) -> None:
        self.event_queue.add_events_from_tick(tick_event)

    def get_time_left(self) -> int:
        passed_microseconds = (time.monotonic_ns() - self.get_tick_start_nano_time()) // 1000
        game_setup = self.get_game_setup()
        if game_setup.turn_timeout is None: # Should not happen if game setup is correct
             raise BotException("turn_timeout is not set in GameSetup")
        return game_setup.turn_timeout - passed_microseconds

    def enable_event_handling(self, enable: bool) -> None:
        if enable:
            self.event_handling_disabled_turn = 0
        else:
            self.event_handling_disabled_turn = self.get_current_tick_or_throw().turn_number

    def get_event_handling_disabled_turn(self) -> bool:
        # Important! Allow an additional turn so events like RoundStarted can be handled
        return self.event_handling_disabled_turn != 0 and \
               self.event_handling_disabled_turn < (self.get_current_tick_or_throw().turn_number - 1)

    def get_events(self) -> List[BotEvent]:
        turn_number = self.get_current_tick_or_throw().turn_number
        return self.event_queue.get_events(turn_number)

    def clear_events(self) -> None:
        self.event_queue.clear_events()

    def set_interruptible(self, interruptible: bool) -> None:
        self.event_queue.set_current_event_interruptible(interruptible)

    def dispatch_events(self, turn_number: int) -> None:
        try:
            self.event_queue.dispatch_events(turn_number)
        except Exception:
            traceback.print_exc()
            
    def set_stop_resume_listener(self, listener: StopResumeListenerABC) -> None:
        self.stop_resume_listener = listener

    def set_running(self, is_running: bool) -> None:
        self.is_running_atomic = is_running

    def is_running(self) -> bool:
        return self.is_running_atomic

    async def _runnable(self, bot: BotABC) -> None:
        self.set_running(True)
        self.enable_event_handling(True)
        try:
            # Assuming bot.run() is async. If not:
            # await asyncio.get_event_loop().run_in_executor(None, bot.run)
            await bot.run()

            while self.is_running():
                try:
                    # Assuming bot.go() is async. If not:
                    # await asyncio.get_event_loop().run_in_executor(None, bot.go)
                    await bot.go()
                except asyncio.CancelledError:
                    return # Task was cancelled
                except Exception as e: # Catch other exceptions during bot.go()
                    # Potentially log the error or handle specific bot exceptions
                    print(f"Exception in bot.go(): {e}") # Basic error logging
                    # Depending on desired behavior, may need to stop or continue
                    self.set_running(False) # Example: stop on error
                    return
        except asyncio.CancelledError:
            # This handles cancellation if bot.run() or the loop itself is cancelled
            pass # Task was cancelled
        except Exception as e:
            # Potentially log the error or handle specific bot exceptions
            print(f"Exception in bot.run() or main loop: {e}") # Basic error logging
            self.set_running(False) # Stop if unhandled exception in run()
        finally:
            self.enable_event_handling(False)

    def start_thread(self, bot: BotABC) -> None:
        if self.thread is not None:
            # Potentially handle if a thread/task is already running
            pass
        self.thread = asyncio.create_task(self._runnable(bot))

    def stop_thread(self) -> None:
        if not self.is_running():
            return
        self.set_running(False)
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
            web_socket_handler = WebSocketHandler(
                self, # base_bot_internals
                self.server_url,
                self.server_secret,
                self.base_bot, # The bot instance itself (IBaseBot)
                self.bot_info,
                self.bot_event_handlers,
                self.internal_event_handlers,
                self.closed_event
            )
            # The WebSocketHandler's connect method should establish the connection
            # and store the websocket client in self.socket if needed by handler,
            # or return it to be stored here. Assuming it stores it internally or this class gets it.
            # For now, let's assume WebSocketHandler.connect() handles storing the socket if needed by it.
            # If BaseBotInternals needs direct access to the socket, WebSocketHandler.connect() should return it.
            self.socket = await web_socket_handler.connect() # Assuming connect returns the socket

        except Exception as ex:
            raise BotException(f"Could not create web socket for URL: {self.server_url}", ex)

    async def start(self) -> None:
        await self._connect()
        await self.closed_event.wait()

    async def execute(self) -> None:
        if not self.is_running():
            return

        turn_number = self.get_current_tick_or_throw().turn_number
        if turn_number != self.last_execute_turn_number:
            self.last_execute_turn_number = turn_number
            self.dispatch_events(turn_number)
            await self._send_intent()
        
        await self._wait_for_next_turn(turn_number)

    async def _send_intent(self) -> None:
        self._render_graphics_to_bot_intent()
        self._transfer_std_out_to_bot_intent()

        if self.socket:
            try:
                json_intent = json.dumps(self.bot_intent)
                await self.socket.send(json_intent) # Assumes self.socket is WebsocketClientProtocol
                if 'teamMessages' in self.bot_intent:
                    self.bot_intent['teamMessages'] = []
            except Exception as e:
                # Handle potential errors during send, e.g., connection closed
                print(f"Error sending bot intent: {e}") # Basic logging
                # Consider if self.socket needs to be invalidated or reconnected
                # For now, just printing error.

    def _transfer_std_out_to_bot_intent(self) -> None:
        # Stdout/stderr redirection and inclusion in intent might be handled differently in Python
        # For now, this is a placeholder.
        pass

    def _render_graphics_to_bot_intent(self) -> None:
        # Graphics rendering will be addressed in a later step.
        pass

    async def _wait_for_next_turn(self, turn_number: int) -> None:
        self._stop_rogue_thread()
        try:
            async with self.next_turn_monitor:
                while (self.is_running() and
                       turn_number == self.get_current_tick_or_throw().turn_number and
                       self.thread == asyncio.current_task() and # Ensure this is the bot's main task
                       not asyncio.current_task().cancelled()):
                    await self.next_turn_monitor.wait()
        except asyncio.CancelledError:
            raise ThreadInterruptedException() # Propagate as specific interruption

    def _stop_rogue_thread(self) -> None:
        # In asyncio, tasks don't have a direct 'thread' equivalent accessible this way for comparison.
        # self.thread is the asyncio.Task object for the bot's main execution.
        # We check if the current task is the one we stored.
        if asyncio.current_task() != self.thread:
            raise ThreadInterruptedException()

    def set_fire(self, firepower: float) -> bool:
        if math.isnan(firepower):
            raise ValueError("'firepower' cannot be NaN")
        
        # Assuming self.base_bot.energy is updated elsewhere based on TickEvent
        current_energy = self.base_bot.energy 
        gun_heat = self.get_gun_heat()

        if current_energy < firepower or gun_heat > 0:
            return False
        
        self.bot_intent['firepower'] = firepower
        return True

    def get_gun_heat(self) -> float:
        return self.tick_event.bot_state.gun_heat if self.tick_event and self.tick_event.bot_state else 0.0

    def get_speed(self) -> float:
        return self.tick_event.bot_state.speed if self.tick_event and self.tick_event.bot_state else 0.0

    def set_turn_rate(self, turn_rate: float) -> None:
        if math.isnan(turn_rate):
            raise ValueError("'turn_rate' cannot be NaN")
        self.bot_intent['turnRate'] = MathUtil.clamp(turn_rate, -self.max_turn_rate, self.max_turn_rate)

    def set_gun_turn_rate(self, gun_turn_rate: float) -> None:
        if math.isnan(gun_turn_rate):
            raise ValueError("'gun_turn_rate' cannot be NaN")
        self.bot_intent['gunTurnRate'] = MathUtil.clamp(gun_turn_rate, -self.max_gun_turn_rate, self.max_gun_turn_rate)

    def set_radar_turn_rate(self, radar_turn_rate: float) -> None:
        if math.isnan(radar_turn_rate):
            raise ValueError("'radar_turn_rate' cannot be NaN")
        self.bot_intent['radarTurnRate'] = MathUtil.clamp(radar_turn_rate, -self.max_radar_turn_rate, self.max_radar_turn_rate)

    def set_target_speed(self, target_speed: float) -> None:
        if math.isnan(target_speed):
            raise ValueError("'target_speed' cannot be NaN")
        self.bot_intent['targetSpeed'] = MathUtil.clamp(target_speed, -self.max_speed, self.max_speed)

    def get_turn_rate(self) -> float:
        if self.bot_intent.get('turnRate') is not None:
            return self.bot_intent['turnRate']
        return self.tick_event.bot_state.turn_rate if self.tick_event and self.tick_event.bot_state else 0.0

    def get_gun_turn_rate(self) -> float:
        if self.bot_intent.get('gunTurnRate') is not None:
            return self.bot_intent['gunTurnRate']
        return self.tick_event.bot_state.gun_turn_rate if self.tick_event and self.tick_event.bot_state else 0.0

    def get_radar_turn_rate(self) -> float:
        if self.bot_intent.get('radarTurnRate') is not None:
            return self.bot_intent['radarTurnRate']
        return self.tick_event.bot_state.radar_turn_rate if self.tick_event and self.tick_event.bot_state else 0.0

    def get_max_speed(self) -> float:
        return self.max_speed

    def set_max_speed(self, max_speed: float) -> None:
        self.max_speed = MathUtil.clamp(max_speed, 0, MAX_SPEED)

    def get_max_turn_rate(self) -> float:
        return self.max_turn_rate

    def set_max_turn_rate(self, max_turn_rate: float) -> None:
        self.max_turn_rate = MathUtil.clamp(max_turn_rate, 0, MAX_TURN_RATE)

    def get_max_gun_turn_rate(self) -> float:
        return self.max_gun_turn_rate

    def set_max_gun_turn_rate(self, max_gun_turn_rate: float) -> None:
        self.max_gun_turn_rate = MathUtil.clamp(max_gun_turn_rate, 0, MAX_GUN_TURN_RATE)

    def get_max_radar_turn_rate(self) -> float:
        return self.max_radar_turn_rate

    def set_max_radar_turn_rate(self, max_radar_turn_rate: float) -> None:
        self.max_radar_turn_rate = MathUtil.clamp(max_radar_turn_rate, 0, MAX_RADAR_TURN_RATE)

    def get_new_target_speed(self, speed: float, distance: float) -> float:
        if distance < 0:
            return -self.get_new_target_speed(-speed, -distance)
        
        target_speed: float
        if math.isinf(distance):
            target_speed = self.max_speed
        else:
            target_speed = min(self.max_speed, self._get_max_speed_for_distance(distance))

        if speed >= 0:
            return MathUtil.clamp(target_speed, speed - self.abs_deceleration, speed + ACCELERATION)
        else: # speed < 0
            return MathUtil.clamp(target_speed, speed - ACCELERATION, speed + self._get_max_deceleration(-speed))

    def _get_max_speed_for_distance(self, distance: float) -> float: # Corresponds to Java's getMaxSpeed(distance)
        deceleration_time = max(1, math.ceil((math.sqrt((4 * 2 / self.abs_deceleration) * distance + 1) - 1) / 2))
        if math.isinf(deceleration_time):
            return MAX_SPEED
        
        deceleration_distance = (deceleration_time / 2) * (deceleration_time - 1) * self.abs_deceleration
        return ((deceleration_time - 1) * self.abs_deceleration) + ((distance - deceleration_distance) / deceleration_time)

    def _get_max_deceleration(self, speed: float) -> float: # Corresponds to Java's getMaxDeceleration(speed)
        deceleration_time = speed / self.abs_deceleration
        acceleration_time = 1 - deceleration_time
        return min(1, deceleration_time) * self.abs_deceleration + max(0, acceleration_time) * ACCELERATION

    def get_distance_traveled_until_stop(self, speed: float) -> float:
        speed = math.fabs(speed)
        distance = 0.0
        while speed > 0:
            speed = self.get_new_target_speed(speed, 0) # This will decelerate
            distance += speed
        return distance

    # Add other methods from BaseBotInternals.java as needed
    def add_condition(self, condition: Condition) -> bool:
        prev_len = len(self.conditions)
        self.conditions.add(condition)
        return len(self.conditions) > prev_len

    def remove_condition(self, condition: Condition) -> bool:
        try:
            prev_len = len(self.conditions)
            self.conditions.remove(condition)
            return len(self.conditions) < prev_len # Or simply return True if no error
        except KeyError:
            return False # Condition not found

    def set_stop(self, overwrite: bool) -> None:
        if not self.is_stopped or overwrite:
            self.is_stopped = True

            self.saved_target_speed = self.bot_intent.get('targetSpeed')
            self.saved_turn_rate = self.bot_intent.get('turnRate')
            self.saved_gun_turn_rate = self.bot_intent.get('gunTurnRate')
            self.saved_radar_turn_rate = self.bot_intent.get('radarTurnRate')

            self.bot_intent['targetSpeed'] = 0.0
            self.bot_intent['turnRate'] = 0.0
            self.bot_intent['gunTurnRate'] = 0.0
            self.bot_intent['radarTurnRate'] = 0.0

            if self.stop_resume_listener is not None:
                self.stop_resume_listener.on_stop()

    def set_resume(self) -> None:
        if self.is_stopped:
            self.bot_intent['targetSpeed'] = self.saved_target_speed
            self.bot_intent['turnRate'] = self.saved_turn_rate
            self.bot_intent['gunTurnRate'] = self.saved_gun_turn_rate
            self.bot_intent['radarTurnRate'] = self.saved_radar_turn_rate

            if self.stop_resume_listener is not None:
                self.stop_resume_listener.on_resume()
            
            self.is_stopped = False # Must be the last step

    def is_teammate(self, bot_id: int) -> bool:
        return bot_id in self.get_teammate_ids()

    def broadcast_team_message(self, message: 'Any') -> None:
        self.send_team_message(None, message)

    def send_team_message(self, teammate_id: Optional[int], message: Any) -> None:
        if teammate_id is not None and teammate_id not in self.get_teammate_ids():
            raise ValueError("No teammate was found with the specified 'teammate_id'")

        # Ensure 'teamMessages' key exists; _new_bot_intent should initialize it.
        team_messages_list = self.bot_intent.get('teamMessages')
        if team_messages_list is None: # Should not happen if _new_bot_intent is correct
            team_messages_list = []
            self.bot_intent['teamMessages'] = team_messages_list


        if len(team_messages_list) >= MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN:
            raise BotException(
                f"The maximum number team messages has already been reached: {MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN}")
        
        if message is None:
            raise ValueError("The 'message' of a team message cannot be null")

        json_message = json.dumps(message)
        if len(json_message.encode('utf-8')) > TEAM_MESSAGE_MAX_SIZE:
            raise ValueError(
                f"The team message is larger than the limit of {TEAM_MESSAGE_MAX_SIZE} bytes (compact JSON format)")

        team_message_dict = {
            'messageType': type(message).__name__, # Using class name for now
            'receiverId': teammate_id,
            'message': json_message
        }
        team_messages_list.append(team_message_dict)
        # self.bot_intent['teamMessages'] = team_messages_list # Not needed as list is modified in place

    # Color and Graphics
    def get_body_color(self) -> Optional[str]:
        return self.tick_event.bot_state.body_color if self.tick_event and self.tick_event.bot_state else None

    def get_turret_color(self) -> Optional[str]:
        return self.tick_event.bot_state.turret_color if self.tick_event and self.tick_event.bot_state else None

    def get_radar_color(self) -> Optional[str]:
        return self.tick_event.bot_state.radar_color if self.tick_event and self.tick_event.bot_state else None

    def get_bullet_color(self) -> Optional[str]:
        return self.tick_event.bot_state.bullet_color if self.tick_event and self.tick_event.bot_state else None

    def get_scan_color(self) -> Optional[str]:
        return self.tick_event.bot_state.scan_color if self.tick_event and self.tick_event.bot_state else None

    def get_tracks_color(self) -> Optional[str]:
        return self.tick_event.bot_state.tracks_color if self.tick_event and self.tick_event.bot_state else None

    def get_gun_color(self) -> Optional[str]:
        return self.tick_event.bot_state.gun_color if self.tick_event and self.tick_event.bot_state else None

    def set_body_color(self, color: Optional[str]) -> None:
        self.bot_intent['bodyColor'] = color

    def set_turret_color(self, color: Optional[str]) -> None:
        self.bot_intent['turretColor'] = color

    def set_radar_color(self, color: Optional[str]) -> None:
        self.bot_intent['radarColor'] = color

    def set_bullet_color(self, color: Optional[str]) -> None:
        self.bot_intent['bulletColor'] = color

    def set_scan_color(self, color: Optional[str]) -> None:
        self.bot_intent['scanColor'] = color

    def set_tracks_color(self, color: Optional[str]) -> None:
        self.bot_intent['tracksColor'] = color

    def set_gun_color(self, color: Optional[str]) -> None:
        self.bot_intent['gunColor'] = color

    def get_graphics(self) -> GraphicsState:
        return self.graphics_state

    def _render_graphics_to_bot_intent(self) -> None:
        current_tick = self.get_current_tick_or_null()
        if current_tick and current_tick.bot_state and current_tick.bot_state.is_debugging_enabled:
            svg_output = self.graphics_state.get_svg_output()
            self.bot_intent['debugGraphics'] = svg_output
            self.graphics_state.clear()
        else:
            # Ensure it's not set if debugging is off or tick not available
            self.bot_intent['debugGraphics'] = None


    # Bullet States
    def get_bullet_states(self) -> List[BulletState]:
        if self.tick_event and self.tick_event.bullet_states:
            return list(self.tick_event.bullet_states)
        return []

    # Server Handshake
    def get_server_handshake(self) -> ServerHandshake: # Renamed from _get_server_handshake
        if self.server_handshake is None:
            raise BotException(NOT_CONNECTED_TO_SERVER_MSG)
        return self.server_handshake

    def set_server_handshake(self, server_handshake: ServerHandshake) -> None:
        self.server_handshake = server_handshake
    
    def get_variant(self) -> str:
        return self.get_server_handshake().variant # type: ignore 

    def get_version(self) -> str:
        return self.get_server_handshake().version # type: ignore
    # ...
