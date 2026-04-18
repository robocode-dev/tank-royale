import pytest
import json
import os
import math
import sys
from typing import Any, List, Dict, Optional, Set
from unittest.mock import MagicMock

# Add src to path
sys.path.append(os.path.join(os.path.dirname(__file__), '../src'))

from robocode_tank_royale.bot_api.internal.base_bot_internals import BaseBotInternals
from robocode_tank_royale.bot_api.internal.intent_validator import IntentValidator
from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.graphics import Color
from robocode_tank_royale.bot_api import constants
from robocode_tank_royale.bot_api.game_type import GameType
from robocode_tank_royale.bot_api.default_event_priority import DefaultEventPriority
from robocode_tank_royale.bot_api.events import *
from robocode_tank_royale.bot_api.bullet_state import BulletState

SHARED_TESTS_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../tests/shared"))

def get_shared_test_cases():
    test_cases = []
    if not os.path.exists(SHARED_TESTS_DIR):
        return test_cases

    for filename in os.listdir(SHARED_TESTS_DIR):
        if filename.endswith(".json") and not filename.endswith("schema.json"):
            with open(os.path.join(SHARED_TESTS_DIR, filename), 'r') as f:
                suite = json.load(f)
                for test in suite['tests']:
                    test_cases.append((suite['suite'], test))
    return test_cases

def parse_arg(arg: Any) -> Any:
    if arg == "NaN":
        return float('nan')
    if arg == "Infinity":
        return float('inf')
    if arg == "-Infinity":
        return float('-inf')
    if isinstance(arg, dict) and 'r' in arg:
        return Color.from_rgba(arg['r'], arg['g'], arg['b'], arg.get('a', 255))
    return arg

def get_constant(name):
    # Try constants module
    if hasattr(constants, name):
        return getattr(constants, name)
    # Try GameType
    if hasattr(GameType, name):
        return getattr(GameType, name)
    # Try DefaultEventPriority (with normalization)
    normalized = name.replace("Event", "")
    import re
    normalized = re.sub(r'([a-z])([A-Z])', r'\1_\2', normalized).upper()
    if hasattr(DefaultEventPriority, normalized):
        return getattr(DefaultEventPriority, normalized)
    # Fallback to direct name in DefaultEventPriority
    if hasattr(DefaultEventPriority, name):
        return getattr(DefaultEventPriority, name)
    return None

def create_event(event_name):
    if event_name == "BotDeathEvent": return BotDeathEvent(turn_number=0, victim_id=0)
    if event_name == "WonRoundEvent": return WonRoundEvent(turn_number=0)
    if event_name == "SkippedTurnEvent": return SkippedTurnEvent(turn_number=0)
    if event_name == "BotHitBotEvent": return HitBotEvent(turn_number=0, victim_id=0, energy=0, x=0, y=0, rammed=False)
    if event_name == "BotHitWallEvent": return HitWallEvent(turn_number=0)
    if event_name == "BulletFiredEvent": return BulletFiredEvent(turn_number=0, bullet=MagicMock(spec=BulletState))
    if event_name == "BulletHitBotEvent": return BulletHitBotEvent(turn_number=0, victim_id=0, bullet=MagicMock(spec=BulletState), damage=0, energy=0)
    if event_name == "BulletHitBulletEvent": return BulletHitBulletEvent(turn_number=0, bullet=MagicMock(spec=BulletState), hit_bullet=MagicMock(spec=BulletState))
    if event_name == "BulletHitWallEvent": return BulletHitWallEvent(turn_number=0, bullet=MagicMock(spec=BulletState))
    if event_name == "HitByBulletEvent": return HitByBulletEvent(turn_number=0, bullet=MagicMock(spec=BulletState), damage=0, energy=0)
    if event_name == "ScannedBotEvent": return ScannedBotEvent(turn_number=0, scanned_by_bot_id=0, scanned_bot_id=0, energy=0, x=0, y=0, direction=0, speed=0)
    if event_name == "CustomEvent": return CustomEvent(turn_number=0, condition=Condition(name="test", callable=lambda: True))
    if event_name == "TeamMessageEvent": return TeamMessageEvent(turn_number=0, message="test", sender_id=0)
    if event_name == "TickEvent": return TickEvent(turn_number=0, round_number=0, bot_state=None, bullet_states=[], events=[])
    if event_name == "DeathEvent": return DeathEvent(turn_number=0)
    if event_name == "HitWallEvent": return HitWallEvent(turn_number=0)
    if event_name == "HitBotEvent": return HitBotEvent(turn_number=0, victim_id=0, energy=0, x=0, y=0, rammed=False)
    raise ValueError(f"Unknown event: {event_name}")

@pytest.mark.parametrize("suite_name, test_case", get_shared_test_cases(), ids=lambda x: f"{x[0]} | {x[1]['id']}" if isinstance(x, tuple) else x)
def test_shared(suite_name, test_case):
    mock_bot = MagicMock()
    mock_bot.energy = 100.0
    mock_bot.gun_heat = 0.0
    
    internals = BaseBotInternals(mock_bot, None, "ws://localhost", None)
    
    setup = test_case.get('setup', {})
    if 'energy' in setup: mock_bot.energy = float(setup['energy'])
    if 'gunHeat' in setup: mock_bot.gun_heat = float(setup['gunHeat'])
    if 'maxSpeed' in setup: internals.set_max_speed(float(setup['maxSpeed']))
    if 'maxTurnRate' in setup: internals.set_max_turn_rate(float(setup['maxTurnRate']))
    if 'maxGunTurnRate' in setup: internals.max_gun_turn_rate = float(setup['maxGunTurnRate'])
    if 'maxRadarTurnRate' in setup: internals.max_radar_turn_rate = float(setup['maxRadarTurnRate'])

    last_action_value = [None]
    
    def run_action():
        args = [parse_arg(a) for a in test_case.get('args', [])]
        method = test_case['method']
        
        if method == "setFire":
            last_action_value[0] = internals.set_fire(args[0])
        elif method == "setTurnRate":
            internals.turn_rate = args[0]
        elif method == "setGunTurnRate":
            internals.gun_turn_rate = args[0]
        elif method == "setRadarTurnRate":
            internals.radar_turn_rate = args[0]
        elif method == "setTargetSpeed":
            internals.target_speed = args[0]
        elif method == "setMaxSpeed":
            internals.set_max_speed(args[0])
        elif method == "setMaxTurnRate":
            internals.set_max_turn_rate(args[0])
        elif method == "getNewTargetSpeed":
            last_action_value[0] = IntentValidator.get_new_target_speed(args[0], args[1], args[2])
        elif method == "getDistanceTraveledUntilStop":
            last_action_value[0] = IntentValidator.get_distance_traveled_until_stop(args[0], args[1])
        elif method == "BotInfo":
            last_action_value[0] = BotInfo(
                args[0], args[1], args[2],
                args[3] if len(args) > 3 else None,
                args[4] if len(args) > 4 else None,
                args[5] if len(args) > 5 else None,
                set(args[6]) if len(args) > 6 and args[6] else None,
                args[7] if len(args) > 7 else None,
                args[8] if len(args) > 8 else None
            )
        elif method == "fromRgb":
            last_action_value[0] = Color.from_rgb(args[0], args[1], args[2])
        elif method == "fromRgba":
            last_action_value[0] = Color.from_rgba(args[0], args[1], args[2], args[3])
        elif method == "colorToHex":
            last_action_value[0] = IntentValidator.color_to_schema(args[0])
        elif method == "getColorConstant":
            last_action_value[0] = getattr(Color, args[0].upper())
        elif method == "getConstant":
            last_action_value[0] = get_constant(args[0])
        elif method == "isCritical":
            last_action_value[0] = create_event(args[0]).critical
        elif method == "getDefaultPriority":
            last_action_value[0] = getattr(DefaultEventPriority, args[0])
        elif method == "calcBulletSpeed":
            last_action_value[0] = mock_bot.calc_bullet_speed(args[0])
        elif method == "calcMaxTurnRate":
            last_action_value[0] = mock_bot.calc_max_turn_rate(args[0])
        elif method == "calcGunHeat":
            last_action_value[0] = mock_bot.calc_gun_heat(args[0])
        elif method == "calcBearing":
            if len(args) == 2:
                mock_bot.direction = args[0]
                last_action_value[0] = mock_bot.calc_bearing(args[1])
            else:
                last_action_value[0] = mock_bot.calc_bearing(args[0])
        elif method == "normalizeAbsoluteAngle":
            last_action_value[0] = mock_bot.normalize_absolute_angle(args[0])
        elif method == "normalizeRelativeAngle":
            last_action_value[0] = mock_bot.normalize_relative_angle(args[0])
        else:
            pytest.fail(f"Method {method} not implemented in runner")

    expected = test_case['expected']
    if 'throws' in expected:
        with pytest.raises((ValueError, Exception)):
            run_action()
    else:
        run_action()
        
        if 'returns' in expected:
            exp_ret = parse_arg(expected['returns'])
            if isinstance(exp_ret, float) and math.isnan(exp_ret):
                assert math.isnan(last_action_value[0])
            elif isinstance(exp_ret, float):
                assert last_action_value[0] == pytest.approx(exp_ret)
            elif isinstance(exp_ret, str) and exp_ret.startswith("#"):
                actual_ret = last_action_value[0]
                if hasattr(actual_ret, 'value'):
                    actual_ret = actual_ret.value
                assert actual_ret.lower() == exp_ret.lower()
            else:
                assert last_action_value[0] == exp_ret

        intent = internals.bot_intent
        for key, val in expected.items():
            if key in ('returns', 'throws'):
                continue
            
            actual = None
            if key == "firepower": actual = intent.firepower or 0.0
            elif key == "turnRate": actual = intent.turn_rate
            elif key == "gunTurnRate": actual = intent.gun_turn_rate
            elif key == "radarTurnRate": actual = intent.radar_turn_rate
            elif key == "targetSpeed": actual = intent.target_speed
            elif key == "maxSpeed": actual = internals.max_speed
            elif key == "maxTurnRate": actual = internals.max_turn_rate
            elif isinstance(last_action_value[0], Color):
                c = last_action_value[0]
                if key == "r": actual = c.red
                elif key == "g": actual = c.green
                elif key == "b": actual = c.blue
                elif key == "a": actual = c.alpha
            elif isinstance(last_action_value[0], BotInfo):
                info = last_action_value[0]
                if key == "name": actual = info.name
                elif key == "version": actual = info.version
                elif key == "authors": actual = info.authors
                elif key == "countryCodes": actual = info.country_codes

            if actual is not None:
                if isinstance(val, float):
                    assert actual == pytest.approx(val)
                else:
                    assert actual == val
