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

        self._connections: Set[websockets.WebSocketServerProtocol] = set()
        self._lock = threading.RLock()  # Reentrant lock for thread safety

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
        self._bot_ready_event = threading.Event()  # Set when bot sends BotReady
        self._tick_event = threading.Event()
        self._bot_intent_event = threading.Event()
        # Initialize continue event to SET so intents flow through by default
        # Tests call reset_bot_intent_latch() to clear it and set up for capture
        self._bot_intent_continue_event = threading.Event()
        self._bot_intent_continue_event.set()  # Allow intents through by default

        # state captured
        # These are accessed from multiple threads and must be protected by _lock
        self._handshake = None
        self._bot_intent = None
        self._bot_intent_lock = threading.Lock()  # Separate lock for intent to reduce contention

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
        # Note: No cleanup needed - daemon thread will be killed on exit

    def stop(self) -> None:
        """
        Stop the mocked server.

        Note: Since the server thread is a daemon thread and we use os._exit(0)
        in conftest.py, we only need to stop the event loop. The daemon thread
        will be killed automatically when Python exits.
        """
        if not self._loop or self._loop.is_closed():
            return

        # Stop the event loop
        if self._loop.is_running():
            try:
                self._loop.call_soon_threadsafe(self._loop.stop)
            except RuntimeError:
                pass

    # Await helpers
    def await_bot_ready(self, timeout_ms: int = 1000) -> bool:
        start = time.time()
        if not self.await_bot_handshake(timeout_ms):
            print("await_bot_ready: await_bot_handshake timed out", flush=True)
            return False

        elapsed = int((time.time() - start) * 1000)
        remaining = max(0, timeout_ms - elapsed)
        if not self.await_game_started(remaining):
            print("await_bot_ready: await_game_started timed out", flush=True)
            return False

        elapsed = int((time.time() - start) * 1000)
        remaining = max(0, timeout_ms - elapsed)

        # Wait for bot to send BotReady before sending tick
        if not self._bot_ready_event.wait(remaining / 1000.0):
            print("await_bot_ready: bot_ready_event timed out", flush=True)
            return False

        elapsed = int((time.time() - start) * 1000)
        remaining = max(0, timeout_ms - elapsed)

        self._tick_event.clear()

        # Get the loop that the server is running on
        loop = self._loop
        if loop and loop.is_running():
            async def send_ticks():
                # Copy connections under lock, then release before async operations
                with self._lock:
                    connections = list(self._connections)
                    turn_number = self._turn_number
                    self._turn_number += len(connections)

                for conn in connections:
                    await self._send_tick(conn, turn_number)
                    turn_number += 1

            asyncio.run_coroutine_threadsafe(send_ticks(), loop)
        else:
            print("await_bot_ready: server loop not running", flush=True)
            return False

        if not self.await_tick(remaining):
            print("await_bot_ready: await_tick timed out", flush=True)
            return False

        return True

    def set_bot_state_and_await_tick(
        self,
        energy: Optional[float] = None,
        gun_heat: Optional[float] = None,
        speed: Optional[float] = None,
        direction: Optional[float] = None,
        gun_direction: Optional[float] = None,
        radar_direction: Optional[float] = None,
    ) -> bool:
        # Update state under lock
        with self._lock:
            if energy is not None:
                self._energy = energy
            if gun_heat is not None:
                self._gun_heat = gun_heat
            if speed is not None:
                self._speed = speed
            if direction is not None:
                self._direction = direction
            if gun_direction is not None:
                self._gun_direction = gun_direction
            if radar_direction is not None:
                self._radar_direction = radar_direction

        self._tick_event.clear()

        loop = self._loop
        if loop and loop.is_running():
            async def send_ticks():
                # Copy connections under lock, then release before async operations
                with self._lock:
                    connections = list(self._connections)
                    turn_number = self._turn_number
                    self._turn_number += len(connections)

                for conn in connections:
                    await self._send_tick(conn, turn_number)
                    turn_number += 1

            asyncio.run_coroutine_threadsafe(send_ticks(), loop)
        else:
            return False

        return self.await_tick(1000)

    def set_initial_bot_state(
        self,
        energy: Optional[float] = None,
        gun_heat: Optional[float] = None,
        speed: Optional[float] = None,
        direction: Optional[float] = None,
        gun_direction: Optional[float] = None,
        radar_direction: Optional[float] = None,
    ) -> None:
        """
        Set the initial bot state without sending a tick event.
        This is useful for setting up the bot state before the bot has sent its first intent.
        Unlike set_bot_state_and_await_tick(), this does not trigger a tick or wait for response.

        Args:
            energy: Bot energy (optional)
            gun_heat: Gun heat (optional)
            speed: Bot speed (optional)
            direction: Bot direction (optional)
            gun_direction: Gun direction (optional)
            radar_direction: Radar direction (optional)
        """
        with self._lock:
            if energy is not None:
                self._energy = energy
            if gun_heat is not None:
                self._gun_heat = gun_heat
            if speed is not None:
                self._speed = speed
            if direction is not None:
                self._direction = direction
            if gun_direction is not None:
                self._gun_direction = gun_direction
            if radar_direction is not None:
                self._radar_direction = radar_direction

    def await_connection(self, timeout_ms: int) -> bool:
        return self._opened_event.wait(timeout_ms / 1000.0)

    def await_bot_handshake(self, timeout_ms: int) -> bool:
        return self._bot_handshake_event.wait(timeout_ms / 1000.0)

    def await_game_started(self, timeout_ms: int) -> bool:
        return self._game_started_event.wait(timeout_ms / 1000.0)

    def await_tick(self, timeout_ms: int) -> bool:
        """Wait for the next tick event to be signaled."""
        return self._tick_event.wait(timeout_ms / 1000.0)

    def reset_tick_event(self) -> None:
        """Reset the tick event before awaiting a new tick."""
        self._tick_event.clear()

    def await_bot_intent(self, timeout_ms: int) -> bool:
        """Wait for bot intent with timeout. Matches Java's awaitBotIntent().

        First releases the continue event to allow the handler to proceed,
        then waits for the intent event to be signaled.
        """
        # Release the continue event (like Java: botIntentContinueLatch.countDown())
        self._bot_intent_continue_event.set()
        # Then wait for the intent event (like Java: botIntentLatch.await())
        return self._bot_intent_event.wait(timeout_ms / 1000.0)

    def reset_bot_intent_latch(self) -> None:
        """Reset the bot intent event. Call before triggering an intent you want to capture."""
        self._bot_intent_event.clear()
        self._bot_intent_continue_event.clear()
        with self._bot_intent_lock:
            self._bot_intent = None

    # Alias for backward compatibility
    def reset_bot_intent_event(self) -> None:
        """Alias for reset_bot_intent_latch for backward compatibility."""
        self.reset_bot_intent_latch()

    def get_bot_intent(self):
        """
        Get the most recently captured bot intent.
        Thread-safe accessor for the captured intent.
        """
        with self._bot_intent_lock:
            return self._bot_intent

    # Config setters used by tests (thread-safe)
    def set_energy(self, energy: float) -> None:
        with self._lock:
            self._energy = energy

    def get_energy(self) -> float:
        with self._lock:
            return self._energy

    def set_gun_heat(self, gun_heat: float) -> None:
        with self._lock:
            self._gun_heat = gun_heat

    def get_gun_heat(self) -> float:
        with self._lock:
            return self._gun_heat

    def get_speed(self) -> float:
        with self._lock:
            return self._speed

    def get_direction(self) -> float:
        with self._lock:
            return self._direction

    def get_gun_direction(self) -> float:
        with self._lock:
            return self._gun_direction

    def get_radar_direction(self) -> float:
        with self._lock:
            return self._radar_direction

    def get_handshake(self):
        """
        Get the captured bot handshake message.
        Thread-safe accessor.
        """
        with self._lock:
            return self._handshake

    def set_speed_increment(self, inc: float) -> None:
        with self._lock:
            self._speed_increment = inc

    def set_turn_increment(self, inc: float) -> None:
        with self._lock:
            self._turn_increment = inc

    def set_gun_turn_increment(self, inc: float) -> None:
        with self._lock:
            self._gun_turn_increment = inc

    def set_radar_turn_increment(self, inc: float) -> None:
        with self._lock:
            self._radar_turn_increment = inc

    def set_speed_min_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._speed_min_limit = v

    def set_speed_max_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._speed_max_limit = v

    def set_direction_min_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._direction_min_limit = v

    def set_direction_max_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._direction_max_limit = v

    def set_gun_direction_min_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._gun_direction_min_limit = v

    def set_gun_direction_max_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._gun_direction_max_limit = v

    def set_radar_direction_min_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._radar_direction_min_limit = v

    def set_radar_direction_max_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._radar_direction_max_limit = v

    # Property versions (0.35.0+ compatibility) - thread-safe
    @property
    def speed_min_limit(self) -> Optional[float]:
        with self._lock:
            return self._speed_min_limit

    @speed_min_limit.setter
    def speed_min_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._speed_min_limit = v

    @property
    def speed_max_limit(self) -> Optional[float]:
        with self._lock:
            return self._speed_max_limit

    @speed_max_limit.setter
    def speed_max_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._speed_max_limit = v

    @property
    def direction_min_limit(self) -> Optional[float]:
        with self._lock:
            return self._direction_min_limit

    @direction_min_limit.setter
    def direction_min_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._direction_min_limit = v

    @property
    def direction_max_limit(self) -> Optional[float]:
        with self._lock:
            return self._direction_max_limit

    @direction_max_limit.setter
    def direction_max_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._direction_max_limit = v

    @property
    def gun_direction_min_limit(self) -> Optional[float]:
        with self._lock:
            return self._gun_direction_min_limit

    @gun_direction_min_limit.setter
    def gun_direction_min_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._gun_direction_min_limit = v

    @property
    def gun_direction_max_limit(self) -> Optional[float]:
        with self._lock:
            return self._gun_direction_max_limit

    @gun_direction_max_limit.setter
    def gun_direction_max_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._gun_direction_max_limit = v

    @property
    def radar_direction_min_limit(self) -> Optional[float]:
        with self._lock:
            return self._radar_direction_min_limit

    @radar_direction_min_limit.setter
    def radar_direction_min_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._radar_direction_min_limit = v

    @property
    def radar_direction_max_limit(self) -> Optional[float]:
        with self._lock:
            return self._radar_direction_max_limit

    @radar_direction_max_limit.setter
    def radar_direction_max_limit(self, v: Optional[float]) -> None:
        with self._lock:
            self._radar_direction_max_limit = v

    # Internal server logic
    async def _handler(self, websocket: websockets.WebSocketServerProtocol):
        """
        WebSocket message handler following the Tank Royale protocol.

        Protocol sequence (per schema/schemas/README.md):
        1. Bot joining: connection -> ServerHandshake -> BotHandshake -> GameStartedEventForBot
        2. Bot ready: BotReady -> RoundStartedEvent -> TickEventForBot
        3. Running next turn: TickEventForBot -> BotIntent -> (state update) -> TickEventForBot

        Audit (2026-01-28): Verified against sequence diagrams in schema/schemas/README.md
        - Bot joining sequence: ✓ Correct
        - Bot ready sequence: ✓ Correct
        - Running next turn: ✓ Correct - intent is captured, state is updated, then tick is sent
        - Memory ordering: ✓ Fixed - intent is stored with lock before event is signaled
        """
        with self._lock:
            self._connections.add(websocket)
        try:
            # on open
            self._opened_event.set()
            await self._send_server_handshake(websocket)

            movement_reset_pending = False

            try:
                async for msg in websocket:
                    message: Message = from_json(msg)  # type: ignore
                    msg_type = getattr(message, "type", None)

                    if msg_type == "BotHandshake":
                        with self._lock:
                            self._handshake = message
                        self._bot_handshake_event.set()
                        await self._send_game_started(websocket)
                        self._game_started_event.set()

                    elif msg_type == "BotReady":
                        self._bot_ready_event.set()  # Signal that bot is ready
                        await self._send_round_started(websocket)
                        # We do NOT send initial tick here anymore if we want to control it via await_bot_ready
                        # But to stay compatible with existing tests that might not use await_bot_ready yet:
                        # Actually, Java/C# removed it from here to avoid races.
                        # Let's see.

                    elif msg_type == "BotIntent":
                        # Per schema/schemas/README.md "running-next-turn" sequence:
                        # Bot -> Server: bot-intent
                        # Server checks limits, updates state, sends tick

                        # Enforce limits (pre intent/state update)
                        with self._lock:
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

                        # Wait for test to call await_bot_intent() which sets the continue event
                        # This prevents feedback loop where tick->intent->tick->intent...
                        # Matches Java's awaitBotIntentContinueOrFail()
                        await self._await_bot_intent_continue()

                        # Store intent immediately
                        with self._bot_intent_lock:
                            self._bot_intent = message

                        # Signal that intent is available
                        self._bot_intent_event.set()

                        # Advance state before sending tick
                        with self._lock:
                            self._speed += self._speed_increment
                            self._direction += self._turn_increment
                            self._gun_direction += self._gun_turn_increment
                            self._radar_direction += self._radar_turn_increment
                            current_turn = self._turn_number
                            self._turn_number += 1

                        await self._send_tick(websocket, current_turn)
                        self._tick_event.set()

                        # Only reset movement after first intent following round start
                        if movement_reset_pending:
                            self._reset_movement_commands()
                            movement_reset_pending = False

            except websockets.exceptions.ConnectionClosed:
                # client closed connection
                pass
        finally:
            with self._lock:
                self._connections.remove(websocket)

    def _reset_movement_commands(self):
        # This method was incorrectly modifying the captured bot intent.
        # The captured intent should remain unchanged for test assertions.
        # If we need to reset server-side state for the next turn, we should use
        # separate internal variables, not modify the captured intent.
        # For now, this method is a no-op since we don't need to reset anything.
        pass

    async def _await_bot_intent_continue(self) -> None:
        """Wait for continue event to be set. Matches Java's awaitBotIntentContinueOrFail().

        Bridge threading.Event into asyncio loop using polling.
        CRITICAL: Do NOT use run_in_executor(None, ...) as it creates a default
        ThreadPoolExecutor that never shuts down and causes pytest to hang!

        Note: We do NOT clear the event here. The event stays set until
        reset_bot_intent_latch() is called. This allows intents to flow
        through during setup, and only blocks when test explicitly resets.
        """
        # Poll with small sleep instead of blocking executor
        while not self._bot_intent_continue_event.wait(timeout=0.01):
            await asyncio.sleep(0)  # Yield to event loop
        # Do NOT clear here - let reset_bot_intent_latch() control blocking

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
        # Read state atomically under lock
        with self._lock:
            energy = self._energy
            direction = self._direction
            gun_direction = self._gun_direction
            radar_direction = self._radar_direction
            speed = self._speed
            gun_heat = self._gun_heat

        # Get intent under its own lock
        with self._bot_intent_lock:
            bot_intent = self._bot_intent

        # Build bot state (outside locks - no async operations while holding locks)
        state = SchemaBotState(
            energy=energy,
            x=self.bot_x,
            y=self.bot_y,
            direction=direction,
            gun_direction=gun_direction,
            radar_direction=radar_direction,
            radar_sweep=self.bot_radar_sweep,
            speed=speed,
            turn_rate=self.bot_turn_rate,
            gun_turn_rate=self.bot_gun_turn_rate,
            radar_turn_rate=self.bot_radar_turn_rate,
            gun_heat=gun_heat,
            enemy_count=self.bot_enemy_count,
        )

        # Override turn rates from intent (if any)
        if bot_intent is not None:
            tr = getattr(bot_intent, "turn_rate", None)
            gtr = getattr(bot_intent, "gun_turn_rate", None)
            rtr = getattr(bot_intent, "radar_turn_rate", None)
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

        fp = getattr(bot_intent, "firepower", None) if bot_intent is not None else None
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
        self._tick_event.set()

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
