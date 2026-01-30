"""
Tests for fire commands (TR-API-CMD-002).

These tests verify the behavior of fire-related methods:
- Firepower is clamped to valid range [0.1, 3.0]
- Fire fails when gun is hot (gun_heat > 0)
- Fire fails when energy is too low
- NaN firepower throws ValueError
"""

import math
import unittest

from tests.bot_api.abstract_bot_test import AbstractBotTest


class CommandsFireTest(AbstractBotTest):
    """Fire commands test class (TR-API-CMD-002)."""

    def test_firepower_below_min_is_clamped(self) -> None:
        """Firepower below 0.1 is clamped to 0.1."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with value below minimum
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(0.05))

        # Fire should succeed (True) with clamped value
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.get("firepower"), 0.1, places=5)

    def test_firepower_above_max_is_clamped(self) -> None:
        """Firepower above 3.0 is clamped to 3.0."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with value above maximum
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(5.0))

        # Fire should succeed (True) with clamped value
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.get("firepower"), 3.0, places=5)

    def test_valid_firepower_is_preserved(self) -> None:
        """Valid firepower (1.0) is preserved in intent."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with valid value
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(1.0))

        # Fire should succeed (True) with exact value
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.get("firepower"), 1.0, places=5)

    def test_fire_fails_when_gun_is_hot(self) -> None:
        """Fire fails when gun is hot (gun_heat > 0)."""
        # Start bot and wait for game started
        bot = self.start_bot()

        # Set high gun heat so bot cannot fire
        self.server.set_initial_bot_state(energy=100.0, gun_heat=5.0)

        # Execute fire - should fail due to gun heat
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(1.0))

        # Fire should fail (False) and firepower should be None
        self.assertFalse(result.result)
        self.assertIsNone(result.intent.get("firepower"))

    def test_fire_fails_when_energy_too_low(self) -> None:
        """Fire fails when energy is too low for firepower."""
        # Start bot and wait for game started
        bot = self.start_bot()

        # Set low energy and no gun heat
        self.server.set_initial_bot_state(energy=0.5, gun_heat=0.0)

        # Execute fire with high firepower - should fail due to energy
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(3.0))

        # Fire should fail (False) and firepower should be None
        self.assertFalse(result.result)
        self.assertIsNone(result.intent.get("firepower"))

    def test_fire_with_nan_throws_exception(self) -> None:
        """Fire with NaN throws ValueError."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with NaN - should throw
        with self.assertRaises(ValueError):
            bot.set_fire(float('nan'))

    def test_fire_with_negative_value_is_clamped(self) -> None:
        """Fire with negative value is clamped to minimum (0.1)."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with negative value - should be clamped to 0.1
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(-1.0))

        # Fire should succeed (True) with clamped value
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.get("firepower"), 0.1, places=5)

    def test_fire_with_infinity_is_clamped(self) -> None:
        """Fire with Infinity is clamped to maximum (3.0)."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with Infinity - should be clamped to 3.0
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(float('inf')))

        # Fire should succeed (True) with clamped value
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.get("firepower"), 3.0, places=5)

    def test_fire_with_exact_minimum_succeeds(self) -> None:
        """Fire with exact minimum (0.1) succeeds."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with exact minimum
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(0.1))

        # Fire should succeed (True)
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.get("firepower"), 0.1, places=5)

    def test_fire_with_exact_maximum_succeeds(self) -> None:
        """Fire with exact maximum (3.0) succeeds."""
        bot = self.start_and_prepare_for_fire()

        # Execute fire with exact maximum
        result = self.execute_command_and_get_intent(lambda: bot.set_fire(3.0))

        # Fire should succeed (True)
        self.assertTrue(result.result)
        self.assertAlmostEqual(result.intent.get("firepower"), 3.0, places=5)


if __name__ == "__main__":
    unittest.main()
