import asyncio
import json
from typing import Any, Optional
import websockets
from typing import Dict

from ..base_bot_abc import BaseBotABC
from ..bot_info import BotInfo
from ..events import (
    ConnectedEvent,
    DisconnectedEvent,
    ConnectionErrorEvent,
    GameStartedEvent,
    GameEndedEvent,
    RoundStartedEvent,
    RoundEndedEvent,
)
from .base_bot_internal_data import BaseBotInternalData
from .bot_event_handlers import BotEventHandlers
from .internal_event_handlers import InternalEventHandlers
from .json_util import to_json, from_json
from ..initial_position import InitialPosition
from ..bot_exception import BotException
from ..mapper.event_mapper import EventMapper
from ..mapper.game_setup_mapper import GameSetupMapper
from ..mapper.results_mapper import ResultsMapper
from robocode_tank_royale.schema import (
    Message,
    TickEventForBot,
    BotReady,
    GameStartedEventForBot,
    GameEndedEventForBot,
    RoundEndedEventForBot,
    SkippedTurnEvent,
    ServerHandshake,
    RoundStartedEvent as RoundStartedEventForBot,
)


class WebSocketHandler:
    """
    Websocket handler for Robocode Tank Royale Bot API that handles websocket connections
    and messages from the server.
    """

    def __init__(
        self,
        base_bot_internal_data: BaseBotInternalData,
        server_url: str,
        server_secret: Optional[str],
        base_bot: BaseBotABC,
        bot_info: BotInfo,
        bot_event_handlers: BotEventHandlers,
        internal_event_handlers: InternalEventHandlers,
        closed_event: asyncio.Event,
        event_queue: 'EventQueue',
    ):
        """Initialize the websocket handler."""
        self.base_bot_internal_data = base_bot_internal_data
        self.server_url = server_url
        self.server_secret: Optional[str] = server_secret
        self.base_bot = base_bot
        self.bot_info = bot_info
        self.bot_event_handlers = bot_event_handlers
        self.internal_event_handlers = internal_event_handlers
        self.closed_event = closed_event
        self.event_queue = event_queue
        self.websocket: None | websockets.ClientConnection = None

    async def connect(self):
        """Connect to the WebSocket server."""
        try:
            self.websocket = await websockets.connect(self.server_url)
            # Publish connected event
            await self.bot_event_handlers.on_connected.publish(
                ConnectedEvent(self.server_url)
            )
            return self.websocket
        except Exception as e:
            await self.bot_event_handlers.on_connection_error.publish(
                ConnectionErrorEvent(self.server_url, e)
            )
            self.closed_event.set()
            raise

    async def disconnect(self, code: int = 1000, reason: str = ""):
        """Disconnect from the WebSocket server."""
        if self.websocket:
            await self.websocket.close(code, reason)

    async def on_close(
        self, websocket: websockets.ClientConnection, code: int, reason: str
    ) -> None:
        """Handle WebSocket close event.""" # Unused parameter, but kept for compatibility
        # Publish and await on both event handlers in parallel
        disconnected_event = DisconnectedEvent(self.server_url, True, code, reason)
        await asyncio.gather(
            self.bot_event_handlers.on_disconnected.publish(disconnected_event),
            self.internal_event_handlers.on_disconnected.publish(disconnected_event)
        )
        self.closed_event.set()

    async def on_error(self, websocket: websockets.ClientConnection, error: Exception):
        """Handle WebSocket error."""
        del websocket  # Unused parameter, but kept for compatibility
        await self.bot_event_handlers.on_connection_error.publish(
            ConnectionErrorEvent(self.server_url, error)
        )
        self.closed_event.set()

    async def receive_messages(self):
        """Main loop for receiving messages from the WebSocket server."""
        assert self.websocket is not None, "WebSocket connection is not established."
        try:
            async for message in self.websocket:
                if isinstance(message, bytes):
                    message = message.decode("utf-8")
                assert isinstance(message, str), "Received message is not a string."
                await self.process_message(message)
        except websockets.exceptions.ConnectionClosed as e:
            assert e.rcvd is not None, "ConnectionClosed without received data."
            await self.on_close(self.websocket, e.rcvd.code, e.rcvd.reason)
        except Exception as e:
            print(f'Unexpected error: {e}')
            await self.on_error(self.websocket, e)

    async def process_message(self, message: str):
        """Process the received WebSocket message."""
        json_msg = json.loads(message)

        if "type" in json_msg:
            msg_type = json_msg["type"]

            if msg_type == "TickEventForBot":
                await self.handle_tick(json_msg)
            elif msg_type == "RoundStartedEvent":
                await self.handle_round_started(json_msg)
            elif msg_type == "RoundEndedEventForBot":
                await self.handle_round_ended(json_msg)
            elif msg_type == "GameStartedEventForBot":
                await self.handle_game_started(json_msg)
            elif msg_type == "GameEndedEventForBot":
                await self.handle_game_ended(json_msg)
            elif msg_type == "SkippedTurnEvent":
                await self.handle_skipped_turn(json_msg)
            elif msg_type == "ServerHandshake":
                await self.handle_server_handshake(json_msg)
            elif msg_type == "GameAbortedEvent":
                await self.handle_game_aborted()
            else:
                raise BotException(f"Unsupported WebSocket message type: {msg_type}")

    def _is_event_handling_disabled(self, current_turn: int) -> bool:
        disabled_turn = self.base_bot_internal_data.event_handling_disabled_turn
        return disabled_turn != 0 and disabled_turn < (int(current_turn) - 1)

    async def handle_tick(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a tick event from the server."""
        # Determine turn number early to apply correct disabled-handling semantics
        turn_number = json_msg.get("turn_number") or json_msg.get("turnNumber")
        if turn_number is not None and self._is_event_handling_disabled(int(turn_number)):
            return

        self.base_bot_internal_data.tick_start_nano_time = int(
            asyncio.get_event_loop().time() * 1_000_000_000
        )

        tick_event_for_bot: TickEventForBot = from_json(json_msg)  # type: ignore
        mapped_tick_event = EventMapper.map_tick_event(
            tick_event_for_bot, self.base_bot
        )

        self.base_bot_internal_data.tick_event = mapped_tick_event

        # Stage events from this tick into the event queue (Java parity)
        self.event_queue.add_events_from_tick(mapped_tick_event)

        bot_intent = self.base_bot_internal_data.bot_intent
        if bot_intent.rescan is not None and bot_intent.rescan:
            bot_intent.rescan = False

        # mapped_tick_event.events should still be iterable
        await asyncio.gather(
            *(
                self.internal_event_handlers.fire_event(event)
                for event in mapped_tick_event.events
            )
        )

        # Trigger next turn (not tick-event!)
        await self.internal_event_handlers.on_next_turn.publish(mapped_tick_event)

    async def handle_round_started(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a round started event from the server."""
        schema_evt: RoundStartedEventForBot = from_json(json_msg)  # type: ignore
        round_started_event = RoundStartedEvent(schema_evt.round_number)

        await asyncio.gather(
            self.bot_event_handlers.on_round_started.publish(round_started_event),
            self.internal_event_handlers.on_round_started.publish(round_started_event),
        )

    async def handle_round_ended(self, json_msg: Dict[Any, Any]):
        """Handle a round ended event from the server."""
        schema_evt: RoundEndedEventForBot = from_json(json_msg)  # type: ignore
        results = ResultsMapper.map(schema_evt.results)
        round_ended_event = RoundEndedEvent(
            schema_evt.round_number, schema_evt.turn_number, results
        )
        await asyncio.gather(
            self.bot_event_handlers.on_round_ended.publish(round_ended_event),
            self.internal_event_handlers.on_round_ended.publish(round_ended_event),
        )

    async def handle_game_started(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a game started event from the server."""
        assert self.websocket is not None, "WebSocket connection is not established."
        game_started_event: GameStartedEventForBot = from_json(json_msg)  # type: ignore
        print(game_started_event)
        self.base_bot_internal_data.my_id = game_started_event.my_id

        if game_started_event.teammate_ids is not None:
            self.base_bot_internal_data.teammate_ids = set(
                id for id in game_started_event.teammate_ids if id is not None
            )

        self.base_bot_internal_data.game_setup = GameSetupMapper.map(
            game_started_event.game_setup
        )

        initial_position = InitialPosition(
            game_started_event.start_x,
            game_started_event.start_y,
            game_started_event.start_direction,
        )
        self.base_bot_internal_data.initial_position = initial_position

        # Send ready signal
        await self.websocket.send(to_json(BotReady(type=Message.Type.BOT_READY)))

        await self.bot_event_handlers.on_game_started.publish(
            GameStartedEvent(
                game_started_event.my_id,
                initial_position,
                self.base_bot_internal_data.game_setup,
            )
        )

    async def handle_game_ended(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a game ended event from the server."""
        schema_evt: GameEndedEventForBot = from_json(json_msg)  # type: ignore

        game_ended_event = GameEndedEvent()
        game_ended_event.number_of_rounds = schema_evt.number_of_rounds
        game_ended_event.results = ResultsMapper.map(schema_evt.results)

        await asyncio.gather(
            self.bot_event_handlers.on_game_ended.publish(game_ended_event),
            self.internal_event_handlers.on_game_ended.publish(game_ended_event),
        )

    async def handle_game_aborted(self) -> None:
        """Handle a game aborted event from the server."""
        await asyncio.gather(
            self.bot_event_handlers.on_game_aborted.publish(None),
            self.internal_event_handlers.on_game_aborted.publish(None),
        )

    async def handle_skipped_turn(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a skipped turn event from the server."""
        turn_number = json_msg.get("turn_number") or json_msg.get("turnNumber")
        if turn_number is not None and self._is_event_handling_disabled(int(turn_number)):
            return

        schema_evt: SkippedTurnEvent = from_json(json_msg)  # type: ignore
        skipped_turn_event = EventMapper.map_skipped_turn_event(schema_evt)
        await self.bot_event_handlers.on_skipped_turn.publish(skipped_turn_event)

    async def handle_server_handshake(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a server handshake from the server."""
        assert self.websocket is not None, "WebSocket connection is not established."
        server_handshake: ServerHandshake = from_json(json_msg)  # type: ignore
        self.base_bot_internal_data.server_handshake = server_handshake

        # Reply by sending bot handshake
        # Infer droid status by marker interface inheritance (Java parity), with fallback to explicit flag for backward compatibility
        try:
            from ..droid_abc import DroidABC  # type: ignore
        except Exception:
            DroidABC = None  # type: ignore
        is_droid: bool = False
        if 'DroidABC' in locals() and DroidABC is not None and isinstance(self.base_bot, DroidABC):  # type: ignore
            is_droid = True
        elif hasattr(self.base_bot, "is_droid"):
            # Allow legacy bots explicitly setting the flag
            is_droid = bool(getattr(self.base_bot, "is_droid"))
        assert isinstance(is_droid, bool), "is_droid must be a boolean value"

        # Create bot handshake message
        from ..internal.bot_handshake_factory import BotHandshakeFactory

        bot_handshake = BotHandshakeFactory.create(
            server_handshake.session_id, self.bot_info, is_droid, self.server_secret
        )

        # Send handshake message
        # Ensure backward compatibility for tests expecting 'session_id' (snake_case)
        payload_str = to_json(bot_handshake)
        try:
            payload = json.loads(payload_str)
            if "sessionId" in payload and "session_id" not in payload:
                payload["session_id"] = payload["sessionId"]
            await self.websocket.send(json.dumps(payload))
        except Exception:
            # Fallback to original payload if any unexpected error occurs
            await self.websocket.send(payload_str)
