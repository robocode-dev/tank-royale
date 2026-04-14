import unittest
from typing import Callable
from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.schema.bot_intent import BotIntent
from tests.bot_api.abstract_bot_test import AbstractBotTest
import time

class CommandsRadarTest(AbstractBotTest):
    """Radar commands test class (TR-API-CMD-003)."""

    def start_radar_bot(self) -> Bot:
        """Start a bot without draining the initial intent.

        Mirrors Java's startRadarBot(): waits for the first tick but leaves the
        initial intent pending at the server. This keeps last_execute_turn_number=0
        so that the first execute(1) call (whether from the bot thread's while-loop
        or from a set_rescan/rescan caller) is guaranteed to send the intent with
        whatever flags were set — including rescan=True.
        """
        bot = Bot(self.bot_info, self.server.server_url)
        self._bots.append(bot)
        self.start_async(bot)
        if not self.server.await_bot_ready(2000):
            raise TimeoutError("Bot failed to become ready")
        return bot

    def await_expected_intent(self, predicate: Callable[[BotIntent], bool]) -> None:
        """Wait for an intent that satisfies a predicate.

        Mirrors Java's awaitExpectedIntent(): continue first, then await,
        then reset only after a non-matching intent.
        """
        start_time = time.time()
        count = 0
        while time.time() - start_time < 10:
            count += 1
            self.server.continue_bot_intent()
            if self.server.await_bot_intent(2000):
                intent = self.server.get_bot_intent()
                if predicate(intent):
                    return
                self.server.reset_bot_intent_latch()
        self.fail("Timed out waiting for expected intent")

    def test_rescan_intent(self) -> None:
        """set_rescan() sets the rescan flag in the intent."""
        bot = self.start_radar_bot()

        bot.set_rescan()
        self.await_expected_intent(lambda intent: intent.rescan is True)

    def test_blocking_rescan(self) -> None:
        """rescan() blocking call sets the rescan flag in the intent."""
        bot = self.start_radar_bot()

        # Run rescan in a separate thread because it's blocking in Java (go() blocks);
        # in Python go() returns immediately from non-bot threads, but we match the
        # Java test structure so the rescan flag is set before execute(1) runs.
        self.go_async(bot.rescan)
        self.await_expected_intent(lambda intent: intent.rescan is True)

    def test_adjust_radar_body(self) -> None:
        """adjust_radar_for_body_turn sets the flag in the intent."""
        bot = self.start_radar_bot()

        # Test True
        bot.adjust_radar_for_body_turn = True
        self.await_expected_intent(lambda intent: intent.adjust_radar_for_body_turn is True)

        # Test False
        bot.adjust_radar_for_body_turn = False
        self.await_expected_intent(lambda intent: intent.adjust_radar_for_body_turn is False)

    def test_adjust_radar_gun(self) -> None:
        """adjust_radar_for_gun_turn sets the flag in the intent."""
        bot = self.start_radar_bot()

        # Test True
        bot.adjust_radar_for_gun_turn = True
        self.await_expected_intent(lambda intent: intent.adjust_radar_for_gun_turn is True)

        # Test False
        bot.adjust_radar_for_gun_turn = False
        self.await_expected_intent(lambda intent: intent.adjust_radar_for_gun_turn is False)

if __name__ == "__main__":
    unittest.main()
