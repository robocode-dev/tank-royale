import math

from robocode_tank_royale.bot_api import constants as C


def test_TR_API_VAL_005_constants_integrity():
    """TR-API-VAL-005 Constants integrity"""
    # Core geometry and motion
    assert C.BOUNDING_CIRCLE_RADIUS == 18
    assert C.SCAN_RADIUS == 1200
    assert C.MAX_TURN_RATE == 10
    assert C.MAX_GUN_TURN_RATE == 20
    assert C.MAX_RADAR_TURN_RATE == 45
    assert C.MAX_SPEED == 8

    # Firepower and bullet speeds
    assert math.isclose(C.MIN_FIREPOWER, 0.1, rel_tol=0, abs_tol=1e-10)
    assert math.isclose(C.MAX_FIREPOWER, 3.0, rel_tol=0, abs_tol=1e-10)

    assert math.isclose(C.MIN_BULLET_SPEED, 20 - 3 * C.MAX_FIREPOWER, rel_tol=0, abs_tol=1e-10)
    assert math.isclose(C.MIN_BULLET_SPEED, 11.0, rel_tol=0, abs_tol=1e-10)
    assert math.isclose(C.MAX_BULLET_SPEED, 20 - 3 * C.MIN_FIREPOWER, rel_tol=0, abs_tol=1e-10)
    assert math.isclose(C.MAX_BULLET_SPEED, 19.7, rel_tol=0, abs_tol=1e-10)

    # Acceleration / deceleration
    assert C.ACCELERATION == 1
    assert C.DECELERATION == -2
