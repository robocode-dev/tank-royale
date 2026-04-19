import pytest
from robocode_tank_royale.bot_api.bot_state import BotState
from robocode_tank_royale.bot_api.bot_results import BotResults
from robocode_tank_royale.bot_api.game_setup import GameSetup
from robocode_tank_royale.bot_api.graphics.color import Color

@pytest.mark.MDL
def test_TR_API_MDL_002_bot_state_constructor():
    body_color = Color.from_hex("#111111")
    turret_color = Color.from_hex("#222222")
    radar_color = Color.from_hex("#333333")
    bullet_color = Color.from_hex("#444444")
    scan_color = Color.from_hex("#555555")
    tracks_color = Color.from_hex("#666666")
    gun_color = Color.from_hex("#777777")

    state = BotState(
        is_droid=True, energy=100.0, x=50.0, y=60.0, direction=45.0,
        gun_direction=90.0, radar_direction=135.0, radar_sweep=5.0,
        speed=1.0, turn_rate=2.0, gun_turn_rate=3.0, radar_turn_rate=4.0,
        gun_heat=0.5, enemy_count=3, body_color=body_color,
        turret_color=turret_color, radar_color=radar_color,
        bullet_color=bullet_color, scan_color=scan_color,
        tracks_color=tracks_color, gun_color=gun_color,
        debugging_enabled=True
    )
    
    assert state.droid is True
    assert state.energy == 100.0
    assert state.x == 50.0
    assert state.y == 60.0
    assert state.direction == 45.0
    assert state.gun_direction == 90.0
    assert state.radar_direction == 135.0
    assert state.radar_sweep == 5.0
    assert state.speed == 1.0
    assert state.turn_rate == 2.0
    assert state.gun_turn_rate == 3.0
    assert state.radar_turn_rate == 4.0
    assert state.gun_heat == 0.5
    assert state.enemy_count == 3
    assert state.body_color == body_color
    assert state.turret_color == turret_color
    assert state.radar_color == radar_color
    assert state.bullet_color == bullet_color
    assert state.scan_color == scan_color
    assert state.tracks_color == tracks_color
    assert state.gun_color == gun_color
    assert state.debugging_enabled is True

@pytest.mark.MDL
def test_TR_API_MDL_003_bot_results_constructor():
    results = BotResults(
        rank=1, survival=100.0, last_survivor_bonus=50.0,
        bullet_damage=30.0, bullet_kill_bonus=20.0,
        ram_damage=10.0, ram_kill_bonus=5.0, total_score=215.0,
        first_places=3, second_places=2, third_places=4
    )
    
    assert results.rank == 1
    assert results.survival == 100.0
    assert results.last_survivor_bonus == 50.0
    assert results.bullet_damage == 30.0
    assert results.bullet_kill_bonus == 20.0
    assert results.ram_damage == 10.0
    assert results.ram_kill_bonus == 5.0
    assert results.total_score == 215.0
    assert results.first_places == 3
    assert results.second_places == 2
    assert results.third_places == 4

@pytest.mark.MDL
def test_TR_API_MDL_004_game_setup_constructor():
    setup = GameSetup(
        game_type="classic", arena_width=800, arena_height=600,
        number_of_rounds=10, gun_cooling_rate=0.1,
        max_inactivity_turns=450, turn_timeout=30000, ready_timeout=1000
    )
    
    assert setup.game_type == "classic"
    assert setup.arena_width == 800
    assert setup.arena_height == 600
    assert setup.number_of_rounds == 10
    assert setup.gun_cooling_rate == 0.1
    assert setup.max_inactivity_turns == 450
    assert setup.turn_timeout == 30000
    assert setup.ready_timeout == 1000
