import unittest
from unittest.mock import Mock, patch, AsyncMock

from robocode_tank_royale.bot_api import (
    BaseBot,
    BotInfo,
    Color,
    BulletState,
    BotState,
    GameSetup,
    Condition,
    InitialPosition,
)
from robocode_tank_royale.bot_api.events import (
    ConnectedEvent,
    DisconnectedEvent,
    ConnectionErrorEvent,
    GameStartedEvent,
    GameEndedEvent,
    RoundStartedEvent,
    RoundEndedEvent,
    TickEvent,
    BotDeathEvent,
    DeathEvent,
    HitBotEvent,
    HitWallEvent,
    BulletFiredEvent,
    HitByBulletEvent,
    BulletHitBotEvent,
    BulletHitBulletEvent,
    BulletHitWallEvent,
    ScannedBotEvent,
    SkippedTurnEvent,
    TeamMessageEvent,
    WonRoundEvent,
    CustomEvent,
)
from robocode_tank_royale.bot_api.bot_exception import BotException

# Mock _BaseBotInternals for all tests in this module
# We need to patch the path where it's looked up by BaseBot
@patch("robocode_tank_royale.bot_api.base_bot._BaseBotInternals", autospec=True)
class TestBaseBot(unittest.IsolatedAsyncioTestCase):
    def test_initialization_default(self, MockBaseBotInternals):
        """Test BaseBot initialization with default arguments."""
        bot = BaseBot()
        MockBaseBotInternals.assert_called_once_with(bot, None, None, None)
        self.assertIsNotNone(bot._internals)

    def test_initialization_with_args(self, MockBaseBotInternals):
        """Test BaseBot initialization with all optional arguments."""
        mock_bot_info = BotInfo(
            name="TestBot",
            version="1.0",
            authors=["TestAuthor"],
            description="A test bot",
            homepage="http://example.com",
            country_codes=["US"],
            platform=".NET",
            programming_lang="C#",
            initial_position=None,
            game_variants=["melee"],
            missiles=False,
        )
        server_url = "ws://localhost:8080"
        server_secret = "secret"

        bot = BaseBot(
            bot_info=mock_bot_info,
            server_url=server_url,
            server_secret=server_secret,
        )
        MockBaseBotInternals.assert_called_once_with(
            bot, mock_bot_info, server_url, server_secret
        )
        self.assertIsNotNone(bot._internals)

    def test_start(self, MockBaseBotInternals):
        """Test the start method."""
        bot = BaseBot()
        bot.start()
        bot._internals.start.assert_called_once()

    async def test_go(self, MockBaseBotInternals):
        """Test the go method."""
        bot = BaseBot()
        # Mock the async go method of internals
        bot._internals.go = AsyncMock()
        await bot.go()
        bot._internals.go.assert_called_once()

    # --- Game State Accessors ---
    def test_get_my_id(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.my_id = 123
        self.assertEqual(bot.get_my_id(), 123)
        bot._internals.get_current_tick_or_throw.assert_called_once()

    def test_get_variant(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.variant = "Tank Royale"
        self.assertEqual(bot.get_variant(), "Tank Royale")

    def test_get_version(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.version = "1.0.0"
        self.assertEqual(bot.get_version(), "1.0.0")

    def test_get_game_type(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.game_setup = GameSetup(game_type="melee")
        self.assertEqual(bot.get_game_type(), "melee")
        bot._internals.get_current_tick_or_throw.assert_called_once()

    def test_get_arena_width(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.game_setup = GameSetup(arena_width=800)
        self.assertEqual(bot.get_arena_width(), 800)

    def test_get_arena_height(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.game_setup = GameSetup(arena_height=600)
        self.assertEqual(bot.get_arena_height(), 600)

    def test_get_number_of_rounds(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.game_setup = GameSetup(number_of_rounds=10)
        self.assertEqual(bot.get_number_of_rounds(), 10)

    def test_get_gun_cooling_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.game_setup = GameSetup(gun_cooling_rate=0.1)
        self.assertEqual(bot.get_gun_cooling_rate(), 0.1)

    def test_get_max_inactivity_turns(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.game_setup = GameSetup(max_inactivity_turns=100)
        self.assertEqual(bot.get_max_inactivity_turns(), 100)

    def test_get_turn_timeout(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.game_setup = GameSetup(turn_timeout=10000)
        self.assertEqual(bot.get_turn_timeout(), 10000)

    def test_get_time_left(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.get_time_left.return_value = 5000
        self.assertEqual(bot.get_time_left(), 5000)
        bot._internals.get_time_left.assert_called_once()
        # No get_current_tick_or_throw for this one

    def test_get_round_number(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_round_number = 3
        self.assertEqual(bot.get_round_number(), 3)

    def test_get_turn_number(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_turn_number = 150
        self.assertEqual(bot.get_turn_number(), 150)

    def test_get_enemy_count(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.get_enemy_count.return_value = 2
        self.assertEqual(bot.get_enemy_count(), 2)
        bot._internals.get_enemy_count.assert_called_once()

    def test_get_energy(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(energy=75.5)
        self.assertEqual(bot.get_energy(), 75.5)

    def test_is_disabled(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(energy=0)
        self.assertTrue(bot.is_disabled())
        bot._internals.current_bot_state = BotState(energy=0.1)
        self.assertFalse(bot.is_disabled())

    def test_get_x(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(x=100.0)
        self.assertEqual(bot.get_x(), 100.0)

    def test_get_y(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(y=200.0)
        self.assertEqual(bot.get_y(), 200.0)

    def test_get_direction(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(direction=90.0)
        self.assertEqual(bot.get_direction(), 90.0)

    def test_get_gun_direction(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(gun_direction=180.0)
        self.assertEqual(bot.get_gun_direction(), 180.0)

    def test_get_radar_direction(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(radar_direction=270.0)
        self.assertEqual(bot.get_radar_direction(), 270.0)

    def test_get_speed(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(speed=5.0)
        self.assertEqual(bot.get_speed(), 5.0)

    def test_get_gun_heat(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(gun_heat=1.5)
        self.assertEqual(bot.get_gun_heat(), 1.5)

    def test_get_bullet_states(self, MockBaseBotInternals):
        bot = BaseBot()
        mock_bullet_states = [Mock(spec=BulletState), Mock(spec=BulletState)]
        bot._internals.current_bullet_states.return_value = mock_bullet_states
        self.assertEqual(bot.get_bullet_states(), mock_bullet_states)
        bot._internals.current_bullet_states.assert_called_once()

    def test_get_events(self, MockBaseBotInternals):
        bot = BaseBot()
        mock_events = [Mock(spec=TickEvent)]
        bot._internals.events = mock_events
        self.assertEqual(bot.get_events(), mock_events)

    def test_clear_events(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.clear_events()
        bot._internals.clear_events.assert_called_once()

    # --- Action Methods ---
    def test_set_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_turn_rate(10.0)
        self.assertEqual(bot._internals.bot_intent.turn_rate, 10.0)

    def test_get_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.turn_rate = -5.0
        self.assertEqual(bot.get_turn_rate(), -5.0)

    def test_set_max_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_max_turn_rate(8.0)
        self.assertEqual(bot._internals.bot_intent.max_turn_rate, 8.0)

    def test_get_max_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.max_turn_rate = 7.0
        self.assertEqual(bot.get_max_turn_rate(), 7.0)

    def test_set_gun_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_gun_turn_rate(15.0)
        self.assertEqual(bot._internals.bot_intent.gun_turn_rate, 15.0)

    def test_get_gun_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.gun_turn_rate = -20.0
        self.assertEqual(bot.get_gun_turn_rate(), -20.0)

    def test_set_max_gun_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_max_gun_turn_rate(12.0)
        self.assertEqual(bot._internals.bot_intent.max_gun_turn_rate, 12.0)

    def test_get_max_gun_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.max_gun_turn_rate = 10.0
        self.assertEqual(bot.get_max_gun_turn_rate(), 10.0)

    def test_set_radar_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_radar_turn_rate(-20.0)
        self.assertEqual(bot._internals.bot_intent.radar_turn_rate, -20.0)

    def test_get_radar_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.radar_turn_rate = 25.0
        self.assertEqual(bot.get_radar_turn_rate(), 25.0)

    def test_set_max_radar_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_max_radar_turn_rate(22.0)
        self.assertEqual(bot._internals.bot_intent.max_radar_turn_rate, 22.0)

    def test_get_max_radar_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.max_radar_turn_rate = 18.0
        self.assertEqual(bot.get_max_radar_turn_rate(), 18.0)

    def test_set_target_speed(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_target_speed(8.0)
        self.assertEqual(bot._internals.bot_intent.target_speed, 8.0)

    def test_get_target_speed(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.target_speed = -7.0
        self.assertEqual(bot.get_target_speed(), -7.0)

    def test_set_max_speed(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_max_speed(6.0)
        self.assertEqual(bot._internals.bot_intent.max_speed, 6.0)

    def test_get_max_speed(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.max_speed = 5.0
        self.assertEqual(bot.get_max_speed(), 5.0)

    def test_set_fire(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.set_fire.return_value = True
        result = bot.set_fire(2.0)
        self.assertTrue(result)
        bot._internals.set_fire.assert_called_once_with(2.0)

    def test_set_fire_fail(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.set_fire.return_value = False
        result = bot.set_fire(1.0)
        self.assertFalse(result)
        bot._internals.set_fire.assert_called_once_with(1.0)

    def test_get_firepower(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.bot_intent.firepower = 3.0
        self.assertEqual(bot.get_firepower(), 3.0)

    def test_set_rescan(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_rescan()
        bot._internals.set_rescan.assert_called_once()

    def test_set_fire_assist(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_fire_assist(True)
        self.assertTrue(bot._internals.bot_intent.fire_assist)
        bot.set_fire_assist(False)
        self.assertFalse(bot._internals.bot_intent.fire_assist)

    def test_set_interruptible(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_interruptible(True)
        bot._internals.set_interruptible.assert_called_once_with(True)

    def test_adjust_settings(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_adjust_gun_for_body_turn(True)
        self.assertTrue(bot._internals.bot_intent.adjust_gun_for_body_turn)
        self.assertTrue(bot.is_adjust_gun_for_body_turn())

        bot.set_adjust_radar_for_body_turn(True)
        self.assertTrue(bot._internals.bot_intent.adjust_radar_for_body_turn)
        self.assertTrue(bot.is_adjust_radar_for_body_turn())

        bot.set_adjust_radar_for_gun_turn(True)
        self.assertTrue(bot._internals.bot_intent.adjust_radar_for_gun_turn)
        self.assertTrue(bot.is_adjust_radar_for_gun_turn())

    # --- Event Handling ---
    def test_event_handlers_callable(self, MockBaseBotInternals):
        """Test that default event handlers can be called without errors."""
        bot = BaseBot()
        # Static methods are not bound to the instance, so we call them on the class
        BaseBot.on_connected(Mock(spec=ConnectedEvent))
        BaseBot.on_disconnected(Mock(spec=DisconnectedEvent))
        BaseBot.on_connection_error(Mock(spec=ConnectionErrorEvent))

        # Instance methods
        bot.on_game_started(Mock(spec=GameStartedEvent))
        bot.on_game_ended(Mock(spec=GameEndedEvent))
        bot.on_round_started(Mock(spec=RoundStartedEvent))
        bot.on_round_ended(Mock(spec=RoundEndedEvent))
        bot.on_tick(Mock(spec=TickEvent))
        bot.on_bot_death(Mock(spec=BotDeathEvent))
        bot.on_death(Mock(spec=DeathEvent))
        bot.on_hit_bot(Mock(spec=HitBotEvent))
        bot.on_hit_wall(Mock(spec=HitWallEvent))
        bot.on_bullet_fired(Mock(spec=BulletFiredEvent))
        bot.on_hit_by_bullet(Mock(spec=HitByBulletEvent))
        bot.on_bullet_hit(Mock(spec=BulletHitBotEvent))
        bot.on_bullet_hit_bullet(Mock(spec=BulletHitBulletEvent))
        bot.on_bullet_hit_wall(Mock(spec=BulletHitWallEvent))
        bot.on_scanned_bot(Mock(spec=ScannedBotEvent))
        bot.on_skipped_turn(Mock(spec=SkippedTurnEvent))
        bot.on_won_round(Mock(spec=WonRoundEvent))
        bot.on_custom_event(Mock(spec=CustomEvent))
        bot.on_team_message(Mock(spec=TeamMessageEvent))
        # No assertions needed, just checking they run without error

    # --- Color Setting Methods ---
    def test_set_body_color(self, MockBaseBotInternals):
        bot = BaseBot()
        color = Color.from_hex("FF0000")
        bot.set_body_color(color)
        self.assertEqual(bot._internals.bot_intent.body_color, color)
        self.assertEqual(bot.get_body_color(), color)

    def test_set_turret_color(self, MockBaseBotInternals):
        bot = BaseBot()
        color = Color.from_hex("00FF00")
        bot.set_turret_color(color)
        self.assertEqual(bot._internals.bot_intent.turret_color, color)
        self.assertEqual(bot.get_turret_color(), color)

    def test_set_radar_color(self, MockBaseBotInternals):
        bot = BaseBot()
        color = Color.BLUE # Using predefined
        bot.set_radar_color(color)
        self.assertEqual(bot._internals.bot_intent.radar_color, color)
        self.assertEqual(bot.get_radar_color(), color)

    def test_set_bullet_color(self, MockBaseBotInternals):
        bot = BaseBot()
        color = Color.YELLOW
        bot.set_bullet_color(color)
        self.assertEqual(bot._internals.bot_intent.bullet_color, color)
        self.assertEqual(bot.get_bullet_color(), color)

    def test_set_scan_color(self, MockBaseBotInternals):
        bot = BaseBot()
        color = Color.CYAN
        bot.set_scan_color(color)
        self.assertEqual(bot._internals.bot_intent.scan_color, color)
        self.assertEqual(bot.get_scan_color(), color)

    def test_set_tracks_color(self, MockBaseBotInternals):
        bot = BaseBot()
        color = Color.MAGENTA
        bot.set_tracks_color(color)
        self.assertEqual(bot._internals.bot_intent.tracks_color, color)
        self.assertEqual(bot.get_tracks_color(), color)

    def test_set_gun_color(self, MockBaseBotInternals):
        bot = BaseBot()
        color = Color.WHITE
        bot.set_gun_color(color)
        self.assertEqual(bot._internals.bot_intent.gun_color, color)
        self.assertEqual(bot.get_gun_color(), color)

    # --- Utility Methods (some require bot state, some don't) ---
    def test_calc_max_turn_rate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.calc_max_turn_rate.return_value = 8.0
        self.assertEqual(bot.calc_max_turn_rate(5.0), 8.0)
        bot._internals.calc_max_turn_rate.assert_called_once_with(5.0)

    def test_calc_bullet_speed(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.calc_bullet_speed.return_value = 15.0
        self.assertEqual(bot.calc_bullet_speed(2.0), 15.0)
        bot._internals.calc_bullet_speed.assert_called_once_with(2.0)

    def test_calc_gun_heat(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.calc_gun_heat.return_value = 1.6
        self.assertEqual(bot.calc_gun_heat(3.0), 1.6)
        bot._internals.calc_gun_heat.assert_called_once_with(3.0)

    def test_get_event_priority(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.get_event_priority.return_value = 70
        self.assertEqual(bot.get_event_priority(ScannedBotEvent), 70)
        bot._internals.get_event_priority.assert_called_once_with(ScannedBotEvent)

    def test_set_event_priority(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_event_priority(TickEvent, 100)
        bot._internals.set_event_priority.assert_called_once_with(TickEvent, 100)

    def test_normalize_absolute_angle(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.normalize_absolute_angle.side_effect = lambda angle: angle % 360 if angle >=0 else (angle % 360) + 360
        self.assertAlmostEqual(bot.normalize_absolute_angle(370), 10)
        self.assertAlmostEqual(bot.normalize_absolute_angle(-10), 350)
        self.assertAlmostEqual(bot.normalize_absolute_angle(0), 0)
        self.assertAlmostEqual(bot.normalize_absolute_angle(360), 0)

    def test_normalize_relative_angle(self, MockBaseBotInternals):
        bot = BaseBot()
        # Simple lambda for testing, real logic in internals
        bot._internals.normalize_relative_angle.side_effect = lambda angle: (angle + 180) % 360 - 180
        self.assertAlmostEqual(bot.normalize_relative_angle(190), -170) # Simplified logic here
        self.assertAlmostEqual(bot.normalize_relative_angle(-190), 170) # Simplified logic here
        # Reset side effect for other tests if they rely on more accurate mock
        bot._internals.normalize_relative_angle.side_effect = None


    def test_calc_delta_angle(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.calc_delta_angle.return_value = -20.0
        self.assertAlmostEqual(bot.calc_delta_angle(10, 30), -20.0)
        bot._internals.calc_delta_angle.assert_called_once_with(10, 30)

    def test_calc_bearing(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(direction=90.0)
        # Mock normalize_relative_angle as it's called internally
        bot._internals.normalize_relative_angle = Mock(side_effect=lambda x: (x + 180) % 360 - 180)
        # calc_bearing = normalize_relative_angle(target_direction - bot_direction)
        # calc_bearing(110) = normalize_relative_angle(110 - 90) = normalize_relative_angle(20) = 20
        self.assertAlmostEqual(bot.calc_bearing(110), 20)
        # calc_bearing(70) = normalize_relative_angle(70 - 90) = normalize_relative_angle(-20) = -20
        self.assertAlmostEqual(bot.calc_bearing(70), -20)
        # calc_bearing(280) = normalize_relative_angle(280 - 90) = normalize_relative_angle(190) = -170
        self.assertAlmostEqual(bot.calc_bearing(280), -170)

    def test_calc_gun_bearing(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(gun_direction=45.0)
        bot._internals.normalize_relative_angle = Mock(side_effect=lambda x: (x + 180) % 360 - 180)
        self.assertAlmostEqual(bot.calc_gun_bearing(60), 15)

    def test_calc_radar_bearing(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(radar_direction=180.0)
        bot._internals.normalize_relative_angle = Mock(side_effect=lambda x: (x + 180) % 360 - 180)
        self.assertAlmostEqual(bot.calc_radar_bearing(170), -10)

    def test_distance_to(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(x=10, y=10)
        self.assertAlmostEqual(bot.distance_to(10, 20), 10)
        self.assertAlmostEqual(bot.distance_to(20, 10), 10)
        self.assertAlmostEqual(bot.distance_to(13, 14), 5) # 3-4-5 triangle

    def test_direction_to(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(x=0, y=0)
        bot._internals.normalize_absolute_angle = Mock(side_effect=lambda angle: angle % 360 if angle >=0 else (angle % 360) + 360)
        self.assertAlmostEqual(bot.direction_to(10, 0), 0)  # East
        self.assertAlmostEqual(bot.direction_to(0, 10), 90)  # North
        self.assertAlmostEqual(bot.direction_to(-10, 0), 180) # West
        self.assertAlmostEqual(bot.direction_to(0, -10), 270) # South

    def test_bearing_to(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(x=0, y=0, direction=45.0) # Facing North-East
        # Mock internal calls for direction_to and normalize_relative_angle
        with patch.object(bot, 'direction_to', return_value=90.0) as mock_dir_to, \
             patch.object(bot, 'normalize_relative_angle', side_effect=lambda x: (x + 180) % 360 - 180) as mock_norm:
            # bearing_to(tx, ty) = normalize_relative_angle(direction_to(tx,ty) - bot_direction)
            # bearing_to(0,10) = normalize_relative_angle(90.0 - 45.0) = normalize_relative_angle(45.0) = 45.0
            self.assertAlmostEqual(bot.bearing_to(0, 10), 45.0) # Target is North
            mock_dir_to.assert_called_once_with(0,10)
            mock_norm.assert_called_once_with(45.0)


    def test_gun_bearing_to(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(x=0, y=0, gun_direction=10.0)
        with patch.object(bot, 'direction_to', return_value=0.0) as mock_dir_to, \
             patch.object(bot, 'normalize_relative_angle', side_effect=lambda x: (x + 180) % 360 - 180) as mock_norm:
            # gun_bearing_to(10,0) = normalize_relative_angle(direction_to(10,0) - gun_direction)
            # gun_bearing_to(10,0) = normalize_relative_angle(0.0 - 10.0) = normalize_relative_angle(-10.0) = -10.0
            self.assertAlmostEqual(bot.gun_bearing_to(10, 0), -10.0) # Target is East
            mock_dir_to.assert_called_once_with(10,0)
            mock_norm.assert_called_once_with(-10.0)


    def test_radar_bearing_to(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.current_bot_state = BotState(x=0, y=0, radar_direction=350.0)
        with patch.object(bot, 'direction_to', return_value=0.0) as mock_dir_to, \
             patch.object(bot, 'normalize_relative_angle', side_effect=lambda x: (x + 180) % 360 - 180) as mock_norm:
            # radar_bearing_to(10,0) = normalize_relative_angle(direction_to(10,0) - radar_direction)
            # radar_bearing_to(10,0) = normalize_relative_angle(0.0 - 350.0) = normalize_relative_angle(-350.0) = 10.0
            self.assertAlmostEqual(bot.radar_bearing_to(10, 0), 10.0) # Target is East
            mock_dir_to.assert_called_once_with(10,0)
            mock_norm.assert_called_once_with(-350.0)

    # --- Teammate Methods ---
    def test_get_teammate_ids(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.get_teammate_ids.return_value = {2, 3}
        self.assertEqual(bot.get_teammate_ids(), {2, 3})
        bot._internals.get_teammate_ids.assert_called_once()

    def test_is_teammate(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.is_teammate.return_value = True
        self.assertTrue(bot.is_teammate(2))
        bot._internals.is_teammate.assert_called_once_with(2)

        bot._internals.is_teammate.return_value = False
        self.assertFalse(bot.is_teammate(4))

    # --- Team Message Methods ---
    def test_broadcast_team_message(self, MockBaseBotInternals):
        bot = BaseBot()
        message = {"type": "attack", "target_id": 5}
        bot.broadcast_team_message(message)
        bot._internals.broadcast_team_message.assert_called_once_with(message)

    def test_send_team_message(self, MockBaseBotInternals):
        bot = BaseBot()
        message = {"type": "info", "status": "low_energy"}
        bot.send_team_message(3, message)
        bot._internals.send_team_message.assert_called_once_with(3, message)

    # --- Custom Event Methods ---
    def test_add_remove_custom_event(self, MockBaseBotInternals):
        bot = BaseBot()
        condition = Mock(spec=Condition)
        bot._internals.add_custom_event.return_value = True
        self.assertTrue(bot.add_custom_event(condition))
        bot._internals.add_custom_event.assert_called_once_with(condition)

        bot._internals.remove_custom_event.return_value = True
        self.assertTrue(bot.remove_custom_event(condition))
        bot._internals.remove_custom_event.assert_called_once_with(condition)

    # --- Stop/Resume ---
    def test_set_stop_resume(self, MockBaseBotInternals):
        bot = BaseBot()
        bot.set_stop(overwrite=True)
        bot._internals.set_stop.assert_called_once_with(True)

        bot.set_resume()
        bot._internals.set_resume.assert_called_once()

    def test_is_stopped(self, MockBaseBotInternals):
        bot = BaseBot()
        bot._internals.is_stopped.return_value = True
        self.assertTrue(bot.is_stopped())
        bot._internals.is_stopped.assert_called_once()

    # --- Debugging ---
    def test_is_debugging_enabled(self, MockBaseBotInternals):
        bot = BaseBot()
        self.assertTrue(bot.is_debugging_enabled()) # Currently hardcoded to True

    def test_get_graphics(self, MockBaseBotInternals):
        bot = BaseBot()
        mock_graphics = Mock()
        bot._internals.get_graphics.return_value = mock_graphics
        self.assertEqual(bot.get_graphics(), mock_graphics)
        bot._internals.get_graphics.assert_called_once()

    # --- BotInfo properties ---
    def test_initial_position_property(self, MockBaseBotInternals):
        bot = BaseBot()
        pos = InitialPosition(10, 20, 30)
        bot.initial_position = pos
        self.assertEqual(bot._internals.initial_position, pos)

        bot._internals.initial_position = pos # Simulate getting
        self.assertEqual(bot.initial_position, pos)

    def test_country_codes_property(self, MockBaseBotInternals):
        bot = BaseBot()
        codes = ["US", "CA"]
        bot.country_codes = codes
        self.assertEqual(bot._internals.bot_info.country_codes, codes)
        bot._internals.settings_updated.assert_called_once()

        bot._internals.bot_info.country_codes = codes # Simulate getting
        self.assertEqual(bot.country_codes, codes)

    def test_game_variants_property(self, MockBaseBotInternals):
        bot = BaseBot()
        variants = ["melee", "1v1"]
        bot.game_variants = variants
        self.assertEqual(bot._internals.bot_info.game_variants, variants)
        bot._internals.settings_updated.assert_called_once()

        bot._internals.bot_info.game_variants = variants
        self.assertEqual(bot.game_variants, variants)

    def test_programming_lang_property(self, MockBaseBotInternals):
        bot = BaseBot()
        lang = "Python"
        bot.programming_lang = lang
        self.assertEqual(bot._internals.bot_info.programming_lang, lang)
        bot._internals.settings_updated.assert_called_once()

        bot._internals.bot_info.programming_lang = lang
        self.assertEqual(bot.programming_lang, lang)

    # Test exception raising for methods that require game to be running
    def test_methods_raise_when_not_started(self, MockBaseBotInternals):
        bot = BaseBot()
        # Configure the mock to raise BotException when get_current_tick_or_throw is called
        bot._internals.get_current_tick_or_throw.side_effect = BotException("Game not started")

        # Test a few representative methods
        with self.assertRaises(BotException):
            bot.get_my_id()
        with self.assertRaises(BotException):
            bot.get_arena_width()
        with self.assertRaises(BotException):
            bot.set_turn_rate(10)
        with self.assertRaises(BotException):
            bot.set_fire(1)
        # ... and so on for other methods that call get_current_tick_or_throw

if __name__ == "__main__":
    unittest.main()
