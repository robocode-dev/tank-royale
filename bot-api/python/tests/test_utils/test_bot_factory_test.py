"""Unit tests for TestBotBuilder."""

import threading
import time
import unittest

from tests.test_utils.mocked_server import MockedServer
from tests.test_utils.test_bot_factory import TestBotBuilder, BotBehavior


class TestBotBuilderTest(unittest.TestCase):
    """Unit tests for TestBotBuilder."""

    def setUp(self) -> None:
        """Set up test fixtures."""
        self.server = MockedServer()
        self.server.start()

    def tearDown(self) -> None:
        """Tear down test fixtures."""
        self.server.stop()

    def test_default_passive_behavior(self) -> None:
        """TestBotBuilder creates bot with default passive behavior."""
        bot = (TestBotBuilder()
            .with_behavior(BotBehavior.PASSIVE)
            .build())

        self.assertIsNotNone(bot)

    def test_custom_name(self) -> None:
        """TestBotBuilder creates bot with custom name."""
        bot = (TestBotBuilder()
            .with_name("CustomBot")
            .with_version("2.0")
            .with_authors("Author1", "Author2")
            .build())

        self.assertIsNotNone(bot)

    def test_aggressive_behavior(self) -> None:
        """TestBotBuilder creates bot with aggressive behavior."""
        bot = (TestBotBuilder()
            .with_behavior(BotBehavior.AGGRESSIVE)
            .build())

        self.assertIsNotNone(bot)

    def test_scanning_behavior(self) -> None:
        """TestBotBuilder creates bot with scanning behavior."""
        bot = (TestBotBuilder()
            .with_behavior(BotBehavior.SCANNING)
            .build())

        self.assertIsNotNone(bot)

    def test_on_tick_callback(self) -> None:
        """TestBotBuilder on_tick callback is invoked."""
        tick_called = threading.Event()

        bot = (TestBotBuilder()
            .on_tick(lambda e: tick_called.set())
            .build())

        # Start bot in separate thread
        bot_thread = threading.Thread(target=bot.start, daemon=True)
        bot_thread.start()

        try:
            # Wait for bot to be ready and receive tick
            self.assertTrue(self.server.await_bot_ready(2000))

            # Give time for tick callback to be invoked
            self.assertTrue(tick_called.wait(timeout=1.0))
        finally:
            # Cleanup
            self.server.stop()
            bot_thread.join(timeout=1.0)

    def test_on_run_callback(self) -> None:
        """TestBotBuilder on_run callback is invoked."""
        run_called = threading.Event()

        bot = (TestBotBuilder()
            .on_run(lambda: run_called.set())
            .build())

        # Start bot in separate thread
        bot_thread = threading.Thread(target=bot.start, daemon=True)
        bot_thread.start()

        try:
            # Wait for bot to be ready
            self.assertTrue(self.server.await_bot_ready(2000))

            # Give time for run callback to be invoked
            self.assertTrue(run_called.wait(timeout=1.0))
        finally:
            # Cleanup
            self.server.stop()
            bot_thread.join(timeout=1.0)

    def test_callback_chaining(self) -> None:
        """TestBotBuilder multiple callbacks can be chained."""
        callback_count = 0

        def increment():
            nonlocal callback_count
            callback_count += 1

        bot = (TestBotBuilder()
            .with_name("ChainedBot")
            .with_behavior(BotBehavior.CUSTOM)
            .on_tick(lambda e: increment())
            .on_scanned_bot(lambda e: increment())
            .on_hit_bot(lambda e: increment())
            .on_hit_wall(lambda e: increment())
            .on_death(lambda e: increment())
            .build())

        self.assertIsNotNone(bot)

    def test_custom_behavior(self) -> None:
        """TestBotBuilder custom behavior relies on callbacks only."""
        custom_tick_handled = threading.Event()

        bot = (TestBotBuilder()
            .with_behavior(BotBehavior.CUSTOM)
            .on_tick(lambda e: custom_tick_handled.set())
            .build())

        # Start bot in separate thread
        bot_thread = threading.Thread(target=bot.start, daemon=True)
        bot_thread.start()

        try:
            # Wait for bot to be ready and receive tick
            self.assertTrue(self.server.await_bot_ready(2000))

            # Give time for tick callback to be invoked
            self.assertTrue(custom_tick_handled.wait(timeout=1.0))
        finally:
            # Cleanup
            self.server.stop()
            bot_thread.join(timeout=1.0)

    def test_multiple_bots_from_same_builder(self) -> None:
        """TestBotBuilder can build multiple bots from same builder."""
        builder = (TestBotBuilder()
            .with_name("ReusableBot")
            .with_behavior(BotBehavior.PASSIVE))

        bot1 = builder.build()
        bot2 = builder.build()

        self.assertIsNotNone(bot1)
        self.assertIsNotNone(bot2)
        self.assertIsNot(bot1, bot2)


if __name__ == "__main__":
    unittest.main()
