import asyncio
import threading
import time
import pytest
from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.events.tick_event import TickEvent
from tests.test_utils.mocked_server import MockedServer

class TestBot(Bot):
    def __init__(self, server_url: str):
        bot_info = BotInfo(
            name="VerificationBot",
            version="1.0",
            authors=["Author"],
            description="Description",
            homepage="https://test.com",
            country_codes=["us"],
            game_types=["classic"],
            platform="Python",
            programming_lang="Python",
        )
        super().__init__(bot_info, server_url)

    def run(self):
        while self.running:
            self.set_fire(1.0)
            # Need to use asyncio.create_task since we're in a sync method
            import asyncio
            try:
                # Create a new event loop for this thread if needed
                loop = asyncio.get_event_loop()
            except RuntimeError:
                loop = asyncio.new_event_loop()
                asyncio.set_event_loop(loop)

            # Run go() synchronously
            loop.run_until_complete(self.go())

def test_verify_state_synchronization_identical():
    """Test with timeout handling to prevent hanging."""
    def run_test():
        server = MockedServer()
        try:
            server.start()
            bot = TestBot(server.server_url)
            bot_thread = threading.Thread(target=lambda: asyncio.run(bot.start()), daemon=True)
            bot_thread.start()

            # 1. Verify await_bot_ready with shorter timeout
            if not server.await_bot_ready(5000):
                raise AssertionError("await_bot_ready should succeed")

            # Give the bot a chance to be ready for the next tick
            time.sleep(0.1)

            # 2. Verify initial state (based on MockedServer defaults)
            assert bot.energy == MockedServer.bot_energy
            assert bot.speed == MockedServer.bot_speed
            assert bot.direction == MockedServer.bot_direction
            assert bot.gun_direction == MockedServer.bot_gun_direction
            assert bot.radar_direction == MockedServer.bot_radar_direction

            # 3. Update all states via set_bot_state_and_await_tick
            new_energy = 42.0
            new_gun_heat = 1.5
            new_speed = 6.5
            new_direction = 180.0
            new_gun_direction = 90.0
            new_radar_direction = 270.0

            success = server.set_bot_state_and_await_tick(
                energy=new_energy,
                gun_heat=new_gun_heat,
                speed=new_speed,
                direction=new_direction,
                gun_direction=new_gun_direction,
                radar_direction=new_radar_direction
            )

            if not success:
                raise AssertionError("set_bot_state_and_await_tick should succeed")

            # 4. Verify bot reflects new state
            assert bot.energy == new_energy
            assert bot.gun_heat == new_gun_heat
            assert bot.speed == new_speed
            assert bot.direction == new_direction
            assert bot.gun_direction == new_gun_direction
            assert bot.radar_direction == new_radar_direction

            # 5. Verify Turn Number increment
            current_turn = bot.turn_number
            server.set_bot_state_and_await_tick()
            assert bot.turn_number == current_turn + 1

            # 6. Manual Turn Number setting
            server.set_turn_number(500)
            server.set_bot_state_and_await_tick()
            assert bot.turn_number == 500

        finally:
            try:
                server.stop()
            except Exception:
                pass
            time.sleep(0.1)

    # Run the test with a timeout
    test_thread = threading.Thread(target=run_test, daemon=True)
    test_thread.start()
    test_thread.join(timeout=30.0)

    if test_thread.is_alive():
        pytest.fail("Test timed out after 30 seconds - likely hanging in MockedServer")
