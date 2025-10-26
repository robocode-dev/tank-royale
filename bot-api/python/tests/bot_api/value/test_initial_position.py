import pytest

from robocode_tank_royale.bot_api import InitialPosition
from robocode_tank_royale.bot_api.mapper import InitialPositionMapper


def test_TR_API_VAL_003_initial_position_defaults():
    """TR-API-VAL-003 InitialPosition defaults"""
    # One value -> x only
    ip = InitialPosition.from_string("10")
    assert ip is not None
    assert ip.x == 10
    assert ip.y is None
    assert ip.direction is None

    # Two values -> x,y
    ip = InitialPosition.from_string("10, 20")
    assert ip is not None
    assert ip.x == 10
    assert ip.y == 20
    assert ip.direction is None

    # Three values -> x,y,dir
    ip = InitialPosition.from_string("10, 20, 30")
    assert ip is not None
    assert ip.x == 10
    assert ip.y == 20
    assert ip.direction == 30


def test_TR_API_VAL_004_initial_position_mapping_round_trip_like():
    """TR-API-VAL-004 InitialPosition mapping round-trip: mapper preserves values"""
    ip = InitialPosition.from_string("11, 22, 33")
    schema_ip = InitialPositionMapper.map(ip)

    # The mapper must preserve values 1:1
    assert schema_ip is not None
    assert schema_ip.x == 11
    assert schema_ip.y == 22
    assert schema_ip.direction == 33

    # If we construct back an InitialPosition via from_string of values, we should get same fields
    ip2 = InitialPosition.from_string(f"{schema_ip.x}, {schema_ip.y}, {schema_ip.direction}")
    assert ip2 is not None
    assert ip2.x == ip.x
    assert ip2.y == ip.y
    assert ip2.direction == ip.direction
