import pytest

from robocode_tank_royale.bot_api.graphics import Color
from robocode_tank_royale.bot_api.util import ColorUtil


def test_TR_API_UTL_001_colorutil_to_hex_and_from_hex_roundtrip():
    """TR-API-UTL-001 ColorUtil conversions: RGB↔HEX within tolerance; invalid input handling"""
    # 6-digit hex
    c = Color.from_rgb(0x00, 0x99, 0xCC)
    hx = ColorUtil.to_hex(c)
    assert hx == "0099cc"  # lower-case hex digits are acceptable
    c2 = ColorUtil.from_hex(hx)
    assert c2 == c

    # 3-digit hex expands properly
    c3 = ColorUtil.from_hex("09C")
    assert c3 == c


def test_TR_API_UTL_001_colorutil_from_string_numeric_rgb():
    """TR-API-UTL-001 ColorUtil from_string: numeric RGB with leading #"""
    c = ColorUtil.from_string("#09C")
    assert c == Color.from_rgb(0x00, 0x99, 0xCC)

    c = ColorUtil.from_string("#0099CC")
    assert c == Color.from_rgb(0x00, 0x99, 0xCC)


def test_TR_API_UTL_001_colorutil_invalid_inputs():
    """TR-API-UTL-001 ColorUtil invalid input handling"""
    with pytest.raises(ValueError):
        ColorUtil.from_hex("GGG")

    with pytest.raises(ValueError):
        ColorUtil.from_string("0099CC")  # missing leading '#'

    assert ColorUtil.to_hex(None) is None
