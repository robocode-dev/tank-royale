import asyncio
import threading
from typing import Optional
from robocode_tank_royale.bot_api import BaseBot, BotInfo
from .mocked_server import MockedServer


class TestBot(BaseBot):
    """Simple test bot for use in tests"""
    def __init__(self, bot_info: BotInfo, server_url: str):
        super().__init__(bot_info, server_url)


class AbstractBotTest:
    """Base class for bot tests providing common test utilities"""

    def __init__(self):
        self.server: Optional[MockedServer] = None
        self.bot: Optional[TestBot] = None
        self._bot_thread: Optional[threading.Thread] = None

    def start_bot(self) -> TestBot:
        """Start a test bot and return it"""
        self.server = MockedServer()
        self.server.start()

        bot_info = BotInfo(
            name="TestBot",
            version="1.0",
            authors=["Test"],
            game_types=["classic"],
            platform="Python",
            programming_lang="Python",
        )

        self.bot = TestBot(bot_info, self.server.server_url)

        # Start bot in a separate thread
        def run_bot():
            asyncio.run(self.bot.start())

        self._bot_thread = threading.Thread(target=run_bot, daemon=True)
        self._bot_thread.start()

        return self.bot

    def await_bot_ready(self, timeout_ms: int = 2000) -> bool:
        """Wait for the bot to be ready"""
        if self.server:
            return self.server.await_bot_ready(timeout_ms)
        return False

    def go_async(self, bot: TestBot) -> None:
        """Trigger bot to send go command asynchronously"""
        def run_go():
            asyncio.run(bot.go())

        go_thread = threading.Thread(target=run_go, daemon=True)
        go_thread.start()

    def await_bot_intent(self, timeout_ms: int = 1000) -> bool:
        """Wait for bot to send intent"""
        if self.server:
            return self.server.await_bot_intent(timeout_ms)
        return False

    def teardown(self):
        """Clean up test resources"""
        if self.server:
            self.server.stop()
        if self._bot_thread:
            self._bot_thread.join(timeout=1.0)
