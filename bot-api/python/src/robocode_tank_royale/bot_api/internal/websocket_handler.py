import asyncio
import json
import websockets
import time  # Added import
from typing import Dict, Set, Any, Optional, Union

from robocode_tank_royale.bot_api import BaseBotABC
from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.events import (
    ConnectedEvent,
    DisconnectedEvent,
    ConnectionErrorEvent,
    GameStartedEvent,
    GameEndedEvent,
    RoundStartedEvent,
    RoundEndedEvent,
    SkippedTurnEvent
)
from robocode_tank_royale.bot_api.internal.bot_event_handlers import BotEventHandlers
from robocode_tank_royale.bot_api.internal.internal_event_handlers import InternalEventHandlers
from robocode_tank_royale.bot_api.initial_position import InitialPosition
from robocode_tank_royale.bot_api.bot_exception import BotException
from robocode_tank_royale.bot_api.mapper.event_mapper import EventMapper
from robocode_tank_royale.bot_api.mapper.game_setup_mapper import GameSetupMapper
from robocode_tank_royale.bot_api.mapper.results_mapper import ResultsMapper
from robocode_tank_royale.schema import ResultsForBot, TickEventForBot, ServerHandshake # Added ServerHandshake


class WebSocketHandler:
    """
    Websocket handler for Robocode Tank Royale Bot API that handles websocket connections
    and messages from the server.
    """

    def __init__(
            self,
            base_bot_internals,
            server_url: str,
            server_secret: str,
            base_bot: BaseBotABC,
            bot_info: BotInfo,
            bot_event_handlers: BotEventHandlers,
            internal_event_handlers: InternalEventHandlers,
            closed_event: asyncio.Event):
        """Initialize the websocket handler."""
        self.base_bot_internals = base_bot_internals
        self.server_url = server_url
        self.server_secret = server_secret
        self.base_bot = base_bot
        self.bot_info = bot_info
        self.bot_event_handlers = bot_event_handlers
        self.internal_event_handlers = internal_event_handlers
        self.closed_event = closed_event
        self.websocket = None

    async def connect(self):
        """Connect to the WebSocket server."""
        try:
            self.websocket = await websockets.connect(self.server_url)
            # Publish connected event
            self.bot_event_handlers.on_connected.publish(ConnectedEvent(self.server_url))
            return self.websocket
        except Exception as e:
            self.bot_event_handlers.on_connection_error.publish(ConnectionErrorEvent(self.server_url, e))
            self.closed_event.set()
            raise

    async def disconnect(self, code: int = 1000, reason: str = ""):
        """Disconnect from the WebSocket server."""
        if self.websocket:
            await self.websocket.close(code, reason)

    async def on_close(self, websocket, code: int, reason: str):
        """Handle WebSocket close event."""
        disconnected_event = DisconnectedEvent(self.server_url, True, code, reason)
        self.bot_event_handlers.on_disconnected.publish(disconnected_event)
        self.internal_event_handlers.on_disconnected.publish(disconnected_event)
        self.closed_event.set()

    async def on_error(self, websocket, error: Exception):
        """Handle WebSocket error."""
        self.bot_event_handlers.on_connection_error.publish(ConnectionErrorEvent(self.server_url, error))
        self.closed_event.set()

    async def receive_messages(self):
        """Main loop for receiving messages from the WebSocket server."""
        try:
            async for message in self.websocket:
                await self.process_message(message)
        except websockets.exceptions.ConnectionClosed as e:
            await self.on_close(self.websocket, e.code, e.reason)
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

    async def handle_tick(self, json_msg: Dict):
        """Handle a tick event from the server."""
        if self.base_bot_internals.get_event_handling_disabled_turn():
            return

        self.base_bot_internals.set_tick_start_nano_time(time.monotonic_ns()) # Changed to time.monotonic_ns()

        tick_event = TickEventForBot(**json_msg)  # Assuming TickEventForBot can be constructed from json_msg
        mapped_tick_event = EventMapper.map_tick_event(tick_event, self.base_bot)

        self.base_bot_internals.add_events_from_tick(mapped_tick_event)

        bot_intent = self.base_bot_internals.get_bot_intent() # This is a dictionary
        if bot_intent.get('rescan'): # Check if 'rescan' key exists and is true
            bot_intent['rescan'] = False # Set 'rescan' to False

        self.base_bot_internals.set_tick_event(mapped_tick_event)

        for event in mapped_tick_event.events:
            self.internal_event_handlers.fire_event(event)

        # Trigger next turn (not tick-event!)
        self.internal_event_handlers.on_next_turn.publish(mapped_tick_event)

    async def handle_round_started(self, json_msg: Dict):
        """Handle a round started event from the server."""
        round_started_event = RoundStartedEvent(json_msg["roundNumber"])

        self.bot_event_handlers.on_round_started.publish(round_started_event)
        self.internal_event_handlers.on_round_started.publish(round_started_event)

    async def handle_round_ended(self, json_msg: Dict):
        """Handle a round ended event from the server."""
        round_ended_event = RoundEndedEvent(
            json_msg["roundNumber"], json_msg["turnNumber"], json_msg["results"])

        self.bot_event_handlers.on_round_ended.publish(round_ended_event)
        self.internal_event_handlers.on_round_ended.publish(round_ended_event)

    async def handle_game_started(self, json_msg: Dict):
        """Handle a game started event from the server."""
        self.base_bot_internals.set_my_id(json_msg["myId"])

        teammate_ids = set(json_msg.get("teammateIds", []))
        self.base_bot_internals.set_teammate_ids(teammate_ids)

        self.base_bot_internals.set_game_setup(GameSetupMapper.map(json_msg["gameSetup"]))

        initial_position = InitialPosition(
            json_msg["startX"],
            json_msg["startY"],
            json_msg["startDirection"]
        )
        self.base_bot_internals.set_initial_position(initial_position)

        # Send ready signal
        ready_msg = {
            "type": "BotReady"
        }
        await self.websocket.send(json.dumps(ready_msg))

        self.bot_event_handlers.on_game_started.publish(
            GameStartedEvent(json_msg["myId"], initial_position, self.base_bot_internals.get_game_setup()))

    async def handle_game_ended(self, json_msg: Dict):
        """Handle a game ended event from the server."""
        number_of_rounds = json_msg.get("numberOfRounds", 0)  # Use get with default value for safety

        results = json_msg.get("results")
        results_for_bot = ResultsForBot(**results)

        game_ended_event = GameEndedEvent()
        game_ended_event.number_of_rounds = number_of_rounds
        game_ended_event.results = ResultsMapper.map(results_for_bot)

        self.bot_event_handlers.on_game_ended.publish(game_ended_event)
        self.internal_event_handlers.on_game_ended.publish(game_ended_event)

    async def handle_game_aborted(self):
        """Handle a game aborted event from the server."""
        self.bot_event_handlers.on_game_aborted.publish(None)
        self.internal_event_handlers.on_game_aborted.publish(None)

    async def handle_skipped_turn(self, json_msg: Dict):
        """Handle a skipped turn event from the server."""
        if self.base_bot_internals.get_event_handling_disabled_turn():
            return

        skipped_turn_event = EventMapper.map_skipped_turn_event(**json_msg)
        self.bot_event_handlers.on_skipped_turn.publish(skipped_turn_event)

    async def handle_server_handshake(self, json_msg: Dict):
        """Handle a server handshake from the server."""
        server_handshake_obj = ServerHandshake(**json_msg)
        self.base_bot_internals.set_server_handshake(server_handshake_obj)

        # Reply by sending bot handshake
        is_droid = hasattr(self.base_bot, 'is_droid') and self.base_bot.is_droid

        # Create bot handshake message
        from robocode_tank_royale.bot_api.internal.bot_handshake_factory import create as create_handshake
        bot_handshake = create_handshake(
            json_msg["sessionId"],
            self.bot_info,
            is_droid,
            self.server_secret
        )

        # Send handshake message
        await self.websocket.send(json.dumps(bot_handshake))
