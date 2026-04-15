"""
Regression tests for issue #202: first-turn skip caused by bot thread starting during turn 1.

The pre-warm fix starts the bot thread at round-started (before any tick).
The thread blocks until tick 1 arrives, after which run() executes with valid bot state.
"""
import pytest
pytestmark = pytest.mark.LEGACY
import threading
import unittest

from robocode_tank_royale.bot_api import BotInfo, Bot
from robocode_tank_royale.bot_api.constants import MAX_RADAR_TURN_RATE
from robocode_tank_royale.bot_api.events import SkippedTurnEvent
from test_utils.mocked_server import MockedServer


class BotRunFirstTurnTest(unittest.TestCase):
    """TR-API-TCK-004 First-turn state availability (regression: issue #202)"""

    def setUp(self) -> None:
        self.server = MockedServer()
        self.server.start()
        self._bot_thread: threading.Thread | None = None

    def tearDown(self) -> None:
        self.server.stop()
        if self._bot_thread is not None:
            self._bot_thread.join(timeout=2.0)

    def _start_bot(self, bot: Bot) -> None:
        self._bot_thread = threading.Thread(target=bot.start, daemon=True)
        self._bot_thread.start()

    def _make_radar_spin_bot(self) -> "RadarSpinBot":
        """
        Create a bot that spins its radar every turn and records the radar direction seen
        in run() on the first call. Mirrors RadarSpinBotPython in bot-api/tests/bots/python/.
        """
        server = self.server

        class RadarSpinBot(Bot):
            def __init__(self_bot) -> None:
                super().__init__(
                    bot_info=BotInfo(name="RadarSpinBot", version="1.0", authors=["Test"]),
                    server_url=server.server_url,
                )
                self_bot.radar_direction_on_first_run: float | None = None
                self_bot.skipped_first_turn: bool = False

            def run(self_bot) -> None:
                self_bot.radar_direction_on_first_run = self_bot.radar_direction
                while self_bot.running:
                    self_bot.set_turn_radar_left(MAX_RADAR_TURN_RATE)
                    self_bot.go()

            def on_skipped_turn(self_bot, event: SkippedTurnEvent) -> None:
                if event.turn_number == 1:
                    self_bot.skipped_first_turn = True

        return RadarSpinBot()

    def test_TR_API_TCK_004a_run_sees_first_tick_state(self):
        """TR-API-TCK-004a run() receives valid bot state on turn 1"""
        bot = self._make_radar_spin_bot()
        self._start_bot(bot)

        if not self.server.await_bot_ready(2000):
            self.fail("Bot failed to become ready")

        # Drain the pre-warm initial intent so the bot can progress through run()
        self.server.continue_bot_intent()
        if not self.server.await_bot_intent(2000):
            self.fail("Timeout waiting for bot intent")

        self.assertIsNotNone(
            bot.radar_direction_on_first_run,
            "run() must have executed before go() on turn 1 (regression: issue #202)",
        )
        self.assertAlmostEqual(
            bot.radar_direction_on_first_run,
            MockedServer.bot_radar_direction,
            places=5,
            msg="run() must not execute before first-tick state is available (regression: issue #202)",
        )

    def test_TR_API_TCK_004b_first_intent_contains_radar_turn_rate(self):
        """TR-API-TCK-004b intent from run() contains radar turn rate set in run()"""
        bot = self._make_radar_spin_bot()
        self._start_bot(bot)

        if not self.server.await_bot_ready(2000):
            self.fail("Bot failed to become ready")

        # Drain the pre-warm initial intent (empty default sent to prevent turn-1 skip)
        self.server.continue_bot_intent()
        if not self.server.await_bot_intent(2000):
            self.fail("Timeout waiting for pre-warm intent")

        # Now capture the intent from run() which carries the radar turn rate
        self.server.reset_bot_intent_latch()
        self.server.continue_bot_intent()
        if not self.server.await_bot_intent(2000):
            self.fail("Timeout waiting for run() intent")

        intent = self.server.get_bot_intent()
        self.assertIsNotNone(
            intent,
            "MockedServer must have received a BotIntent (regression: issue #202)",
        )
        self.assertAlmostEqual(
            intent.radar_turn_rate,
            float(MAX_RADAR_TURN_RATE),
            places=5,
            msg="Intent from run() must include the radar turn rate (regression: issue #202)",
        )


if __name__ == "__main__":
    unittest.main()
