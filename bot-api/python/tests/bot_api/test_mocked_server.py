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

    def test_set_initial_bot_state(self):
        """Test that set_initial_bot_state properly configures state before bot runs."""
        # Set state before bot starts
        initial_energy = 75.0
        initial_gun_heat = 2.5
        self.server.set_initial_bot_state(energy=initial_energy, gun_heat=initial_gun_heat)

        # Start bot and verify it receives the configured state
        bot = self.start_bot()

        # Verify the bot sees the initial state we configured
        reflected = self.await_condition(lambda: abs(bot.energy - initial_energy) < 1e-6, 1000)
        self.assertTrue(reflected, f"Expected energy {initial_energy}, got {bot.energy}")

        reflected_gun_heat = self.await_condition(lambda: abs(bot.gun_heat - initial_gun_heat) < 1e-6, 1000)
        self.assertTrue(reflected_gun_heat, f"Expected gun_heat {initial_gun_heat}, got {bot.gun_heat}")

    def test_execute_command_and_get_intent(self):
        """Test that execute_command_and_get_intent properly captures bot intent without race conditions."""
        bot = self.start_bot()

        # Execute a command and capture the intent using property assignment
        def set_turn():
            bot.turn_rate = -5.0  # Use realistic value within max turn rate
        result, intent = self.execute_command_and_get_intent(set_turn)

        # Verify the command returned the expected result
        self.assertIsNone(result)  # property assignment returns None

        # Verify the intent was captured
        self.assertIsNotNone(intent, "Intent should be captured")
        self.assertIsNotNone(intent.turn_rate, "Intent should have turn_rate set")
        self.assertAlmostEqual(intent.turn_rate, -5.0, places=5)

    def test_reset_bot_intent_event_synchronization(self):
        """Test that reset_bot_intent_event properly synchronizes intent capture."""
        bot = self.start_bot()

        # Execute multiple commands in sequence to verify proper synchronization
        for i in range(3):
            self.server.reset_bot_intent_event()
            bot.turn_rate = -10.0 * i  # Use property assignment
            self.await_bot_intent()
            intent = self.server.get_bot_intent()
            self.assertIsNotNone(intent, f"Intent {i} should be captured")

    def test_concurrent_state_access_thread_safety(self):
        """
        Test that concurrent state access from multiple threads is thread-safe.
        This test would expose race conditions if locks were not properly used.
        """
        import threading
        import random

        bot = self.start_bot()
        errors = []
        iterations = 50

        def reader_thread():
            """Continuously read state from the server."""
            try:
                for _ in range(iterations):
                    # Read various state fields
                    _ = self.server.get_energy()
                    _ = self.server.get_gun_heat()
                    _ = self.server.get_speed()
                    _ = self.server.get_direction()
                    _ = self.server.get_gun_direction()
                    _ = self.server.get_radar_direction()
                    _ = self.server.get_bot_intent()
                    _ = self.server.get_handshake()
                    time.sleep(0.001)  # Small delay to interleave with other threads
            except Exception as e:
                errors.append(f"Reader error: {e}")

        def writer_thread():
            """Continuously write state to the server."""
            try:
                for _ in range(iterations):
                    # Write various state fields
                    self.server.set_energy(random.uniform(50.0, 100.0))
                    self.server.set_gun_heat(random.uniform(0.0, 10.0))
                    self.server.set_speed_increment(random.uniform(-1.0, 1.0))
                    self.server.set_turn_increment(random.uniform(-5.0, 5.0))
                    time.sleep(0.001)  # Small delay to interleave with other threads
            except Exception as e:
                errors.append(f"Writer error: {e}")

        def state_setter_thread():
            """Use set_initial_bot_state which should be atomic."""
            try:
                for _ in range(iterations):
                    self.server.set_initial_bot_state(
                        energy=random.uniform(50.0, 100.0),
                        gun_heat=random.uniform(0.0, 10.0),
                        speed=random.uniform(0.0, 8.0)
                    )
                    time.sleep(0.001)
            except Exception as e:
                errors.append(f"State setter error: {e}")

        # Create and start multiple threads
        threads = []
        for _ in range(2):
            threads.append(threading.Thread(target=reader_thread))
            threads.append(threading.Thread(target=writer_thread))
            threads.append(threading.Thread(target=state_setter_thread))

        for t in threads:
            t.start()

        # Wait for all threads to complete
        for t in threads:
            t.join(timeout=10.0)

        # Check for errors
        self.assertEqual(len(errors), 0, f"Thread safety violations detected: {errors}")

        # Verify no threads are still alive (hung)
        for t in threads:
            self.assertFalse(t.is_alive(), "Thread should have completed")

    def test_teardown_completes_within_timeout(self):
        """
        Test that teardown completes within a reasonable time without hanging.
        This verifies that async cleanup and thread joining work correctly.
        """
        import time

        bot = self.start_bot()

        # Execute some commands to ensure the bot is active
        for i in range(3):
            def set_turn():
                bot.turn_rate = -10.0
            self.execute_command(set_turn)

        # Measure teardown time
        start_time = time.time()
        self.tearDown()
        teardown_time = time.time() - start_time

        # Teardown should complete within 3 seconds (acceptance criteria)
        self.assertLess(teardown_time, 3.0,
                        f"Teardown took {teardown_time:.2f}s, expected < 3.0s")

        # Need to reinitialize for next test
        self.setUp()

if __name__ == "__main__":
    unittest.main()
