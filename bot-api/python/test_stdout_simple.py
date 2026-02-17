"""Simple test to verify stdout capture works"""

import sys
import os

# Change to src directory
os.chdir(os.path.dirname(__file__))
sys.path.insert(0, 'src')

# Create a simple test bot
from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.base_bot import BaseBot

class TestBot(BaseBot):
    def run(self):
        pass

# Create bot
bot_info = BotInfo(
    name="Test",
    version="1.0",
    authors=["Test"],
    description="Test",
    country_codes=["us"],
    game_types=["melee"],
    platform="Python",
    programming_lang="Python"
)

bot = TestBot(bot_info)

# Test 1: Check if stdout is redirected
from robocode_tank_royale.bot_api.internal.recording_text_writer import RecordingTextWriter
print(f"TEST 1: stdout type = {type(sys.stdout).__name__}")
print(f"Is RecordingTextWriter? {isinstance(sys.stdout, RecordingTextWriter)}")

# Test 2: Print some output
print("CAPTURED OUTPUT LINE 1")
print("CAPTURED OUTPUT LINE 2")

# Test 3: Transfer to bot_intent
bot._internals._transfer_std_out_to_bot_intent()
print(f"TEST 3: bot_intent.std_out = {repr(bot._internals.data.bot_intent.std_out)}")

# Test 4: Check JSON
from robocode_tank_royale.bot_api.internal.json_util import to_json
import json
json_str = to_json(bot._internals.data.bot_intent)
parsed = json.loads(json_str)
print(f"TEST 4: stdOut in JSON? {'stdOut' in parsed}")
if 'stdOut' in parsed:
    print(f"  stdOut value: {repr(parsed['stdOut'])}")

