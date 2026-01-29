import threading
import time
import asyncio
from typing import Optional, Callable, Any
import unittest
from robocode_tank_royale.bot_api import BotInfo, Bot, BotException
from robocode_tank_royale.bot_api.internal.thread_interrupted_exception import ThreadInterruptedException
from tests.test_utils.mocked_server import MockedServer

class AbstractBotTest(unittest.TestCase):
    """
    Abstract base class for bot API tests.

    Provides common test infrastructure including:
    - MockedServer lifecycle management
    - Bot thread tracking for clean shutdown
    - Command execution utilities with intent capture
    - Synchronization helpers
    """
    bot_info = BotInfo(
        name="TestBot",
        version="1.0",
        authors=["Author 1", "Author 2"],
        description="Short description",
        homepage="https://testbot.robocode.dev",
        country_codes=["gb", "us"],
        game_types=["classic", "melee", "1v1"],
        platform="Python 3.10",
        programming_lang="Python",
    )

    def setUp(self) -> None:
        self.server = MockedServer()
        self.server.start()
        self._threads = []
        self._bots = []  # Track bots for proper cleanup

    def tearDown(self) -> None:
        """
        Clean up test resources with proper timeout handling.

        Cleanup sequence:
        1. Stop the mocked server (causes bot threads to exit naturally)
        2. Wait for bot threads with timeout
        3. Clear tracking lists

        Note: We stop the server FIRST (like Java does) rather than calling bot.stop()
        from the test thread, which would trigger ThreadInterruptedException.
        Stopping the server causes the WebSocket connection to close, which makes
        the bot threads exit cleanly.
        """
        # Stop the server - this causes bot threads to exit naturally
        try:
            self.server.stop()
        except Exception:
            pass  # Ignore all errors during cleanup

        # Wait for bot threads to finish with timeout
        timeout_per_thread = 0.5  # Short timeout per thread
        for t in self._threads:
            if t.is_alive():
                t.join(timeout=timeout_per_thread)


        # Clear tracking lists
        self._threads.clear()
        self._bots.clear()

    def start_bot(self, bot: Optional[Bot] = None) -> Bot:
        """
        Create and start a test bot, waiting for it to be ready.
        The bot thread is automatically tracked for clean shutdown.

        Args:
            bot: Optional bot instance. If None, creates a default test bot.

        Returns:
            The started bot instance.

        Raises:
            TimeoutError: If bot fails to become ready within timeout.
        """
        if bot is None:
            bot = Bot(self.bot_info, self.server.server_url)
        self._bots.append(bot)  # Track for cleanup
        self.start_async(bot)
        if not self.server.await_bot_ready(2000):
            raise TimeoutError("Bot failed to become ready")
        return bot

    def start_and_prepare_for_fire(self, bot: Optional[Bot] = None, energy: float = 100.0) -> Bot:
        """
        Start a bot and prepare it for fire command tests.

        Sets gun_heat to 0.0 so the bot can fire immediately,
        and sets energy to the specified value (default 100.0).

        Args:
            bot: Optional bot instance. If None, creates a default test bot.
            energy: Initial energy level (default 100.0).

        Returns:
            The started bot instance ready for fire tests.

        Raises:
            TimeoutError: If bot fails to become ready within timeout.
        """
        # Set initial state BEFORE starting the bot
        self.server.set_initial_bot_state(energy=energy, gun_heat=0.0)
        return self.start_bot(bot)

    def start_async(self, bot: Bot) -> threading.Thread:
        """
        Start a bot asynchronously in a tracked thread.
        The thread is registered for cleanup during teardown.

        Args:
            bot: The bot to start.

        Returns:
            The thread running the bot.
        """
        if bot not in self._bots:
            self._bots.append(bot)  # Ensure bot is tracked

        def run_bot():
            bot.start()

        t = threading.Thread(target=run_bot)
        t.start()
        self._threads.append(t)
        return t

    def go_async(self, bot: Bot) -> threading.Thread:
        """
        Execute bot.go() asynchronously in a tracked thread.
        The thread is registered for cleanup during teardown.

        Args:
            bot: The bot to run.

        Returns:
            The thread running bot.go().
        """
        if bot not in self._bots:
            self._bots.append(bot)  # Ensure bot is tracked

        def run_go():
            bot.go()

        t = threading.Thread(target=run_go)
        t.start()
        self._threads.append(t)
        return t

    def await_bot_handshake(self) -> None:
        self.assertTrue(self.server.await_bot_handshake(1000))

    def await_game_started(self, bot: Bot) -> None:
        self.assertTrue(self.server.await_game_started(1000))
        start_time = time.time()
        while time.time() - start_time < 1.0:
            try:
                _ = bot.game_type
                return
            except BotException:
                time.sleep(0.01)
        self.fail("Timed out waiting for bot to initialize after GameStarted")

    def await_tick(self, bot: Bot) -> None:
        self.assertTrue(self.server.await_tick(1000))
        start_time = time.time()
        while time.time() - start_time < 1.0:
            try:
                _ = bot.energy
                return
            except BotException:
                time.sleep(0.01)
        self.fail("Timed out waiting for bot to receive tick")

    def await_bot_intent(self) -> None:
        self.assertTrue(self.server.await_bot_intent(1000))

    def execute_command(self, command: Callable[[], Any]) -> Any:
        """
        Execute a command and wait for the bot to send its intent to the server.
        This is useful for testing non-blocking commands that immediately return.

        Args:
            command: The command callable to execute.

        Returns:
            The result of the command.
        """
        self.server.reset_bot_intent_event()
        result = command()
        self.await_bot_intent()
        return result

    def execute_blocking(self, action: Callable[[], None]) -> None:
        """
        Execute a blocking action and wait for the bot to send its intent to the server.
        This is useful for testing blocking commands like go().

        Args:
            action: The blocking action callable to execute.
        """
        self.server.reset_bot_intent_event()
        action()
        self.await_bot_intent()

    def execute_command_and_get_intent(self, command: Callable[[], Any]) -> tuple[Any, Any]:
        """
        Execute a command and capture both the result and the bot intent sent to the server.
        This is useful for verifying that commands produce the expected intent values.

        Args:
            command: The command to execute

        Returns:
            A tuple of (result, intent) where result is the command's return value
            and intent is the captured BotIntent
        """
        self.server.reset_bot_intent_event()
        result = command()
        self.await_bot_intent()
        return result, self.server.get_bot_intent()

    def await_condition(self, condition: Callable[[], bool], timeout_ms: int = 1000) -> bool:
        start_time = time.time()
        while (time.time() - start_time) * 1000 < timeout_ms:
            try:
                if condition():
                    return True
            except BotException:
                pass
            time.sleep(0.01)
        return False
