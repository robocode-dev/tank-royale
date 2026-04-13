import unittest
from typing import Callable
from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.schema.bot_intent import BotIntent
from tests.bot_api.abstract_bot_test import AbstractBotTest
import time
import threading

class CommandsRadarTest(AbstractBotTest):
    """Radar commands test class (TR-API-CMD-003)."""

    def await_expected_intent(self, predicate: Callable[[BotIntent], bool]) -> None:
        start_time = time.time()
        while time.time() - start_time < 10:
            # Ensure we are ready to capture the next intent
            self.server.reset_bot_intent_latch()
            # Signal the server to process the current intent and send the next tick
            self.server.continue_bot_intent()
            # Wait for the server to capture the next intent
            if self.server.await_bot_intent(2000):
                intent = self.server.get_bot_intent()
                if predicate(intent):
                    return
        self.fail("Timed out waiting for expected intent")

    def test_rescan_intent(self) -> None:
        """set_rescan() sets the rescan flag in the intent."""
        bot = self.start_bot()

        bot.set_rescan()
        self.await_expected_intent(lambda intent: intent.rescan is True)

    def test_blocking_rescan(self) -> None:
        """rescan() blocking call sets the rescan flag in the intent."""
        bot = self.start_bot()

        # Run rescan in a separate thread because it's blocking
        self.go_async(bot.rescan)
        self.await_expected_intent(lambda intent: intent.rescan is True)

    def test_adjust_radar_body(self) -> None:
        """adjust_radar_for_body_turn sets the flag in the intent."""
        bot = self.start_bot()

        # Test True
        bot.adjust_radar_for_body_turn = True
        self.await_expected_intent(lambda intent: intent.adjust_radar_for_body_turn is True)

        # Test False
        bot.adjust_radar_for_body_turn = False
        self.await_expected_intent(lambda intent: intent.adjust_radar_for_body_turn is False)

    def test_adjust_radar_gun(self) -> None:
        """adjust_radar_for_gun_turn sets the flag in the intent."""
        bot = self.start_bot()

        # Test True
        bot.adjust_radar_for_gun_turn = True
        self.await_expected_intent(lambda intent: intent.adjust_radar_for_gun_turn is True)

        # Test False
        bot.adjust_radar_for_gun_turn = False
        self.await_expected_intent(lambda intent: intent.adjust_radar_for_gun_turn is False)

if __name__ == "__main__":
    unittest.main()
