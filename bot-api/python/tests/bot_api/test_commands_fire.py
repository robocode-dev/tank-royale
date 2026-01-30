"""
Tests for fire commands (TR-API-CMD-002).

These tests verify the behavior of fire-related methods:
- Firepower is sent as-is to server (server clamps to valid range [0.1, 3.0])
- Fire fails when gun is hot (gun_heat > 0)
- Fire fails when energy is too low
- NaN firepower throws ValueError
"""

import math
import unittest

from tests.bot_api.abstract_bot_test import AbstractBotTest


class CommandsFireTest(AbstractBotTest):
    """Fire commands test class (TR-API-CMD-002)."""

    def test_firepower_below_min_sent_as_is(self) -> None:
        """Firepower below 0.1 is sent as-is (server clamps)."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with value below minimum
        result = self.set_fire_and_get_intent(bot, 0.05)

        # Fire should succeed - API sends raw value, server will clamp
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.firepower, 0.05, places=5)

    def test_firepower_above_max_sent_as_is(self) -> None:
        """Firepower above 3.0 is sent as-is (server clamps)."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with value of 5.0 - this is above max but passes energy check (100 > 5)
        result = self.set_fire_and_get_intent(bot, 5.0)

        # Fire should succeed - API sends raw value, server will clamp
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.firepower, 5.0, places=5)

    def test_valid_firepower_is_preserved(self) -> None:
        """Valid firepower (1.0) is preserved in intent."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with valid value
        result = self.set_fire_and_get_intent(bot, 1.0)

        # Fire should succeed (True) with exact value
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.firepower, 1.0, places=5)

    def test_fire_fails_when_gun_is_hot(self) -> None:
        """Fire fails when gun is hot (gun_heat > 0)."""
        # Start bot and wait for game started
        bot = self.start_bot()
        self.await_game_started(bot)

        # Set high gun heat so bot cannot fire - use set_bot_state_and_await_tick to send state to bot
        self.server.set_bot_state_and_await_tick(energy=100.0, gun_heat=5.0)
        # Wait for bot to process the updated state
        self.await_condition(lambda: bot.gun_heat == 5.0, 1000)

        # Execute fire - should fail due to gun heat
        result = self.set_fire_and_get_intent(bot, 1.0)

        # Fire should fail (False) and firepower should be None
        self.assertFalse(result.result)
        self.assertIsNone(result.intent.firepower)

    def test_fire_fails_when_energy_too_low(self) -> None:
        """Fire fails when energy is too low for firepower."""
        # Start bot and wait for game started
        bot = self.start_bot()
        self.await_game_started(bot)

        # Set low energy and no gun heat - use set_bot_state_and_await_tick to send state to bot
        self.server.set_bot_state_and_await_tick(energy=0.5, gun_heat=0.0)
        # Wait for bot to process the updated state
        self.await_condition(lambda: bot.energy == 0.5 and bot.gun_heat == 0.0, 1000)

        # Execute fire with high firepower - should fail due to energy
        result = self.set_fire_and_get_intent(bot, 3.0)

        # Fire should fail (False) and firepower should be None
        self.assertFalse(result.result)
        self.assertIsNone(result.intent.firepower)

    def test_fire_with_nan_throws_exception(self) -> None:
        """Fire with NaN throws ValueError."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with NaN - should throw
        with self.assertRaises(ValueError):
            bot.set_fire(float('nan'))

    def test_fire_with_negative_value_sets_raw_value(self) -> None:
        """Fire with negative value sets raw value (API does not clamp)."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with negative value - API does not validate/clamp
        result = self.set_fire_and_get_intent(bot, -1.0)

        # Fire succeeds because energy check passes (100.0 >= -1.0)
        # The raw value is sent to the server (no clamping in client API)
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.firepower, -1.0, places=5)

    def test_fire_with_infinity_fails_energy_check(self) -> None:
        """Fire with Infinity fails because energy is insufficient."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with Infinity - fails because energy < Infinity
        result = self.set_fire_and_get_intent(bot, float('inf'))

        # Fire should fail (False) because energy (100.0) < Infinity
        self.assertFalse(result.result)
        self.assertIsNone(result.intent.firepower)

    def test_fire_with_exact_minimum_succeeds(self) -> None:
        """Fire with exact minimum (0.1) succeeds."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with exact minimum
        result = self.set_fire_and_get_intent(bot, 0.1)

        # Fire should succeed (True)
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.firepower, 0.1, places=5)

    def test_fire_with_exact_maximum_succeeds(self) -> None:
        """Fire with exact maximum (3.0) succeeds."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with exact maximum
        result = self.set_fire_and_get_intent(bot, 3.0)

        # Fire should succeed (True)
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.firepower, 3.0, places=5)


if __name__ == "__main__":
    unittest.main()
