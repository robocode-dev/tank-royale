import asyncio
import threading
import socket
from typing import Optional, Set, List

import websockets

from robocode_tank_royale.bot_api.internal.json_util import to_json, from_json
from robocode_tank_royale.schema import (
    Message,
    ServerHandshake,
    GameStartedEventForBot,
    GameSetup,
    RoundStartedEvent,
    TickEventForBot,
    BotState as SchemaBotState,
    ScannedBotEvent,
    BulletFiredEvent as SchemaBulletFiredEvent,
    BulletState as SchemaBulletState,
)


class MockedServer:
    """
    Minimal mocked server for Python tests mirroring Java/.NET test scaffolding.

    Features:
    - Dynamic port and server_url property
    - Handshake: sends ServerHandshake on open; on BotHandshake, sends GameStartedEventForBot
    - On BotReady: sends RoundStartedEvent and initial TickEventForBot
    - Captures BotIntent; supports await/continue gating; applies increments and optional limits
    - Emits a ScannedBotEvent each tick; when firepower is set, also emits a BulletFiredEvent
    """

    # Static defaults (keep in sync with Java/.NET values)
    session_id: str = "123abc"
    name: str = "MockedServer"
    version: str = "1.0.0"
    variant: str = "Tank Royale"
    game_types: Set[str] = {"melee", "classic", "1v1"}
    my_id: int = 1

    game_type: str = "classic"
    arena_width: int = 800
    arena_height: int = 600
    number_of_rounds: int = 10
    gun_cooling_rate: float = 0.1
    max_inactivity_turns: int = 450
    turn_timeout: int = 30_000
    ready_timeout: int = 1_000_000

    bot_enemy_count: int = 7
    bot_energy: float = 99.7
    bot_x: float = 44.5
    bot_y: float = 721.34
    bot_direction: float = 120.1
    bot_gun_direction: float = 103.45
    bot_radar_direction: float = 253.3
    bot_radar_sweep: float = 13.5
    bot_speed: float = 8.0
    bot_turn_rate: float = 5.1
    bot_gun_turn_rate: float = 18.9
    bot_radar_turn_rate: float = 34.1
    bot_gun_heat: float = 7.6

    def __init__(self) -> None:
        # dynamic port
        self._port: int = self._find_available_port()
        self._server_url: str = f"ws://127.0.0.1:{self._port}"

        # runtime state
        self._turn_number: int = 1
        self._energy: float = self.bot_energy
        self._gun_heat: float = self.bot_gun_heat
        self._speed: float = self.bot_speed
        self._direction: float = self.bot_direction
        self._gun_direction: float = self.bot_gun_direction
        self._radar_direction: float = self.bot_radar_direction

        self._speed_increment: float = 0.0
        self._turn_increment: float = 0.0
        self._gun_turn_increment: float = 0.0
        self._radar_turn_increment: float = 0.0

        self._speed_min_limit: Optional[float] = None
        self._speed_max_limit: Optional[float] = None
        self._direction_min_limit: Optional[float] = None
        self._direction_max_limit: Optional[float] = None
        self._gun_direction_min_limit: Optional[float] = None
        self._gun_direction_max_limit: Optional[float] = None
        self._radar_direction_min_limit: Optional[float] = None
        self._radar_direction_max_limit: Optional[float] = None

        # sync primitives
        self._opened_event = threading.Event()
        self._bot_handshake_event = threading.Event()
        self._game_started_event = threading.Event()
        self._tick_event = threading.Event()
        self._bot_intent_event = threading.Event()
        self._bot_intent_continue_event = threading.Event()

        # state captured
        self.handshake = None
        self._bot_intent = None

        # server/loop/thread
        self._loop: Optional[asyncio.AbstractEventLoop] = None
        self._thread: Optional[threading.Thread] = None
        self._server: Optional[websockets.server.Serve] = None

    @property
    def server_url(self) -> str:
        return self._server_url

    @staticmethod
    def _find_available_port() -> int:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind(("127.0.0.1", 0))
            return s.getsockname()[1]

    # Control API (mirroring Java/.NET names semantically)
    def start(self) -> None:
        self._loop = asyncio.new_event_loop()
        self._thread = threading.Thread(target=self._run_loop, daemon=True)
        self._thread.start()

    def _run_loop(self) -> None:
        assert self._loop is not None
        asyncio.set_event_loop(self._loop)
        self._server = websockets.serve(self._handler, "127.0.0.1", self._port)
        self._loop.run_until_complete(self._server)
        self._loop.run_forever()

    def stop(self) -> None:
        if self._loop:
            self._loop.call_soon_threadsafe(self._loop.stop)
        if self._thread:
            self._thread.join(timeout=1.0)

    # Await helpers
    def await_connection(self, timeout_ms: int) -> bool:
        return self._opened_event.wait(timeout_ms / 1000.0)

    def await_bot_handshake(self, timeout_ms: int) -> bool:
        return self._bot_handshake_event.wait(timeout_ms / 1000.0)

    def await_game_started(self, timeout_ms: int) -> bool:
        return self._game_started_event.wait(timeout_ms / 1000.0)

    def await_tick(self, timeout_ms: int) -> bool:
        return self._tick_event.wait(timeout_ms / 1000.0)

    def await_bot_intent(self, timeout_ms: int) -> bool:
        # Allow next bot intent to continue processing
        self._bot_intent_continue_event.set()
        return self._bot_intent_event.wait(timeout_ms / 1000.0)

    # Config setters used by tests
    def set_energy(self, energy: float) -> None:
        self._energy = energy

    def set_gun_heat(self, gun_heat: float) -> None:
        self._gun_heat = gun_heat

    def set_speed_increment(self, inc: float) -> None:
        self._speed_increment = inc

    def set_turn_increment(self, inc: float) -> None:
        self._turn_increment = inc

    def set_gun_turn_increment(self, inc: float) -> None:
        self._gun_turn_increment = inc

    def set_radar_turn_increment(self, inc: float) -> None:
        self._radar_turn_increment = inc

    def set_speed_min_limit(self, v: float) -> None:
        self._speed_min_limit = v

    def set_speed_max_limit(self, v: float) -> None:
        self._speed_max_limit = v

    def set_direction_min_limit(self, v: float) -> None:
        self._direction_min_limit = v

    def set_direction_max_limit(self, v: float) -> None:
        self._direction_max_limit = v

    def set_gun_direction_min_limit(self, v: float) -> None:
        self._gun_direction_min_limit = v

    def set_gun_direction_max_limit(self, v: float) -> None:
        self._gun_direction_max_limit = v

    def set_radar_direction_min_limit(self, v: float) -> None:
        self._radar_direction_min_limit = v

    def set_radar_direction_max_limit(self, v: float) -> None:
        self._radar_direction_max_limit = v

    # Internal server logic
    async def _handler(self, websocket: websockets.WebSocketServerProtocol):
        # on open
        self._opened_event.set()
        await self._send_server_handshake(websocket)

        try:
            async for msg in websocket:
                message: Message = from_json(msg)  # type: ignore
                msg_type = getattr(message, "type", None)

                if msg_type == "BotHandshake":
                    self.handshake = message
                    self._bot_handshake_event.set()

                    await self._send_game_started(websocket)
                    self._game_started_event.set()

                elif msg_type == "BotReady":
                    await self._send_round_started(websocket)
                    await self._send_tick(websocket, self._turn_number)
                    self._turn_number += 1
                    self._tick_event.set()

                elif msg_type == "BotIntent":
                    # Enforce limits (pre intent/state update)
                    if self._speed_min_limit is not None and self._speed < self._speed_min_limit:
                        continue
                    if self._speed_max_limit is not None and self._speed > self._speed_max_limit:
                        continue

                    if self._direction_min_limit is not None and self._direction < self._direction_min_limit:
                        continue
                    if self._direction_max_limit is not None and self._direction > self._direction_max_limit:
                        continue

                    if self._gun_direction_min_limit is not None and self._gun_direction < self._gun_direction_min_limit:
                        continue
                    if self._gun_direction_max_limit is not None and self._gun_direction > self._gun_direction_max_limit:
                        continue

                    if self._radar_direction_min_limit is not None and self._radar_direction < self._radar_direction_min_limit:
                        continue
                    if self._radar_direction_max_limit is not None and self._radar_direction > self._radar_direction_max_limit:
                        continue

                    # Wait for external allowance to continue
                    await self._wait_for_intent_continue()

                    self._bot_intent = message
                    self._bot_intent_event.set()

                    await self._send_tick(websocket, self._turn_number)
                    self._turn_number += 1
                    self._tick_event.set()

                    # Advance state after sending tick
                    self._speed += self._speed_increment
                    self._direction += self._turn_increment
                    self._gun_direction += self._gun_turn_increment
                    self._radar_direction += self._radar_turn_increment

        except websockets.exceptions.ConnectionClosed:
            # client closed connection
            pass

    async def _wait_for_intent_continue(self) -> None:
        # Bridge threading.Event into asyncio loop
        loop = asyncio.get_running_loop()
        if self._bot_intent_continue_event.is_set():
            # Reset for next await
            self._bot_intent_continue_event.clear()
            return
        await loop.run_in_executor(None, self._bot_intent_continue_event.wait)
        self._bot_intent_continue_event.clear()

    async def _send_server_handshake(self, websocket) -> None:
        msg = ServerHandshake()
        msg.type = "ServerHandshake"
        msg.session_id = self.session_id
        msg.name = self.name
        msg.version = self.version
        msg.variant = self.variant
        msg.game_types = list(self.game_types)
        msg.game_setup = None
        await websocket.send(to_json(msg))

    async def _send_game_started(self, websocket) -> None:
        evt = GameStartedEventForBot()
        evt.type = "GameStartedEventForBot"
        evt.my_id = self.my_id

        gs = GameSetup()
        gs.game_type = self.game_type
        gs.arena_width = self.arena_width
        gs.arena_height = self.arena_height
        gs.number_of_rounds = self.number_of_rounds
        gs.gun_cooling_rate = self.gun_cooling_rate
        gs.max_inactivity_turns = self.max_inactivity_turns
        gs.turn_timeout = self.turn_timeout
        gs.ready_timeout = self.ready_timeout
        evt.game_setup = gs

        await websocket.send(to_json(evt))

    async def _send_round_started(self, websocket) -> None:
        evt = RoundStartedEvent()
        evt.type = "RoundStartedEvent"
        evt.round_number = 1
        await websocket.send(to_json(evt))

    async def _send_tick(self, websocket, turn_number: int) -> None:
        tick = TickEventForBot()
        tick.type = "TickEventForBot"
        tick.round_number = 1
        tick.turn_number = turn_number

        # Base state
        state = SchemaBotState()
        state.energy = self._energy
        state.x = self.bot_x
        state.y = self.bot_y
        state.direction = self._direction
        state.gun_direction = self._gun_direction
        state.radar_direction = self._radar_direction
        state.radar_sweep = self.bot_radar_sweep
        state.speed = self._speed
        # turn rates default then overridden by intent values if present
        state.turn_rate = self.bot_turn_rate
        state.gun_turn_rate = self.bot_gun_turn_rate
        state.radar_turn_rate = self.bot_radar_turn_rate
        state.gun_heat = self._gun_heat
        state.enemy_count = self.bot_enemy_count
        tick.bot_state = state

        # Override turn rates from intent (if any)
        if self._bot_intent is not None:
            tr = getattr(self._bot_intent, "turn_rate", None)
            gtr = getattr(self._bot_intent, "gun_turn_rate", None)
            rtr = getattr(self._bot_intent, "radar_turn_rate", None)
            if tr is not None:
                state.turn_rate = tr
            if gtr is not None:
                state.gun_turn_rate = gtr
            if rtr is not None:
                state.radar_turn_rate = rtr

        # Bullet states example
        b1 = self._create_bullet_state(1)
        b2 = self._create_bullet_state(2)
        tick.bullet_states = [b1, b2]

        # Events
        events: List[object] = []
        scanned = ScannedBotEvent()
        scanned.type = "ScannedBotEvent"
        scanned.direction = 45.0
        scanned.x = 134.56
        scanned.y = 256.7
        scanned.energy = 56.9
        scanned.speed = 9.6
        scanned.turn_number = 1
        scanned.scanned_bot_id = 2
        scanned.scanned_by_bot_id = 1
        events.append(scanned)

        fp = getattr(self._bot_intent, "firepower", None) if self._bot_intent is not None else None
        if fp is not None:
            bullet_evt = SchemaBulletFiredEvent()
            bullet_evt.type = "BulletFiredEvent"
            bullet_evt.bullet = self._create_bullet_state(99)
            events.append(bullet_evt)

        tick.events = events

        await websocket.send(to_json(tick))

    @staticmethod
    def _create_bullet_state(bid: int) -> SchemaBulletState:
        b = SchemaBulletState()
        b.bullet_id = bid
        b.x = 0.0
        b.y = 0.0
        b.owner_id = 0
        b.direction = 0.0
        b.power = 0.0
        return b
