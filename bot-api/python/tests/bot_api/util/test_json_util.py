import pytest
import json
from robocode_tank_royale.bot_api.internal.json_util import to_json, from_json
from robocode_tank_royale.schema import BotIntent

@pytest.mark.UTL
@pytest.mark.TR_API_UTL_002
def test_json_util_serialization():
    intent = BotIntent(type="BotIntent", firepower=1.5, turn_rate=10.0)
    json_str = to_json(intent)
    
    # Verify it contains expected fields
    data = json.loads(json_str)
    assert data["firepower"] == 1.5
    assert data["turnRate"] == 10.0
    
def test_json_util_deserialization():
    json_str = '{"type": "BotIntent", "firepower": 2.0}'
    intent = from_json(json_str)
    
    assert isinstance(intent, BotIntent)
    assert intent.firepower == 2.0
