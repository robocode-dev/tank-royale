"""
Comprehensive tests for stdout/stderr capture in Python Bot API.

Tests RecordingTextWriter class and integration with bot workflow.
"""

import sys
import threading
from io import StringIO

from robocode_tank_royale.bot_api.internal.recording_text_writer import RecordingTextWriter


# =============================================================================
# Unit Tests - RecordingTextWriter
# =============================================================================

def test_basic_write_and_read():
    """Test basic write and read functionality"""
    underlying = StringIO()
    recorder = RecordingTextWriter(underlying)

    recorder.write("Hello")
    recorder.write(" World")
    recorder.flush()

    # Should have written to underlying writer
    assert underlying.getvalue() == "Hello World"

    # Should have recorded output
    recorded = recorder.read_next()
    assert recorded == "Hello World"

    # After read_next, buffer should be cleared
    recorded_again = recorder.read_next()
    assert recorded_again == ""


def test_multiple_read_cycles():
    """Test multiple read cycles clear buffer properly"""
    underlying = StringIO()
    recorder = RecordingTextWriter(underlying)

    recorder.write("First")
    first = recorder.read_next()
    assert first == "First"

    recorder.write("Second")
    second = recorder.read_next()
    assert second == "Second"

    # Underlying should have both
    assert underlying.getvalue() == "FirstSecond"


def test_thread_safety():
    """Test thread-safety with concurrent writes from multiple threads"""
    underlying = StringIO()
    recorder = RecordingTextWriter(underlying)

    num_threads = 10
    writes_per_thread = 100

    def write_lines(thread_id):
        for i in range(writes_per_thread):
            recorder.write(f"Thread{thread_id}-Line{i}\n")

    threads = []
    for i in range(num_threads):
        t = threading.Thread(target=write_lines, args=(i,))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    # Get all recorded output
    output = recorder.read_next()
    lines = output.strip().split('\n')

    # Should have all lines (order may vary due to threading)
    assert len(lines) == num_threads * writes_per_thread

    # Each thread should have contributed its lines
    for i in range(num_threads):
        thread_lines = [line for line in lines if line.startswith(f"Thread{i}-")]
        assert len(thread_lines) == writes_per_thread


def test_print_redirection():
    """Test that print() works correctly when sys.stdout is redirected"""
    original_stdout = sys.stdout

    try:
        underlying = StringIO()
        recorder = RecordingTextWriter(underlying)
        sys.stdout = recorder

        print("Test message")
        print("Another message")

        # Should be captured
        recorded = recorder.read_next()
        assert "Test message" in recorded
        assert "Another message" in recorded

    finally:
        sys.stdout = original_stdout


# =============================================================================
# Integration Tests - Bot Workflow
# =============================================================================

def test_bot_workflow_simulation():
    """
    Simulate complete bot workflow:
    1. Redirect stdout/stderr at initialization
    2. Bot code runs and calls print()
    3. Transfer captured output to bot_intent
    4. Verify output is captured and buffer is cleared between turns
    """
    original_stdout = sys.stdout
    original_stderr = sys.stderr

    try:
        # Step 1: Initialize recording writers
        underlying_stdout = StringIO()
        underlying_stderr = StringIO()

        recording_stdout = RecordingTextWriter(underlying_stdout)
        recording_stderr = RecordingTextWriter(underlying_stderr)

        sys.stdout = recording_stdout
        sys.stderr = recording_stderr

        # Step 2: Simulate bot code (e.g., in OnScannedBot)
        print("Scanned enemy at distance: 250.5")
        print(f"Bearing: {45.2}")
        print("Firing!")
        sys.stderr.write("Warning: Low energy\n")

        # Step 3: Transfer to bot_intent
        stdout_output = recording_stdout.read_next()
        stderr_output = recording_stderr.read_next()

        assert "Scanned enemy at distance: 250.5" in stdout_output
        assert "Bearing: 45.2" in stdout_output
        assert "Firing!" in stdout_output
        assert "Warning: Low energy" in stderr_output

        # Step 4: Next turn - buffer should be cleared
        print("Turn 2: Moving forward")

        stdout_output_2 = recording_stdout.read_next()
        stderr_output_2 = recording_stderr.read_next()

        assert "Turn 2: Moving forward" in stdout_output_2
        assert "Scanned enemy" not in stdout_output_2  # Old output cleared
        assert stderr_output_2 == ""  # No new stderr

        # Step 5: Verify forwarding to underlying streams
        assert "Scanned enemy at distance: 250.5" in underlying_stdout.getvalue()
        assert "Turn 2: Moving forward" in underlying_stdout.getvalue()
        assert "Warning: Low energy" in underlying_stderr.getvalue()

    finally:
        sys.stdout = original_stdout
        sys.stderr = original_stderr


def test_empty_output_handling():
    """Test that empty output returns empty string, not None"""
    original_stdout = sys.stdout

    try:
        underlying = StringIO()
        recorder = RecordingTextWriter(underlying)
        sys.stdout = recorder

        # Don't print anything
        output = recorder.read_next()

        assert output == ""
        assert output is not None

    finally:
        sys.stdout = original_stdout


def test_unicode_handling():
    """Test unicode and special characters are handled correctly"""
    original_stdout = sys.stdout

    try:
        underlying = StringIO()
        recorder = RecordingTextWriter(underlying)
        sys.stdout = recorder

        print("Emoji: 🤖 🎯 💥")
        print("Unicode: äöü ñ 中文")
        print("Special chars: \n\t\r")

        output = recorder.read_next()

        assert "🤖" in output
        assert "中文" in output
        assert "äöü" in output

    finally:
        sys.stdout = original_stdout


# =============================================================================
# Test Runner
# =============================================================================

if __name__ == "__main__":
    print("Running stdout/stderr capture tests...\n")

    # Unit tests
    print("Unit Tests:")
    test_basic_write_and_read()
    print("  ✓ Basic write and read")

    test_multiple_read_cycles()
    print("  ✓ Multiple read cycles")

    test_thread_safety()
    print("  ✓ Thread safety")

    test_print_redirection()
    print("  ✓ Print redirection")

    # Integration tests
    print("\nIntegration Tests:")
    test_bot_workflow_simulation()
    print("  ✓ Bot workflow simulation")

    test_empty_output_handling()
    print("  ✓ Empty output handling")

    test_unicode_handling()
    print("  ✓ Unicode handling")

    print("\n" + "="*60)
    print("ALL TESTS PASSED ✅")
    print("="*60)

