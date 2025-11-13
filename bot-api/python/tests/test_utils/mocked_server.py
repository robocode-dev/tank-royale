import asyncio
import threading
import socket
import time
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
    BotDeathEvent as SchemaBotDeathEvent,
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
        self._server_started_event = threading.Event()
        self._bot_handshake_event = threading.Event()
        self._game_started_event = threading.Event()
        self._tick_event = threading.Event()
        self._bot_intent_event = threading.Event()
        self._bot_intent_continue_event = threading.Event()

        # state captured
        self.handshake = None
        self._bot_intent = None

        # optional injections
        self._self_death_turn: Optional[int] = None

        # server/loop/thread
        self._loop: Optional[asyncio.AbstractEventLoop] = None
        self._thread: Optional[threading.Thread] = None
        self._server: Optional[websockets.serve] = None

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
        # Wait until the server is accepting connections to avoid race conditions
        self._server_started_event.wait(timeout=2.0)

    def _run_loop(self) -> None:
        assert self._loop is not None
        asyncio.set_event_loop(self._loop)

        async def _start_server():
            # Create the server inside a running event loop context using modern API
            self._server = await websockets.serve(self._handler, "127.0.0.1", self._port)
            # Signal that the server is ready to accept connections
            self._server_started_event.set()

        # Start the server while the loop is running
        self._loop.run_until_complete(_start_server())
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

        movement_reset_pending = False

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
                    movement_reset_pending = True  # defer movement reset until after first intent

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

                    # Only reset movement after first intent following round start
                    if movement_reset_pending:
                        self._reset_movement_commands()
                        movement_reset_pending = False

        except websockets.exceptions.ConnectionClosed:
            # client closed connection
            pass

    def _reset_movement_commands(self):
        # This should match the Java/.NET logic
        if self._bot_intent:
            self._bot_intent.turn_rate = None
            self._bot_intent.gun_turn_rate = None
            self._bot_intent.radar_turn_rate = None
            self._bot_intent.target_speed = None
            self._bot_intent.firepower = None

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
        # Build ServerHandshake using required constructor args per generated schema
        msg = ServerHandshake(
            session_id=self.session_id,
            variant=self.variant,
            version=self.version,
            game_types=list(self.game_types),
            type=Message.Type.SERVER_HANDSHAKE,
            name=self.name,
            game_setup=None,
        )
        await websocket.send(to_json(msg))

    async def _send_game_started(self, websocket) -> None:
        # Build GameSetup with required constructor params; use unlocked flags and defaults
        gs = GameSetup(
            game_type=self.game_type,
            arena_width=self.arena_width,
            is_arena_width_locked=False,
            arena_height=self.arena_height,
            is_arena_height_locked=False,
            min_number_of_participants=2,
            is_min_number_of_participants_locked=False,
            is_max_number_of_participants_locked=False,
            number_of_rounds=self.number_of_rounds,
            is_number_of_rounds_locked=False,
            gun_cooling_rate=self.gun_cooling_rate,
            is_gun_cooling_rate_locked=False,
            max_inactivity_turns=self.max_inactivity_turns,
            is_max_inactivity_turns_locked=False,
            turn_timeout=self.turn_timeout,
            is_turn_timeout_locked=False,
            ready_timeout=self.ready_timeout,
            is_ready_timeout_locked=False,
            default_turns_per_second=30,
            max_number_of_participants=None,
        )
        # Build GameStartedEventForBot
        evt = GameStartedEventForBot(
            my_id=self.my_id,
            game_setup=gs,
            type=Message.Type.GAME_STARTED_EVENT_FOR_BOT,
            start_x=None,
            start_y=None,
            start_direction=None,
            teammate_ids=None,
        )
        await websocket.send(to_json(evt))

    async def _send_round_started(self, websocket) -> None:
        evt = RoundStartedEvent(
            round_number=1,
            type=Message.Type.ROUND_STARTED_EVENT,
        )
        await websocket.send(to_json(evt))

    async def _send_tick(self, websocket, turn_number: int) -> None:
        # Build bot state
        state = SchemaBotState(
            energy=self._energy,
            x=self.bot_x,
            y=self.bot_y,
            direction=self._direction,
            gun_direction=self._gun_direction,
            radar_direction=self._radar_direction,
            radar_sweep=self.bot_radar_sweep,
            speed=self._speed,
            turn_rate=self.bot_turn_rate,
            gun_turn_rate=self.bot_gun_turn_rate,
            radar_turn_rate=self.bot_radar_turn_rate,
            gun_heat=self._gun_heat,
            enemy_count=self.bot_enemy_count,
        )

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
        bullet_states = [b1, b2]

        # Events
        events: List[object] = []
        scanned = ScannedBotEvent(
            scanned_by_bot_id=1,
            scanned_bot_id=2,
            energy=56.9,
            x=134.56,
            y=256.7,
            direction=45.0,
            speed=9.6,
            turn_number=1,
            type=Message.Type.SCANNED_BOT_EVENT,
        )
        events.append(scanned)

        # Inject self-death event if configured for this turn
        if self._self_death_turn is not None and turn_number == self._self_death_turn:
            events.append(
                SchemaBotDeathEvent(
                    victim_id=self.my_id,
                    turn_number=turn_number,
                    type=Message.Type.BOT_DEATH_EVENT,
                )
            )

        fp = getattr(self._bot_intent, "firepower", None) if self._bot_intent is not None else None
        if fp is not None:
            bullet_evt = SchemaBulletFiredEvent(
                bullet=self._create_bullet_state(99),
                turn_number=1,
                type=Message.Type.BULLET_FIRED_EVENT,
            )
            events.append(bullet_evt)

        tick = TickEventForBot(
            round_number=1,
            bot_state=state,
            bullet_states=bullet_states,
            events=events,
            turn_number=turn_number,
            type=Message.Type.TICK_EVENT_FOR_BOT,
        )

        await websocket.send(to_json(tick))

    @staticmethod
    def _create_bullet_state(bid: int) -> SchemaBulletState:
        return SchemaBulletState(
            bullet_id=bid,
            owner_id=0,
            power=0.0,
            x=0.0,
            y=0.0,
            direction=0.0,
        )

    # Test helpers (API)
    def set_self_death_on_turn(self, turn_number: int | None) -> None:
        self._self_death_turn = turn_number
