import math
import threading
import unittest

from robocode_tank_royale.bot_api import BotInfo, Bot
from robocode_tank_royale.bot_api.constants import (
    MAX_TURN_RATE,
    MAX_GUN_TURN_RATE,
    MAX_RADAR_TURN_RATE,
    MAX_SPEED,
)
from test_utils.mocked_server import MockedServer


class TestCommandsMovement(unittest.TestCase):
    def setUp(self) -> None:
        self.server = MockedServer()
        self.server.start()
        self._bot_thread: threading.Thread | None = None

    def tearDown(self) -> None:
        self.server.stop()
        if self._bot_thread is not None:
            self._bot_thread.join(timeout=1.0)

    def _start_bot(self, bot: Bot) -> None:
        self._bot_thread = threading.Thread(target=bot.start, daemon=True)
        self._bot_thread.start()

    def test_TR_API_CMD_001_movement_commands_clamped_in_intent(self):
        """TR-API-CMD-001 Movement commands: setting movement rates/speed updates next intent and clamps to limits"""
        # Arrange
        # Ensure all movement limits are unset so intent is always accepted
        self.server.speed_min_limit = None
        self.server.speed_max_limit = None
        self.server.direction_min_limit = None
        self.server.direction_max_limit = None
        self.server.gun_direction_min_limit = None
        self.server.gun_direction_max_limit = None
        self.server.radar_direction_min_limit = None
        self.server.radar_direction_max_limit = None
        bot = Bot(
            bot_info=BotInfo(name="CmdBot", version="1.0", authors=["Tester"]),
            server_url=self.server.server_url,
        )
        self._start_bot(bot)
        if not self.server.await_game_started(2000):
            self.fail("Timeout waiting for game started")

        # Act: set values beyond limits to verify clamping
        bot.turn_rate = 999  # > MAX_TURN_RATE
        bot.gun_turn_rate = -999  # < -MAX_GUN_TURN_RATE
        bot.radar_turn_rate = 1000  # > MAX_RADAR_TURN_RATE
        bot.target_speed = 123  # > MAX_SPEED

        # Trigger intent emission in daemon thread (go() blocks forever)
        go_thread = threading.Thread(target=bot.go, daemon=True)
        go_thread.start()

        if not self.server.await_bot_intent(2000):
            self.fail("Timeout waiting for bot intent")

        # Assert (Python schema uses snake_case fields)
        intent = self.server._bot_intent  # type: ignore[attr-defined]
        self.assertIsNotNone(intent)
        self.assertEqual(intent.turn_rate, float(MAX_TURN_RATE))
        self.assertEqual(intent.gun_turn_rate, float(-MAX_GUN_TURN_RATE))
        self.assertEqual(intent.radar_turn_rate, float(MAX_RADAR_TURN_RATE))
        self.assertEqual(intent.target_speed, float(MAX_SPEED))

    def test_TR_API_CMD_001_movement_commands_nan_raises(self):
        """TR-API-CMD-001 Movement commands: NaN values raise ValueError in setters"""
        bot = Bot(
            bot_info=BotInfo(name="CmdBot", version="1.0", authors=["Tester"]),
            server_url=self.server.server_url,
        )
        # No need to start/connect to test validation
        with self.assertRaises(ValueError):
            bot.turn_rate = math.nan
        with self.assertRaises(ValueError):
            bot.gun_turn_rate = math.nan
        with self.assertRaises(ValueError):
            bot.radar_turn_rate = math.nan
        with self.assertRaises(ValueError):
            bot.target_speed = math.nan


if __name__ == "__main__":
    unittest.main()
