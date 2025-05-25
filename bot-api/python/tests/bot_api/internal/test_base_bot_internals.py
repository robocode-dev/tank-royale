import unittest
from unittest.mock import patch, MagicMock
import os
import asyncio

from robocode_tank_royale.bot_api.internal.base_bot_internals import BaseBotInternals, DEFAULT_SERVER_URL, GAME_NOT_RUNNING_MSG, TICK_NOT_AVAILABLE_MSG
from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.bot_abc import BaseBotABC
from robocode_tank_royale.bot_api.events import TickEvent, RoundStartedEvent
from robocode_tank_royale.bot_api.game_setup import GameSetup
from robocode_tank_royale.bot_api.initial_position import InitialPosition
from robocode_tank_royale.bot_api.bot_state import BotState
from robocode_tank_royale.bot_api.bot_exception import BotException
from robocode_tank_royale.schema import ServerHandshake # Assuming this is the correct path

# Minimal mock for BaseBotABC
class MockBot(BaseBotABC):
    def __init__(self):
        super().__init__(BotInfo(name="TestBot", version="1.0", authors="TestAuthor", game_types={"melee"}))
        self.energy = 100.0 # For set_fire tests

    async def run(self):
        pass

    async def go(self):
        pass

    def get_bot_info(self) -> BotInfo: # Add this method
        return self._bot_info


class TestBaseBotInternals(unittest.TestCase):
    def setUp(self):
        self.mock_bot = MockBot()
        # Using the BotInfo from the mock_bot instance itself
        self.bot_info = self.mock_bot.get_bot_info()

        # Instantiate BaseBotInternals with explicit server_url and server_secret for most tests
        # For testing default URL, a separate instance might be needed or os.environ manipulation
        self.internals = BaseBotInternals(self.mock_bot, self.bot_info, server_url="ws://testserver", server_secret="testsecret")

        # ServerHandshake and GameSetup from robocode_tank_royale.schema
        # Note: ServerHandshake in .schema might be different from .bot_api.events.ServerHandshake
        # Using robocode_tank_royale.schema.ServerHandshake as per task
        self.mock_server_handshake = ServerHandshake(
            variant="RobocodeTankRoyale", # Example value
            version="1.0", # Example value
            game_types=["melee", "1v1"] # Example value
            # If your ServerHandshake schema has different fields, adjust accordingly
        )
        self.internals.set_server_handshake(self.mock_server_handshake)

        # GameSetup also from robocode_tank_royale.bot_api.game_setup
        # The task implies robocode_tank_royale.schema.GameSetup, but GameSetup is usually in bot_api
        self.mock_game_setup = GameSetup(
            game_type="melee",
            arena_width=800,
            arena_height=600,
            number_of_rounds=10,
            gun_cooling_rate=0.1,
            inactivity_turns=100,
            turn_timeout=10000,
            ready_timeout=5000,
            max_team_size=1,
            is_fixed_gun_heat_enabled=False, # Example, adjust if needed
            is_fixed_scan_enabled=False     # Example, adjust if needed
        )
        self.internals.set_game_setup(self.mock_game_setup)
        self.internals.set_my_id(1) # Set a default ID

    def test_initialization_default_settings(self):
        # Test with None for server_url to check default fallback
        internals_default = BaseBotInternals(self.mock_bot, self.bot_info, server_url=None, server_secret=None)
        self.assertEqual(internals_default.server_url, DEFAULT_SERVER_URL)
        self.assertIsNotNone(internals_default.bot_intent)
        self.assertTrue('type' in internals_default.bot_intent)
        self.assertEqual(internals_default.bot_intent['type'], 'BotIntent')

    @patch.dict(os.environ, {}, clear=True) # Clear os.environ for this test
    def test_initialization_with_bot_info_none(self):
        # This test requires mocking EnvVars.get_bot_info()
        # For now, let's assume EnvVars.get_bot_info() returns a known BotInfo
        # or we can patch it.
        mock_env_bot_info = BotInfo(
            name="EnvBot", version="0.1", authors="EnvAuthor", game_types={"melee"}
        )
        with patch('robocode_tank_royale.bot_api.internal.env_vars.get_bot_info', return_value=mock_env_bot_info) as mock_get_info:
            internals_env = BaseBotInternals(self.mock_bot, bot_info=None, server_url=None, server_secret=None)
            mock_get_info.assert_called_once()
            self.assertEqual(internals_env.bot_info.name, "EnvBot")

    def test_my_id_game_not_running(self):
        # Create a new instance that hasn't had set_my_id called by setUp
        fresh_internals = BaseBotInternals(self.mock_bot, self.bot_info, server_url=None, server_secret=None)
        with self.assertRaisesRegex(BotException, GAME_NOT_RUNNING_MSG):
            fresh_internals.get_my_id()

    def test_set_get_my_id(self):
        self.internals.set_my_id(123)
        self.assertEqual(self.internals.get_my_id(), 123)

    def test_get_game_setup_not_running(self):
        fresh_internals = BaseBotInternals(self.mock_bot, self.bot_info, server_url=None, server_secret=None)
        with self.assertRaisesRegex(BotException, GAME_NOT_RUNNING_MSG):
            fresh_internals.get_game_setup()
            
    def test_set_get_game_setup(self):
        new_game_setup = GameSetup(game_type="1v1", arena_width=1000, arena_height=1000, number_of_rounds=5, gun_cooling_rate=0.2, inactivity_turns=50, turn_timeout=100, ready_timeout=100, max_team_size=2)
        self.internals.set_game_setup(new_game_setup)
        self.assertEqual(self.internals.get_game_setup().game_type, "1v1")
        self.assertEqual(self.internals.get_game_setup().arena_width, 1000)

    def test_current_tick_not_available(self):
        # self.internals.tick_event is None by default after setup
        with self.assertRaisesRegex(BotException, TICK_NOT_AVAILABLE_MSG):
            self.internals.get_current_tick_or_throw()

    def test_set_get_tick_event(self):
        # Minimal BotState
        mock_bot_state = BotState(energy=100, x=10, y=20, direction=90, gun_direction=90, radar_direction=90, radar_sweep=0, speed=0, turn_rate=0, gun_turn_rate=0, radar_turn_rate=0, gun_heat=0, body_color=None, turret_color=None, radar_color=None, bullet_color=None, scan_color=None, tracks_color=None, gun_color=None, is_disabled=False, is_droid=False, is_debugging_enabled=False)
        mock_tick = TickEvent(turn_number=1, round_number=1, enemy_count=0, bot_state=mock_bot_state, bullet_states=[], events=[])
        self.internals.set_tick_event(mock_tick)
        self.assertEqual(self.internals.get_current_tick_or_throw().turn_number, 1)
        self.assertEqual(self.internals.get_current_tick_or_null(), mock_tick)
        
    @patch('time.monotonic_ns')
    def test_get_time_left(self, mock_monotonic_ns):
        self.internals.set_tick_start_nano_time(1_000_000_000) # 1 second in ns
        # game_setup.turn_timeout is 10000 microseconds
        # Let's say 2000 microseconds (2ms) have passed
        mock_monotonic_ns.return_value = 1_000_000_000 + 2_000_000 # 1s + 2ms in ns
        
        # turn_timeout is 10000 microseconds
        # time_left = 10000 - ( (1_002_000_000 - 1_000_000_000) / 1000 )
        # time_left = 10000 - ( 2_000_000 / 1000 )
        # time_left = 10000 - 2000 = 8000
        self.assertEqual(self.internals.get_time_left(), 8000)

    # Test Settings
    @patch.dict(os.environ, {"ROBOCODE_SERVER_URL": "ws://env_url:1234"}, clear=True)
    def test_get_server_url_from_env_vars(self):
        # Need a new instance to pick up env var at construction
        internals_env = BaseBotInternals(self.mock_bot, self.bot_info, server_url=None, server_secret=None)
        self.assertEqual(internals_env.server_url, "ws://env_url:1234")

    @patch.dict(os.environ, {"SERVER_URL": "ws://env_url_fallback:5678"}, clear=True)
    def test_get_server_url_from_env_vars_fallback(self):
        internals_env = BaseBotInternals(self.mock_bot, self.bot_info, server_url=None, server_secret=None)
        self.assertEqual(internals_env.server_url, "ws://env_url_fallback:5678")

    @patch.dict(os.environ, {"ROBOCODE_SERVER_SECRET": "env_secret"}, clear=True)
    def test_get_server_secret_from_env_vars(self):
        internals_env = BaseBotInternals(self.mock_bot, self.bot_info, server_url=None, server_secret=None)
        self.assertEqual(internals_env.server_secret, "env_secret")

    @patch.dict(os.environ, {"SERVER_SECRET": "env_secret_fallback"}, clear=True)
    def test_get_server_secret_from_env_vars_fallback(self):
        internals_env = BaseBotInternals(self.mock_bot, self.bot_info, server_url=None, server_secret=None)
        self.assertEqual(internals_env.server_secret, "env_secret_fallback")

    # Test Bot Actions
    def test_set_fire_success(self):
        self.mock_bot.energy = 50.0 # Ensure bot has energy
        # Ensure gun is cool, set up a minimal tick_event and bot_state
        mock_bot_state = BotState(energy=50.0, x=0,y=0,direction=0,gun_direction=0,radar_direction=0,radar_sweep=0,speed=0,turn_rate=0,gun_turn_rate=0,radar_turn_rate=0,gun_heat=0.0, body_color=None, turret_color=None, radar_color=None, bullet_color=None, scan_color=None, tracks_color=None, gun_color=None, is_disabled=False, is_droid=False, is_debugging_enabled=False)
        mock_tick = TickEvent(turn_number=1, round_number=1, enemy_count=0, bot_state=mock_bot_state, bullet_states=[], events=[])
        self.internals.set_tick_event(mock_tick)
        
        can_fire = self.internals.set_fire(2.5)
        self.assertTrue(can_fire)
        self.assertEqual(self.internals.bot_intent['firepower'], 2.5)

    def test_set_fire_no_energy(self):
        self.mock_bot.energy = 0.5 # Less than firepower
        mock_bot_state = BotState(energy=0.5, x=0,y=0,direction=0,gun_direction=0,radar_direction=0,radar_sweep=0,speed=0,turn_rate=0,gun_turn_rate=0,radar_turn_rate=0,gun_heat=0.0, body_color=None, turret_color=None, radar_color=None, bullet_color=None, scan_color=None, tracks_color=None, gun_color=None, is_disabled=False, is_droid=False, is_debugging_enabled=False)
        mock_tick = TickEvent(turn_number=1, round_number=1, enemy_count=0, bot_state=mock_bot_state, bullet_states=[], events=[])
        self.internals.set_tick_event(mock_tick)

        can_fire = self.internals.set_fire(1.0)
        self.assertFalse(can_fire)
        # Firepower in intent should not be set if fire failed
        self.assertNotEqual(self.internals.bot_intent.get('firepower'), 1.0)


    def test_set_fire_gun_hot(self):
        self.mock_bot.energy = 50.0
        # Gun is hot
        mock_bot_state = BotState(energy=50.0, x=0,y=0,direction=0,gun_direction=0,radar_direction=0,radar_sweep=0,speed=0,turn_rate=0,gun_turn_rate=0,radar_turn_rate=0,gun_heat=1.0, body_color=None, turret_color=None, radar_color=None, bullet_color=None, scan_color=None, tracks_color=None, gun_color=None, is_disabled=False, is_droid=False, is_debugging_enabled=False)
        mock_tick = TickEvent(turn_number=1, round_number=1, enemy_count=0, bot_state=mock_bot_state, bullet_states=[], events=[])
        self.internals.set_tick_event(mock_tick)

        can_fire = self.internals.set_fire(1.0)
        self.assertFalse(can_fire)
        self.assertNotEqual(self.internals.bot_intent.get('firepower'), 1.0)

    def test_set_turn_rate(self):
        self.internals.set_max_turn_rate(10.0) # MAX_TURN_RATE_CONST
        self.internals.set_turn_rate(5.0)
        self.assertEqual(self.internals.bot_intent['turnRate'], 5.0)
        self.internals.set_turn_rate(15.0) # Above max
        self.assertEqual(self.internals.bot_intent['turnRate'], 10.0)
        self.internals.set_turn_rate(-15.0) # Below min
        self.assertEqual(self.internals.bot_intent['turnRate'], -10.0)

    def test_max_speed_clamping(self):
        # MAX_SPEED_CONST is 8.0
        self.internals.set_max_speed(10.0) # Try to set above constant max
        self.assertEqual(self.internals.get_max_speed(), 8.0)
        self.internals.set_max_speed(5.0)
        self.assertEqual(self.internals.get_max_speed(), 5.0)
        self.internals.set_max_speed(-1.0) # Should clamp to 0
        self.assertEqual(self.internals.get_max_speed(), 0.0)

    # Test Event Handling (Basic)
    def test_on_round_started_resets_state(self):
        # Modify some state that should be reset
        self.internals.is_stopped = True
        self.internals.bot_intent['targetSpeed'] = 5.0
        self.internals.last_execute_turn_number = 10

        mock_round_started_event = RoundStartedEvent(round_number=1) # turn_number is not part of RoundStartedEvent
        self.internals._on_round_started(mock_round_started_event)

        self.assertFalse(self.internals.is_stopped)
        self.assertIsNone(self.internals.bot_intent['targetSpeed']) # Movement reset
        self.assertEqual(self.internals.last_execute_turn_number, -1)
        self.assertEqual(self.internals.event_handling_disabled_turn, 0)

    # Test Team Messaging
    def test_send_team_message_success(self):
        self.internals.set_teammate_ids({2, 3}) # Add some teammates
        message_content = {"data": "test"}
        self.internals.send_team_message(2, message_content)
        
        self.assertEqual(len(self.internals.bot_intent['teamMessages']), 1)
        sent_msg = self.internals.bot_intent['teamMessages'][0]
        self.assertEqual(sent_msg['receiverId'], 2)
        self.assertEqual(sent_msg['messageType'], type(message_content).__name__)
        import json
        self.assertEqual(json.loads(sent_msg['message']), message_content)

    def test_send_team_message_too_many(self):
        self.internals.set_teammate_ids({2})
        # MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN is 16
        for i in range(16):
            self.internals.send_team_message(2, f"msg_{i}")
        
        with self.assertRaisesRegex(BotException, "maximum number team messages has already been reached"):
            self.internals.send_team_message(2, "another_message")

    def test_send_team_message_too_large(self):
        self.internals.set_teammate_ids({2})
        # TEAM_MESSAGE_MAX_SIZE is 128
        large_message = "a" * 129 # 129 bytes as string, json encoding adds quotes making it 131
        with self.assertRaisesRegex(ValueError, "larger than the limit"):
            self.internals.send_team_message(2, large_message)

    # Test Stop/Resume
    def test_set_stop_and_resume(self):
        self.internals.bot_intent['targetSpeed'] = 5.0
        self.internals.bot_intent['turnRate'] = 2.0
        
        mock_listener = MagicMock()
        self.internals.set_stop_resume_listener(mock_listener)

        self.internals.set_stop(overwrite=False)
        
        self.assertTrue(self.internals.is_stopped)
        self.assertEqual(self.internals.bot_intent['targetSpeed'], 0.0)
        self.assertEqual(self.internals.bot_intent['turnRate'], 0.0)
        self.assertEqual(self.internals.saved_target_speed, 5.0)
        self.assertEqual(self.internals.saved_turn_rate, 2.0)
        mock_listener.on_stop.assert_called_once()

        self.internals.set_resume()
        self.assertFalse(self.internals.is_stopped)
        self.assertEqual(self.internals.bot_intent['targetSpeed'], 5.0)
        self.assertEqual(self.internals.bot_intent['turnRate'], 2.0)
        mock_listener.on_resume.assert_called_once()


if __name__ == '__main__':
    unittest.main()
