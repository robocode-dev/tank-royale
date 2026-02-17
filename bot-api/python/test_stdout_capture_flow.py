"""Test to verify stdout is being captured and transferred to BotIntent"""

import sys
sys.path.insert(0, 'src')

# Open a debug file BEFORE any redirection happens
debug_file = open('test_stdout_debug.txt', 'w', encoding='utf-8')

def log(msg):
    """Write to debug file"""
    debug_file.write(msg + '\n')
    debug_file.flush()

log("=" * 60)
log("Testing stdout capture and transfer to BotIntent")
log("=" * 60)

from robocode_tank_royale.bot_api.internal.base_bot_internals import BaseBotInternals
from robocode_tank_royale.bot_api.internal.recording_text_writer import RecordingTextWriter
from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.base_bot import BaseBot

class DummyBot(BaseBot):
    def run(self):
        pass

try:
    log("Creating bot info...")
    # Create a minimal bot setup
    bot_info = BotInfo(
        name="TestBot",
        version="1.0",
        authors=["Test"],
        description="Test",
        country_codes=["us"],
        game_types=["melee"],
        platform="Python",
        programming_lang="Python"
    )

    log("Creating bot...")
    bot = DummyBot(bot_info)

    # Check if stdout has been redirected
    log(f"Checking stdout type: {type(sys.stdout)}")

    # Verify redirection happened
    if isinstance(sys.stdout, RecordingTextWriter):
        log("✓ stdout HAS been redirected to RecordingTextWriter")
    else:
        log(f"✗ stdout is NOT redirected! Type: {type(sys.stdout)}")

    # Print some test output (this should be captured)
    print("Test message 1")
    print("Test message 2")
    log("Printed test messages to stdout")

    # Now try to transfer to bot_intent
    log("Calling _transfer_std_out_to_bot_intent()...")
    bot._internals._transfer_std_out_to_bot_intent()

    # Check if it was captured in bot_intent
    log(f"Checking bot_intent.std_out...")
    if bot._internals.data.bot_intent.std_out:
        log(f"✓ stdout captured in bot_intent:")
        log(f"  Content: {repr(bot._internals.data.bot_intent.std_out[:100])}")
    else:
        log(f"✗ stdout NOT captured in bot_intent!")
        log(f"  bot_intent.std_out = {bot._internals.data.bot_intent.std_out}")

    # Try serializing to JSON to see what gets sent
    log("Serializing to JSON...")
    from robocode_tank_royale.bot_api.internal.json_util import to_json
    json_output = to_json(bot._internals.data.bot_intent)
    log(f"✓ JSON serialization successful")
    if '"stdOut"' in json_output:
        log(f"✓ 'stdOut' field found in JSON")
        # Find and print the stdOut value
        import json
        parsed = json.loads(json_output)
        if 'stdOut' in parsed and parsed['stdOut']:
            log(f"✓ stdOut value: {repr(parsed['stdOut'][:100])}")
        else:
            log(f"✗ stdOut is null or missing in JSON: {parsed.get('stdOut')}")
    else:
        log(f"✗ 'stdOut' field NOT found in JSON")

except Exception as e:
    log(f"ERROR: {e}")
    import traceback
    log(traceback.format_exc())

log("=" * 60)
log("Test complete - check test_stdout_debug.txt for results")
debug_file.close()

print("\n>>> Check test_stdout_debug.txt for results <<<\n")




