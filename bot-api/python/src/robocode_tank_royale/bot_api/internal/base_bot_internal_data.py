from typing import Optional, Set

from ..graphics import SvgGraphics, GraphicsABC

from ..bot_exception import BotException
from ..bot_info import BotInfo
from ..events import Condition, TickEvent
from ..game_setup import GameSetup
from ..initial_position import InitialPosition

from robocode_tank_royale.schema import Message, BotIntent, ServerHandshake  

GAME_NOT_RUNNING_MSG = "Game is not running"
TICK_NOT_AVAILABLE_MSG = "Tick event is not available"
NOT_CONNECTED_TO_SERVER_MSG = "Not connected to server"


class BaseBotInternalData:
    """
    Internal data for the base bot.
    """

    def __init__(self, bot_info: Optional[BotInfo]):
        self.bot_info: Optional[BotInfo] = bot_info
        self.bot_intent: BotIntent = BotIntent(type=Message.Type.BOT_INTENT, team_messages=[])
        self._my_id: Optional[int] = None
        self._teammate_ids: Set[int] = set()
        self._game_setup: Optional[GameSetup] = None
        self._initial_position: Optional[InitialPosition] = None
        self._tick_event: Optional[TickEvent] = None
        self._tick_start_nano_time: Optional[int] = None
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
        if self._my_id is None: # Assuming if my_id is None, game is not running
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

    @property
    def current_tick_or_null(self) -> Optional[TickEvent]:
        return self._tick_event

    @property
    def tick_start_nano_time(self) -> int:
        if self._tick_start_nano_time is None:
            raise BotException(TICK_NOT_AVAILABLE_MSG)
        return self._tick_start_nano_time

    @tick_start_nano_time.setter
    def tick_start_nano_time(self, value: int):
        self._tick_start_nano_time = value

    @property
    def server_handshake(self) -> ServerHandshake:  
        if self._server_handshake is None:  
            raise BotException(NOT_CONNECTED_TO_SERVER_MSG)  
        return self._server_handshake  

    @server_handshake.setter
    def server_handshake(self, value: ServerHandshake):  
        self._server_handshake = value

    @property
    def conditions(self) -> Set[Condition]:
        return self._conditions

    @property
    def is_running(self) -> bool:
        return self._is_running_atomic

    @is_running.setter
    def is_running(self, value: bool):
        self._is_running_atomic = value

    @property
    def event_handling_disabled_turn(self) -> int:
        return self._event_handling_disabled_turn

    @event_handling_disabled_turn.setter
    def event_handling_disabled_turn(self, value: int):
        self._event_handling_disabled_turn = value
