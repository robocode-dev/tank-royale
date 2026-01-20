import asyncio
import time
import threading
from typing import Optional
from robocode_tank_royale.bot_api import BaseBot, BotInfo
from tests.test_utils.mocked_server import MockedServer


class TestBot(BaseBot):
    def __init__(self, bot_info: BotInfo, server_url: str):
        super().__init__(bot_info, server_url)


def run_bot(bot):
    asyncio.run(bot.start())


def test_await_bot_ready():
    server = MockedServer()
    server.start()
    try:
        bot_info = BotInfo(
            name="TestBot",
            version="1.0",
            authors=["Author"],
            game_types=["classic"],
            platform="Python",
            programming_lang="Python",
        )
        bot = TestBot(bot_info, server.server_url)

        # Start bot in a separate thread
        bot_thread = threading.Thread(target=run_bot, args=(bot,))
        bot_thread.start()

        # await_bot_ready should succeed within a reasonable timeout
        assert server.await_bot_ready(2000) is True

        # Give bot a tiny bit of time to update internal state from the tick it just received
        time.sleep(0.1)

        # Verify bot is actually ready (no exception on getting energy)
        assert bot.get_energy() is not None

    finally:
        server.stop()
        bot_thread.join(timeout=1.0)


def test_set_bot_state_and_await_tick():
    server = MockedServer()
    server.start()
    try:
        bot_info = BotInfo(
            name="TestBot",
            version="1.0",
            authors=["Author"],
            game_types=["classic"],
            platform="Python",
            programming_lang="Python",
        )
        bot = TestBot(bot_info, server.server_url)

        bot_thread = threading.Thread(target=run_bot, args=(bot,))
        bot_thread.start()

        assert server.await_bot_ready(2000) is True

        # Update state and await tick
        new_energy = 50.0
        new_speed = 4.0
        success = server.set_bot_state_and_await_tick(energy=new_energy, speed=new_speed)

        assert success is True

        # Give bot a tiny bit of time to process the tick event and update its internal state
        time.sleep(0.1)

        assert bot.get_energy() == new_energy
        assert bot.get_speed() == new_speed

    finally:
        server.stop()
        bot_thread.join(timeout=1.0)


def test_reset_events():
    server = MockedServer()
    server.start()
    try:
        bot_info = BotInfo(
            name="TestBot",
            version="1.0",
            authors=["Author"],
            game_types=["classic"],
        )
        bot = TestBot(bot_info, server.server_url)
        bot_thread = threading.Thread(target=run_bot, args=(bot,))
        bot_thread.start()

        assert server.await_bot_ready(2000) is True

        # Reset events
        server.reset_events()

        # Now awaiting should timeout (false)
        assert server.await_tick(100) is False

        # Trigger a new tick manually
        assert server.set_bot_state_and_await_tick() is True

        # Now awaiting should succeed
        assert server.await_tick(100) is True

    finally:
        server.stop()
        bot_thread.join(timeout=1.0)


def test_set_turn_number():
    server = MockedServer()
    server.start()
    try:
        bot_info = BotInfo(
            name="TestBot",
            version="1.0",
            authors=["Author"],
            game_types=["classic"],
        )
        bot = TestBot(bot_info, server.server_url)
        bot_thread = threading.Thread(target=run_bot, args=(bot,))
        bot_thread.start()

        assert server.await_bot_ready(2000) is True

        # Set turn number and trigger tick
        server.set_turn_number(100)
        assert server.set_bot_state_and_await_tick() is True

        time.sleep(0.1)
        assert bot.get_turn_number() == 100

    finally:
        server.stop()
        bot_thread.join(timeout=1.0)
