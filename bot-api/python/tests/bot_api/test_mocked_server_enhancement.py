import threading
import time
import pytest
pytestmark = pytest.mark.LEGACY
import unittest
from robocode_tank_royale.bot_api import BotInfo, Bot
from test_utils.mocked_server import MockedServer


def _poll_equal(getter, expected, timeout: float = 2.0) -> bool:
    """Poll until getter() == expected or timeout expires; returns True on match.

    Exceptions from getter() (e.g. BotException before the first tick) are
    treated as "not ready yet" and cause the poll to retry.
    """
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            if getter() == expected:
                return True
        except Exception:
            pass
        time.sleep(0.01)
    return False

class MockedServerEnhancementTest(unittest.TestCase):
    def setUp(self) -> None:
        self.server = MockedServer()
        self.server.start()
        self._bot_thread: threading.Thread | None = None
        self.bot_info = BotInfo(name="TestBot", version="1.0", authors=["Test"])

    def tearDown(self) -> None:
        self.server.stop()
        if self._bot_thread is not None:
            self._bot_thread.join(timeout=2.0)

    def _start_bot(self, bot: Bot) -> None:
        self._bot_thread = threading.Thread(target=bot.start, daemon=True)
        self._bot_thread.start()

    def test_await_bot_ready_should_succeed(self):
        bot = Bot(self.bot_info, self.server.server_url)
        self._start_bot(bot)

        ready = self.server.await_bot_ready(2000)
        self.assertTrue(ready)

    def test_set_bot_state_and_await_tick_should_update_state(self):
        server = self.server
        class BotWithLoop(Bot):
            def run(self):
                while self.running:
                    self.go()

        bot = BotWithLoop(self.bot_info, self.server.server_url)
        self._start_bot(bot)

        self.assertTrue(self.server.await_bot_ready(2000))

        # Poll until bot has processed tick 1 and energy is readable.
        # At 30 TPS a tick takes ~33 ms; this typically resolves in < 20 ms.
        self.assertTrue(
            _poll_equal(lambda: bot.energy, MockedServer.bot_energy),
            "bot.energy should match server initial energy after first tick",
        )

        # Update state
        new_energy = 50.0
        new_gun_heat = 1.5
        success = self.server.set_bot_state_and_await_tick(energy=new_energy, gun_heat=new_gun_heat)

        self.assertTrue(success)

        # Poll until bot has processed the updated tick (typically < 20 ms at 30 TPS).
        self.assertTrue(
            _poll_equal(lambda: bot.energy, new_energy),
            "bot.energy should reflect the updated value after next tick",
        )
        self.assertEqual(bot.gun_heat, new_gun_heat)

if __name__ == "__main__":
    unittest.main()
