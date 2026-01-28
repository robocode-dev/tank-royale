import threading
import time
import asyncio
from typing import Optional, Callable, Any
import unittest
from robocode_tank_royale.bot_api import BotInfo, Bot, BotException
from tests.test_utils.mocked_server import MockedServer

class AbstractBotTest(unittest.TestCase):
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

    def tearDown(self) -> None:
        self.server.stop()
        for t in self._threads:
            if t.is_alive():
                # We can't easily force stop a thread in Python
                t.join(timeout=1.0)

    def start_bot(self, bot: Optional[Bot] = None) -> Bot:
        if bot is None:
            bot = Bot(self.bot_info, self.server.server_url)
        self.start_async(bot)
        if not self.server.await_bot_ready(2000):
            raise TimeoutError("Bot failed to become ready")
        return bot

    def start_async(self, bot: Bot) -> threading.Thread:
        def run_bot():
            asyncio.run(bot.start())

        t = threading.Thread(target=run_bot)
        t.start()
        self._threads.append(t)
        return t

    def go_async(self, bot: Bot) -> threading.Thread:
        def run_go():
            asyncio.run(bot.go())

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
        self.server.reset_bot_intent_event()
        result = command()
        self.await_bot_intent()
        return result

    def execute_blocking(self, action: Callable[[], None]) -> None:
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
        return result, self.server._bot_intent

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
