import threading
import time
import asyncio
from typing import Optional, Callable, Any, NamedTuple
import unittest
from robocode_tank_royale.bot_api import BotInfo, Bot, BotException
from robocode_tank_royale.bot_api.internal.thread_interrupted_exception import ThreadInterruptedException
from tests.test_utils.mocked_server import MockedServer


class CommandResult(NamedTuple):
    """Wrapper that holds both a command's return value and the captured bot intent."""
    result: Any
    intent: dict


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
        bot = self.start_bot(bot)
        self.await_game_started(bot)
        # Set gun heat to 0 and energy so bot can fire immediately
        # Use set_bot_state_and_await_tick to actually send the state to the bot
        tick_sent = self.server.set_bot_state_and_await_tick(energy=energy, gun_heat=0.0)
        self.assertTrue(tick_sent, "set_bot_state_and_await_tick should send tick")
        # Wait for bot to update its internal state by polling until energy matches
        state_updated = self.await_condition(lambda: bot.energy == energy and bot.gun_heat == 0.0, 2000)
        self.assertTrue(state_updated, f"Bot state should update to energy={energy}, gunHeat=0 (actual: energy={bot.energy}, gunHeat={bot.gun_heat})")
        return bot

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

    def execute_command_and_get_intent(self, command: Callable[[], Any]) -> CommandResult:
        """
        Execute a command and capture both the result and the bot intent sent to the server.
        This is useful for verifying that commands produce the expected intent values.

        Args:
            command: The command to execute

        Returns:
            A CommandResult containing the command's return value and captured BotIntent
        """
        self.server.reset_bot_intent_event()
        result = command()
        self.await_bot_intent()
        return CommandResult(result, self.server.get_bot_intent())

    def set_fire_and_get_intent(self, bot: Bot, firepower: float) -> CommandResult:
        """
        Helper to test set_fire and capture the resulting intent.
        After calling set_fire, we need to trigger go() to actually send the intent.

        Args:
            bot: The bot to fire with
            firepower: The firepower value to set

        Returns:
            A CommandResult containing the fire result and captured BotIntent
        """
        self.server.reset_bot_intent_event()
        result = bot.set_fire(firepower)

        # Fire command just sets the intent value; we need go() to send it.
        # Use daemon thread since go() will throw ThreadInterruptedException
        # when called from a non-bot thread, but the intent is sent first.
        go_thread = threading.Thread(target=bot.go, daemon=True)
        go_thread.start()

        self.await_bot_intent()
        return CommandResult(result, self.server.get_bot_intent())

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
