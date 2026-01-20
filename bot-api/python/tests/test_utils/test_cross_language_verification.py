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
        while self.is_running:
            self.set_fire(1.0)
            self.go()

def test_verify_state_synchronization_identical():
    server = MockedServer()
    server.start()
    try:
        bot = TestBot(server.server_url)
        bot_thread = threading.Thread(target=lambda: asyncio.run(bot.start()), daemon=True)
        bot_thread.start()

        # 1. Verify await_bot_ready
        assert server.await_bot_ready(30000) is True, "await_bot_ready should succeed"

        # Give the bot a chance to be ready for the next tick
        time.sleep(0.5)

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

        assert success is True, "set_bot_state_and_await_tick should succeed"

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
        server.stop()
