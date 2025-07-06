import asyncio
import json
from typing import Any
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
from .json_util import to_json
from ..initial_position import InitialPosition
from ..bot_exception import BotException
from ..mapper.event_mapper import EventMapper
from ..mapper.game_setup_mapper import GameSetupMapper
from ..mapper.results_mapper import ResultsMapper
from robocode_tank_royale.schema import Message, ResultsForBot, TickEventForBot, BotReady



class WebSocketHandler:
    """
    Websocket handler for Robocode Tank Royale Bot API that handles websocket connections
    and messages from the server.
    """

    def __init__(
        self,
        base_bot_internal_data: BaseBotInternalData,
        server_url: str,
        server_secret: str,
        base_bot: BaseBotABC,
        bot_info: BotInfo,
        bot_event_handlers: BotEventHandlers,
        internal_event_handlers: InternalEventHandlers,
        closed_event: asyncio.Event,
    ):
        """Initialize the websocket handler."""
        self.base_bot_internal_data = base_bot_internal_data
        self.server_url = server_url
        self.server_secret = server_secret
        self.base_bot = base_bot
        self.bot_info = bot_info
        self.bot_event_handlers = bot_event_handlers
        self.internal_event_handlers = internal_event_handlers
        self.closed_event = closed_event
        self.websocket: None | websockets.ClientConnection = None

    async def connect(self):
        """Connect to the WebSocket server."""
        try:
            self.websocket = await websockets.connect(self.server_url)
            # Publish connected event
            self.bot_event_handlers.on_connected.publish(
                ConnectedEvent(self.server_url)
            )
            return self.websocket
        except Exception as e:
            self.bot_event_handlers.on_connection_error.publish(
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
        """Handle WebSocket close event."""
        del websocket  # Unused parameter, but kept for compatibility
        disconnected_event = DisconnectedEvent(self.server_url, True, code, reason)
        self.bot_event_handlers.on_disconnected.publish(disconnected_event)
        self.internal_event_handlers.on_disconnected.publish(disconnected_event)
        self.closed_event.set()

    async def on_error(self, websocket: websockets.ClientConnection, error: Exception):
        """Handle WebSocket error."""
        del websocket  # Unused parameter, but kept for compatibility
        self.bot_event_handlers.on_connection_error.publish(
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

    async def handle_tick(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a tick event from the server."""
        if self.base_bot_internal_data.event_handling_disabled_turn:
            return

        self.base_bot_internal_data.tick_start_nano_time = int(
            asyncio.get_event_loop().time() * 1_000_000_000
        )

        tick_event_for_bot = TickEventForBot(**json_msg)
        mapped_tick_event = EventMapper.map_tick_event(
            tick_event_for_bot, self.base_bot
        )

        self.base_bot_internal_data.tick_event = mapped_tick_event

        # The add_events_from_tick from BaseBotInternals used to also add individual events from the tick to event_queue
        # This logic needs to be preserved if EventQueue is still used in a similar manner.
        # For now, assuming EventQueue will source its events based on the new tick_event if needed,
        # or that this responsibility shifts elsewhere. The subtask description focuses on BaseBotInternalData.

        bot_intent = self.base_bot_internal_data.bot_intent
        if bot_intent.get("rescan") is not None and bot_intent.get("rescan"):  # type: ignore
            bot_intent["rescan"] = False  # type: ignore

        for (
            event
        ) in (
            mapped_tick_event.events
        ):  # mapped_tick_event.events should still be iterable
            self.internal_event_handlers.fire_event(event)

        # Trigger next turn (not tick-event!)
        self.internal_event_handlers.on_next_turn.publish(mapped_tick_event)

    async def handle_round_started(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a round started event from the server."""
        round_started_event = RoundStartedEvent(json_msg["roundNumber"])

        self.bot_event_handlers.on_round_started.publish(round_started_event)
        self.internal_event_handlers.on_round_started.publish(round_started_event)

    async def handle_round_ended(self, json_msg: Dict[Any, Any]):
        """Handle a round ended event from the server."""
        round_ended_event = RoundEndedEvent(
            json_msg["roundNumber"], json_msg["turnNumber"], json_msg["results"]
        )

        self.bot_event_handlers.on_round_ended.publish(round_ended_event)
        self.internal_event_handlers.on_round_ended.publish(round_ended_event)

    async def handle_game_started(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a game started event from the server."""
        assert self.websocket is not None, "WebSocket connection is not established."
        self.base_bot_internal_data.my_id = json_msg["myId"]

        teammate_ids = set(json_msg.get("teammateIds", []))
        self.base_bot_internal_data.teammate_ids = teammate_ids

        self.base_bot_internal_data.game_setup = GameSetupMapper.map(
            json_msg["gameSetup"]
        )

        initial_position = InitialPosition(
            json_msg["startX"], json_msg["startY"], json_msg["startDirection"]
        )
        self.base_bot_internal_data.initial_position = initial_position

        # Send ready signal
        await self.websocket.send(to_json(BotReady(type=Message.Type.BOT_READY)))

        self.bot_event_handlers.on_game_started.publish(
            GameStartedEvent(
                json_msg["myId"],
                initial_position,
                self.base_bot_internal_data.game_setup,
            )
        )

    async def handle_game_ended(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a game ended event from the server."""
        number_of_rounds = json_msg.get(
            "numberOfRounds", 0
        )  # Use get with default value for safety

        results = json_msg.get("results")
        results_for_bot = ResultsForBot(**results)  # type: ignore - schema code is generated and should be valid

        game_ended_event = GameEndedEvent()
        game_ended_event.number_of_rounds = number_of_rounds
        game_ended_event.results = ResultsMapper.map(results_for_bot)

        self.bot_event_handlers.on_game_ended.publish(game_ended_event)
        self.internal_event_handlers.on_game_ended.publish(game_ended_event)

    async def handle_game_aborted(self) -> None:
        """Handle a game aborted event from the server."""
        self.bot_event_handlers.on_game_aborted.publish(None)
        self.internal_event_handlers.on_game_aborted.publish(None)

    async def handle_skipped_turn(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a skipped turn event from the server."""
        if self.base_bot_internal_data.event_handling_disabled_turn:
            return

        skipped_turn_event = EventMapper.map_skipped_turn_event(**json_msg)
        self.bot_event_handlers.on_skipped_turn.publish(skipped_turn_event)

    async def handle_server_handshake(self, json_msg: Dict[Any, Any]) -> None:
        """Handle a server handshake from the server."""
        assert self.websocket is not None, "WebSocket connection is not established."
        self.base_bot_internal_data.server_handshake = json_msg  # type: ignore

        # Reply by sending bot handshake
        is_droid: bool = hasattr(self.base_bot, "is_droid") and self.base_bot.is_droid  # type: ignore
        assert isinstance(is_droid, bool), "is_droid must be a boolean value"

        # Create bot handshake message
        from ..internal.bot_handshake_factory import BotHandshakeFactory

        bot_handshake = BotHandshakeFactory.create(
            json_msg["sessionId"], self.bot_info, is_droid, self.server_secret
        )

        # Send handshake message
        await self.websocket.send(to_json(bot_handshake))
