"""Integration test for stdout/stderr capture in bot workflow"""

import sys
from io import StringIO

from robocode_tank_royale.bot_api.internal.recording_text_writer import RecordingTextWriter


def test_bot_workflow_simulation():
    """
    Simulate the actual bot workflow:
    1. Redirect stdout/stderr at bot initialization
    2. Bot code runs and calls print()
    3. Transfer captured output to bot_intent
    4. Verify output is captured and buffer is cleared
    """
    # Save original streams
    original_stdout = sys.stdout
    original_stderr = sys.stderr

    try:
        # Step 1: Initialize recording writers (simulating _redirect_stdout_and_stderr)
        underlying_stdout = StringIO()
        underlying_stderr = StringIO()

        recording_stdout = RecordingTextWriter(underlying_stdout)
        recording_stderr = RecordingTextWriter(underlying_stderr)

        sys.stdout = recording_stdout
        sys.stderr = recording_stderr

        # Step 2: Simulate bot code running (e.g., in OnScannedBot event handler)
        print("Scanned enemy at distance: 250.5")
        print(f"Bearing: {45.2}")
        print("Firing at target!")

        # Also test stderr
        sys.stderr.write("Warning: Low energy\n")

        # Step 3: Transfer to bot_intent (simulating _transfer_std_out_to_bot_intent)
        stdout_output = recording_stdout.read_next()
        stderr_output = recording_stderr.read_next()

        # Verify captured output
        assert "Scanned enemy at distance: 250.5" in stdout_output
        assert "Bearing: 45.2" in stdout_output
        assert "Firing at target!" in stdout_output
        assert "Warning: Low energy" in stderr_output

        print("First turn output captured ✓")

        # Step 4: Simulate next turn - buffer should be cleared
        print("Turn 2: Moving forward")

        # Transfer again
        stdout_output_2 = recording_stdout.read_next()
        stderr_output_2 = recording_stderr.read_next()

        # Should only have new output
        assert "Turn 2: Moving forward" in stdout_output_2
        assert "Scanned enemy" not in stdout_output_2  # Old output should be gone
        assert stderr_output_2 == ""  # No new stderr output

        print("Second turn output captured ✓")
        print("Buffer clearing verified ✓")

        # Step 5: Verify output also went to underlying streams
        assert "Scanned enemy at distance: 250.5" in underlying_stdout.getvalue()
        assert "Turn 2: Moving forward" in underlying_stdout.getvalue()
        assert "Warning: Low energy" in underlying_stderr.getvalue()

        print("Output forwarding verified ✓")

    finally:
        # Restore original streams
        sys.stdout = original_stdout
        sys.stderr = original_stderr


def test_empty_output_handling():
    """Test that empty output is handled correctly (should return empty string, not None)"""
    original_stdout = sys.stdout

    try:
        underlying = StringIO()
        recorder = RecordingTextWriter(underlying)
        sys.stdout = recorder

        # Don't print anything
        output = recorder.read_next()

        # Should be empty string, not None
        assert output == ""
        assert output is not None

        print("Empty output handling verified ✓")

    finally:
        sys.stdout = original_stdout


def test_unicode_and_special_characters():
    """Test that unicode and special characters are handled correctly"""
    original_stdout = sys.stdout

    try:
        underlying = StringIO()
        recorder = RecordingTextWriter(underlying)
        sys.stdout = recorder

        # Print various special characters
        print("Emoji: 🤖 🎯 💥")
        print("Unicode: äöü ñ 中文")
        print("Special chars: \n\t\r")

        output = recorder.read_next()

        assert "🤖" in output
        assert "中文" in output
        assert "äöü" in output

        print("Unicode handling verified ✓")

    finally:
        sys.stdout = original_stdout


if __name__ == "__main__":
    test_bot_workflow_simulation()
    print("\n✅ Bot workflow simulation test PASSED")

    test_empty_output_handling()
    print("✅ Empty output handling test PASSED")

    test_unicode_and_special_characters()
    print("✅ Unicode handling test PASSED")

    print("\n" + "="*60)
    print("ALL INTEGRATION TESTS PASSED! ✅")
    print("The implementation is ready for real bot usage.")
    print("="*60)

