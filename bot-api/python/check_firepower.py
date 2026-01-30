import inspect
import json
import sys
from robocode_tank_royale.schema import BotIntent, Message
from robocode_tank_royale.bot_api.internal.json_util import to_json, from_json, MessageEncoder

print("=== Test 1: Create BotIntent with firepower ===", file=sys.stderr)
intent = BotIntent(type=Message.Type.BOT_INTENT, firepower=1.0, team_messages=[])
print(f"Original firepower: {intent.firepower}", file=sys.stderr)
print(f"Intent __dict__: {intent.__dict__}", file=sys.stderr)

print("\n=== Test 2: Serialize to JSON ===", file=sys.stderr)
json_str = to_json(intent)
print(f"Serialized JSON:\n{json_str}", file=sys.stderr)

print("\n=== Test 3: Parse JSON dict ===", file=sys.stderr)
parsed_dict = json.loads(json_str)
print(f"Parsed dict keys: {list(parsed_dict.keys())}", file=sys.stderr)
print(f"'firepower' in parsed_dict: {'firepower' in parsed_dict}", file=sys.stderr)
print(f"parsed_dict['type']: {parsed_dict.get('type')}", file=sys.stderr)

print("\n=== Test 4: Deserialize back ===", file=sys.stderr)
deserialized = from_json(json_str)
print(f"Deserialized type: {type(deserialized)}", file=sys.stderr)
print(f"Deserialized firepower: {deserialized.firepower}", file=sys.stderr)

print("\n=== Test 5: Check BotIntent __init__ params ===", file=sys.stderr)
sig = inspect.signature(BotIntent.__init__)
params = list(sig.parameters.keys())
print(f"'firepower' in BotIntent.__init__ params: {'firepower' in params}", file=sys.stderr)
print(f"All params: {params}", file=sys.stderr)

