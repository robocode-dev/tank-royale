import unittest
import time
from tests.bot_api.abstract_bot_test import AbstractBotTest

class MockedServerTest(AbstractBotTest):

    def test_await_bot_ready(self):
        bot = self.start_bot()
        self.assertTrue(self.server.await_bot_ready(1000))

    def test_set_bot_state_and_await_tick(self):
        bot = self.start_bot()
        # No need to await game started separately as start_bot does await_bot_ready

        new_energy = 42.5
        new_gun_heat = 0.33

        ok = self.server.set_bot_state_and_await_tick(energy=new_energy, gun_heat=new_gun_heat)
        self.assertTrue(ok)

        # wait until bot reflects the updated state
        reflected = self.await_condition(lambda: abs(bot.energy - new_energy) < 1e-6, 1000)
        self.assertTrue(reflected)

        reflected_gun_heat = self.await_condition(lambda: abs(bot.gun_heat - new_gun_heat) < 1e-6, 1000)
        self.assertTrue(reflected_gun_heat)

if __name__ == "__main__":
    unittest.main()
