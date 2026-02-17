"""Tests for RecordingTextWriter"""

import sys
import threading
from io import StringIO

from robocode_tank_royale.bot_api.internal.recording_text_writer import RecordingTextWriter


def test_basic_write():
    """Test basic write functionality"""
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


def test_multiple_reads():
    """Test multiple read cycles"""
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


def test_concurrent_writes():
    """Test thread-safety with concurrent writes"""
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

        # Use print (which writes to sys.stdout)
        print("Test message")
        print("Another message")

        # Should be captured
        recorded = recorder.read_next()
        assert "Test message" in recorded
        assert "Another message" in recorded

    finally:
        sys.stdout = original_stdout


if __name__ == "__main__":
    test_basic_write()
    print("✓ test_basic_write passed")

    test_multiple_reads()
    print("✓ test_multiple_reads passed")

    test_concurrent_writes()
    print("✓ test_concurrent_writes passed")

    test_print_redirection()
    print("✓ test_print_redirection passed")

    print("\nAll tests passed! ✅")

